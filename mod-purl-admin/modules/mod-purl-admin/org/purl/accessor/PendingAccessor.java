package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.purl.accessor.domain.DomainResolver;
import org.purl.accessor.util.*;
import org.purl.accessor.user.UserResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PendingAccessor extends NKFAccessorImpl {
    
    private static Map<String,String> pendingRequestMap;
    private static Map<String,String> approveRequestMap;
    private static Map<String,String> denyRequestMap;    
    
    private static URIResolver domainResolver = new DomainResolver();
    private static URIResolver userResolver = new UserResolver();
    
    static {
        pendingRequestMap = new HashMap<String,String>();
        pendingRequestMap.put("domain", "active:purl-storage-pending-domains");
        pendingRequestMap.put("user", "active:purl-storage-pending-users");
        approveRequestMap = new HashMap<String,String>();
        approveRequestMap.put("domain", "active:purl-storage-approve-domain");
        approveRequestMap.put("user", "active:purl-storage-approve-user");
        denyRequestMap = new HashMap<String,String>();        
        denyRequestMap.put("domain", "active:purl-storage-reject-domain");
        denyRequestMap.put("user", "active:purl-storage-reject-user");                
    }

    public PendingAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        String methodArg = context.getThisRequest().getArgument("method");
        String method = ((StringAspect)context.sourceAspect(methodArg, IAspectString.class)).getString();

        String type = ((IAspectString) context.sourceAspect("this:param:type", IAspectString.class)).getString().toLowerCase();
        
        INKFResponse resp = null;
        
        if(method.equals("GET")) {
            String requestURI = pendingRequestMap.get(type);

            INKFRequest req = context.createSubRequest(requestURI);
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA pending = (IAspectXDA) context.issueSubRequestForAspect(req);
            
            if(requestURI != null) {
                resp = context.createResponseFrom(pending);
            } else {
                // TODO: Handle
                resp = context.createResponseFrom(new StringAspect("<purl-error>Invalid pending request type: "+ type + ".</purl-error>"));
            }
        } else if(method.equals("POST")) {
            if(context.exists("this:param:param")) {
                IAspectNVP params = (IAspectNVP) context.sourceAspect("this:param:param", IAspectNVP.class);
                IAspectString param = null;
                String decisionResult = params.getValue("decision");
                String uri = null;
                String resource = null;
                boolean approval = false;
                
                if(decisionResult.equals("approve")) {
                    uri = approveRequestMap.get(type);
                    approval = true;
                } else if(decisionResult.equals("deny")){
                    uri = denyRequestMap.get(type);                    
                } else {
                    // TODO: Handle Error
                }
                
                if(type.equals("user")) {
                    resource = userResolver.getURI(context).substring(12);
                    param = new StringAspect("<user><id>" + DataHelper.cleanseInput(resource )+ "</id></user>");

                } else if(type.equals("domain")) {
                    resource = domainResolver.getURI(context).substring(13);
                    param = new StringAspect("<domain><id>" + DataHelper.cleanseInput(resource) + "</id></domain>");                    
                } else {
                    // TODO: Handle Error
                }

                INKFRequest req = context.createSubRequest(uri);
                req.addArgument("param", param);
                IURRepresentation res = context.issueSubRequest(req);
                resp = context.createResponseFrom(res);
                
                if(approval) {
                    req=context.createSubRequest("active:cutGoldenThread");
                    req.addArgument("param", "gt:resource:" + resource);
                    context.issueSubRequest(req);
                }
                
            } else {
                // TODO: Handle Error
            }
        }

        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }

}
