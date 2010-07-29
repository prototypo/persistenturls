package org.purl.accessor.domain;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.purl.accessor.AccessController;
import org.purl.accessor.user.UserHelper;
import org.purl.accessor.util.NKHelper;

public class DomainAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        boolean retValue = false;
        
        retValue = UserHelper.isAdminUser(context, user) ||
            NKHelper.userIsDomainMaintainer(context, user, uri);
        
        return retValue;
    }
}
