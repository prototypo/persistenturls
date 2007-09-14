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
import org.purl.accessor.PURLException;
import org.purl.accessor.ResourceCreator;
import org.purl.accessor.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class UpdateResourceCommand extends PURLCommand {

    private ResourceCreator resCreator;

    public UpdateResourceCommand(URIResolver uriResolver, ResourceCreator resCreator) {
        super(uriResolver);
        this.resCreator = resCreator;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            //IAspectNVP params = getParams(context);
            String id = NKHelper.getLastSegment(context);
            if(resourceExists(context)) {
                try {
                    // Update the user
                    //IURAspect iur = resCreator.createResource(context, params);
                    IURAspect iur = context.sourceAspect("this:param:param", IAspectString.class);
                    StringAspect sa = (StringAspect) iur;
                    System.out.println(sa.getString());
                    context.sinkAspect(uriResolver.getURI(context), iur);
                    String message = "Updated resource: " + id;
                    IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 200);
                    retValue = context.createResponseFrom(rep);
                    retValue.setMimeType(NKHelper.MIME_TEXT);
                    NKHelper.log(context,message);
                } catch(PURLException p) {
                    IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(p.getMessage()), p.getResponseCode());
                    retValue = context.createResponseFrom(rep);
                    retValue.setMimeType(NKHelper.MIME_TEXT);
                    NKHelper.log(context, p.getMessage());
                }
            } else {
                String message = "Cannot update. No such resource: " + id;
                IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 404);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_TEXT);
                NKHelper.log(context,message);
            }

        } catch (NKFException e) {
            // TODO Handle
            e.printStackTrace();
        }

        return retValue;
    }

}
