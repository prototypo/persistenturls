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
import java.util.StringTokenizer;

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.purl.accessor.util.DefaultResourceDeleter;
import org.purl.accessor.util.GroupResolver;
import org.purl.accessor.util.GroupResourceStorage;
import org.purl.accessor.util.GroupSearchHelper;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.util.ResourceCreator;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
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

        URIResolver groupResolver = new GroupResolver();

        ResourceFilter groupFilter = new GroupPrivateDataFilter();
        ResourceStorage groupStorage = new GroupResourceStorage();
        ResourceCreator groupCreator = new GroupCreator(new UserResolver(), new UserResourceStorage());

        commandMap.put("GET", new GetResourceCommand(TYPE, groupResolver, groupStorage, new GroupSearchHelper(), groupFilter));
        commandMap.put("POST", new CreateResourceCommand(TYPE, groupResolver, groupCreator, groupFilter, groupStorage));
        commandMap.put("DELETE", new DeleteResourceCommand(TYPE, groupResolver, groupStorage));
        commandMap.put("PUT", new UpdateResourceCommand(TYPE, groupResolver, groupCreator, groupStorage));
    }

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        return commandMap.get(method);
    }

    static public class GroupCreator implements ResourceCreator {

        private ResourceStorage userStorage;
        private URIResolver userResolver;

        public GroupCreator(URIResolver userResolver, ResourceStorage userStorage) {
            this.userResolver = userResolver;
            this.userStorage = userStorage;
        }

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {

            String maintainers = params.getValue("maintainers");
            String members = params.getValue("members");

            StringTokenizer st = new StringTokenizer(maintainers, "\n,");
            while(st.hasMoreTokens()) {
                String next = st.nextToken();
                if(!userStorage.resourceExists(context, userResolver.getURI(next))) {
                    throw new PURLException("User " + next + " does not exist", 400);
                }
            }

            st = new StringTokenizer(members, "\n,");
            while(st.hasMoreTokens()) {
                String next = st.nextToken();
                if(!userStorage.resourceExists(context, userResolver.getURI(next))) {
                    throw new PURLException("User " + next + " does not exist", 400);
                }
            }

            String groupId = NKHelper.getLastSegment(context); 
            StringBuffer sb = new StringBuffer("<group>");
            sb.append("<id>");
            sb.append(groupId);
            sb.append("</id>");
            sb.append("<name>");
            sb.append(params.getValue("name"));
            sb.append("</name>");
            sb.append("<maintainers>");
            
            st = new StringTokenizer(maintainers, ",");
            while(st.hasMoreElements()) {
                sb.append("<uid>");
                sb.append(st.nextToken().trim());
                sb.append("</uid>");
            }
            
            sb.append("</maintainers>");
            sb.append("<members>");
            st = new StringTokenizer(members, ",");
            while(st.hasMoreElements()) {
                sb.append("<uid>");
                String userId = st.nextToken().trim(); 
                sb.append(userId);
                addGroupForUser(context, groupId, userId);
                sb.append("</uid>");
            }
            sb.append("</members>");
            sb.append("<comments>");
            sb.append(params.getValue("comments"));
            sb.append("</comments>");
            sb.append("</group>");
            
            return new StringAspect(sb.toString());
        }
    }
    
    private static void addGroupForUser(INKFConvenienceHelper context, String group, String user) throws NKFException {
        context.sinkAspect("active:purl-groups-for-user+user@user:" + user, new StringAspect("<groups><group id=\"" + group + "\"/></groups>"));
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
