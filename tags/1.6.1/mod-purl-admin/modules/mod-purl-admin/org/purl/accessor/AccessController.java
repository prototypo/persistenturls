package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public interface AccessController {
    public boolean userHasAccess(INKFConvenienceHelper context, String user, String uri);
}
