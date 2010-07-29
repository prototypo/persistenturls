package org.purl.accessor.util;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectBoolean;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;
import org.purl.accessor.domain.Domain;
import org.purl.accessor.domain.DomainIterator;
import org.purl.accessor.domain.DomainResolver;
import org.purl.accessor.purl.PURLURIResolver;
import org.purl.accessor.user.UserHelper;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;
import org.apache.commons.lang.StringEscapeUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

public class NKHelper {
    public static final String
            MIME_TEXT = "text/plain",
            MIME_XML = "text/xml",
            MIME_HTML = "text/html";

    private static URIResolver domainResolver = new DomainResolver();
    private static URIResolver purlResolver = new PURLURIResolver();

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
        String[] parts = path.split("/");
        return parts[parts.length - 1];
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

        if (req.argumentExists(argument)) {
            retValue = req.getArgument(argument);
        }

        return retValue;
    }

    /**
     * Associate an HTTP response code with the specified aspect
     *
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
    public static IURRepresentation attachGoldenThread(INKFConvenienceHelper context, String goldenThreadName, IURRepresentation representation) {
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
    public static IURRepresentation attachGoldenThread(INKFConvenienceHelper context, String goldenThreadName, IURAspect resource) {
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
    public static void cutGoldenThread(INKFConvenienceHelper context, String goldenThreadName) {
        try {
            INKFRequest req = context.createSubRequest("active:cutGoldenThread");
            req.addArgument("param", goldenThreadName);
            context.issueSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }
    }

    public static String getMD5Value(INKFConvenienceHelper context, String value) {
        String retValue = null;

        try {
            INKFRequest req = context.createSubRequest("active:md5");
            req.addArgument("operand", new StringAspect("<key>" + StringEscapeUtils.escapeXml(value) + "</key>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = result.getXDA().getText("/md5", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static String getDESValue(INKFConvenienceHelper context, String salt, String key) {
        String retValue = null;

        try {
            INKFRequest req = context.createSubRequest("active:des");
            req.addArgument("operand", new StringAspect("<des><salt>" + salt + "</salt>" +
                    "<key>" + StringEscapeUtils.escapeXml(key)+ "</key></des>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = result.getXDA().getText("/des", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static String getUser(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String sessionURI = context.getThisRequest().getArgument("session");
            String tokenURI = sessionURI + "+key@ffcpl:/credentials";
            IAspectString credentials = (IAspectString) context.sourceAspect(tokenURI, IAspectString.class);
            retValue = credentials.getString().trim().toLowerCase();
        } catch (NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    public static boolean purlExists(INKFConvenienceHelper context, String path) {
        boolean retValue = false;

        try {
            String purl = purlResolver.getURI(path);
            purl = purl.replaceAll("\\+", "%2B");

            INKFRequest req = context.createSubRequest("active:purl-storage-purl-exists");
            req.addArgument("uri", purl);
            req.setAspectClass(IAspectBoolean.class);
            IAspectBoolean result = (IAspectBoolean) context.issueSubRequestForAspect(req);
            retValue = result.isTrue();

        } catch (NKFException nfe) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static boolean validGroup(INKFConvenienceHelper context, String group) {
        boolean retValue = false;

        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-group-valid");
            req.addArgument("uri", "ffcpl:/group/" + group);
            req.setAspectClass(IAspectBoolean.class);
            IAspectBoolean result = (IAspectBoolean) context.issueSubRequestForAspect(req);
            retValue = result.isTrue();
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static boolean validURL(String url) {
        boolean retValue = false;

        try {
            URL u = new URL(url);
            retValue = true;
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
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

    private static boolean userHasPermission(INKFConvenienceHelper context, String user, String resource, String resourceType, String key) {
        boolean retValue = false;
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-query-" + resourceType);
            req.addArgument("uri", resource);
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA res = (IAspectXDA) context.issueSubRequestForAspect(req);
            IXDAReadOnlyIterator itor = res.getXDA().readOnlyIterator("/" + resourceType + "/" + key + "/uid");

            while (!retValue && itor.hasNext()) {
                itor.next();
                String uid = itor.getText(".", true);
                retValue = uid.equalsIgnoreCase(user);
            }

            //retValue = res.getXDA().isTrue("/domain/maintainers/uid = '" + user + "'");

            if (!retValue && res.getXDA().isTrue("/" + resourceType + "/" + key + "/gid")) {
                // If the user is not spelled out explicitly, see if he/she is a member of a
                // group that is a maintainer

                // Get groups that the user is a member of.
                Set<String> groups = UserHelper.getGroupsForUser(context, user);
                for (String group : groups) {
                    
                    retValue = retValue || res.getXDA().isTrue("/" + resourceType + "/" + key + "/gid = '" + group + "'");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static boolean userIsGroupMaintainer(INKFConvenienceHelper context, String user, String group) {
        return userHasPermission(context, user, group, "group", "maintainers");
    }

    public static boolean userIsDomainMaintainer(INKFConvenienceHelper context, String user, String domain) {
        return userHasPermission(context, user, domain, "domain", "maintainers");
    }

    public static boolean userIsDomainWriter(INKFConvenienceHelper context, String user, String domain) {
        return userHasPermission(context, user, domain, "domain", "writers");
    }

    public static boolean userIsPURLMaintainer(INKFConvenienceHelper context, String user, String purl) {
        return userHasPermission(context, user, purl, "purl", "maintainers");
    }

    public static String getDomainForPURL(INKFConvenienceHelper context, String resource) {
        String retValue = null;

        DomainIterator itor = new DomainIterator(resource);

        while (itor.hasNext()) {
            String domain = itor.next();
            try {
                INKFRequest req = context.createSubRequest("active:purl-storage-domain-exists");
                req.addArgument("uri", domain);
                req.setAspectClass(IAspectBoolean.class);
                IAspectBoolean resp = (IAspectBoolean) context.issueSubRequestForAspect(req);
                if (resp.isTrue()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static boolean domainIsValid(INKFConvenienceHelper context, String domain) {
        boolean retValue = false;

        try {
            String uri = domainResolver.getURI(domain);

            if (uri != null) {
                INKFRequest req = context.createSubRequest("active:purl-storage-query-domain");
                req.addArgument("uri", uri);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
                retValue = result.getXDA().isTrue("/domain[@status='1']");

                //@TODO Check to see if the domain is allowed by the config 
            }
        } catch (Exception e) {
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
                (domainIsPublic(context, domain) ||
                        (userIsDomainMaintainer(context, user, domain) ||
                                userIsDomainWriter(context, user, domain))
                );
    }

    public static void createNecessarySubdomains(INKFConvenienceHelper context, String purl) {

        DomainIterator pdi = new DomainIterator(purl);
        String tld = null;
        IAspectXDA tldXDA = null;
        boolean needToCreate = false;
        Domain d = null;

        String[] maintainers = null;
        String[] writers = null;

        while (pdi.hasNext()) {
            String domain = pdi.next();

            if (tld == null) {
                tld = domain;
            }

            INKFRequest req = null;

            String domainName = null;
            boolean tldIsPublic = false;

            try {
                if (!needToCreate) {
                    req = context.createSubRequest("active:purl-storage-domain-exists");
                    req.addArgument("uri", domain);
                    req.setAspectClass(IAspectBoolean.class);
                    IAspectBoolean res = (IAspectBoolean) context.issueSubRequestForAspect(req);
                    needToCreate = !res.isTrue();
                }

                if (needToCreate) {
                    if (tldXDA == null) {
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

                            while (itor.hasNext()) {
                                itor.next();
                                maintainers[index++] = tldXDARO.getText(itor.getCurrentXPath(), true);
                            }

                            itor = tldXDARO.readOnlyIterator("/domain/writers/uid");
                            index = 0;

                            while (itor.hasNext()) {
                                itor.next();
                                writers[index++] = tldXDARO.getText(itor.getCurrentXPath(), true);
                            }

                        } catch (XPathLocationException e) {
                            e.printStackTrace();
                        }
                    }

                    String domainID = domainResolver.getDisplayName(domain);
                    d = new Domain(domainID, tldIsPublic, domainName + "(subdomain)", maintainers, writers);

                    req = context.createSubRequest("active:purl-storage-create-domain");
                    req.addArgument("param", new StringAspect(d.toString()));
                    context.issueSubRequest(req);

                    // Auto-approve these domains

                    req = context.createSubRequest("active:purl-storage-approve-domain");
                    req.addArgument("param", new StringAspect("<domain><id>" + domainID + "</id></domain>"));
                    context.issueSubRequest(req);
                }

            } catch (NKFException nfe) {
                nfe.printStackTrace();
            }
        }
    }

}
