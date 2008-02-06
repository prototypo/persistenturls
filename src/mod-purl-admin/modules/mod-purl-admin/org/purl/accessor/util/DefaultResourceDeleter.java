package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class DefaultResourceDeleter implements ResourceDeleter {

    URIResolver uriResolver;

    public DefaultResourceDeleter(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public boolean deleteResource(INKFConvenienceHelper context) throws PURLException, NKFException {
        return context.delete(uriResolver.getURI(context));
    }

}
