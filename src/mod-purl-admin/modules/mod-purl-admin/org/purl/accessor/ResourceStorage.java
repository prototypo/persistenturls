package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.util.URIResolver;

import com.ten60.netkernel.urii.IURAspect;

/**
 * An interface to a storage subsystem.
 *
 * @author brian
 *
 */
public interface ResourceStorage {
    public boolean resourceExists(INKFConvenienceHelper context, String uri) throws NKFException;
    public boolean resourceExists(INKFConvenienceHelper context, URIResolver resolver) throws NKFException;
    public boolean resourceIsTombstoned(INKFConvenienceHelper context, String uri) throws NKFException;
    public IURAspect storeResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException;
    public boolean updateResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException;
    public boolean deleteResource(INKFConvenienceHelper context, String uri) throws NKFException;
    public boolean deleteResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException;        
    public IURAspect getResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException;
    public IURAspect getResource(INKFConvenienceHelper context, String uri) throws NKFException;

}
