package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.aspect.StringAspect;

public class NKHelper {
    public static final String
        MIME_TEXT = "text/plain",
        MIME_XML = "text/xml",
        MIME_HTML = "text/html";

    public static String getLastSegment(INKFConvenienceHelper context) throws NKFException {
        String path=context.getThisRequest().getArgument("path");
        String[] parts=path.split("/");
        return parts[parts.length-1];
    }

    /**
     * Log the specified message to the application log.
     *
     * @param context
     * @param logMessage
     */
    static protected void log(INKFConvenienceHelper context, String logMessage) {
        try {
            INKFRequest req = context.createSubRequest("active:application-log");
            req.addArgument("operand", new StringAspect(logMessage));
            context.issueAsyncSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }
    }
}
