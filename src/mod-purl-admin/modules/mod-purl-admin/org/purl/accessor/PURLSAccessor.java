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
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.layer1.representation.NVPAspect;
import org.ten60.netkernel.layer1.representation.NVPImpl;
import org.ten60.netkernel.xml.representation.DOMXDAAspect;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.DOMXDA;
import org.ten60.netkernel.xml.xda.IXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XDOIncompatibilityException;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
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
            boolean valid = xdaRO.isTrue("/b/text()='t'");
            
            // TODO: Make this more declarative
            // TODO: What's the best way to do this?
            if(valid) {
                try {
                     xdaParam = checkPURLsAndUsers(context, xdaParam);
                     xdaParam = checkForClones(context, xdaParam);
                     xdaParam = checkForChains(context, xdaParam);
                     req = context.createSubRequest("active:purl-storage-batch-load");
                     req.addArgument("param", xdaParam);
                     req.addArgument("currentuser", "data:text/plain," + NKHelper.getUser(context));
                     IURRepresentation iur = context.issueSubRequest(req);
                      resp = context.createResponseFrom(iur);
                }  catch(PURLException pe) {
                    errorMessage = pe.getMessage();
                    errorCode = pe.getResponseCode();
                } catch(Throwable t) {
                	errorMessage = "Error Processing Batchload";
                	errorCode = 500;
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
	
	private IAspectXDA checkPURLsAndUsers(INKFConvenienceHelper context, IAspectXDA batchXDA) throws PURLException {
        IXDA retValue = null;
        boolean modified = false;

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
	            
	            // If we come across a PURL with a trailing '/', normalize it to our
	            // displayName format.
	            
	            if(p_id.endsWith("/")) {
	                if(!modified) {
	                    retValue = batchXDA.getClonedXDA();
	                    modified = true;
	                }
	                
	                try {
	                    retValue.setText(xdaROItor.getCurrentXPath() + "/@id", purlResolver.getDisplayName(p_id));
	                } catch (XDOIncompatibilityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
	            }
	        }
	        
	        xdaROItor = batchXDA.getXDA().readOnlyIterator("//maintainers/uid");
	        
	        Set<String> maintainerSet = new HashSet<String>();
	        
	        // TODO: Add this to the larger iteration
	        while(xdaROItor.hasNext()) {
	            xdaROItor.next();
	            String maintainer = xdaROItor.getText(".", true);
	            maintainerSet.add(maintainer);
	        }
	        
	        xdaROItor = batchXDA.getXDA().readOnlyIterator("//maintainers/gid");
	        
	        // TODO: Add this to the larger iteration
	        while(xdaROItor.hasNext()) {
	            xdaROItor.next();
	            String maintainer = xdaROItor.getText(".", true);
	            maintainerSet.add(maintainer);
	        }
	        
	        Iterator<String> maintainerItor = maintainerSet.iterator();
	        
	        while(maintainerItor.hasNext()) {
	            String next = maintainerItor.next();
	            if(!NKHelper.validUser(context, next) && !NKHelper.validGroup(context, next)) {
	                throw new PURLException("Invalid user or group: " + next + " in batch.", 400);
	            }
	        }
	    } catch (XPathLocationException e) {
            e.printStackTrace();
        } 
	    
        return modified ? new DOMXDAAspect((DOMXDA)retValue) : batchXDA;
	}
	
	private IAspectXDA checkForClones(INKFConvenienceHelper context, IAspectXDA batchXDA) throws PURLException
	{
	    IXDA retValue = null;
	    boolean modified = false;

	    try {
	        IXDAReadOnlyIterator xdaROItor = batchXDA.getXDA().readOnlyIterator("/purls/purl[@type='clone']");
	        
	        if(xdaROItor.hasNext()) {
	            retValue = batchXDA.getClonedXDA();
	        }
	        
	        while(xdaROItor.hasNext()) {
	            xdaROItor.next();
	            
	            try {
	                String basepurl = xdaROItor.getText("basepurl/@path", true);
	                String baseURI = purlResolver.getURI(basepurl);
	                
	                if(!purlStorage.resourceExists(context, baseURI)) {
	                    throw new PURLException("Invalid cloned PURL: " + basepurl + " for PURL:" 
	                            + xdaROItor.getText("@id", true), 400);
	                }
	                
                   IURAspect iur = purlStorage.getResource(context, baseURI);
                   IAspectXDA oldPurlXDAOrig = (IAspectXDA) context.transrept(iur, IAspectXDA.class);
                   IXDAReadOnly xdaRO = oldPurlXDAOrig.getXDA();
                   String type = xdaRO.getText("/purl/type", true);

	                
	                StringBuffer sb = new StringBuffer("<purl id=\"");
	                sb.append(xdaROItor.getText("@id", true));
	                sb.append("\" type=\"");
	                sb.append(type);
	                sb.append("\">");
                    sb.append("<maintainers>");
                    IXDAReadOnlyIterator maintainersItor = xdaRO.readOnlyIterator("maintainers/uid");
                    
                    while(maintainersItor.hasNext()) {
                        maintainersItor.next();
                        sb.append("<maintainer id=\"");
                        sb.append(maintainersItor.getText(".", true));
                        sb.append("\"/>");
                    }
                    
                    sb.append("</maintainers>");
                    
                    if(isNumber(type)) {
                        int numType = Integer.valueOf(type).intValue();
                        
                        switch(numType) {
                            case 301:
                            case 302:
                            case 307:
                                sb.append("<target url=\"");
                                sb.append(xdaRO.getText("/purl/target/url", true));
                                sb.append("\"/>");
                                break;
                            case 303:
                                sb.append("<seealso url=\"");
                                sb.append(xdaRO.getText("/purl/target/url", true));
                                sb.append("\"/>");                                
                                break;
                            case 404:
                            case 410:
                                break;
                        }
                    } else {
                        System.out.println("************************THIS NEEDS DOING");
                    }
                    
	                sb.append("</purl>");
	                
	                IAspectXDA newPURLXDA = (IAspectXDA) context.transrept(new StringAspect(sb.toString()), IAspectXDA.class);
	                retValue.replace(newPURLXDA.getXDA(), "/purl", xdaROItor.getCurrentXPath());	                

	                modified = true;
	            } catch(NKFException nfe) {
	                nfe.printStackTrace();
	            } catch (XDOIncompatibilityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
	        }
	    } catch (XPathLocationException e) {
            e.printStackTrace();
        }
	    
        return modified ? new DOMXDAAspect((DOMXDA)retValue) : batchXDA;
	}
	
   private IAspectXDA checkForChains(INKFConvenienceHelper context, IAspectXDA batchXDA) throws PURLException
   {
	   IXDA retValue = null;
	   boolean modified = false;
	   
	   try {
           IXDAReadOnlyIterator xdaROItor = batchXDA.getXDA().readOnlyIterator("/purls/purl[@type='chain']");
           
           if(xdaROItor.hasNext()) {
               retValue = batchXDA.getClonedXDA();
           }
           
           while(xdaROItor.hasNext()) {
               xdaROItor.next();
               String basepurl = xdaROItor.getText("basepurl/@path", true);
               String baseURI = purlResolver.getURI(basepurl);
               if(purlStorage.resourceExists(context, baseURI)) {
                   StringAspect targetShim = new StringAspect("<target url=\"" + basepurl + "\"/>");
                   DOMXDAAspect targetShimDOMXDA = (DOMXDAAspect) context.transrept(targetShim, DOMXDAAspect.class);
                   retValue.append(targetShimDOMXDA.getXDA(), "/", xdaROItor.getCurrentXPath());
                   modified = true;
               } else {
                   throw new PURLException("Invalid chained PURL: " + basepurl + " for PURL:" 
                           + xdaROItor.getText("@id", true), 400);
               }
           }
	   } catch (XPathLocationException e) {
	       e.printStackTrace();
	   } catch (NKFException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
	   } catch (XDOIncompatibilityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
	   } 
	   
       return modified ? new DOMXDAAspect((DOMXDA)retValue) : batchXDA;
   }
	
    private boolean isNumber(String number) {
        boolean retValue = true;
        int idx = 0;

        if(number != null) {
            int len = number.length();

            while(retValue && idx < len) {
                retValue = Character.isDigit(number.charAt(idx++));
            }
        }

        return retValue;
    }
}