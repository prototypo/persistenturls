package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class DeleteResourceCommand extends PURLCommand {

    public DeleteResourceCommand(URIResolver uriResolver) {
        super(uriResolver);
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            String id = NKHelper.getLastSegment(context);
            if(resourceExists(context)) {
                // Default response code of 200 is fine
                context.delete(uriResolver.getURI(context));
                String message = "Deleted resource: " + id;
                IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect(message), 200);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_TEXT);
                NKHelper.log(context,message);

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

        return retValue;
    }
}
