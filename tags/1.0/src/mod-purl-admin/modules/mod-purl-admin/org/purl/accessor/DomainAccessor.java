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
import org.purl.accessor.util.DomainAccessController;
import org.purl.accessor.util.DomainAllowableResource;
import org.purl.accessor.util.DomainResolver;
import org.purl.accessor.util.DomainResourceStorage;
import org.purl.accessor.util.DomainSearchHelper;
import org.purl.accessor.util.GroupResolver;
import org.purl.accessor.util.NKHelper;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.util.ResourceCreator;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UserHelper;
import org.purl.accessor.util.UserResolver;
import org.purl.accessor.util.UserResourceStorage;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class DomainAccessor extends AbstractAccessor {

    public static final String TYPE = "domain";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();
    
    private ResourceStorage domainStorage;

    public DomainAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        URIResolver domainResolver = new DomainResolver();

        ResourceFilter domainFilter = new DomainPrivateDataFilter();
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

            if(method.equals("POST")) {
                retValue = commandMap.get("POST");
                
                IXDAReadOnly configXDA = config.getXDA();
                if(configXDA.isTrue("/purl-config/allowTopLevelDomainAutoCreation")) {
                    retValue = new DomainAutoCreateCommand(retValue, domainStorage);
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

    static public class DomainCreator implements ResourceCreator {

        private URIResolver domainResolver;
        private URIResolver userResolver;
        private URIResolver groupResolver;

        public DomainCreator(URIResolver domainResolver, URIResolver userResolver, URIResolver groupResolver, ResourceStorage userStorage) {
            this.domainResolver = domainResolver;
            this.userResolver = userResolver;
            this.groupResolver = groupResolver;
        }

        public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {

            String currentUser = NKHelper.getUser(context);
            String maintainers = params.getValue("maintainers");
            String writers = params.getValue("writers");

            StringTokenizer st = new StringTokenizer(maintainers, ", ");
            while(st.hasMoreTokens()) {
                String next = st.nextToken();
                if(!UserHelper.isValidUser(context, userResolver.getURI(next)) &&
                   !UserHelper.isValidGroup(context, groupResolver.getURI(next))) 
                {                
                    throw new PURLException("User or group " + next + " does not exist", 400);
                }
            }

            st = new StringTokenizer(writers, ", ");
            while(st.hasMoreTokens()) {
                String next = st.nextToken();
                if(!UserHelper.isValidUser(context, userResolver.getURI(next))&&
                   !UserHelper.isValidGroup(context, groupResolver.getURI(next))) 
                {                                
                    throw new PURLException("User or group " + next + " does not exist", 400);
                }
            }
            
            //TODO: Migrate to use Domain Aspect
            
            String domain = domainResolver.getDisplayName(domainResolver.getURI(context));

            StringBuffer sb = new StringBuffer("<domain>");
            sb.append("<public>");
            sb.append(params.getValue("public"));
            sb.append("</public>");
            sb.append("<id>");
            sb.append(domain);
            sb.append("</id>");
            sb.append("<name>");
            sb.append(cleanseInput(params.getValue("name")));
            sb.append("</name>");
            sb.append("<maintainers>");
            
            st = new StringTokenizer(maintainers, ",");

            Set<String> maintainerList = new HashSet<String>();
            
            while(st.hasMoreElements()) {
                String maintainer = st.nextToken().trim();
                
                // Avoid duplicates                
                if(maintainerList.contains(maintainer)) {
                    continue; 
                }

                if(UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
                    sb.append("<uid>");
                    sb.append(maintainer.trim());
                    sb.append("</uid>");
                } else {
                    sb.append("<gid>");
                    sb.append(maintainer.trim());
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
            
            sb.append("<writers>");
            st = new StringTokenizer(writers, ",");
            
            while(st.hasMoreElements()) {
                String maintainer = st.nextToken().trim();
                
                // Avoid duplicates                
                if(maintainerList.contains(maintainer)) {
                    continue; 
                }
                
                if(UserHelper.isValidUser(context, userResolver.getURI(maintainer))) {
                    sb.append("<uid>");
                    sb.append(maintainer.trim());
                    sb.append("</uid>");
                } else {
                    sb.append("<gid>");
                    sb.append(maintainer.trim());
                    sb.append("</gid>");                    
                }
                
                maintainerList.add(maintainer);
            }
            
            sb.append("</writers>");
            sb.append("</domain>");
            return new StringAspect(sb.toString());
        }
    }
    
    static public class DomainPrivateDataFilter implements ResourceFilter {

        public IURAspect filter(INKFConvenienceHelper context, IURAspect iur) {
            IURAspect retValue = null;

            try {
                INKFRequest req = context.createSubRequest();
                req.setURI("active:xslt");
                req.addArgument("operand", iur);
                req.addArgument("operator", "ffcpl:/filters/domain.xsl");
                req.setAspectClass(IAspectString.class);
                retValue = context.issueSubRequestForAspect(req);
            } catch(NKFException nfe) {
                // TODO: return something other than the raw user
                nfe.printStackTrace();
            }

            return retValue;
        }

    }
    
    static private class DomainAutoCreateCommand extends PURLCommand {
        
        private PURLCommand createCmd;
        private ResourceFilter domainFilter = new DomainPrivateDataFilter();
        private ResourceStorage domainStorage;
        
        private DomainAutoCreateCommand() {
            super(null, null, null, null);
        }
        
        public DomainAutoCreateCommand(PURLCommand createCmd, ResourceStorage domainStorage) {
            super(createCmd.getType(), createCmd.getURIResolver(), createCmd.getAccessController(), createCmd.getResourceStorage());            
            this.createCmd = createCmd;
            this.domainStorage = domainStorage;
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
