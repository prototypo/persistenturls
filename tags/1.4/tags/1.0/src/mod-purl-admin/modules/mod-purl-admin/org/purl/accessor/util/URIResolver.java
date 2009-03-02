package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

abstract public class URIResolver {
    abstract public String getURI(INKFConvenienceHelper context);
    abstract public String getURI(String id);
    abstract public String getDisplayName(String id);
}
