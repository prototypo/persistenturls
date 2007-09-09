package org.purl.accessor.command;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;

abstract public class PURLResolveCommand {
    abstract public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl);
}
