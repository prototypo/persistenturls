package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.purl.accessor.command.PURLChainCommand;
import org.purl.accessor.command.PURLGoneResolveCommand;
import org.purl.accessor.command.PURLPartialRedirectResolveCommand;
import org.purl.accessor.command.PURLRedirectResolveCommand;
import org.purl.accessor.command.PURLResolveCommand;
import org.purl.accessor.command.PURLSeeAlsoResolveCommand;
import org.purl.accessor.command.PURLValidatorCommand;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLResourceStorage;
import org.purl.accessor.util.PURLURIResolver;
import org.purl.accessor.util.ResourceStorage;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLResolverAccessor extends NKFAccessorImpl {

    static private PURLURIResolver purlResolver = new PURLURIResolver();
    static private ResourceStorage purlStorageResolver = new PURLResourceStorage();

    private static Map<String,PURLResolveCommand> commandMap = new HashMap<String,PURLResolveCommand>();

    static {
        PURLResolveCommand redirectResolver = new PURLRedirectResolveCommand();
        PURLResolveCommand goneResolver = new PURLGoneResolveCommand();
        PURLResolveCommand seeAlsoResolver = new PURLSeeAlsoResolveCommand();
        PURLResolveCommand partialRedirectResolver = new PURLPartialRedirectResolveCommand();
        PURLResolveCommand purlValidator = new PURLValidatorCommand();
        PURLResolveCommand chainResolver = new PURLChainCommand();

        commandMap.put("301", redirectResolver);
        commandMap.put("302", redirectResolver);
        commandMap.put("303", seeAlsoResolver);
        commandMap.put("307", redirectResolver);
        commandMap.put("404", goneResolver);
        commandMap.put("410", goneResolver);
        commandMap.put("partial", partialRedirectResolver);
        commandMap.put("validate", purlValidator);
        commandMap.put("chain", chainResolver);
    }

    public PURLResolverAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }

    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        String path = NKHelper.getArgument(context, "path");
        String mode = NKHelper.getArgument(context, "mode");

        String purlloc = path.substring(6);
        String errMsg = null;

        INKFResponse resp = null;
        IAspectXDA purlXDA = null;
        PURLResolveCommand cmd = null;

        boolean found = false;
        boolean done = false;

        // Partial redirects require some special handling
        while(!found && !done) {
            found = purlStorageResolver.resourceExists(context, purlResolver.getURI(purlloc));

            if(!found) {
                purlloc = extractNextPurlLevel(purlloc);
                done = (purlloc == null) || (purlloc.equals("ffcpl:"));
            }
        }
        
        if(found) {
            String uri = purlResolver.getURI(purlloc);
            
            if(!purlStorageResolver.resourceIsTombstoned(context, uri)) {
                try {
                    IURAspect purl = purlStorageResolver.getResource(context, uri);
                    purlXDA = (IAspectXDA) context.transrept(purl, IAspectXDA.class);
                    IXDAReadOnly purlXDARO = purlXDA.getXDA();
                    String type = mode.equals("mode:validate") ? "validate" : purlXDARO.getText("/purl/type", true);
                    cmd = commandMap.get(type);

                    if(cmd != null) {
                        try {
                            resp = cmd.execute(context, purlXDA);
                        } catch(Throwable t){
                            // TODO: Add a default HTML Page
                            errMsg = "<purl-error>Error Resolving PURL "+ path.substring(6) + ". Please try again later.</purl-error>";
                        }
                    } else {
                        // TODO: Add a default HTML Page
                        errMsg = "<purl-error>Invalid PURL "+ path.substring(6) +"</purl-error>";
                    }
                } catch(Throwable t) {
                    errMsg = "<purl-error>Invalid PURL "+ path.substring(6) +"</purl-error>";
                }
            } else {
                    IURRepresentation iur = generatePURLResolveErrorResponse(context, 
                        purlResolver.getDisplayName(uri),
                        "ffcpl:/pub/purl-tombstoned.html");
                    resp = context.createResponseFrom(iur);
                    resp.setMimeType("text/html");
            }

            if(errMsg!=null) {
                resp = context.createResponseFrom(new StringAspect(errMsg));
                resp.setMimeType("text/xml");
            }

        } else {
            if(mode != null && !mode.equals("mode:validate")) {
                //  TODO: it seems the original intention here was to check the config for a redirect based on the path.
                //  For now, we will return a 404 until we sort out what we want to do.

                IURRepresentation code =  NKHelper.setResponseCode(context, (IURAspect)context.source("ffcpl:/pub/404-gone.html"), 404);
			    resp = context.createResponseFrom(code);
			    resp.setMimeType("text/html");

            } else {
                cmd = commandMap.get("validate");
                resp = cmd.execute(context, null);
            }
        }

        context.setResponse(resp);
    }

    private String extractNextPurlLevel(String purl) {
        String retValue = null;

        // Return the next possible purl for a partial redirect
        // i.e. if ffcpl:/tld/mypurl/redirect/foo/bar/baz is the PURL we are resolving
        // and /tld/mypurl/redirect is the base purl, we want to pop up to that until
        // we find a purl that exists. We do it from the bottom to be as specific as
        // possible.

        if(purl!=null) {
            int slashIndex = purl.lastIndexOf('/');
            if(slashIndex > 1 ) {
                retValue = purl.substring(0, slashIndex);
            }
        }
        return retValue;
    }
    
    private IURRepresentation generatePURLResolveErrorResponse(INKFConvenienceHelper context, String purl, String fileURI) throws NKFException {
        IURRepresentation retValue = null;
        
        try {
            IURRepresentation responseBody = context.source(fileURI);
            // TODO: Turn this into a resource!
            StringBuffer sed = new StringBuffer("<sed><pattern><regex>@@PURL@@</regex><replace>");
            sed.append(purl);
            sed.append("</replace></pattern></sed>");
            
            INKFRequest req = context.createSubRequest("active:sed");
            req.addArgument("operand", responseBody);
            req.addArgument("operator", new StringAspect(sed.toString()));
            retValue = context.issueSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }
        
        
        return retValue;
    }
}
