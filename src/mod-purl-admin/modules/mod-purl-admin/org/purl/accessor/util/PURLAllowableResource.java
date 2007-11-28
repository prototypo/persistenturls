package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class PURLAllowableResource extends DefaultAllowableResource {

    public PURLAllowableResource(ResourceStorage resStorage, URIResolver resResolver) {
        super(resStorage, resResolver);
    }
    
    public String getDenyMessage(INKFConvenienceHelper context, String resourceName) {
        if(resourceName.startsWith("ffcpl:/purl")) {
            resourceName = resourceName.substring(11);
        }
        
        return "PURL: " + resourceName + " already exists.";
    }

}
