package org.purl.accessor.command;

import org.purl.accessor.NKHelper;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLValidatorCommand extends PURLResolveCommand {

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse retValue = null;

        try {
            String path = NKHelper.getArgument(context, "path");
            StringBuffer sb = new StringBuffer("<purl><id>");
            sb.append(path.substring(6));
            sb.append("</id>");
            if(purl!=null) {
                try {
                    String type = purl.getXDA().getText("/purl/type", true);

                    if(type.equals("404") || type.equals("410")) {
                        sb.append("<status result=\"validated\">Validated</status></purl>");
                    } else if(type.equals("301") || type.equals("302") || type.equals("303") || type.equals("307")) {

                        String url = null;
                        if(type.equals("303")) {
                            url = purl.getXDA().getText("/purl/seealso/url", true);
                        } else {
                            url = purl.getXDA().getText("/purl/target/url", true);
                        }

                        INKFRequest req = context.createSubRequest("active:httpGet");
                        req.addArgument("url", url);
                        IURRepresentation iur = context.issueSubRequest(req);

                        req = context.createSubRequest("active:httpResponseCodeFilter");
                        req.addArgument("operand", iur);
                        context.issueSubRequest(req);

                    } else {
                        System.out.println("**********");
                    }
                } catch(XPathLocationException xple) {
                    xple.printStackTrace();
                }
            } else {
                sb.append("<status result=\"failure\">ERROR: PURL does not exist.</status></purl>");
            }

            System.out.println(sb.toString());

            retValue = context.createResponseFrom(new StringAspect(sb.toString()));
            retValue.setMimeType("text/xml");

        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

}
