/**
 *
 */
package org.purl.accessor.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.purl.accessor.AccessController;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

/**
 * @version 1.0, 16 August 2007
 * @author Brian Sletten (brian at http://zepheira.com/)
 *
 *=========================================================================
 *
 *  Copyright (C) 2007 OCLC (http://oclc.org)
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *=========================================================================
 *
 */
abstract public class PURLCommand {

    protected String type;
    protected URIResolver uriResolver;
    protected ResourceStorage resStorage;
    protected AccessController accessController;
    protected static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    /**
     *
     * @param uriResolver
     */
    protected PURLCommand(String type, URIResolver uriResolver, AccessController accessController, ResourceStorage resStorage) {
        this.type = type;
        this.uriResolver = uriResolver;
        this.accessController = accessController;
        this.resStorage = resStorage;
    }

	abstract public INKFResponse execute(INKFConvenienceHelper context);
	
	public String getType() {
	    return type;
	}
	
	public URIResolver getURIResolver() {
	    return uriResolver;
	}
	
	public AccessController getAccessController() {
	    return accessController;
	}
	
	public ResourceStorage getResourceStorage() {
	    return resStorage;
	}
	
    protected void recordCommandState(INKFConvenienceHelper context, String command, String resource ) {
        StringBuffer sb = new StringBuffer(sdf.format(new Date()));
        sb.append(" ");
        sb.append(command);
        sb.append(" ");
        sb.append(resource);
    }

	protected IAspectNVP getParams(INKFConvenienceHelper context) throws NKFException {
		IAspectNVP retValue = null;

		if(context.exists("this:param:param")) {
			retValue = (IAspectNVP) context.sourceAspect("this:param:param", IAspectNVP.class);
		}

		if(retValue == null) {
			if(context.exists("this:param:param2")) {
				retValue = (IAspectNVP) context.sourceAspect("this:param:param2", IAspectNVP.class);
			}
		}

        if(retValue == null ) {
             //retValue = (IAspectNVP) context.sourceAspect("this:param:params", IAspectNVP.class);
            String paramsArg = context.getThisRequest().getArgument("params");
            retValue = (IAspectNVP) context.sourceAspect(paramsArg, IAspectNVP.class);
        }

		return retValue;
	}
	

}