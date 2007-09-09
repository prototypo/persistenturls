package org.purl.accessor.command;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLRedirectResolveCommand extends PURLResolveCommand {

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse resp = null;
        IXDAReadOnly purlXDARO = purl.getXDA();
        try {
            String type = purlXDARO.getText("/purl/type", true);
            String url = purlXDARO.getText("/purl/target/url", true);
            INKFRequest req=context.createSubRequest();
            req.setURI("active:HTTPResponseCode");
            StringBuffer respCode = new StringBuffer("<HTTPResponseCode><code>");
            respCode.append(type);
            respCode.append("</code><header><name>Location</name><value>");
            respCode.append(url);
            respCode.append("</value></header></HTTPResponseCode>");
            req.addArgument("param", new StringAspect(respCode.toString()));
            IURRepresentation result=context.issueSubRequest(req);
            resp=context.createResponseFrom(result);
        } catch(Throwable t) {
            t.printStackTrace();
        }
        return resp;
    }
}
