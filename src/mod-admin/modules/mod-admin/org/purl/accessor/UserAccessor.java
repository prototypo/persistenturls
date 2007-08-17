package org.purl.accessor;

/**
 * version 1.0, 16 August 2007
 * Brian Sletten (brian at http://zepheira.com/)
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
 *	=========================================================================
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

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.layer1.representation.IAspectNVP;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class UserAccessor extends NKFAccessorImpl {

	private static Map<String, PURLCommand> commandMap = new HashMap<String, PURLCommand>();
	private static final String
		TEXT = "text/plain",
		XML = "text/xml",
		HTML = "text/html";

	static {
		commandMap.put("GET", new GetUserPURLCommand());
		commandMap.put("POST", new CreateUserPURLCommand());
		commandMap.put("DELETE", new DeleteUserPURLCommand());
		commandMap.put("PUT", new UpdateUserPURLCommand());
	}

	public UserAccessor() {
		super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);

	}

	@Override
	public void processRequest(INKFConvenienceHelper context) throws Exception {
		INKFResponse resp = null;

		String method = (((IAspectString)context.sourceAspect("literal:method", IAspectString.class)).getString());

		PURLCommand cmd = commandMap.get(method);

		if(cmd != null) {
			resp = cmd.execute(context);
		} else {
			// TODO: Generate an ERROR
		}

		context.setResponse(resp);
	}

	static abstract public class PURLCommand {
		abstract INKFResponse execute(INKFConvenienceHelper context);

		protected IAspectNVP getParams(INKFConvenienceHelper context) throws NKFException {
			IAspectNVP retValue = null;

			if(context.exists("this:param:param")) {
				retValue = (IAspectNVP) context.sourceAspect("this:param:param", IAspectNVP.class);
				IAspectString param = (IAspectString) context.sourceAspect("this:param:param", IAspectString.class);
				System.out.println(param.getString());
			}

			if(retValue == null) {
				if(context.exists("this:param:param2")) {
					retValue = (IAspectNVP) context.sourceAspect("this:param:param2", IAspectNVP.class);
				}
			}

			return retValue;
		}

		protected String getId(INKFConvenienceHelper context) throws NKFException {
			String retValue = null;
			String path=context.getThisRequest().getArgument("path");
			String[] parts=path.split("/");
			if(!parts[parts.length-1].equals("user")) {
				retValue = parts[parts.length-1];
			}
			return retValue;
		}

		protected String generateResourceURI(String id) {
			return "ffcpl:/users/" + id;
		}

		protected boolean userExists(String id, INKFConvenienceHelper context) throws NKFException {
			String resource = generateResourceURI(id);
			System.out.println("Checking on... " + resource);
			return context.exists(resource);
		}

		protected IURRepresentation setResponseCode(INKFConvenienceHelper context, IURAspect aspect, int code) throws NKFException {
			StringBuffer sb = new StringBuffer("<HTTPResponseCode>");
			sb.append("<code>");
			sb.append(code);
			sb.append("</code>");
			sb.append("</HTTPResponseCode>");

			System.out.println(sb.toString());

			INKFRequest req = context.createSubRequest("active:HTTPResponseCode");
			req.addArgument("operand", aspect);
			req.addArgument("param", new StringAspect(sb.toString()));
			IURRepresentation resp = context.issueSubRequest(req);
			System.out.println(resp.toString());
			return resp;
		}
	}

	static public class CreateUserPURLCommand extends PURLCommand {
		protected String generateUser(String id, IAspectNVP params) {
			StringBuffer sb = new StringBuffer("<user>");
			sb.append("<id>");
			sb.append(id);
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
			return sb.toString();
		}

		public INKFResponse execute(INKFConvenienceHelper context) {
			INKFResponse retValue = null;

			try {
				IAspectNVP params = getParams(context);
				String id = getId(context);
				if(userExists(id, context)) {
					// Cannot create the same name
					IURRepresentation rep = setResponseCode(context, new StringAspect("User: " + id + " already exists."), 409);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(TEXT);
				} else {
					IURAspect iur = new StringAspect(generateUser(id, params));
					context.sinkAspect(generateResourceURI(id), iur);
					IURRepresentation rep = setResponseCode(context, iur, 201);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(XML);
				}

			} catch (NKFException e) {
				// TODO Handle
				e.printStackTrace();
			}

			return retValue;
		}

	}

	static public class GetUserPURLCommand extends PURLCommand {

		public INKFResponse execute(INKFConvenienceHelper context) {

			INKFResponse retValue = null;

			try {
				IAspectNVP params = getParams(context);
				String id = getId(context);
				if(userExists(id, context)) {
					// Default response code of 200 is fine

					IURRepresentation rep = setResponseCode(context, context.sourceAspect(generateResourceURI(id), IAspectString.class), 200);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(XML);
				} else {
					IURRepresentation rep = setResponseCode(context, new StringAspect("No such user: " + id), 404);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(TEXT);
				}

			} catch (NKFException e) {
				// TODO Handle
				e.printStackTrace();
			}

			return retValue;
		}

	}

	static public class DeleteUserPURLCommand extends PURLCommand {

		public INKFResponse execute(INKFConvenienceHelper context) {
			INKFResponse retValue = null;

			try {
				IAspectNVP params = getParams(context);
				String id = getId(context);
				if(userExists(id, context)) {
					// Default response code of 200 is fine
					context.delete(generateResourceURI(id));
					IURRepresentation rep = setResponseCode(context, new StringAspect("Deleted user: " + id), 200);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(TEXT);

				} else {
					IURRepresentation rep = setResponseCode(context, new StringAspect("No such user: " + id), 404);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(TEXT);
				}

			} catch (NKFException e) {
				// TODO Handle
				e.printStackTrace();
			}

			return retValue;

		}

	}

	static public class UpdateUserPURLCommand extends CreateUserPURLCommand {

		public INKFResponse execute(INKFConvenienceHelper context) {
			INKFResponse retValue = null;

			try {
				IAspectNVP params = getParams(context);
				String id = getId(context);
				if(userExists(id, context)) {
					// Update the user
					IURAspect iur = new StringAspect(generateUser(id, params));
					context.sinkAspect(generateResourceURI(id), iur);
					IURRepresentation rep = setResponseCode(context, new StringAspect("Updated user: " + id), 200);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(TEXT);
				} else {
					IURRepresentation rep = setResponseCode(context, new StringAspect("No such user: " + id), 404);
					retValue = context.createResponseFrom(rep);
					retValue.setMimeType(TEXT);
				}

			} catch (NKFException e) {
				// TODO Handle
				e.printStackTrace();
			}

			return retValue;
		}

	}

}
