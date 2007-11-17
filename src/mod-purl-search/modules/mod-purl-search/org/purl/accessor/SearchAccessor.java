package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class SearchAccessor extends NKFAccessorImpl {
    public SearchAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        
        INKFRequest req = context.createSubRequest("active:luceneSearch");
        String index=context.getThisRequest().getArgument("index");
        IAspectXDA queryXDA= (IAspectXDA) context.sourceAspect("this:param:query", IAspectXDA.class);
        String query = queryXDA.getXDA().getText("/query", true);
        System.out.println("QUERY: " + query);
        IURRepresentation retValue = null;
        
        StringBuffer sb = new StringBuffer("<luceneSearch><index>");
        sb.append(index);
        sb.append("</index><query>");
        sb.append(query);
        sb.append("</query>");
        sb.append("</luceneSearch>");
        
        System.out.println(sb.toString());
        req.addArgument("operator", new StringAspect(sb.toString()));
        retValue = context.issueSubRequest(req);
        
        INKFResponse resp = context.createResponseFrom(retValue);
        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }

}
