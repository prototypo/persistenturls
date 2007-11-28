package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class DomainResolver extends URIResolver {
    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            retValue = getURI(NKHelper.getLastSegment(context)).toLowerCase();
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    @Override
    public String getURI(String id) {
        return "ffcpl:/domain/" + id;
    }

}
