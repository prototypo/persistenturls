package org.purl.accessor.purl;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.purl.accessor.util.*;
import org.purl.accessor.user.UserHelper;
import org.purl.accessor.ResourceStorage;

public class PURLAllowableResource extends DefaultAllowableResource {
    
    private URIResolver domainResolver;

    public PURLAllowableResource(ResourceStorage resStorage, URIResolver resResolver, URIResolver domainResolver) {
        super(resStorage, resResolver);
        
        this.domainResolver = domainResolver;
    }
    
    public boolean allow(INKFConvenienceHelper context, String resourceName) {
        // If the PURL does not already exist and the user has permission
        // to create a PURL for this domain, we allow the resource
        
        String domain = NKHelper.getDomainForPURL(context, resourceName);
        
        return (domain != null)
               && super.allow(context, resourceName)
               && NKHelper.domainIsValid(context, domain)
               && (UserHelper.isAdminUser(context, NKHelper.getUser(context)) ||
                   NKHelper.userCanCreatePURL(context, resourceName));
    }
    
    public String getDenyMessage(INKFConvenienceHelper context, String resourceName) {
        String retValue = null;
        String displayName = getResourceResolver().getDisplayName(resourceName);
        String uri = getResourceResolver().getURI(resourceName);
        
        if(!super.allow(context, resourceName)) {
            retValue = "PURL: " + displayName + " already exists.";
        } else {
            String domain = NKHelper.getDomainForPURL(context, resourceName);
            if(domain != null) {
                if(!NKHelper.validDomain(context, domain)) {
                    retValue = "Top Level Domain: " + domainResolver.getDisplayName(domain) + " has not been approved.";
                } else {
                    retValue = "User: " + NKHelper.getUser(context) 
                        + " is not authorized to create: " + displayName;
                }
            } else {
                retValue = "Top Level Domain does not exist for PURL: " + getResourceResolver().getDisplayName(resourceName) + ". See <a href=\"/docs/domain.html#create\">domain creation</a> to create a new top-level domain.";
            }
        }
        
        return retValue;
    }

}
