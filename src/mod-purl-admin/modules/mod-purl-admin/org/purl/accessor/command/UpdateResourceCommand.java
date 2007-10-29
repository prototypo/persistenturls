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
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class UpdateResourceCommand extends PURLCommand {
    private ResourceCreator resCreator;

    public UpdateResourceCommand(String type, URIResolver uriResolver, ResourceCreator resCreator, ResourceStorage resStorage) {
        super(type, uriResolver, resStorage);
        this.resCreator = resCreator;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;
        String id = null;

        try {
            //IAspectNVP params = getParams(context);
            id = NKHelper.getLastSegment(context);
            if(resStorage.resourceExists(context,uriResolver)) {
                try {
                    // Update the user

                    //PUT should come across on the param2 param

                    IAspectNVP params = getParams(context);
                    IURAspect iur = resCreator.createResource(context, params);
                    if(resStorage.storeResource(context, uriResolver, iur)) {
                        String message = "Updated resource: " + id;
                        IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 200);
                        retValue = context.createResponseFrom(rep);
                        retValue.setMimeType(NKHelper.MIME_TEXT);
                        NKHelper.log(context,message);

                        // Cut golden thread for the resource
                        INKFRequest req = context.createSubRequest("active:cutGoldenThread");
                        String path = NKHelper.getArgument(context, "path").toLowerCase();
                        req.addArgument("param", "gt:" + path);
                        context.issueSubRequest(req);
                    } else {
                        // TODO: Handle failed update
                        NKHelper.log(context, "ERROR UPDATING RESOURCE");
                    }

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

        if(id != null) {
            NKHelper.cutGoldenThread(context, "gt:" + type + ":" + id);
        }

        return retValue;
    }

}
