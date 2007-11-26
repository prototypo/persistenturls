package org.purl.accessor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.purl.accessor.util.GroupResolver;
import org.purl.accessor.util.GroupResourceStorage;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLDeleter;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.util.PURLResourceStorage;
import org.purl.accessor.util.PURLURIResolver;
import org.purl.accessor.util.PurlSearchHelper;
import org.purl.accessor.util.ResourceCreator;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UserResolver;
import org.purl.accessor.util.UserResourceStorage;
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

        purlResolver = new PURLURIResolver();

        URIResolver userResolver = new UserResolver();
        URIResolver groupResolver = new GroupResolver();

        ResourceCreator purlCreator = new PurlCreator(new URIResolver[] { userResolver, groupResolver }, new UserResourceStorage());
        ResourceStorage purlStorage = new PURLResourceStorage();
        ResourceStorage groupStorage = new GroupResourceStorage();

        commandMap.put("POST", new CreateResourceCommand(TYPE, purlResolver, purlCreator, null, purlStorage));
        commandMap.put("PUT", new UpdateResourceCommand(TYPE, purlResolver, purlCreator, purlStorage));
        commandMap.put("DELETE", new DeleteResourceCommand(TYPE, purlResolver, new PURLDeleter(purlResolver), purlStorage));
        commandMap.put("GET", new GetResourceCommand(TYPE, purlResolver, purlStorage, new PurlSearchHelper(groupResolver, groupStorage)));
    }

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        return commandMap.get(method);
    }


    static public class PurlCreator implements ResourceCreator {
        private URIResolver[] maintainerResolvers;
        private ResourceStorage maintainerStorage;

        public PurlCreator( URIResolver[] maintainerResolvers, ResourceStorage maintainerStorage) {
            this.maintainerResolvers = maintainerResolvers;
            this.maintainerStorage = maintainerStorage;
        }

        private static IURAspect createChainedPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;

            String purl = NKHelper.getArgument(context, "path");
            if(purl.startsWith("ffcpl:")) {
                purl = purl.substring(6);
            }

            String existingPurl = params.getValue("basepurl");
            String oldURI = purlResolver.getURI(existingPurl);

            if(context.exists(oldURI)) {
                StringBuffer sb = new StringBuffer("<purl>");
                sb.append("<id>");
                sb.append(purl);
                sb.append("</id>");
                sb.append("<type>302</type>");

                String requestURL = context.getThisRequest().getArgument("requestURL");
                int slashIdx = requestURL.indexOf("/", 7);
                String target = requestURL.substring(0, slashIdx) + existingPurl;
                sb.append("<target><url>");
                target = target.replaceAll("&", "&amp;");
                sb.append(target);
                sb.append("</url></target>");

                String maintainers=params.getValue("maintainers");
                if(maintainers!=null) {
                    sb.append("<maintainers>");
                    StringTokenizer st = new StringTokenizer(maintainers, ",");
                    while(st.hasMoreElements()) {
                        sb.append("<uid>");
                        sb.append(st.nextToken().trim());
                        sb.append("</uid>");
                    }
                    sb.append("</maintainers>");
                }

                sb.append("</purl>");

                retValue = new StringAspect(sb.toString());


            } else {
                throw new PURLException("Cannot chain non-existent PURL: "
                    + existingPurl, 400);
            }

            return retValue;
        }

        private static IURAspect createClonedPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;
            String purl = NKHelper.getArgument(context, "path");
            if(purl.startsWith("ffcpl:")) {
                purl = purl.substring(6);
            }
            String existingPurl = params.getValue("basepurl");
            String oldURI = purlResolver.getURI(existingPurl);

            if(context.exists(oldURI)) {

                IAspectXDA oldPurlXDAOrig = (IAspectXDA) context.sourceAspect(oldURI, IAspectXDA.class);
                IXDA oldPurlXDA = oldPurlXDAOrig.getClonedXDA();
                try {
                    oldPurlXDA.replaceByText("/purl/id/text()", purl);
                    retValue = new DOMXDAAspect((DOMXDA)oldPurlXDA);
                } catch (XPathLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (XDOIncompatibilityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                throw new PURLException("Cannot clone non-existent PURL: "
                        + existingPurl, 400);
            }

            return retValue;
        }

        private static IURAspect createPartialRedirectPURL(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;

            StringBuffer sb = new StringBuffer("<purl>");
            String target = params.getValue("target");
            String purl = NKHelper.getArgument(context, "path");
            if(purl.startsWith("ffcpl:")) {
                purl = purl.substring(6);
            }

            sb.append("<id>");
            sb.append(purl);
            sb.append("</id>");
            sb.append("<type>partial</type>");

            sb.append("<target><url>");
            target = target.replaceAll("&", "&amp;");
            sb.append(target);
            sb.append("</url></target>");

            String maintainers=params.getValue("maintainers");
            if(maintainers!=null) {
                sb.append("<maintainers>");
                StringTokenizer st = new StringTokenizer(maintainers, ",");
                while(st.hasMoreElements()) {
                    sb.append("<uid>");
                    sb.append(st.nextToken().trim());
                    sb.append("</uid>");
                }
                sb.append("</maintainers>");
            }

            sb.append("</purl>");

            retValue = new StringAspect(sb.toString());

            return retValue;
        }

        private static IURAspect createNumericPURL(INKFConvenienceHelper context, IAspectNVP params, int type) throws NKFException, UnsupportedEncodingException {
            IURAspect retValue = null;

            StringBuffer sb = new StringBuffer("<purl>");
            String target = params.getValue("target");
            String purl = NKHelper.getArgument(context, "path");
            if(purl.startsWith("ffcpl:")) {
                purl = purl.substring(6);
            }

            sb.append("<id>");
            sb.append(purl);
            sb.append("</id>");
            sb.append("<type>");
            sb.append(type);
            sb.append("</type>");

            switch(type) {
            case 301:
            case 302:
            case 307:
                sb.append("<target><url>");
                target = target.replaceAll("&", "&amp;");
                sb.append(target);
                sb.append("</url></target>");
                break;
            case 303:
                String seealsos=params.getValue("seealso");
                StringTokenizer st = new StringTokenizer(seealsos, " \n");
                while(st.hasMoreElements()) {
                    sb.append("<seealso><url>");
                    sb.append(st.nextToken());
                    sb.append("</url></seealso>");
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
            if(maintainers!=null) {
                sb.append("<maintainers>");
                StringTokenizer st = new StringTokenizer(maintainers, ",");
                while(st.hasMoreElements()) {
                    sb.append("<uid>");
                    sb.append(st.nextToken().trim());
                    sb.append("</uid>");
                }
                sb.append("</maintainers>");
            }

            sb.append("</purl>");

            retValue = new StringAspect(sb.toString());


            return retValue;
        }

        public boolean checkMaintainersList(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            boolean permitted = false;
            String maintainers = params.getValue("maintainers");

            StringTokenizer st = new StringTokenizer(maintainers, ",\n");

            List<String> notFoundList = null;

            while(!permitted && st.hasMoreTokens()) {
                String next = st.nextToken();
                String uri = null;

                for(URIResolver ur : maintainerResolvers) {
                    uri = ur.getURI(next);
                    permitted = maintainerStorage.resourceExists(context, uri);

                    if(permitted) {
                        break;
                    }
                }

                if(!permitted) {
                    if(notFoundList == null) {
                        notFoundList = new ArrayList<String>();
                    }

                    notFoundList.add(next);
                }
            }

            if(!permitted) {
                String errorMessage = null;

                if(notFoundList.size() == 1) {
                    errorMessage = "Maintainer not found: " + notFoundList.get(0);
                } else {
                    StringBuffer sb = new StringBuffer("Maintainers not found: ");
                    Iterator <String> nfItor = notFoundList.iterator();
                    sb.append(nfItor.next());

                    while(nfItor.hasNext()) {
                        sb.append(",");
                        sb.append(nfItor.next());
                    }
                }
                throw new PURLException(errorMessage, 400);
            }

            return permitted;
        }

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;

            // TODO: Validate the PURLs against the existing character restrictions

            String type = params.getValue("type");
            String target = params.getValue("target");

            if(type==null) {
                if(target!=null) {
                    type = "302";
                }
            }

            if(!type.equals("clone")) {
                checkMaintainersList(context, params);
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
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
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
