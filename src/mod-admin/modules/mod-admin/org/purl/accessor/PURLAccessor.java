package org.purl.accessor;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLAccessor extends AbstractAccessor {

    static {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        URIResolver purlResolver = new URIResolver() {

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

        };

        ResourceCreator purlCreator = new PurlCreator();

        commandMap.put("POST", new CreateResourceCommand(purlResolver, purlCreator));
        commandMap.put("PUT", new UpdateResourceCommand(purlResolver, purlCreator));
        commandMap.put("DELETE", new DeleteResourceCommand(purlResolver));
    }

    static public class PurlCreator implements ResourceCreator {

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IURAspect retValue = null;
            Iterator itor = context.getThisRequest().getArguments();
            System.out.println("ARGUMENTS:");
            while(itor.hasNext()) {
                System.out.println((String)itor.next());
            }
            System.out.println("=============");
            String type = params.getValue("type");
            String target = params.getValue("target");

            if(type==null) {
                if(target!=null) {
                    type = "302";
                }
            }

            if(type != null) {
                int typeInt = -1;

                if(!type.equals("clone") && !type.equals("chain") && !type.equals("partial")) {
                    try {
                        typeInt = Integer.valueOf(type).intValue();
                    } catch(NumberFormatException nfe){
                        // TODO: Handle
                    }
                }

                StringBuffer sb = new StringBuffer("<purl>");
                sb.append("<pid>");
                sb.append(NKHelper.getArgument(context, "path"));
                sb.append("</pid>");
                sb.append("<type>");
                sb.append(type);
                sb.append("</type>");

                switch(typeInt) {
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
                case -1:
                    // clone, chain or partial
                    break;
                }

                String maintainers=params.getValue("maintainers");
                System.out.println("maintainers: " + maintainers);
                if(maintainers!=null) {
                    sb.append("<maintainers>");
                    StringTokenizer st = new StringTokenizer(maintainers, " \n");
                    while(st.hasMoreElements()) {
                        sb.append("<uid>");
                        sb.append(st.nextToken());
                        sb.append("</uid>");
                    }
                    sb.append("</maintainers>");
                }

                sb.append("</purl>");

                System.out.println(sb.toString());
                retValue = new StringAspect(sb.toString());
            } else {
                throw new PURLException("Missing PURL type parameter", 400);
            }

            return retValue;
        }
    }
}
