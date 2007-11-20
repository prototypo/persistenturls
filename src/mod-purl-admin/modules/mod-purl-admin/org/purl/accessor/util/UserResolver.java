package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

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

    public String getURI(String id) {
        return "ffcpl:/users/" + id;
    }
}
