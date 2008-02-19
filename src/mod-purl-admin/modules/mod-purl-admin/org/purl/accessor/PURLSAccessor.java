package org.purl.accessor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.purl.accessor.util.AllowableResource;
import org.purl.accessor.util.DomainResolver;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLAllowableResource;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.util.PURLResourceStorage;
import org.purl.accessor.util.PURLURIResolver;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.layer1.representation.NVPAspect;
import org.ten60.netkernel.layer1.representation.NVPImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLSAccessor extends NKFAccessorImpl {
    
    private URIResolver purlResolver;
    private URIResolver domainResolver;
    private ResourceStorage purlStorage;
    private AllowableResource purlAllowableResource;

	public PURLSAccessor() {
		super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
		
		purlResolver = new PURLURIResolver();
		domainResolver = new DomainResolver();
        purlStorage = new PURLResourceStorage();
		purlAllowableResource = new PURLAllowableResource(purlStorage, purlResolver, domainResolver);
	}
	
	@Override
	public void processRequest(INKFConvenienceHelper context) throws Exception {
        INKFResponse resp = null;
        
        String methodArg = context.getThisRequest().getArgument("method");
        String method = ((StringAspect)context.sourceAspect(methodArg, IAspectString.class)).getString();
        IAspectXDA xdaParam = (IAspectXDA) context.sourceAspect("this:param:param", IAspectXDA.class);
        INKFRequest req = null;
        String errorMessage = null;
        int errorCode = -1;
        
        if(method.equals("POST")) { 
            // Validate the input document against the batch schema
            req = context.createSubRequest("active:validateRNG");
            req.addArgument("operand", xdaParam);
            req.addArgument("operator", "ffcpl:/schemas/batchPurls.rng");
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA xda = (IAspectXDA)context.issueSubRequestForAspect(req);
            IXDAReadOnly xdaRO = xda.getXDA();
            
            // TODO: Make this more declarative
            // TODO: What's the best way to do this?
            if(xdaRO.isTrue("/b/text()='t'")) {
                try {
                 if(checkPURLsAndUsers(context, xdaParam)) {
                     req = context.createSubRequest("active:purl-storage-batch-load");
                     req.addArgument("param", xdaParam);
                     req.addArgument("currentuser", "data:text/plain," + NKHelper.getUser(context));
                     IURRepresentation iur = context.issueSubRequest(req);
                      resp = context.createResponseFrom(iur);
                 }
                }  catch(PURLException pe) {
                    errorMessage = pe.getMessage();
                    errorCode = pe.getResponseCode();
                }
             } else {
                 errorMessage = "Batch Load does not validate against schema.";
                 errorCode = 400;
             }
        } else if(method.equals("PUT")) {
            IXDAReadOnlyIterator xdaROItor = xdaParam.getXDA().readOnlyIterator("/purls/purl");
            int count = 0;
            
            while(xdaROItor.hasNext()) {
                xdaROItor.next();

                //TODO: Handle transactions

                NVPImpl nvp = new NVPImpl();

                String pid = xdaROItor.getText("@id", true);
                String type = xdaROItor.getText("@type", true);
                IXDAReadOnlyIterator maintainerXdaROItor = xdaParam.getXDA().readOnlyIterator( xdaROItor.getCurrentXPath() + "/maintainers/maintainer");
                StringBuffer sb = new StringBuffer();
                while(maintainerXdaROItor.hasNext()) {
                    maintainerXdaROItor.next();
                    if(sb.length()>0) {
                        sb.append(",");
                    }
                    sb.append(maintainerXdaROItor.getText("@id", true));
                }

                nvp.addNVP("maintainers", sb.toString());
                nvp.addNVP("type", type);

                if(type.equals("303")) {
                    String seeAlso = xdaROItor.getText("seealso/@url", true);
                    nvp.addNVP("seealso", seeAlso);
                } else if(type.equals("clone") || type.equals("chain")) {
                    String basepurl = xdaROItor.getText("basepurl/@path", true);
                    nvp.addNVP("basepurl", basepurl);
                } else if(!type.equals("404") && !type.equals("410")) {
                    String target = xdaROItor.getText("target/@url", true);
                    nvp.addNVP("target", target);
                }

                req=context.createSubRequest("active:purl");
                req.addArgument("path", "ffcpl:/purl" + pid );
                req.addArgument("method", context.source(context.getThisRequest().getArgument("method")));
                req.addArgument("params", new NVPAspect(nvp));
                req.addArgument("requestURL", context.getThisRequest().getArgument("requestURL"));
                req.addArgument("cookie", context.getThisRequest().getArgument("cookie"));
                req.addArgument("session", context.getThisRequest().getArgument("session"));                
                // TODO: Right now we are not doing anything with the results. 
                IURRepresentation iur=context.issueSubRequest(req);
                count++;
            }
            
            StringBuffer sb = new StringBuffer("<purl-batch-success numCreated=\"");
            sb.append(count);
            sb.append("\"/>");
            
            resp = context.createResponseFrom(new StringAspect(sb.toString()));
        } else {
            errorMessage = "Invalid Batch PURL HTTP Method: " + method;
            errorCode = 400;
        }

        if(errorMessage != null) {
            StringAspect sa = new StringAspect("<error message=\"" + errorMessage + "\"/>");
            IURRepresentation iur = NKHelper.setResponseCode(context, sa, errorCode);
            resp = context.createResponseFrom(iur); 
        }
 
        resp.setMimeType("text/xml");
        context.setResponse(resp);
	}
	
	private boolean checkPURLsAndUsers(INKFConvenienceHelper context, IAspectXDA batchXDA) throws PURLException {
	    boolean retValue = false;

	    Set<String> alreadySeen = new HashSet<String>();
	    
	    try {
	        IXDAReadOnlyIterator xdaROItor = batchXDA.getXDA().readOnlyIterator("/purls/purl");
	        while(xdaROItor.hasNext()) {
	            xdaROItor.next();
	            String p_id = xdaROItor.getText("@id", true);
	            if(purlAllowableResource.allow(context, purlResolver.getURI(p_id))) {
	                if(!alreadySeen.contains(p_id)) {
	                    alreadySeen.add(p_id);
	                } else {
	                    throw new PURLException("Duplicate PURL specified in batch: " + purlResolver.getDisplayName(p_id), 400);
	                }
	            } else {
	                throw new PURLException("PURL " + purlResolver.getDisplayName(p_id) + " cannot be created.", 400);
	            }
	        }
	        
	        xdaROItor = batchXDA.getXDA().readOnlyIterator("//maintainers/maintainer");
	        
	        Set<String> maintainerSet = new HashSet<String>();
	        
	        // TODO: Add this to the larger iteration
	        while(xdaROItor.hasNext()) {
	            xdaROItor.next();
	            String maintainer = xdaROItor.getText("./@id", true);
	            maintainerSet.add(maintainer);
	        }
	        
	        Iterator<String> maintainerItor = maintainerSet.iterator();
	        
	        while(maintainerItor.hasNext()) {
	            String next = maintainerItor.next();
	            if(!NKHelper.validUser(context, next)) {
	                throw new PURLException("Invalid user: " + next + " in batch.", 400);
	            }
	        }
	        
	        retValue = true;
	    } catch (XPathLocationException e) {
            e.printStackTrace();
        } 
	    
	    return retValue;
	}
}