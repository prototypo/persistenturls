package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
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
        return getLastSegment(getArgument(context, "path"));
    }
    
    public static String getLastSegment(String path) {
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
    public static void log(INKFConvenienceHelper context, String logMessage) {
        try {
            INKFRequest req = context.createSubRequest("active:application-log");
            req.addArgument("operand", new StringAspect(logMessage));
            context.issueAsyncSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attach a Golden Thread to a representation.
     */
    public static IURRepresentation attachGoldenThread(INKFConvenienceHelper context, String goldenThreadName, IURRepresentation representation){
        IURRepresentation retValue = null;

        try {
            INKFRequest req = context.createSubRequest("active:attachGoldenThread");
            req.addArgument("operand", representation);
            req.addArgument("param", goldenThreadName);
            retValue = context.issueSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    /**
     * Attach a Golden Thread to a resource.
     */
    public static IURRepresentation attachGoldenThread(INKFConvenienceHelper context, String goldenThreadName, IURAspect resource){
        IURRepresentation retValue = null;

        try {
            INKFRequest req = context.createSubRequest("active:attachGoldenThread");
            req.addArgument("operand", resource);
            req.addArgument("param", goldenThreadName);
            retValue = context.issueSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    /**
     * Delete a Golden Thread to a representation.
     */
    public static void cutGoldenThread(INKFConvenienceHelper context, String goldenThreadName){
        try {
            INKFRequest req = context.createSubRequest("active:cutGoldenThread");
            req.addArgument("param", goldenThreadName);
            context.issueSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }
    }

    public static void initializeLuceneIndex(INKFConvenienceHelper context, String indexName) {
        try {
            StringBuffer sb = new StringBuffer("<luceneIndex><index>");
            sb.append(indexName);
            sb.append("</index><reset/><close/></luceneIndex>");
            INKFRequest req = context.createSubRequest("active:luceneIndex");
            req.addArgument("operator", new StringAspect(sb.toString()));
            context.issueSubRequest(req);

        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
    }

    public static void indexResource(INKFConvenienceHelper context, String indexName, String id, IURAspect res) {
        try {
            StringAspect sa = (StringAspect) context.transrept(res, IAspectString.class);
            StringBuffer sb = new StringBuffer("<luceneIndex><index>");
            sb.append(indexName);
            sb.append("</index><id>");
            sb.append(id);
            sb.append("</id></luceneIndex>");
            INKFRequest req = context.createSubRequest("active:luceneIndex");
            req.addArgument("operand", res);
            req.addArgument("operator", new StringAspect(sb.toString()));
            context.issueSubRequest(req);

            sb = new StringBuffer("<luceneIndex><index>");
            sb.append(indexName);
            sb.append("</index><close/></luceneIndex>");
            req=context.createSubRequest("active:luceneIndex");
            req.addArgument("operator", new StringAspect(sb.toString()));
            context.issueSubRequest(req);
        } catch(NKFException e) {
            e.printStackTrace();
        }
    }

    public static IURRepresentation search(INKFConvenienceHelper context, String indexName, String query) {
        IURRepresentation retValue = null;

        try {
            INKFRequest req = context.createSubRequest("active:luceneSearch");
            StringBuffer sb = new StringBuffer("<luceneSearch><index>");
            sb.append(indexName);
            sb.append("</index><query>");
            sb.append(query);
            sb.append("</query>");
            sb.append("</luceneSearch>");
            req.addArgument("operator", new StringAspect(sb.toString()));
            retValue = context.issueSubRequest(req);

        } catch(NKFException e) {
            e.printStackTrace();
        }

        return retValue;
    }
    
    public static String getMD5Value(INKFConvenienceHelper context, String value) {
        String retValue = null;
        
        try {
            INKFRequest req = context.createSubRequest("active:md5");
            req.addArgument("operand", new StringAspect("<key>" + value + "</key>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = result.getXDA().getText("/md5", true);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
}
