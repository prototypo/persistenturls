package org.purl.accessor.command;

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

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLGoneResolveCommand extends PURLResolveCommand {

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse resp = null;
        IXDAReadOnly purlXDARO = purl.getXDA();
        try {
            String type = purlXDARO.getText("/purl/type", true);
            INKFRequest req=context.createSubRequest();
            IURRepresentation page = context.source("ffcpl:/resources/" + type + "-gone.html");
            //StringAspect page = new StringAspect("<html><body>Goone Daddy Gone</body></html>");
            req.setURI("active:HTTPResponseCode");
            StringBuffer respCode = new StringBuffer("<HTTPResponseCode><code>");
            respCode.append(type);
            respCode.append("</code></HTTPResponseCode>");
            req.addArgument("param", new StringAspect(respCode.toString()));
            req.addArgument("operand", page);
            IURRepresentation result=context.issueSubRequest(req);
            resp=context.createResponseFrom(result);
            resp.setMimeType("text/html");
        } catch(Throwable t) {
            t.printStackTrace();
        }
        return resp;
    }

}
