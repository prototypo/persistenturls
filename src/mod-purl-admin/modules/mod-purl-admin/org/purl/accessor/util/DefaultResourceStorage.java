package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class DefaultResourceStorage implements ResourceStorage {

    public boolean storeResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException {
        boolean retValue = false;
        context.sinkAspect(resolver.getURI(context), resource);
        retValue = true;
        return retValue;
    }

    public IURAspect getResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        IURAspect retValue = context.sourceAspect(resolver.getURI(context), IAspectString.class);
        return retValue;
    }

    public IURAspect getResource(INKFConvenienceHelper context, String uri) throws NKFException {
        IURAspect retValue = context.sourceAspect(uri, IAspectString.class);
        return retValue;
    }

    public boolean resourceExists(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return context.exists(resolver.getURI(context));
    }

    public boolean resourceExists(INKFConvenienceHelper context, String uri) throws NKFException {
        return context.exists(uri);
    }
}
