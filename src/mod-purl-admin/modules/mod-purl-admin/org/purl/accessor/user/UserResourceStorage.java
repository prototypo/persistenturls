package org.purl.accessor.user;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.XPathLocationException;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectBoolean;

public class UserResourceStorage implements ResourceStorage {

    public IURAspect getResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return getResource(context, resolver.getURI(context));
    }

    public IURAspect getResource(INKFConvenienceHelper context, String uri) throws NKFException {
        INKFRequest req = context.createSubRequest("active:purl-storage-query-user");
        req.addArgument("uri", uri);
        IURRepresentation res = context.issueSubRequest(req);
        return context.transrept(res, IAspectXDA.class);
    }

    public boolean resourceExists(INKFConvenienceHelper context, String uri) throws NKFException {
        boolean retValue = false;
        INKFRequest req = context.createSubRequest("active:purl-storage-user-exists");
        req.addArgument("uri", uri);
        req.setAspectClass(IAspectBoolean.class);
        retValue = ((IAspectBoolean) context.issueSubRequestForAspect(req)).isTrue();
        return retValue;
    }

    public boolean resourceExists(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return resourceExists(context, resolver.getURI(context));
    }
    
    public boolean resourceIsTombstoned(INKFConvenienceHelper context, String uri) throws NKFException {
        boolean retValue = false;
        
        IURAspect resource = getResource(context, uri);
        IAspectXDA resourceXDA = (IAspectXDA) context.transrept(resource, IAspectXDA.class);
        try {
            retValue = resourceXDA.getXDA().isTrue("/user[@status='2']");
        } catch (XPathLocationException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public IURAspect storeResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException {
        IURAspect retValue = null;
        INKFRequest req = context.createSubRequest("active:purl-storage-create-user");
        req.addArgument("param", resource);
        req.setAspectClass(IAspectXDA.class);
        retValue = context.issueSubRequestForAspect(req);
        return retValue;
    }
    
    public boolean updateResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException {
        boolean retValue = false;
        
        INKFRequest req = context.createSubRequest("active:purl-storage-update-user");
        req.addArgument("param", resource);
        context.issueSubRequest(req);
        retValue = true;
        
        return retValue;
    }  
    
    public boolean deleteResource(INKFConvenienceHelper context, String uri) throws NKFException {
        boolean retValue = false;
        INKFRequest req = context.createSubRequest("active:purl-storage-delete-user");
        req.addArgument("uri", uri);
        context.issueSubRequest(req);
        retValue = true;
        
        return retValue;
    }

    public boolean deleteResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
        return deleteResource(context, resolver.getURI(context));
    }   
}
