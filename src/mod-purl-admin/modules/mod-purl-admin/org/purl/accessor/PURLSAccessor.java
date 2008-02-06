package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLSAccessor extends NKFAccessorImpl {

	public PURLSAccessor() {
		super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
	}
	@Override
	public void processRequest(INKFConvenienceHelper context) throws Exception {
        INKFResponse resp = null;
        
        String methodArg = context.getThisRequest().getArgument("method");
        String method = ((StringAspect)context.sourceAspect(methodArg, IAspectString.class)).getString();
        
        if(method.equals("POST") || method.equals("PUT")) {
            IAspectXDA xdaParam = (IAspectXDA) context.sourceAspect("this:param:param", IAspectXDA.class);

            // Validate the input document against the batch schema
            INKFRequest req = context.createSubRequest("active:validateRNG");
            req.addArgument("operand", xdaParam);
            req.addArgument("operator", "ffcpl:/schemas/batchPurls.rng");
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA xda = (IAspectXDA)context.issueSubRequestForAspect(req);
            IXDAReadOnly xdaRO = xda.getXDA();
            
            // TODO: Make this more declarative
            // TODO: What's the best way to do this?
            if(xdaRO.isTrue("/b/text()='t'")) {
                req = context.createSubRequest("active:purl-storage-batch-load");
                req.addArgument("param", xdaParam);
                IURRepresentation iur = context.issueSubRequest(req);
                resp = context.createResponseFrom(iur);
            }
        } else {
            resp = context.createResponseFrom(new StringAspect("<error message=\"Invalid Batch PURL HTTP Method: " + method + "\"/>"));
        }

		resp.setMimeType("text/xml");
		context.setResponse(resp);
	}
}