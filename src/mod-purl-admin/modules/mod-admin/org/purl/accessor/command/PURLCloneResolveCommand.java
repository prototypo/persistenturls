package org.purl.accessor.command;

import org.purl.accessor.PURLURIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;

public class PURLCloneResolveCommand extends PURLResolveCommand {

    private PURLURIResolver uriResolver;

    public PURLCloneResolveCommand(PURLURIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        // TODO Auto-generated method stub
        return null;
    }

}
