package org.purl.accessor.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class PURLURIResolver extends URIResolver {

    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String path = NKHelper.getArgument(context, "path");
            if(path.startsWith("ffcpl:")) {
                path = path.substring(6);
            }
            
            path="ffcpl:/purl" + (!path.startsWith("/") ? ("/"+path) : path);

            retValue = path;
            
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    @Override
    public String getURI(String path) {
        String retValue = null;
        
        if(!path.startsWith("ffcpl:")) {
            path="ffcpl:/purl" + (!path.startsWith("/") ? ("/"+path) : path);
        }
        retValue = path;

        return retValue;
    }


}
