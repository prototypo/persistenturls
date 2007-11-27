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

import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.ResourceDeleter;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class DeleteResourceCommand extends PURLCommand {

    public DeleteResourceCommand(String type, URIResolver uriResolver, ResourceStorage resStorage) {
        super(type, uriResolver, resStorage);
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;
        String id = null;

        try {
            String path = context.getThisRequest().getArgument("path").toLowerCase();

            if(path.startsWith("ffcpl:")) {
                path = path.substring(6);
            }

            id = NKHelper.getLastSegment(context);
            if(resStorage.resourceExists(context,uriResolver)) {
                // Default response code of 200 is fine
                // TODO: Cut golden thread for the resource

                if(resStorage.deleteResource(context, uriResolver.getURI(context))) {
                    recordCommandState(context, "DELETE", path);

                    String message = "Deleted resource: " + id;
                    IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 200);
                    retValue = context.createResponseFrom(rep);
                    retValue.setMimeType(NKHelper.MIME_TEXT);
                    NKHelper.log(context,message);

                    // Cut golden thread for the resource
                    INKFRequest req = context.createSubRequest("active:cutGoldenThread");
                    req.addArgument("param", "gt:" + path);
                    context.issueSubRequest(req);
                }

            } else {
                String message = "No such resource: " + id;
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
