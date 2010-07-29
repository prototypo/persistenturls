package org.purl.accessor.purl;

import java.util.HashMap;
import java.util.Map;

import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.*;
import org.purl.accessor.domain.DomainResolver;
import org.purl.accessor.group.GroupResolver;
import org.purl.accessor.group.GroupResourceStorage;
import org.purl.accessor.purl.PURLCreator;
import org.purl.accessor.purl.PURLResourceStorage;
import org.purl.accessor.purl.PURLURIResolver;
import org.purl.accessor.purl.PurlSearchHelper;
import org.purl.accessor.ResourceCreator;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UnconstrainedGETAccessController;
import org.purl.accessor.user.UserResolver;
import org.purl.accessor.user.UserResourceStorage;
import org.purl.accessor.AllowableResource;
import org.purl.accessor.*;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class PURLAccessor extends AbstractAccessor {

    public static final String TYPE = "purl";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();

    private static URIResolver purlResolver;

    public PURLAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        purlResolver = new PURLURIResolver();

        URIResolver userResolver = new UserResolver();
        URIResolver groupResolver = new GroupResolver();
        URIResolver domainResolver = new DomainResolver();
        
        ResourceFilter purlFilter = new PURLPrivateDataFilter();

        ResourceCreator purlCreator = new PURLCreator(new URIResolver[] { userResolver, groupResolver }, purlResolver, new PURLResourceStorage(), new UserResourceStorage());
        ResourceStorage purlStorage = new PURLResourceStorage();
        ResourceStorage groupStorage = new GroupResourceStorage();
        
        AllowableResource purlAllowableResource = new PURLAllowableResource(purlStorage, purlResolver, domainResolver);
        AccessController purlAccessController = new PURLAccessController();

        commandMap.put("POST", new CreateResourceCommand(TYPE, purlAllowableResource, purlResolver, purlAccessController, purlCreator, purlFilter, purlStorage));
        commandMap.put("PUT", new UpdateResourceCommand(TYPE, purlResolver, purlAccessController, purlCreator, purlStorage));
        commandMap.put("DELETE", new DeleteResourceCommand(TYPE, purlResolver, purlAccessController, purlStorage));
        commandMap.put("GET", new GetResourceCommand(TYPE, purlResolver, new UnconstrainedGETAccessController(), purlStorage, new PurlSearchHelper(groupResolver, groupStorage), purlFilter));
    }

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        return commandMap.get(method);
    }
    
    static public class PURLPrivateDataFilter implements ResourceFilter {

        public IURAspect filter(INKFConvenienceHelper context, IURAspect iur) {
            IURAspect retValue = null;

            try {
                INKFRequest req = context.createSubRequest();
                req.setURI("active:xslt");
                req.addArgument("operand", iur);
                req.addArgument("operator", "ffcpl:/filters/purl.xsl");
                req.setAspectClass(IAspectString.class);
                retValue = context.issueSubRequestForAspect(req);
            } catch(NKFException nfe) {
                // TODO: return something other than the raw user
                nfe.printStackTrace();
            }

            return retValue;
        }

    }
}
