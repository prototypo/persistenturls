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
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class CreateResourceCommand extends PURLCommand {

    private ResourceCreator resCreator;
    private ResourceFilter resFilter;

    public CreateResourceCommand(String type, URIResolver uriResolver, ResourceCreator resCreator) {
        this(type, uriResolver, resCreator, null, null);
    }

    public CreateResourceCommand(String type, URIResolver uriResolver, ResourceCreator resCreator, ResourceFilter resFilter, ResourceStorage resStorage) {
        super(type, uriResolver, resStorage);
        this.resCreator = resCreator;
        this.resFilter = resFilter;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            IAspectNVP params = getParams(context);
            String id = NKHelper.getLastSegment(context);

            if(resStorage.resourceExists(context, uriResolver)) {
                // Cannot create the same name
                String message = "Resource: " + id + " already exists.";
                IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 409);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_TEXT);
                NKHelper.log(context, message);
            } else {
                try {
                    IURAspect iur = resCreator.createResource(context, params);

                    // Store the full resource
                    if(resStorage.storeResource(context, uriResolver, iur)) {

                        //context.sinkAspect(uriResolver.getURI(context), iur);

                        NKHelper.indexResource(context, "ffcpl:/index/purls", uriResolver.getURI(context), iur);

                        // Filter it if there is one
                        if(resFilter!=null) {
                            iur = resFilter.filter(context, iur);
                        }

                        IURRepresentation rep = NKHelper.setResponseCode(context, iur, 201);
                        retValue = context.createResponseFrom(rep);
                        retValue.setMimeType(NKHelper.MIME_XML);
                        NKHelper.log(context, "Created new resource: " + id);
                    } else {
                        System.out.println("ERROR CREATING NEW RESOURCE");
                    }
                } catch(PURLException p) {
                    IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(p.getMessage()), p.getResponseCode());
                    retValue = context.createResponseFrom(rep);
                    retValue.setMimeType(NKHelper.MIME_TEXT);
                    NKHelper.log(context, p.getMessage());
                }
            }

        } catch (NKFException e) {
            // TODO Handle
            e.printStackTrace();
        }

        return retValue;
    }
}
