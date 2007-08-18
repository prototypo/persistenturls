package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GetResourceCommand extends PURLCommand {

    public GetResourceCommand(URIResolver uriResolver) {
        super(uriResolver);
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            String id = NKHelper.getLastSegment(context);
            if(resourceExists(context)) {
                // Default response code of 200 is fine
                IURRepresentation rep = setResponseCode(context, context.sourceAspect(uriResolver.getURI(context), IAspectString.class), 200);
                retValue = context.createResponseFrom(rep);
                retValue.setMimeType(NKHelper.MIME_XML);
            } else {
                IURRepresentation rep = setResponseCode(context, new StringAspect("No such resource: " + id), 404);
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
