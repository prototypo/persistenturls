package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectBoolean;

public class PURLResourceStorage implements ResourceStorage {

    public IURAspect getResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return getResource(context, resolver.getURI(context));
    }

    public IURAspect getResource(INKFConvenienceHelper context, String uri) throws NKFException {
        INKFRequest req = context.createSubRequest("active:purl-storage-query-purl");
        req.addArgument("uri", uri);
        req.setAspectClass(IAspectXDA.class);
        return (IAspectXDA) context.issueSubRequestForAspect(req);
    }

    public boolean resourceExists(INKFConvenienceHelper context, String uri) throws NKFException {
        boolean retValue = false;
        INKFRequest req = context.createSubRequest("active:purl-storage-purl-exists");
        req.addArgument("uri", uri);
        req.setAspectClass(IAspectBoolean.class);
        retValue = ((IAspectBoolean) context.issueSubRequestForAspect(req)).isTrue();
        return retValue;
    }

    public boolean resourceExists(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return resourceExists(context, resolver.getURI(context));
    }

    public boolean storeResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException {
        boolean retValue = false;
        
        INKFRequest req = context.createSubRequest("active:purl-storage-create-purl");
        req.addArgument("param", resource);
        context.issueSubRequest(req);
        retValue = true;
        
        return retValue;
    }
}
