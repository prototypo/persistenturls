package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class UnconstrainedGETAccessController implements AccessController {

    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri) {
        return true;
    }
}
