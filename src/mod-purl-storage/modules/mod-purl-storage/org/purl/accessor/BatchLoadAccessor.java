package org.purl.accessor;

/**
 *=========================================================================
 *
 *  Copyright (C) 2007-2008 OCLC (http://oclc.org)
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
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class BatchLoadAccessor extends NKFAccessorImpl {
    
    /**
     * Default constructor to indicate that we are good for source requests
     * and is safe for concurrent use.
     *
     */
    public BatchLoadAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }
    
    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        
        // We'll maintain a local copy of any maintainer info to avoid unnecessary
        // kernel request scheduling
        
        Map<String,String> maintainerMap = new HashMap<String,String>();
        
        if(!context.exists("this:param:param")) {
            throw new IllegalArgumentException("Missing param argument");
        }
        
        IURRepresentation iur = context.source("this:param:param");
        INKFRequest req = null; 
        INKFResponse resp = null;
        
        // If the batch file contains any apostrophes, escape them for processing below
        req=context.createSubRequest("active:SQLEscapeXML");
        req.addArgument("operand", iur);
        iur=context.issueSubRequest(req);
        
        IAspectXDA xdaParam = (IAspectXDA) context.transrept(iur, IAspectXDA.class);
        String currentUser = ((IAspectString) context.sourceAspect("this:param:currentuser", IAspectString.class)).getString();
        
        // TODO: Authenticate the batch format
        // TODO: THIS IS DANGEROUS. WE NEED TO DETERMINE WHAT THE TRANSACTIONAL PROPERTIES OF BATCH LOADS ARE.
        int count = Integer.valueOf(xdaParam.getXDA().eval("count(/purls/purl)").getStringValue()).intValue();

        IXDAReadOnlyIterator maintainerItor = xdaParam.getXDA().readOnlyIterator("//maintainers/uid");
        
        while(maintainerItor.hasNext()) {
            maintainerItor.next();
            String maintainer = maintainerItor.getText(".", true);
            String z_id = maintainerMap.get(maintainer);
            
            if(z_id == null) {
                req = context.createSubRequest("active:purl-storage-query-user");
                req.addArgument("uri", "ffcpl:/user/" + maintainer);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA maintainerXDA = (IAspectXDA)context.issueSubRequestForAspect(req);
                z_id = maintainerXDA.getXDA().getText("/user/z_id", true);
                maintainerMap.put(maintainer, z_id);
            }
        }
        
        if(!maintainerMap.containsKey(currentUser)) {
            req = context.createSubRequest("active:purl-storage-query-user");
            req.addArgument("uri", "ffcpl:/user/" + currentUser);
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA maintainerXDA = (IAspectXDA)context.issueSubRequestForAspect(req);
            String z_id = maintainerXDA.getXDA().getText("/user/z_id", true);
            maintainerMap.put(currentUser, z_id);           
        }
        
        // Iterate over the groups
        maintainerItor = xdaParam.getXDA().readOnlyIterator("//maintainers/gid");
        
        while(maintainerItor.hasNext()) {
            maintainerItor.next();
            String maintainer = maintainerItor.getText(".", true);
            String z_id = maintainerMap.get(maintainer);
            
            if(z_id == null) {
                req = context.createSubRequest("active:purl-storage-query-group");
                req.addArgument("uri", "ffcpl:/group/" + maintainer);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA maintainerXDA = (IAspectXDA)context.issueSubRequestForAspect(req);
                z_id = maintainerXDA.getXDA().getText("/group/z_id", true);
                maintainerMap.put(maintainer, z_id);
            }
        }
        
        if(!maintainerMap.containsKey(currentUser)) {
            req = context.createSubRequest("active:purl-storage-query-user");
            req.addArgument("uri", "ffcpl:/user/" + currentUser);
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA maintainerXDA = (IAspectXDA)context.issueSubRequestForAspect(req);
            String z_id = maintainerXDA.getXDA().getText("/user/z_id", true);
            maintainerMap.put(currentUser, z_id);           
        }
        
        Iterator<String> itor = maintainerMap.keySet().iterator();
        
        StringBuffer maintainerSB = new StringBuffer("<sed>");
        
        while(itor.hasNext()) {
            String key = itor.next();
            maintainerSB.append("<pattern><regex>@@MAINTAINER-");
            maintainerSB.append(key);
            maintainerSB.append("@@</regex><replace>");
            maintainerSB.append(maintainerMap.get(key));
            maintainerSB.append("</replace></pattern>");
        }
        
        maintainerSB.append("<pattern><regex>@@CURRENTUSER@@</regex><replace>");
        maintainerSB.append(maintainerMap.get(currentUser));
        maintainerSB.append("</replace></pattern>");        
        maintainerSB.append("</sed>");
        
        req=context.createSubRequest();
        req.setURI("active:xsltc");
        req.addArgument("operand", xdaParam);
        req.addArgument("operator", "ffcpl:/sql/db/batchload.xsl");
        iur = context.issueSubRequest(req);
 
         req = context.createSubRequest();
        req.setURI("active:sed");
        req.addArgument("operator", new StringAspect(maintainerSB.toString()));
        req.addArgument("operand", iur);
        iur = context.issueSubRequest(req);
        
        try {
        req = context.createSubRequest("active:sqlBatch");
        req.addArgument("operand", iur);
        iur = context.issueSubRequest(req);
        } catch(Throwable t) {
        	t.printStackTrace();
        }
        
        StringBuffer sb = new StringBuffer("<purl-batch-success numCreated=\"");
        sb.append(count);
        sb.append("\"/>");
        
        resp = context.createResponseFrom(new StringAspect(sb.toString()));
        context.setResponse(resp);

        resp.setMimeType("text/xml");
        resp.setExpired();
        context.setResponse(resp);
    }
}
