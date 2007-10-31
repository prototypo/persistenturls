package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public interface ResourceDeleter {
    public boolean deleteResource(INKFConvenienceHelper context) throws PURLException, NKFException;
}
