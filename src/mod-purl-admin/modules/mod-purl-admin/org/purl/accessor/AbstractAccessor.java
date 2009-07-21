package org.purl.accessor;

import org.purl.accessor.command.PURLCommand;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

abstract public class AbstractAccessor extends NKFAccessorImpl {

//    private Map<String, PURLCommand> commandMap;

    /**
     * Default constructor to indicate that we are good for source requests
     * and is safe for concurrent use.
     *
     */
    protected AbstractAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }

    /**
     * Method to handle the request to the accessor.
     *
     * @param context
     */
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        INKFResponse resp = null;
        
        // TODO: Can this fail?
        String methodArg = context.getThisRequest().getArgument("method");
        String method = ((StringAspect)context.sourceAspect(methodArg, IAspectString.class)).getString();

        // Retrieve the command associated with the method and
        // execute it. These commands should not maintain any
        // state.

        PURLCommand cmd = null;

        try {
            cmd = getCommand(context, method);

            if(cmd != null) {
                resp = cmd.execute(context);
            } else {
                // TODO: Generate an ERROR
            }

        } catch(Throwable t) {
            // TODO: Come up with some good standards for error
            resp = context.createResponseFrom(new StringAspect("Error performing request"));
            resp.setMimeType("text/plain");
            t.printStackTrace();
        }

        context.setResponse(resp);
    }

    protected abstract PURLCommand getCommand(INKFConvenienceHelper ctx, String method);
}
