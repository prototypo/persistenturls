package org.purl.accessor;

import java.util.Date;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class IndexAccessor extends NKFAccessorImpl {

    public IndexAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }
    
    private void checkIndex(INKFConvenienceHelper context, String index) throws NKFException {
        String initializedIndexRes = index + "index-initialized";
        if(!context.exists(initializedIndexRes)) {
            INKFRequest req = context.createSubRequest("active:luceneIndex");
            StringBuffer sb = new StringBuffer("<luceneIndex><index>");
            sb.append(index);
            sb.append("</index><reset/><close/></luceneIndex>");
            req = context.createSubRequest("active:luceneIndex");
            req.addArgument("operator", new StringAspect(sb.toString()));
            context.issueSubRequest(req);
            context.sinkAspect(initializedIndexRes, new StringAspect("<index-created>" + new Date() + "</index-created>"));
        }
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        try {
            IURRepresentation rep = context.source("this:param:operand");
            String path=context.getThisRequest().getArgument("path");
            String index=context.getThisRequest().getArgument("index");
            checkIndex(context,index);            

            StringBuffer sb = new StringBuffer("<luceneIndex><index>");
            sb.append(index);
            sb.append("</index><id>");
            sb.append(path);
            sb.append("</id></luceneIndex>");
            INKFRequest req = context.createSubRequest("active:luceneIndex");
            req.addArgument("operand", rep);
            req.addArgument("operator", new StringAspect(sb.toString()));
            context.issueSubRequest(req);

            sb = new StringBuffer("<luceneIndex><index>");
            sb.append(index);
            sb.append("</index><close/></luceneIndex>");
            req=context.createSubRequest("active:luceneIndex");
            req.addArgument("operator", new StringAspect(sb.toString()));
            context.issueSubRequest(req); 
        } catch(NKFException e) {
            e.printStackTrace();
        }        
        INKFResponse resp = context.createResponseFrom(new StringAspect("<indexing>foo</indexing>"));
        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }
}
