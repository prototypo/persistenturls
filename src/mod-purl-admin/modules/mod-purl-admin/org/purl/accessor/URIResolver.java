package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

abstract public class URIResolver {
    abstract public String getURI(INKFConvenienceHelper context);
    abstract public String getURI(String id);
}
