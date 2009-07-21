package org.purl.accessor.purl.command;

/**
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
 */

import org.purl.accessor.util.NKHelper;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.XPathLocationException;
import org.apache.commons.lang.StringEscapeUtils;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

abstract public class PURLResolveCommand {
    abstract public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl);

    INKFResponse generateResponseCode(INKFConvenienceHelper context, String responseCode, String url) throws NKFException {
        return generateResponseCode(context, responseCode, url, null, "application/void");
    }

    INKFResponse generateResponseCode(INKFConvenienceHelper context, String responseCode, String url, IURRepresentation body, String mimeType) throws NKFException {

        String path = NKHelper.getArgument(context, "path").toLowerCase();

        IAspectXDA configXDA = (IAspectXDA) context.sourceAspect("ffcpl:/etc/PURLConfig.xml", IAspectXDA.class);
        String serverInfo = null;

        try {
            serverInfo = configXDA.getXDA().getText("/purl-config/serverInfo", true);
        } catch (XPathLocationException e) {
            // TODO: Handle this
        }

        INKFResponse resp = null;
        INKFRequest req=context.createSubRequest();
        req.setURI("active:HTTPResponseCode");
        StringBuffer respCode = new StringBuffer("<HTTPResponseCode><code>");
        respCode.append(responseCode);
        respCode.append("</code>");

        respCode.append("<header><name>X-Purl</name><value>");
        respCode.append(serverInfo);
        respCode.append("</value></header>");
        
        respCode.append("<header><name>Content-Type</name><value>");
        respCode.append(mimeType);
        respCode.append("</value></header>");

        if(url!=null) {
            respCode.append("<header><name>Location</name><value>");
            respCode.append(StringEscapeUtils.escapeXml(url));
            respCode.append("</value></header>");
        }

        respCode.append("</HTTPResponseCode>");
        req.addArgument("param", new StringAspect(respCode.toString()));

        if(body != null) {
            req.addArgument("operand", body);
        }

        IURRepresentation result=context.issueSubRequest(req);

        // Cache the result

        req = context.createSubRequest("active:attachGoldenThread");
        req.addArgument("operand", result);
        req.addArgument("param", "gt:" + path);
        result=context.issueSubRequest(req);

        resp=context.createResponseFrom(result);
        resp.setCacheable();
        resp.setMimeType(mimeType);
        return resp;
    }
    
    protected IURRepresentation parameterizeBodyDoc(INKFConvenienceHelper context, IURRepresentation bodyDoc, String type, String url) {
        IURRepresentation retValue = null;

        try {
            // TODO: Turn this into a resource!
            StringBuffer sed = new StringBuffer("<sed><pattern><regex>@@PURL_TYPE@@</regex><replace>");
            sed.append(type);
            sed.append("</replace></pattern><pattern><regex>@@PURL_TARGET@@</regex><replace>");
            sed.append(StringEscapeUtils.escapeXml(url));
            sed.append("</replace></pattern></sed>");
            
            INKFRequest req = context.createSubRequest("active:sed");
            req.addArgument("operand", bodyDoc);
            req.addArgument("operator", new StringAspect(sed.toString()));
            retValue = context.issueSubRequest(req);
        } catch (NKFException e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
}


