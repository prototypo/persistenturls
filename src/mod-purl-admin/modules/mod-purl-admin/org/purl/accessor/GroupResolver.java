package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class GroupResolver extends URIResolver {

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
        return "ffcpl:/groups/" + id;
    }

}
