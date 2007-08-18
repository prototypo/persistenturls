package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import com.ten60.netkernel.urii.aspect.IAspectString;

abstract public class AbstractAccessor extends NKFAccessorImpl {

    protected static Map<String, PURLCommand> commandMap = new HashMap<String, PURLCommand>();

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
        String method = (((IAspectString)context.sourceAspect("literal:method", IAspectString.class)).getString());

        // Retrieve the command associated with the method and
        // execute it. These commands should not maintain any
        // state.

        PURLCommand cmd = commandMap.get(method);

        if(cmd != null) {
            resp = cmd.execute(context);
        } else {
            // TODO: Generate an ERROR
        }

        context.setResponse(resp);
    }

}
