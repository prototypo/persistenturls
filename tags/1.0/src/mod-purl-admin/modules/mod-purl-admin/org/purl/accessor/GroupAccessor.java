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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.purl.accessor.util.AccessController;
import org.purl.accessor.util.AllowableResource;
import org.purl.accessor.util.GroupAccessController;
import org.purl.accessor.util.GroupHelper;
import org.purl.accessor.util.GroupResolver;
import org.purl.accessor.util.GroupResourceStorage;
import org.purl.accessor.util.GroupSearchHelper;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.util.ResourceCreator;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UnconstrainedGETAccessController;
import org.purl.accessor.util.UserGroupAllowableResource;
import org.purl.accessor.util.UserHelper;
import org.purl.accessor.util.UserResolver;
import org.purl.accessor.util.UserResourceStorage;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GroupAccessor extends AbstractAccessor {

    public static final String TYPE = "group";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();

    public GroupAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        URIResolver userResolver = new UserResolver();
        URIResolver groupResolver = new GroupResolver();

        ResourceFilter groupFilter = new GroupPrivateDataFilter();
        ResourceStorage groupStorage = new GroupResourceStorage();
        ResourceStorage userStorage = new UserResourceStorage();
        
        AllowableResource userGroupAllowableResource = new UserGroupAllowableResource(userStorage, userResolver, groupStorage, groupResolver);        
        ResourceCreator groupCreator = new GroupCreator(new UserResolver(), groupResolver);
        
        AccessController groupAccessController = new GroupAccessController();

        commandMap.put("GET", new GetResourceCommand(TYPE, groupResolver, new UnconstrainedGETAccessController(), groupStorage, new GroupSearchHelper(), groupFilter));
        commandMap.put("POST", new CreateResourceCommand(TYPE, userGroupAllowableResource, groupResolver, groupAccessController, groupCreator, groupFilter, groupStorage));
        commandMap.put("DELETE", new DeleteResourceCommand(TYPE, groupResolver, groupAccessController, groupStorage));
        commandMap.put("PUT", new UpdateResourceCommand(TYPE, groupResolver, groupAccessController, groupCreator, groupStorage));
    }

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        return commandMap.get(method);
    }

    static public class GroupCreator implements ResourceCreator {

        private URIResolver userResolver;
        private URIResolver groupResolver;

        public GroupCreator(URIResolver userResolver, URIResolver groupResolver) {
            this.userResolver = userResolver;
            this.groupResolver = groupResolver;
        }

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {

            String currentUser = NKHelper.getUser(context);
            String maintainers = params.getValue("maintainers");
            String members = params.getValue("members");

            StringTokenizer st = new StringTokenizer(maintainers, "\n, ");
            while(st.hasMoreTokens()) {
                String next = st.nextToken();
                if(!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
                   !GroupHelper.isValidGroup(context, groupResolver.getURI(next))) 
                {
                    throw new PURLException(next + " does not exist or is not an approved user.", 400);
                }
            }

            st = new StringTokenizer(members, "\n, ");
            while(st.hasMoreTokens()) {
                String next = st.nextToken();
                if(!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
                   !GroupHelper.isValidGroup(context, groupResolver.getURI(next))) 
                {                
                    throw new PURLException(next + " does not exist or is not an approved user.", 400);
                }
            }

            String groupId = groupResolver.getURI(context);
            StringBuffer sb = new StringBuffer("<group>");
            sb.append("<id>");
            sb.append(groupResolver.getDisplayName(groupId));
            sb.append("</id>");
            sb.append("<name>");
            sb.append(cleanseInput(params.getValue("name")));
            sb.append("</name>");
            sb.append("<maintainers>");
            
            Set<String> maintainerList = new HashSet<String>();
            
            st = new StringTokenizer(maintainers, ", ");
            while(st.hasMoreElements()) {
                String maintainer = st.nextToken().trim();
                
                // Avoid duplicates                
                if(maintainerList.contains(maintainer)) {
                    continue; 
                }
                
                if(UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
                    sb.append("<uid>");
                    sb.append(maintainer);
                    sb.append("</uid>");
                } else {
                    sb.append("<gid>");
                    sb.append(maintainer);
                    sb.append("</gid>");                    
                }
                
                maintainerList.add(maintainer);
            }
            
            if(!maintainerList.contains(currentUser)) {
                sb.append("<uid>");
                sb.append(currentUser);
                sb.append("</uid>");
            }
            
            sb.append("</maintainers>");
            
            maintainerList.clear();
            
            sb.append("<members>");
            st = new StringTokenizer(members, "\n, ");
            while(st.hasMoreElements()) {
                String member = st.nextToken().trim();
                
                // Avoid duplicates                
                if(maintainerList.contains(member)) {
                    continue; 
                }
                
                if(UserHelper.isValidUser(context, userResolver.getURI(member))) {
                    sb.append("<uid>");
                    sb.append(member);
                    sb.append("</uid>");
                } else {
                    sb.append("<gid>");
                    sb.append(member);
                    sb.append("</gid>");                    
                }
                
                maintainerList.add(member);
            }
            
            sb.append("</members>");
            sb.append("<comments>");
            sb.append(cleanseInput(params.getValue("comments")));
            sb.append("</comments>");
            sb.append("</group>");
            
            return new StringAspect(sb.toString());
        }
    }
    
    static public class GroupPrivateDataFilter implements ResourceFilter {

        public IURAspect filter(INKFConvenienceHelper context, IURAspect iur) {
            IURAspect retValue = null;

            try {
                INKFRequest req = context.createSubRequest();
                req.setURI("active:xslt");
                req.addArgument("operand", iur);
                req.addArgument("operator", "ffcpl:/filters/group.xsl");
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
