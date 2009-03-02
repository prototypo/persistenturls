package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class PURLDeleter implements ResourceDeleter {

    private URIResolver uriResolver;

    public PURLDeleter(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public boolean deleteResource(INKFConvenienceHelper context) throws PURLException, NKFException {
        boolean retValue = false;

        String res = uriResolver.getURI(context);
        IURAspect iur = context.sourceAspect(res, IAspectString.class);
        String tombstoned = res.replaceFirst("storedpurls", "tombstonedpurls");
        System.out.println("Moving: " + res + " to: " + tombstoned);
        context.sinkAspect(tombstoned,iur);
        retValue = context.delete(res);

        return retValue;
    }

}
