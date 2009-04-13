package org.purl.accessor.purl.command;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.apache.commons.lang.StringEscapeUtils;

public class PURLChainCommand extends PURLResolveCommand {

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse resp = null;
        IXDAReadOnly purlXDARO = purl.getXDA();
        try {
            String chainedPURL = purlXDARO.getText("/purl/target/url", true);
            String requestURL = context.getThisRequest().getArgument("requestURL");
            requestURL = StringEscapeUtils.escapeXml(requestURL);
            int slashIdx = requestURL.indexOf("/", 7);
            String url = requestURL.substring(0, slashIdx) + chainedPURL;

            // TODO: Resolve the terminal PURL here without doing all the redirects
            // in case there are multiple PURLs chained together
            resp = generateResponseCode(context, "302", url);

        } catch(Throwable t) {
            t.printStackTrace();
        }
        return resp;
    }

}
