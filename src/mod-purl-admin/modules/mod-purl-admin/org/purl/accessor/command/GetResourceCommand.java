package org.purl.accessor.command;

/**
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

import org.purl.accessor.NKHelper;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GetResourceCommand extends PURLCommand {

    private ResourceFilter filter;

    public GetResourceCommand(URIResolver uriResolver) {
        this(uriResolver, null);
    }

    public GetResourceCommand(URIResolver uriResolver, ResourceFilter filter) {
        super(uriResolver);
        this.filter = filter;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            String id = NKHelper.getLastSegment(context);
            if(resourceExists(context)) {
                IURAspect asp = context.sourceAspect(uriResolver.getURI(context), IAspectString.class);
                // Filter the response if we have a filter

                if(filter!=null) {
                    asp = filter.filter(context, asp);
                }

                // Default response code of 200 is fine
                IURRepresentation rep = NKHelper.setResponseCode(context, asp, 200);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_XML);
            } else {
                IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect("No such resource: " + id), 404);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_TEXT);
            }

        } catch (NKFException e) {
            // TODO Handle
            e.printStackTrace();
        }

        return retValue;
    }
}
