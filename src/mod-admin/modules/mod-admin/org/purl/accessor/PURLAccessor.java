package org.purl.accessor;

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
        commandMap.put("DELETE", new DeleteResourceCommand(purlResolver));
    }

    static public class PurlCreator implements ResourceCreator {

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            StringBuffer sb = new StringBuffer("<purl>");
            sb.append("<pid>");
            sb.append(NKHelper.getArgument(context, "path"));
            sb.append("</pid>");
            sb.append("</purl>");
            return new StringAspect(sb.toString());
        }
    }
}
