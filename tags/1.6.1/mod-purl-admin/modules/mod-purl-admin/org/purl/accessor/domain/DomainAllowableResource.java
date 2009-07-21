package org.purl.accessor.domain;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.AllowableResource;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;

public class DomainAllowableResource implements AllowableResource {
    
    private URIResolver domainResolver;
    private ResourceStorage domainStorage;
    
    public DomainAllowableResource(URIResolver domainResolver, ResourceStorage domainStorage) {
        this.domainResolver = domainResolver;
        this.domainStorage = domainStorage;
    }

    public boolean allow(INKFConvenienceHelper context, String resourceName) {
        boolean retValue = false;
        
        try {
            String domain = resourceName;
            if(!domainStorage.resourceExists(context, domain)) {
            	String root = null;
            	DomainIterator itor = new DomainIterator(domain);
           		root = domainResolver.getURI(itor.next());
           		if(itor.hasNext() && root != null) {
           			retValue = domainStorage.resourceExists(context, root);
           		} else {
           			retValue = true;
           		}
            }
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }

    public String getDenyMessage(INKFConvenienceHelper context, String resourceName) {
        try {
            if (domainStorage.resourceExists(context, resourceName)) {
                return "Domain: " + domainResolver.getDisplayName(resourceName) + " cannot be created because it already exists.";
            } else {
                String root = domainResolver.getDisplayName(domainResolver.getURI(new DomainIterator(resourceName).next()));
                return "Domain: " + domainResolver.getDisplayName(resourceName) + " cannot be created because the root domain " + root + " does not exist.";
            }
        } catch (NKFException nfe) {
             return "Domain: " + domainResolver.getDisplayName(resourceName) + " cannot be created due to a database connection failure.";
        }
    }
}
