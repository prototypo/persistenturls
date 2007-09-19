package org.purl.accessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.DOMXDAAspect;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.DOMXDA;
import org.ten60.netkernel.xml.xda.IXDA;
import org.ten60.netkernel.xml.xda.XDOIncompatibilityException;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLAccessor extends AbstractAccessor {

    public static final String TYPE = "purl";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();

    private static URIResolver purlResolver;

    public PURLAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        purlResolver = new PURLURIResolver(); /*new URIResolver() {

            @Override
            public String getURI(INKFConvenienceHelper context) {
                String retValue = null;

                try {
                    String path = NKHelper.getArgument(context, "path");
                    retValue = "ffcpl:/storedpurls" + (!path.startsWith("/") ? ("/"+path) : path);
                } catch(NKFException nfe) {
                    nfe.printStackTrace();
                }

                return retValue;
            }

        }; */

        ResourceCreator purlCreator = new PurlCreator();
        ResourceStorage purlStorage = new DefaultResourceStorage();

        commandMap.put("http:POST", new CreateResourceCommand(TYPE, purlResolver, purlCreator, null, purlStorage));
        commandMap.put("http:PUT", new UpdateResourceCommand(TYPE, purlResolver, purlCreator, purlStorage));
        commandMap.put("http:DELETE", new DeleteResourceCommand(TYPE, purlResolver, purlStorage));
    }

    protected PURLCommand getCommand(String method) {
        return commandMap.get(method);
    }


    static public class PurlCreator implements ResourceCreator {

        private static IURAspect createChainedPURL(INKFConvenienceHelper context, IAspectNVP params) {
            IURAspect retValue = null;

            return retValue;
        }

        private static IURAspect createClonedPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;
            String purl = NKHelper.getArgument(context, "path");
            String existingPurl = params.getValue("existingpurl");
            String newURI = purlResolver.getURI(context);
            String oldURI = newURI.replace(purl, existingPurl);

            if(context.exists(oldURI)) {

                IAspectXDA oldPurlXDAOrig = (IAspectXDA) context.sourceAspect(oldURI, IAspectXDA.class);
                IXDA oldPurlXDA = oldPurlXDAOrig.getClonedXDA();
                try {
                    oldPurlXDA.replaceByText("/purl/pid", purl);
                    retValue = new DOMXDAAspect((DOMXDA)oldPurlXDA);
                } catch (XPathLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (XDOIncompatibilityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // TODO: Handle missing existing PURL
            }

            return retValue;
        }

        private static IURAspect createPartialRedirectPURL(INKFConvenienceHelper context, IAspectNVP params) {
            IURAspect retValue = null;

            return retValue;
        }

        private static IURAspect createNumericPURL(INKFConvenienceHelper context, IAspectNVP params, int type) throws NKFException {
            IURAspect retValue = null;

            StringBuffer sb = new StringBuffer("<purl>");
            String target = params.getValue("target");

            sb.append("<pid>");
            sb.append(NKHelper.getArgument(context, "path"));
            sb.append("</pid>");
            sb.append("<type>");
            sb.append(type);
            sb.append("</type>");

            switch(type) {
            case 301:
            case 302:
            case 307:
                sb.append("<target><url>");
                sb.append(target);
                sb.append("</url></target>");
                break;
            case 303:
                String seealsos=params.getValue("seealso");
                StringTokenizer st = new StringTokenizer(seealsos, " \n");
                while(st.hasMoreElements()) {
                    sb.append("<seealso>");
                    sb.append(st.nextToken());
                    sb.append("</seealso>");
                }

                break;
            case 404:
            case 410:
                break;
            default :
                // TODO: Handle Error
                break;
            }

            String maintainers=params.getValue("maintainers");
            System.out.println("maintainers: " + maintainers);
            if(maintainers!=null) {
                sb.append("<maintainers>");
                StringTokenizer st = new StringTokenizer(maintainers, " \n");
                while(st.hasMoreElements()) {
                    sb.append("<uid>");
                    sb.append(st.nextToken().trim());
                    sb.append("</uid>");
                }
                sb.append("</maintainers>");
            }

            sb.append("</purl>");

            System.out.println(sb.toString());
            retValue = new StringAspect(sb.toString());


            return retValue;
        }

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;
            Iterator itor = context.getThisRequest().getArguments();
            System.out.println("ARGUMENTS:");
            while(itor.hasNext()) {
                System.out.println((String)itor.next());
            }

            String type = params.getValue("type");
            String target = params.getValue("target");

            if(type==null) {
                if(target!=null) {
                    type = "302";
                }
            }

            if(type != null) {
                if(!isNumber(type)) {
                    if(type.equals("chain")) {
                        retValue = createChainedPURL(context, params);
                    } else if(type.equals("clone")) {
                        retValue = createClonedPURL(context, params);
                    } else if(type.equals("partial")) {
                        retValue = createPartialRedirectPURL(context, params);
                    } else {
                        // TODO: Handle unexpected
                    }
                } else {
                    try {
                        int typeInt = Integer.valueOf(type).intValue();
                        retValue = createNumericPURL(context, params, typeInt);
                    } catch(NumberFormatException nfe){
                        // TODO: Handle
                    }
                }

             } else {
                throw new PURLException("Missing PURL type parameter", 400);
            }

            return retValue;
        }
    }



    /**
     * Determine if the specified number is a number.
     * @param number String
     * @return boolean indicating whether it was true or not
     */
    private static boolean isNumber(String number) {
        boolean retValue = true;
        int idx = 0;

        if(number != null) {
            int len = number.length();

            while(retValue && idx < len) {
                retValue = Character.isDigit(number.charAt(idx++));
            }
        }

        return retValue;
    }
}
