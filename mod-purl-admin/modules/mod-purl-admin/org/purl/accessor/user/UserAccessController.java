package org.purl.accessor.user;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.purl.accessor.AccessController;

public class UserAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        boolean retValue = false;
        
        if(user != null) {
            retValue = UserHelper.isAdminUser(context, user) || uri.toLowerCase().endsWith(user.toLowerCase());
        }
        
        return retValue;
    }
}
