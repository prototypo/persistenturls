package org.purl.accessor.group;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.NKHelper;

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
        return "ffcpl:/group/" + getDisplayName(id);
    }

    @Override
    public String getDisplayName(String id) {
        String retValue = null;
        
        if(!id.startsWith("ffcpl:/group/") && !id.startsWith("ffcpl:/admin/group")) {
            retValue = id;
        } else {
            if(id.startsWith("ffcpl:/admin/group")){
                retValue = id.substring(19);
            } else {
                retValue = id.substring(13);
            }
        }
        
        return retValue;
    }
}
