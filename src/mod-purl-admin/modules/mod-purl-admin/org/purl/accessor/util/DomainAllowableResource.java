package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class DomainAllowableResource implements AllowableResource {
    
    private URIResolver domainResolver;
    private ResourceStorage domainStorage;
    
    public DomainAllowableResource(URIResolver domainResolver, ResourceStorage domainStorage) {
        this.domainResolver = domainResolver;
        this.domainStorage = domainStorage;
    }

    public boolean allow(INKFConvenienceHelper context, String resourceName) {
        boolean retValue = false;
        boolean done = false;
        
        try {
            String domain = resourceName;

            while(!done) {
                done = domainStorage.resourceExists(context, domain);
                if(!done) {
                    int lastSlash = domain.lastIndexOf("/");

                    if(lastSlash <= 13){
                        done = true;
                        retValue = true;
                    } else {
                        domain = domain.substring(0, lastSlash);
                    }
                }
            }
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }

    public String getDenyMessage(INKFConvenienceHelper context, String resourceName) {
        return "Domain: " + domainResolver.getDisplayName(resourceName) + " cannot be created because it or a parent domain already exist.";
    }
}
