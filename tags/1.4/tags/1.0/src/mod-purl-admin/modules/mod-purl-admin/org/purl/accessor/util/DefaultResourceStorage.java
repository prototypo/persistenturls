package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class DefaultResourceStorage implements ResourceStorage {

    public IURAspect storeResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException {
        context.sinkAspect(resolver.getURI(context), resource);
        return context.sourceAspect(resolver.getURI(context), IAspectXDA.class);
    }

    public boolean updateResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException {
        storeResource(context, resolver, resource);
        return true;
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
    
    public boolean resourceIsTombstoned(INKFConvenienceHelper context, String uri) throws NKFException {
        boolean retValue = false;
        
        System.out.println("****************TELL BRIAN THIS GOT CALLED");
        
        IURAspect resource = getResource(context, uri);
        IAspectXDA resourceXDA = (IAspectXDA) context.transrept(resource, IAspectXDA.class);
        try {
            retValue = resourceXDA.getXDA().isTrue("/purl[@status='2']");
        } catch (XPathLocationException e) {
            e.printStackTrace();
        }

        return retValue;
    }
    
    public boolean deleteResource(INKFConvenienceHelper context, String uri) throws NKFException {
        return context.delete(uri);
    }

    public boolean deleteResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return deleteResource(context, resolver.getURI(context));
    }   
}
