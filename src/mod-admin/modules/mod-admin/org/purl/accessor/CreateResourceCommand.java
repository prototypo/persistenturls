package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class CreateResourceCommand extends PURLCommand {

    private ResourceCreator resCreator;

    public CreateResourceCommand(URIResolver uriResolver, ResourceCreator resCreator) {
        super(uriResolver);
        this.resCreator = resCreator;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            IAspectNVP params = getParams(context);
            String id = NKHelper.getLastSegment(context);

            if(resourceExists(context)) {
                // Cannot create the same name
                String message = "Resource: " + id + " already exists.";
                IURRepresentation rep = setResponseCode(context, new StringAspect(message), 409);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_TEXT);
                NKHelper.log(context, message);
            } else {
                IURAspect iur = resCreator.createResource(context, params);
                System.out.println(uriResolver.getURI(context));
                context.sinkAspect(uriResolver.getURI(context), iur);
                IURRepresentation rep = setResponseCode(context, iur, 201);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_XML);
                NKHelper.log(context, "Created new user: " + id);
            }

        } catch (NKFException e) {
            // TODO Handle
            e.printStackTrace();
        }

        return retValue;
    }
}
