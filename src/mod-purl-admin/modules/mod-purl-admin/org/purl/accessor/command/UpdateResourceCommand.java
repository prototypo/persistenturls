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

import java.util.Iterator;

import org.purl.accessor.NKHelper;
import org.purl.accessor.PURLException;
import org.purl.accessor.ResourceCreator;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
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
                    //IURAspect iur = resCreator.createResource(context, params);
                    IAspectNVP params = (IAspectNVP) context.sourceAspect("this:param:param", IAspectNVP.class);
                    IURAspect iur = resCreator.createResource(context, params);

                    Iterator itor = params.getNames().iterator();
                    while(itor.hasNext()) {
                        String next = (String) itor.next();
                        System.out.println("Key: " + next);
                        System.out.println("Value: " + params.getValue(next));
                    }


                    System.out.println("-------");
                    System.out.println(((IAspectString) iur).getString());
                    System.out.println("-------");

                    if(resStorage.storeResource(context, uriResolver, iur)) {
//                      context.sinkAspect(uriResolver.getURI(context), iur);
                        String message = "Updated resource: " + id;
                        IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 200);
                        retValue = context.createResponseFrom(rep);
                        retValue.setMimeType(NKHelper.MIME_TEXT);
                        NKHelper.log(context,message);
                    } else {
                        // TODO: Handle failed update
                        System.out.println("ERROR! FAILED TO UPDATE RESOURCE");
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
