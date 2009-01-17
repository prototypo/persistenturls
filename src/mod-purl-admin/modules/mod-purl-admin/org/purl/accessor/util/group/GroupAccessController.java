package org.purl.accessor.util.group;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.purl.accessor.util.AccessController;
import org.purl.accessor.util.user.UserHelper;
import org.purl.accessor.util.NKHelper;

public class GroupAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        boolean retValue = false;
        
        // TODO: 
        // If user is a group maintainer
        // retValue = true;
        String parts[] = uri.split("/");
        String group = parts[parts.length - 1];
        retValue = UserHelper.isAdminUser(context, user) ||
            NKHelper.userIsGroupMaintainer(context, user, group);
        
        return retValue;
    }

}
