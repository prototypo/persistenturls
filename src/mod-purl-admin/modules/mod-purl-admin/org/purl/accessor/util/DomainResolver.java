package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class DomainResolver extends URIResolver {
    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String path = NKHelper.getArgument(context, "path");
            if(!path.startsWith("ffcpl:/domain/")){
                retValue = getURI(path);
            } else {
                retValue = path;
            }

        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    @Override
    public String getURI(String id) {
        String retValue = null;
        
        if(!id.startsWith("ffcpl:/domain/")) {
            id="ffcpl:/domain" + getDisplayName(id);
        }
        retValue = id;
        return retValue;        
    }

    @Override
    public String getDisplayName(String id) {
        String retValue = null;
        
        if(!id.startsWith("ffcpl:/domain/") && !id.startsWith("ffcpl:/admin/domain/")) {
            retValue = (!id.startsWith("/") ? ("/"+id) : id);
        } else {
            if(id.startsWith("ffcpl:/admin")) {
                retValue = id.substring(19);                
            } else {
                retValue = id.substring(13);
            }
        }
        
        return retValue;
    }

}
