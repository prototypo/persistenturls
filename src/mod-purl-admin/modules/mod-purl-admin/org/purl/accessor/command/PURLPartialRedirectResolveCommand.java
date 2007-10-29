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
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

public class PURLPartialRedirectResolveCommand extends PURLResolveCommand {

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse resp = null;
        IXDAReadOnly purlXDARO = purl.getXDA();
        try {
            String path = context.getThisRequest().getArgument("path");
            String pid = purlXDARO.getText("/purl/pid", true).toLowerCase();
            String url = purlXDARO.getText("/purl/target/url", true);

            if(!path.startsWith(pid)) {
                // TODO : handle exception
            }

            url = url + path.substring(pid.length() + 1);
            url = url.replaceAll("&", "&amp;");

            // We treat the partial redirect as a 302
            resp = generateResponseCode(context, "302", url);

        } catch(Throwable t) {
            t.printStackTrace();
        }
        return resp;
    }

}
