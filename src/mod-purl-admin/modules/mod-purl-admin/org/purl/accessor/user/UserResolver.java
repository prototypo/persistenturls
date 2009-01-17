package org.purl.accessor.user;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.NKHelper;

public class UserResolver extends URIResolver {
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
        return "ffcpl:/user/" + getDisplayName(id);
    }

    @Override
    public String getDisplayName(String id) {
        String retValue = null;
        
        if(!id.startsWith("ffcpl:/user/") && !id.startsWith("ffcpl:/admin/user")) {
            retValue = id;
        } else {
            if(id.startsWith("ffcpl:/admin/user")) {
                retValue = id.substring(18);                
            } else {
                retValue = id.substring(12);
            }
        }
        
        return retValue;
    }
}
