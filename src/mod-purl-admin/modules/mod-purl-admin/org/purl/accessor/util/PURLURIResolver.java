package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class PURLURIResolver extends URIResolver {

    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String path = NKHelper.getArgument(context, "path");
            if(!path.startsWith("ffcpl:/purl/")) {
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
    public String getURI(String path) {
        String retValue = null;
        
        if(!path.startsWith("ffcpl:/purl")) {
            path="ffcpl:/purl" + getDisplayName(path);
        }
        
        retValue = path;

        return retValue;
    }

    @Override
    public String getDisplayName(String path) {
        String retValue = null;
        
        if(!path.startsWith("ffcpl:/purl")) {
            retValue = (!path.startsWith("/") ? ("/"+path) : path);
        } else {
            retValue = path.substring(11);
        }
        
        return retValue;
    }
}
