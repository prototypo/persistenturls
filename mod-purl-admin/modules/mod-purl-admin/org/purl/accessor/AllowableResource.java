package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public interface AllowableResource {
    boolean allow(INKFConvenienceHelper context, String resourceName);
    String getDenyMessage(INKFConvenienceHelper context, String resourceName);
}
