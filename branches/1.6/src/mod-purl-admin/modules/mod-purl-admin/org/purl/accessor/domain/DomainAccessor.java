package org.purl.accessor.domain;

/**
 * @version 1.0, 16 August 2007
 * @author Brian Sletten (brian at http://zepheira.com/)
 *
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
 * The DomainAccessor manages user accounts and is part of the admin
 * interface for the PURLS service
 *
 * Requests getting mapped to this accessor are expected to fall into the following
 * categories:
 *
 * 1) Create a new user
 * Returns a copy of the public information for the new user on success.
 *
 * Possible outcomes:
 * Success - a 201 will be returned via the HTTP transport indicating the resource was
 *             successfully created.
 * Failure - a 409 will be returned if the user already exists
 *           (other failure cases?)
 * 2) Update an existing user
 *
 * Does not return a copy of the user on success.
 * Success - a 200
 * Failure - a 409
 *
 * 3) Delete an existing user
 *
 * Success - a 200
 * Failure -
 * 4) Search for users
 *
 * These are documented here:
 *
 * http://purlz.org/project/purl/documentation/requirements/index.html
 *
 * Success:
 * GET: 200 (OK)
 * POST: 201 (Created)
 * PUT: 200 (OK)
 * DELETE 200 (OK)
 * Failure:
 * Bad params: 400 (Bad Request)
 * PUT/POST conflicts: 409 (Conflict)
 * Unsupported HTTP verb on a URL: (405 Method Not Allowed)
 * Attempt to modify an uncreated resource: 412 (Precondition Failed)
*/

import java.util.HashMap;
import java.util.Map;

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.purl.accessor.domain.DomainResolver;
import org.purl.accessor.domain.DomainResourceStorage;
import org.purl.accessor.domain.DomainSearchHelper;
import org.purl.accessor.group.GroupResolver;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.user.UserResolver;
import org.purl.accessor.user.UserResourceStorage;
import org.purl.accessor.util.XSLTResourceFilter;
import org.purl.accessor.domain.DomainAccessController;
import org.purl.accessor.AccessController;
import org.purl.accessor.*;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.aspect.StringAspect;

public class DomainAccessor extends AbstractAccessor {

    public static final String TYPE = "domain";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();
    
    private ResourceStorage domainStorage;

    public DomainAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        URIResolver domainResolver = new DomainResolver();

        ResourceFilter domainFilter = new XSLTResourceFilter("ffcpl:/filters/domain.xsl");
        ResourceCreator domainCreator = new DomainCreator(domainResolver, new UserResolver(), new GroupResolver(), new UserResourceStorage());
        domainStorage = new DomainResourceStorage();
        AllowableResource domainAllowableResource = new DomainAllowableResource(domainResolver, domainStorage); //new DefaultAllowableResource(domainStorage, domainResolver);
        
        AccessController domainAccessController = new DomainAccessController();

        commandMap.put("GET", new GetResourceCommand(TYPE, domainResolver, domainAccessController, domainStorage, new DomainSearchHelper(), domainFilter));
        commandMap.put("POST", new CreateResourceCommand(TYPE, domainAllowableResource, domainResolver, domainAccessController, domainCreator, domainFilter, domainStorage));
        commandMap.put("DELETE", new DeleteResourceCommand(TYPE, domainResolver, domainAccessController, domainStorage));
        commandMap.put("PUT", new UpdateResourceCommand(TYPE, domainResolver, domainAccessController, domainCreator, domainStorage));
    }

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        PURLCommand retValue = null;

        try {
            IAspectXDA config = (IAspectXDA) context.sourceAspect("ffcpl:/etc/PURLConfig.xml", IAspectXDA.class);
            retValue = commandMap.get(method);
            
            if(method.equals("POST")) {
                IXDAReadOnly configXDA = config.getXDA();
                if(configXDA.isTrue("/purl-config/allowTopLevelDomainAutoCreation")) {
                    retValue = new DomainAutoCreateCommand(retValue, domainStorage);
                }
            }
        } catch (NKFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retValue;
    }
    
    static private class DomainAutoCreateCommand extends PURLCommand {
        
        private PURLCommand createCmd;
        private ResourceFilter domainFilter = new XSLTResourceFilter("ffcpl:/filters/domain.xsl");
        
        private DomainAutoCreateCommand() {
            super(null, null, null, null);
        }
        
        public DomainAutoCreateCommand(PURLCommand createCmd, ResourceStorage domainStorage) {
            super(createCmd.getType(), createCmd.getURIResolver(), createCmd.getAccessController(), createCmd.getResourceStorage());            
            this.createCmd = createCmd;
        }

        @Override
        public INKFResponse execute(INKFConvenienceHelper context) {
            INKFResponse resp = null;
            
            try {
                resp = createCmd.execute(context);
                URIResolver uriResolver = getURIResolver();
                String domainURI = uriResolver.getURI(context);

                INKFRequest req = context.createSubRequest("active:purl-storage-query-domain");
                req.addArgument("uri", domainURI);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
                
                if(result.getXDA().isTrue("/domain/@status='0'")) {
                    String domain = uriResolver.getDisplayName(domainURI);
                    req = context.createSubRequest("active:purl-storage-approve-domain");
                    req.addArgument("param", new StringAspect("<domain><id>" + domain + "</id></domain>")); 
                    context.issueSubRequest(req);
                    
        			// TODO: This should be handled by the active:purl-storage-approve-domain
                    req=context.createSubRequest("active:cutGoldenThread");
                    req.addArgument("param", "gt:resource:" + domain);
                    context.issueSubRequest(req);

                    req=context.createSubRequest("active:purl-storage-query-domain");
                    req.addArgument("uri", domainURI);
                    req.setAspectClass(IAspectXDA.class);
                    
                    result = (IAspectXDA) context.issueSubRequestForAspect(req);
                    resp = context.createResponseFrom(domainFilter.filter(context, result));
                    resp.setMimeType("text/xml");
                }
                 
            } catch(Throwable t) {
                t.printStackTrace();
            }
            
            return resp;
        }
    }
}
