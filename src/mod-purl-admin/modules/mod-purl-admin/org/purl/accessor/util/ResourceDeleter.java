package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.util.purl.PURLException;

public interface ResourceDeleter {
    public boolean deleteResource(INKFConvenienceHelper context) throws PURLException, NKFException;
}
