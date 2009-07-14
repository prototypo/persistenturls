package org.purl.accessor.user;

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
 * The UserAccessor manages user accounts and is part of the admin
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

import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.*;
import org.purl.accessor.group.GroupResolver;
import org.purl.accessor.group.GroupResourceStorage;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.SearchHelper;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.DataHelper;
import org.purl.accessor.user.UserAccessController;
import org.purl.accessor.user.UserCreator;
import org.purl.accessor.user.UserGroupAllowableResource;
import org.purl.accessor.user.UserRequestResolver;
import org.purl.accessor.user.UserResolver;
import org.purl.accessor.user.UserResourceStorage;
import org.purl.accessor.user.UserSearchHelper;
import org.purl.accessor.AccessController;
import org.purl.accessor.AllowableResource;
import org.purl.accessor.*;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class UserAccessor extends AbstractAccessor {
    public static final String TYPE = "user";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();

    // This is used in the UserResourceStorage
    private URIResolver userResolver;
    private URIResolver userRequestResolver;

	public UserAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        userResolver = new UserResolver();
        userRequestResolver = new UserRequestResolver();
        ResourceCreator userCreator = new UserCreator();
        ResourceFilter userFilter = new UserPrivateDataFilter();
        ResourceStorage userStorage = new UserResourceStorage();
        ResourceStorage groupStorage = new GroupResourceStorage();
        URIResolver groupResolver = new GroupResolver();
        AllowableResource userGroupAllowableResource = new UserGroupAllowableResource(userStorage, userResolver, groupStorage, groupResolver);
        
        SearchHelper userSearchHelper = new UserSearchHelper();
        
        AccessController userAccessController = new UserAccessController();

		commandMap.put("GET", new GetResourceCommand(TYPE, userResolver, userAccessController, userStorage, userSearchHelper, userFilter));
        commandMap.put("POST", new CreateResourceCommand(TYPE, userGroupAllowableResource, userResolver, userAccessController, userCreator, userFilter, userStorage));
		commandMap.put("DELETE", new DeleteResourceCommand(TYPE, userResolver, userAccessController, userStorage));
		commandMap.put("PUT", new UpdateResourceCommand(TYPE, userResolver, userAccessController, userCreator, userStorage));
	}

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        PURLCommand retValue = null;

        try {
            IAspectXDA config = (IAspectXDA) context.sourceAspect("ffcpl:/etc/PURLConfig.xml", IAspectXDA.class);

            if(method.equals("POST")) {
                retValue = commandMap.get("POST");
                
                IXDAReadOnly configXDA = config.getXDA();
                if(configXDA.isTrue("/purl-config/allowUserAutoCreation")) {
                    retValue = new UserAutoCreateCommand(retValue);
                }
            } else {
                retValue = commandMap.get(method);
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

    /**
     * An implementation of the ResourceFilter to prevent sensitive
     * user information from being returned.
     *
     * @author brian
     *
     */
    static public class UserPrivateDataFilter implements ResourceFilter {

        public IURAspect filter(INKFConvenienceHelper context, IURAspect iur) {
            IURAspect retValue = null;

            try {
                INKFRequest req = context.createSubRequest();
                req.setURI("active:xslt");
                req.addArgument("operand", iur);
                req.addArgument("operator", "ffcpl:/filters/user.xsl");
                req.setAspectClass(IAspectString.class);
                retValue = context.issueSubRequestForAspect(req);
            } catch(NKFException nfe) {
                // TODO: return something other than the raw user
                nfe.printStackTrace();
            }

            return retValue;
        }

    }
    
    static private class UserAutoCreateCommand extends PURLCommand {
        
        private PURLCommand createCmd;
        private ResourceFilter userFilter = new UserPrivateDataFilter();
        
        private UserAutoCreateCommand() {
            super(null, null, null, null);
        }
        
        public UserAutoCreateCommand(PURLCommand createCmd) {
            super(createCmd.getType(), createCmd.getURIResolver(), createCmd.getAccessController(), createCmd.getResourceStorage());            
            this.createCmd = createCmd;
        }

        @Override
        public INKFResponse execute(INKFConvenienceHelper context) {
            INKFResponse resp = null;
            
            try {
                resp = createCmd.execute(context);
                
                URIResolver uriResolver = getURIResolver();
                String uri = uriResolver.getURI(context);
                
                INKFRequest req = context.createSubRequest("active:purl-storage-query-user");
                req.addArgument("uri", uri);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);

                if(result.getXDA().isTrue("/user/@status='0'")) {
                    String user = uriResolver.getDisplayName(uri);
                
                    req = context.createSubRequest("active:purl-storage-approve-user");                
                    req.addArgument("param", new StringAspect("<user><id>" + DataHelper.cleanseInput(user) + "</id></user>")); 
                    context.issueSubRequest(req);
                
                    req=context.createSubRequest("active:purl-storage-query-user");
                    req.addArgument("uri", uri);
                    req.setAspectClass(IAspectXDA.class);
                    result = (IAspectXDA) context.issueSubRequestForAspect(req);
                    resp = context.createResponseFrom(userFilter.filter(context, result));
                    resp.setMimeType("text/xml");
                }
                
            } catch(Throwable t) {
                t.printStackTrace();
            }
            
            return resp;
        }
    }
}
