package org.purl.accessor;

import java.net.URLDecoder;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class SearchAccessor extends NKFAccessorImpl {
    
    public SearchAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        
        INKFRequest req = context.createSubRequest("active:luceneSearch");
        INKFResponse resp = null;
        String index=context.getThisRequest().getArgument("index");
        IURRepresentation retValue = null;
        int kwidx = 0;
        String kwname = getKeywordName(kwidx);
        
        StringBuffer sb = new StringBuffer("<luceneSearch><index>");
        sb.append(index);
        sb.append("</index><query>");
        
        // TODO: Use a static list of names 'keyword0', 'keyword1', etc.
        while(context.getThisRequest().argumentExists(kwname)) {
            if(kwidx > 0) {
                sb.append(" ");
            }
            String terms = URLDecoder.decode(context.getThisRequest().getArgument(kwname).substring(8));
            sb.append(terms);
            kwname=getKeywordName(++kwidx);
        }
        //sb.append(query);
        sb.append("</query>");
        sb.append("</luceneSearch>");
        
        try {
            req.addArgument("operator", new StringAspect(sb.toString()));
            retValue = context.issueSubRequest(req);
            resp = context.createResponseFrom(retValue);
        } catch(Throwable t) {
            t.printStackTrace();
            resp = context.createResponseFrom(new StringAspect("<results/>"));
        }
        
        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }
    
    private String getKeywordName(int kwidx) {
        // TODO: Optimize this to avoid object creation for, say up to ten keywords
        // and then return dynamically generated names for pathological cases.
        return "keyword" + kwidx;
    }

}
