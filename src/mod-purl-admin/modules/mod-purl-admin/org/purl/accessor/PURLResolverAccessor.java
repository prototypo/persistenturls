package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.purl.accessor.command.PURLChainResolveCommand;
import org.purl.accessor.command.PURLCloneResolveCommand;
import org.purl.accessor.command.PURLGoneResolveCommand;
import org.purl.accessor.command.PURLPartialRedirectResolveCommand;
import org.purl.accessor.command.PURLRedirectResolveCommand;
import org.purl.accessor.command.PURLResolveCommand;
import org.purl.accessor.command.PURLSeeAlsoResolveCommand;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.aspect.IAspectString;

public class PURLResolverAccessor extends NKFAccessorImpl {

    static private PURLURIResolver purlResolver = new PURLURIResolver();

    private static Map<String,PURLResolveCommand> commandMap = new HashMap<String,PURLResolveCommand>();

    static {
        PURLResolveCommand redirectResolver = new PURLRedirectResolveCommand();
        PURLResolveCommand goneResolver = new PURLGoneResolveCommand();
        PURLResolveCommand seeAlsoResolver = new PURLSeeAlsoResolveCommand();
        PURLResolveCommand cloneResolver = new PURLCloneResolveCommand(purlResolver);
        PURLResolveCommand chainResolver = new PURLChainResolveCommand();
        PURLResolveCommand partialRedirectResolver = new PURLPartialRedirectResolveCommand();

        commandMap.put("301", redirectResolver);
        commandMap.put("302", redirectResolver);
        commandMap.put("303", seeAlsoResolver);
        commandMap.put("307", redirectResolver);
        commandMap.put("404", goneResolver);
        commandMap.put("410", goneResolver);
        commandMap.put("chain", chainResolver);
        commandMap.put("clone", cloneResolver);
        commandMap.put("partial", partialRedirectResolver);
    }

    public PURLResolverAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }

    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {

//        String method = (((IAspectString)context.sourceAspect("method", IAspectString.class)).getString());

        String methodArg = context.getThisRequest().getArgument("method");
        String method = (((IAspectString)context.sourceAspect(methodArg, IAspectString.class)).getString());
        System.out.println(method);

        String purlloc = purlResolver.getURI(context);
        INKFResponse resp = null;

        if(context.exists(purlloc)) {

            IAspectXDA purlXDA = (IAspectXDA) context.sourceAspect(purlloc, IAspectXDA.class);
            IXDAReadOnly purlXDARO = purlXDA.getXDA();
            String type = purlXDARO.getText("/purl/type", true);

            PURLResolveCommand cmd = commandMap.get(type);

            if(cmd != null) {
                try {
                    resp = cmd.execute(context, purlXDA);
                } catch(Throwable t){
                    //TODO: handle
                    t.printStackTrace();
                }
            }

        } else {
            // TODO: Handle error
        }

        context.setResponse(resp);
    }

}
