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

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.apache.commons.lang.StringEscapeUtils;
import com.ten60.netkernel.urii.IURRepresentation;

public class PURLPartialRedirectResolveCommand extends PURLResolveCommand {

    private boolean ignoreExtension = false;

    public PURLPartialRedirectResolveCommand(boolean ignoreExtension) {
        this.ignoreExtension = ignoreExtension;

    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context, IAspectXDA purl) {
        INKFResponse resp = null;
        IXDAReadOnly purlXDARO = purl.getXDA();
        try {
            String path = context.getThisRequest().getArgument("path").substring(6);

            String pid = purlXDARO.getText("/purl/id", true);
            String url = purlXDARO.getText("/purl/target/url", true);
            String queryChar = "?";
            if(!path.equals(pid)) {
                String remainder =  path.substring(pid.length());
                if (url.contains(queryChar)) {

                    if (remainder.contains(queryChar)) {
                        remainder = remainder.replaceFirst("\\?", "&");
                        queryChar="&";
                    }
                    if (remainder.startsWith("/")) {
                        remainder = remainder.replaceFirst("/", ""); 
                    }
                }
                if (ignoreExtension) {
                    String query = "";
                    if (remainder.contains(queryChar)) {
                        query = remainder.substring(remainder.indexOf(queryChar));
                        remainder = remainder.substring(0, remainder.indexOf(queryChar));
                    }
                    if (remainder.contains(".")) {
                        remainder = remainder.substring(0,remainder.lastIndexOf('.'));
                    }
                    remainder = remainder+query;
                }
                url = url + remainder;
            }
            
            IURRepresentation bodyDoc = context.source("ffcpl:/pub/redirect.html");

            // We treat the partial redirect as a 302
            resp = generateResponseCode(context, "302", url,
                    parameterizeBodyDoc(context, bodyDoc, "302", url),
                    "text/html; charset=iso-8859-1");

        } catch(Throwable t) {
            t.printStackTrace();
        }
        return resp;
    }

}
