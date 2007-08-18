package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
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
            IAspectNVP params = getParams(context);
            String id = NKHelper.getLastSegment(context);
            if(resourceExists(context)) {
                // Update the user
                IURAspect iur = resCreator.createResource(context, params);
                context.sinkAspect(uriResolver.getURI(context), iur);
                String message = "Updated resource: " + id;
                IURRepresentation rep = setResponseCode(context, new StringAspect(message), 200);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_TEXT);
                NKHelper.log(context,message);
            } else {
                String message = "Cannot update. No such resource: " + id;
                IURRepresentation rep = setResponseCode(context, new StringAspect(message), 404);
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
