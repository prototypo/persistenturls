package org.purl.accessor.user;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.AllowableResource;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.NKHelper;

public class UserGroupAllowableResource implements AllowableResource {

    private ResourceStorage userStorage;
    private URIResolver userResolver;
    
    private ResourceStorage groupStorage;
    private URIResolver groupResolver;    
    
    public UserGroupAllowableResource(ResourceStorage userStorage, URIResolver userResolver, ResourceStorage groupStorage, URIResolver groupResolver) {
        this.userStorage = userStorage;
        this.userResolver = userResolver;
        this.groupStorage = groupStorage;
        this.groupResolver = groupResolver;
    }

    public boolean allow(INKFConvenienceHelper context, String resourceName) {
        boolean retValue = false;
        try {
            String resource = NKHelper.getLastSegment(resourceName);
            retValue = !(userStorage.resourceExists(context, userResolver.getURI(resource)) || 
                         groupStorage.resourceExists(context, groupResolver.getURI(resource)));
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }
    
    public String getDenyMessage(INKFConvenienceHelper context, String resourceName) {
        String retValue = null;
        String resource = NKHelper.getLastSegment(resourceName);
        
        try {
            if(userStorage.resourceExists(context, userResolver.getURI(resource))) {
                retValue = "User: " + resource + " exists. Cannot create another resource with the same name."; 
            } else if(groupStorage.resourceExists(context, groupResolver.getURI(resource))) {
                retValue = "Group: " + resource + " already exists. Cannot create another resource with the same name.";                 
            }
            
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }
}
