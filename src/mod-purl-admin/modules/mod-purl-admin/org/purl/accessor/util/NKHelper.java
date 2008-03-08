package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectBoolean;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

import java.net.URI;
import java.net.URISyntaxException;

public class NKHelper {
    public static final String
        MIME_TEXT = "text/plain",
        MIME_XML = "text/xml",
        MIME_HTML = "text/html";
    
    private static URIResolver domainResolver = new DomainResolver();

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

    public static void updateIndices(INKFConvenienceHelper context) {
        try {
            INKFRequest req = context.createSubRequest("active:purl-entries-reindex");
            context.issueAsyncSubRequest(req);
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
    }
    
    public static void indexResource(INKFConvenienceHelper context, String indexName, String id, IURAspect res) {
        try {
            //acquireLock(context, "active:purl-reindex");
            indexResourceNoClose(context, indexName, id, res);
            closeIndex(context, indexName);
        } catch(NKFException e) {
            e.printStackTrace();
        } finally {
/*            try {
                //releaseLock(context, "active:purl-reindex");
            } catch(NKFException e) {
                e.printStackTrace();
            } */
        }
    }
    
    public static void indexResourceNoClose(INKFConvenienceHelper context, String indexName, String id, IURAspect res) throws NKFException {
        StringBuffer sb = new StringBuffer("<luceneIndex><index>");
        sb.append(indexName);
        sb.append("</index><id>");
        sb.append(id);
        sb.append("</id></luceneIndex>");
        INKFRequest req = context.createSubRequest("active:luceneIndex");
        req.addArgument("operand", res);
        req.addArgument("operator", new StringAspect(sb.toString()));
        context.issueSubRequest(req);            
    }

    public static void closeIndex(INKFConvenienceHelper context, String indexName) throws NKFException {
        StringBuffer sb = new StringBuffer("<luceneIndex><index>");
        sb.append(indexName);
        sb.append("</index><close/></luceneIndex>");
        INKFRequest req=context.createSubRequest("active:luceneIndex");
        req.addArgument("operator", new StringAspect(sb.toString()));
        context.issueSubRequest(req);
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
    
    public static String getUser(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String sessionURI=context.getThisRequest().getArgument("session");
            String tokenURI=sessionURI+"+key@ffcpl:/credentials";
            IAspectString credentials = (IAspectString)context.sourceAspect(tokenURI,IAspectString.class);
            retValue = credentials.getString().trim().toLowerCase();
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }
    
    public static boolean validUser(INKFConvenienceHelper context, String user) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-user-valid");
            req.addArgument("uri", "ffcpl:/user/" + user); 
            req.setAspectClass(IAspectBoolean.class);
            IAspectBoolean result = (IAspectBoolean) context.issueSubRequestForAspect(req);
            retValue = result.isTrue();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
    
    public static boolean validDomain(INKFConvenienceHelper context, String domain) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-domain-valid");
            req.addArgument("uri", domainResolver.getURI(domain));
            req.setAspectClass(IAspectBoolean.class);
            IAspectBoolean result = (IAspectBoolean) context.issueSubRequestForAspect(req);
            retValue = result.isTrue();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
    
    public static boolean validURI(String uri) {
        boolean retValue = false;
        
        try {
            URI u = new URI(uri);
            retValue = true;
        } catch (URISyntaxException e) {
            // Swallow this silently, we'll report
            // the failure elsewhere
        } 
        
        return retValue;
    }
    
    public static boolean userIsGroupMaintainer(INKFConvenienceHelper context, String user, String group) {
        boolean retValue = false;
        
        try {
            INKFRequest req=context.createSubRequest("active:purl-storage-query-groupmaintainers");
            req.addArgument("param", new StringAspect("<group><id>" + group + "</id></group>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA res = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = res.getXDA().isTrue("/maintainers/uid = '" + user + "'") ||
                       res.getXDA().isTrue("/maintainers/gid = '" + user + "'");
        } catch(Exception e) {
         e.printStackTrace();   
        }
        
        return retValue;
    }
    
    public static boolean userIsDomainMaintainer(INKFConvenienceHelper context, String user, String domain) {
        boolean retValue = false;
        
        try {
            INKFRequest req=context.createSubRequest("active:purl-storage-query-domain");
            req.addArgument("uri", domain);
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA res = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = res.getXDA().isTrue("/domain/maintainers/uid = '" + user + "'");
            
            if(!retValue && res.getXDA().isTrue("/domain/maintainers/gid")) {
                // If the user is not spelled out explicitly, see if he/she is a member of a 
                // group that is a maintainer
                
                IAspectXDA groupListXDA = (IAspectXDA) context.transrept(UserHelper.getGroupsForUser(context, user), IAspectXDA.class);
                IXDAReadOnlyIterator itor = groupListXDA.getXDA().readOnlyIterator("/groups/group");
                
                while(!retValue && itor.hasNext()) {
                    retValue = res.getXDA().isTrue("/domain/maintainers/gid = '" + itor.getText(".", true) + "'");
                    itor.next();
                }
            }            
        } catch(Exception e) {
         e.printStackTrace();   
        }
        
        return retValue;
    }
    
    public static boolean userIsDomainWriter(INKFConvenienceHelper context, String user, String domain) {
        boolean retValue = false;
        
        try {
            INKFRequest req=context.createSubRequest("active:purl-storage-query-domainwriters");
            req.addArgument("param", new StringAspect("<domain><id>" + domain.substring(13) + "</id></domain>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA res = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = res.getXDA().isTrue("/writers/uid = '" + user + "'");
            
            if(!retValue && res.getXDA().isTrue("/writers/gid")) {
                // If the user is not spelled out explicitly, see if he/she is a member of a 
                // group that is a writer
                
                IAspectXDA groupListXDA = (IAspectXDA) context.transrept(UserHelper.getGroupsForUser(context, user), IAspectXDA.class);
                retValue = groupListXDA.getXDA().isTrue("/groups/group = '" + user + "'");
            }
        } catch(Exception e) {
            e.printStackTrace();   
        }
        
        return retValue;
    }
    
    public static boolean userIsPURLMaintainer(INKFConvenienceHelper context, String user, String purl) {
        boolean retValue = false;

        try {
            INKFRequest req=context.createSubRequest("active:purl-storage-query-purlmaintainers");
            req.addArgument("param", new StringAspect("<purl><id>" + purl.substring(11) + "</id></purl>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA res = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = res.getXDA().isTrue("/maintainers/uid = '" + user + "'");
        } catch(Exception e) {
         e.printStackTrace();   
        }
        
        return retValue;
    }
        
    public static String getDomainForPURL(INKFConvenienceHelper context, String resource) {
        String retValue = null;
        
        PURLDomainIterator itor = new PURLDomainIterator(resource);

        while(itor.hasNext()) {
            String domain = itor.next();
            try {
                INKFRequest req = context.createSubRequest("active:purl-storage-domain-exists");
                req.addArgument("uri", domain);
                req.setAspectClass(IAspectBoolean.class);
                IAspectBoolean resp = (IAspectBoolean) context.issueSubRequestForAspect(req);
                if(resp.isTrue()) {
                    retValue = domain;
                }
            } catch (NKFException e) {
                e.printStackTrace();
            } 
        }
        
        return retValue;
    }
    
    public static boolean domainIsPublic(INKFConvenienceHelper context, String domain) {
        boolean retValue = false;

        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-query-domain");
            req.addArgument("uri", domainResolver.getURI(domain));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = result.getXDA().isTrue("/domain/public = 'true'");
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
    
    public static boolean domainIsValid(INKFConvenienceHelper context, String domain) {
        boolean retValue = false;

        try {
            String uri = domainResolver.getURI(domain);
            
            if(uri != null) {
                INKFRequest req = context.createSubRequest("active:purl-storage-query-domain");
                req.addArgument("uri", uri);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
                retValue = result.getXDA().isTrue("/domain[@status='1']");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }    
    
    
    public static boolean userCanCreatePURL(INKFConvenienceHelper context, String resource) {
        return userCanCreatePURL(context, NKHelper.getUser(context), resource);
    }
    
    public static boolean userCanCreatePURL(INKFConvenienceHelper context, String user, String resource) {
        String domain = getDomainForPURL(context, resource);
        return domain != null && validDomain(context, domain) &&
                ( domainIsPublic(context, domain) ||
                (userIsDomainMaintainer(context, user, domain) || 
                userIsDomainWriter(context, user, domain) )
              );
    }
    
    public static void createNecessarySubdomains(INKFConvenienceHelper context, String purl) {
        PURLDomainIterator pdi = new PURLDomainIterator(purl);
        String tld = null;
        IAspectXDA tldXDA = null;
        boolean needToCreate = false;
        Domain d = null;
        
        while(pdi.hasNext()) {
            String domain = pdi.next();
            
            if(tld == null) {
                tld = domain;
            }
            
            INKFRequest req = null;
            String[] maintainers = null;
            String[] writers = null;            
            String domainName = null;
            boolean tldIsPublic = false;

            try {
                if(!needToCreate) {
                    req = context.createSubRequest("active:purl-storage-domain-exists");            
                    req.addArgument("uri", domain);
                    req.setAspectClass(IAspectBoolean.class);
                    IAspectBoolean res = (IAspectBoolean) context.issueSubRequestForAspect(req);
                    needToCreate = !res.isTrue();
                }
                
                if(needToCreate) {
                    if(tldXDA == null) {
                        req = context.createSubRequest("active:purl-storage-query-domain");            
                        req.addArgument("uri", tld);
                        req.setAspectClass(IAspectXDA.class);
                        tldXDA = (IAspectXDA) context.issueSubRequestForAspect(req);
                        try {
                            IXDAReadOnly tldXDARO = tldXDA.getXDA();
                            domainName = tldXDARO.getText("/domain/name", true);
                            tldIsPublic = tldXDARO.isTrue("/domain/public = 'true'");
                            int numMaintainers = Integer.valueOf(tldXDARO.eval("count(/domain/maintainers/uid)").getStringValue()).intValue();
                            int numWriters = Integer.valueOf(tldXDARO.eval("count(/domain/writers/uid)").getStringValue()).intValue();
                            maintainers = new String[numMaintainers];
                            writers = new String[numWriters];
                            int index = 0;
                            IXDAReadOnlyIterator itor = tldXDARO.readOnlyIterator("/domain/maintainers/uid");

                            while(itor.hasNext()) {
                                itor.next();
                                maintainers[index++] = tldXDARO.getText(itor.getCurrentXPath(), true);
                            }
                            
                            itor = tldXDARO.readOnlyIterator("/domain/writers/uid");
                            index = 0;

                            while(itor.hasNext()) {
                                itor.next();
                                writers[index++] = tldXDARO.getText(itor.getCurrentXPath(), true);
                            }

                        } catch (XPathLocationException e) {
                            e.printStackTrace();
                        } 
                    }
                    
                    d = new Domain(domainResolver.getDisplayName(domain), tldIsPublic, domainName + "(subdomain)", maintainers, writers);
                    req=context.createSubRequest("active:purl-storage-create-domain");
                    req.addArgument("param", new StringAspect(d.toString()));
                    context.issueSubRequest(req);
                }

            } catch(NKFException nfe) {
                nfe.printStackTrace();
            }
        }
    }
    
    public static void acquireLock(INKFConvenienceHelper context, String uri) throws NKFException {
        INKFRequest req=context.createSubRequest("active:lock");
        req.addArgument("operand", uri);
        context.issueSubRequest(req);
    }
    
    public static void releaseLock(INKFConvenienceHelper context, String uri) throws NKFException {
        INKFRequest req=context.createSubRequest("active:unlock");
        req.addArgument("operand", uri);
        context.issueSubRequest(req);
    }
}
