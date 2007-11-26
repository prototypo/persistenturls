package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PendingAccessor extends NKFAccessorImpl {
    
    private static Map<String,String> pendingRequestMap;
    
    static {
        pendingRequestMap = new HashMap<String,String>();
        pendingRequestMap.put("domain", "active:purl-storage-pending-domains");
        pendingRequestMap.put("user", "active:purl-storage-pending-users");        
    }

    public PendingAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE); 
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        String path = context.getThisRequest().getArgument("path");
        String methodArg = context.getThisRequest().getArgument("method");
        String method = ((StringAspect)context.sourceAspect(methodArg, IAspectString.class)).getString();

        String type = ((IAspectString) context.sourceAspect("this:param:type", IAspectString.class)).getString();
        String requestURI = pendingRequestMap.get(type);
        INKFResponse resp = null;
        
        INKFRequest req = context.createSubRequest(requestURI);
        req.setAspectClass(IAspectXDA.class);
        IAspectXDA pending = (IAspectXDA) context.issueSubRequestForAspect(req);
        
        if(method.equals("GET")) {
            if(requestURI != null) {
                resp = context.createResponseFrom(pending);
            } else {
                // TODO: Handle
                resp = context.createResponseFrom(new StringAspect("<purl-error>Invalid pending request type: "+ type + ".</purl-error>"));
            }
        } else if(method.equals("POST")) {
            IAspectXDA param = (IAspectXDA) context.sourceAspect("this:param:param", IAspectXDA.class);
            IXDAReadOnly paramRO = param.getXDA();
            IAspectString paramSA = (IAspectString) context.transrept(param, IAspectString.class);
            System.out.println(paramSA.getString());
            
            resp = context.createResponseFrom(param);
        }

        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }

}
