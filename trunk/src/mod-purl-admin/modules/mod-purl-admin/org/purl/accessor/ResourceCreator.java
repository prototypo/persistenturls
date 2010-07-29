package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.purl.accessor.util.PURLException;

import com.ten60.netkernel.urii.IURAspect;

public interface ResourceCreator {
    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws PURLException, NKFException;
}
