package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

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
    public boolean storeResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException;
    public boolean updateResource(INKFConvenienceHelper context, URIResolver resolver, IURAspect resource) throws NKFException;    
    public IURAspect getResource(INKFConvenienceHelper context, URIResolver resolver) throws NKFException;
    public IURAspect getResource(INKFConvenienceHelper context, String uri) throws NKFException;
}
