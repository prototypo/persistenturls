package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class DomainAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        boolean retValue = false;
        
        retValue = UserHelper.isAdminUser(context, user) || 
            NKHelper.userIsDomainMaintainer(context, user, uri);
        
        return retValue;
    }
}
