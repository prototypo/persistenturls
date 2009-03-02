package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class PURLAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        boolean retValue = false;
        retValue = UserHelper.isAdminUser(context, user) || 
            NKHelper.userIsPURLMaintainer(context, user, uri);
        return retValue;
    }

}
