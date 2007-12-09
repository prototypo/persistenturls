package org.purl.accessor;

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

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.purl.accessor.util.AccessController;
import org.purl.accessor.util.AllowableResource;
import org.purl.accessor.util.GroupResolver;
import org.purl.accessor.util.GroupResourceStorage;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.util.ResourceCreator;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.SearchHelper;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UserAccessController;
import org.purl.accessor.util.UserGroupAllowableResource;
import org.purl.accessor.util.UserRequestResolver;
import org.purl.accessor.util.UserResolver;
import org.purl.accessor.util.UserResourceStorage;
import org.purl.accessor.util.UserSearchHelper;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
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
     * A ResourceCreator instance to fill out a user instance
     * from parameters that were passed in.
     *
     * @author brian
     *
     */
    static public class UserCreator implements ResourceCreator {

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            IAspectXDA config = (IAspectXDA)  context.sourceAspect("ffcpl:/etc/PURLConfig.xml", IAspectXDA.class);
            IXDAReadOnly configXDA = config.getXDA();
            StringAspect retValue = null;

            try {
                boolean createUser = false;

                if(configXDA.isTrue("/purl-config/allowUserAutoCreation")) {
                    createUser = true;
                }

                StringBuffer sb = new StringBuffer( "<user admin=\"false\">" );
                sb.append("<id>");
                sb.append(NKHelper.getLastSegment(context));
                sb.append("</id>");
                sb.append("<name>");
                sb.append(params.getValue("name"));
                sb.append("</name>");
                sb.append("<affiliation>");
                sb.append(params.getValue("affiliation"));
                sb.append("</affiliation>");
                sb.append("<email>");
                sb.append(params.getValue("email"));
                sb.append("</email>");
                sb.append("<password>");
                sb.append(NKHelper.getMD5Value(context, params.getValue("passwd")));
                sb.append("</password>");
                sb.append("<hint>");
                sb.append(params.getValue("hint"));
                sb.append("</hint>");
                sb.append("<justification>");
                sb.append(params.getValue("justification"));
                sb.append("</justification>");
                sb.append("</user>");
                retValue = new StringAspect(sb.toString());
            } catch (XPathLocationException e) {
                // TODO What should the error code be?
                throw new PURLException("Unable to create user: " + NKHelper.getLastSegment(context), 500);
            }

            return retValue;
        }
    }

 /*   class UserResourceStorage extends DefaultResourceStorage {
        public boolean resourceExists(INKFConvenienceHelper context, URIResolver resolver) throws NKFException {
            boolean retValue = super.resourceExists(context, resolver);
            // First check to see if the is an existing user-request
            if(!retValue) {
                // If not, check to see if there is a user
                retValue = super.resourceExists(context, userResolver);
            }

            return retValue;
        }
    } */

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
                
                INKFRequest req = context.createSubRequest("active:purl-storage-approve-user");
                URIResolver uriResolver = getURIResolver();
                String uri = uriResolver.getURI(context);
                String[] parts = uri.split("/");
                int length = parts.length;
                req.addArgument("param", new StringAspect("<user><id>" + parts[length-1] + "</id></user>")); 
                context.issueSubRequest(req);
                
                req=context.createSubRequest("active:purl-storage-query-user");
                req.addArgument("uri", "ffcpl:/user/" + parts[length-1]);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
                resp = context.createResponseFrom(userFilter.filter(context, result));
                resp.setMimeType("text/xml");                
                
            } catch(Throwable t) {
                t.printStackTrace();
            }
            
            return resp;
        }
        
    }
}
