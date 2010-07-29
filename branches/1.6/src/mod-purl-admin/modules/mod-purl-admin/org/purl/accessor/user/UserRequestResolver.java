package org.purl.accessor.user;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.NKHelper;

public class UserRequestResolver extends URIResolver {

    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;
        try {
            retValue = getURI(NKHelper.getLastSegment(context));
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    @Override
    public String getURI(String id) {
        return "ffcpl:/user-requests/" + id;
    }

    @Override
    public String getDisplayName(String id) {
        String retValue = null;
        
        if(!id.startsWith("ffcpl:/user-requests/")) {
            retValue = id;
        } else {
            retValue = id.substring(21);
        }
        
        return retValue;
    }

}
