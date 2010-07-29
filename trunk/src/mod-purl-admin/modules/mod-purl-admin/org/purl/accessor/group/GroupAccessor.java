package org.purl.accessor.group;

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
import org.purl.accessor.group.GroupAccessController;
import org.purl.accessor.group.GroupCreator;
import org.purl.accessor.group.GroupResolver;
import org.purl.accessor.group.GroupResourceStorage;
import org.purl.accessor.group.GroupSearchHelper;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UnconstrainedGETAccessController;
import org.purl.accessor.user.UserGroupAllowableResource;
import org.purl.accessor.user.UserResolver;
import org.purl.accessor.user.UserResourceStorage;
import org.purl.accessor.util.XSLTResourceFilter;
import org.purl.accessor.AllowableResource;
import org.purl.accessor.*;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class GroupAccessor extends AbstractAccessor {

    public static final String TYPE = "group";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();

    public GroupAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        URIResolver userResolver = new UserResolver();
        URIResolver groupResolver = new GroupResolver();

        ResourceFilter groupFilter = new XSLTResourceFilter("ffcpl:/filters/group.xsl");
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
}
