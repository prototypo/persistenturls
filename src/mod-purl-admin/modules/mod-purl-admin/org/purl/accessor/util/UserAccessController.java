package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class UserAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        boolean retValue = false;
        
        if(user != null) {
            retValue = UserHelper.isAdminUser(context, user) || uri.endsWith(user);
        }
        
        return retValue;
    }
}
