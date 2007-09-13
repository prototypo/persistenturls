package org.purl.accessor.command;

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
