package org.purl.accessor.purl.command;

import java.io.IOException;
import java.net.UnknownHostException;

import org.purl.accessor.util.NKHelper;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.representation.DOMXDAAspect;
import org.ten60.netkernel.xml.xda.XPathLocationException;
import org.ten60.netkernel.xml.xda.DOMXDA;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;
import com.ten60.netkernel.util.NetKernelException;

public class PURLValidatorCommand extends PURLResolveCommand {

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse retValue = null;
        int responseCode = 200;
        try {
            String path = NKHelper.getArgument(context, "path");
            //StringBuffer sb = new StringBuffer("<purl><id>");
            //sb.append(path.substring(6));
            //sb.append("</id>");
            IURAspect asp;
            if(purl!=null) {

                try {
                    DOMXDA result = (DOMXDA)purl.getClonedXDA();
                    String type = purl.getXDA().getText("/purl/type", true);
                    String pid = purl.getXDA().getText("/purl/id", true);

                    if(type.equals("404") || type.equals("410")) {

                            result.appendPath("/purl", "@validation", "validated");
                        //sb.append("<status result=\"validated\">Validated</status></purl>");
                    } else if(type.equals("301") || type.equals("302") || type.equals("303") || type.equals("307")) {

                        String url = null;
                        if(type.equals("303")) {
                            url = purl.getXDA().getText("/purl/seealso/url", true);
                        } else {
                            url = purl.getXDA().getText("/purl/target/url", true);
                        }

                        try {
                            INKFRequest req = context.createSubRequest("active:httpHead");
                            req.addArgument("url", url);
                            IURRepresentation iur = context.issueSubRequest(req);

                            req = context.createSubRequest("active:httpResponseCodeFilter");
                            req.addArgument("operand", iur);
                            context.issueSubRequest(req);

                            //sb.append("<status result=\"success\">Success</status>");

                            result.appendPath("/purl", "@validation", "success");

                        } catch(NKFException e) {
                            String error = null;
                            Throwable t = e.getCause();
                            if(t instanceof NetKernelException) {
                                t=t.getCause();
                                if(t instanceof UnknownHostException) {
                                    error = "Cannot find host: " + url;
                                } else if((t instanceof NetKernelException) || (t instanceof IOException)) {
                                    error = "Error resolving PURL target: " + url;
                                }
                            }

                            if(error==null) {
                                error = "Could not validate purl: " + pid;
                            }

                            result.appendPath("/purl", "@validation", "failure");
                            result.appendPath("/purl", "message", error);
                            responseCode = 409;
                        }


                    } else {
                        System.out.println("**********");
                    }
                    asp = new DOMXDAAspect(result);
                } catch(XPathLocationException xple) {
                    xple.printStackTrace();
                    StringBuffer sb = new StringBuffer();
                    sb.append("<purl validation=\"failure\"><id>");
                    sb.append(path.substring(6));
                    sb.append("</id>");
                    sb.append("<message>Invalid input XML</message></purl>");
                    sb.append("</purl>");
                    asp = new StringAspect(sb.toString());
                    responseCode = 409;
                }

            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("<purl validation=\"failure\"><id>");
                sb.append(path.substring(6));
                sb.append("</id>");            
                sb.append("<message >PURL does not exist.</message>");
                sb.append("</purl>");
                asp = new StringAspect(sb.toString());
                responseCode = 409;
            }


            IURRepresentation rep = NKHelper.setResponseCode(context, asp, responseCode);
            rep = NKHelper.attachGoldenThread(context, "gt:" + path , rep);
            retValue = context.createResponseFrom(rep);
            retValue.setCacheable();
            retValue.setMimeType(NKHelper.MIME_XML);

        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

}
