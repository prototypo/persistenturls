package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;

public class DefaultResourceDeleter implements ResourceDeleter {

    URIResolver uriResolver;

    DefaultResourceDeleter(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public boolean deleteResource(INKFConvenienceHelper context) throws PURLException, NKFException {
        return context.delete(uriResolver.getURI(context));
    }

}
