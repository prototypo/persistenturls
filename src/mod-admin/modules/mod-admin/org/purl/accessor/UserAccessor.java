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

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class UserAccessor extends AbstractAccessor {

	static {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        URIResolver userResolver = new URIResolver() {
            @Override
            public String getURI(INKFConvenienceHelper context) {
                String retValue = null;

                try {
                    retValue = "ffcpl:/users/" + NKHelper.getLastSegment(context);
                } catch(NKFException nfe) {
                    nfe.printStackTrace();
                }

                return retValue;
            }

        };

        ResourceCreator userCreator = new UserCreator();

		commandMap.put("GET", new GetResourceCommand(userResolver));
		commandMap.put("POST", new CreateResourceCommand(userResolver, userCreator));
		commandMap.put("DELETE", new DeleteResourceCommand(userResolver));
		commandMap.put("PUT", new UpdateResourceCommand(userResolver, userCreator));
	}

    static public class UserCreator implements ResourceCreator {

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
            StringBuffer sb = new StringBuffer("<user>");
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
            sb.append("</user>");
            return new StringAspect(sb.toString());
        }
    }
}
