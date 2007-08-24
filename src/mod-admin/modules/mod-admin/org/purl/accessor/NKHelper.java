package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class NKHelper {
    public static final String
        MIME_TEXT = "text/plain",
        MIME_XML = "text/xml",
        MIME_HTML = "text/html";

    /**
     * Retrieve the last segment in a purl path segment.
     *
     * @param context
     * @return
     * @throws NKFException
     */
    public static String getLastSegment(INKFConvenienceHelper context) throws NKFException {
        String path=getArgument(context, "path");
        String[] parts=path.split("/");
        return parts[parts.length-1];
    }

    /**
     * Retrieve the specified argument from the context if it exists.
     *
     * @param context
     * @param argument
     * @return
     * @throws NKFException
     */
    public static String getArgument(INKFConvenienceHelper context, String argument) throws NKFException {
        String retValue = null;

        INKFRequestReadOnly req = context.getThisRequest();

        if(req.argumentExists(argument)) {
            retValue = req.getArgument(argument);
        }

        return retValue;
    }

    /**
     * Associate an HTTP response code with the specified aspect
     * @param context
     * @param aspect
     * @param code
     * @return
     * @throws NKFException
     */
    public static IURRepresentation setResponseCode(INKFConvenienceHelper context, IURAspect aspect, int code) throws NKFException {
        StringBuffer sb = new StringBuffer("<HTTPResponseCode>");
        sb.append("<code>");
        sb.append(code);
        sb.append("</code>");
        sb.append("</HTTPResponseCode>");

        INKFRequest req = context.createSubRequest("active:HTTPResponseCode");
        req.addArgument("operand", aspect);
        req.addArgument("param", new StringAspect(sb.toString()));
        IURRepresentation resp = context.issueSubRequest(req);
        return resp;
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
