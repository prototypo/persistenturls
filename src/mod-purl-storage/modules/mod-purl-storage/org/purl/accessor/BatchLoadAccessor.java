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

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.StringAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class BatchLoadAccessor extends NKFAccessorImpl {

    /**
     * Default constructor to indicate that we are good for source requests
     * and is safe for concurrent use.
     */
    public BatchLoadAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }


    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {

        if (!context.exists("this:param:param")) {
            throw new IllegalArgumentException("Missing param argument");
        }


        BatchLoadRequest request = new BatchLoadRequest(context);

        // Iterate over the supplied list of purls and process each individually
        IXDAReadOnly purlXDA = request.getPurlXDA();
        IXDAReadOnlyIterator it = purlXDA.readOnlyIterator("/purls/purl");
        int successful = 0,failed = 0;
        StringBuffer failedSB = new StringBuffer();

        while (it.next()) {
            if (processPurl(request, new StringAspect(it.toString()), failedSB)) {
                successful++;
            } else {
                failed++;
            }

        }

        // Prepare the results, including any failed PURL descriptions
        StringBuffer sb = new StringBuffer("<purl-batch total=\"" + request.getPurlCount() + "\" numCreated=\"");
        sb.append(successful);
        sb.append("\" failed=\"" + failed + "\">");
        if (failedSB.length() > 0) {
            sb.append(failedSB.toString());
        }
        sb.append("</purl-batch>");

        INKFResponse resp = context.createResponseFrom(new StringAspect(sb.toString()));
        context.setResponse(resp);

        resp.setMimeType("text/xml");
        resp.setExpired();
        context.setResponse(resp);
    }

    /**
     * Attempts to create a purl based on the supplied XML fragment.
     * @param request The current batch load request
     * @param purlXML an XML fragment describing the PURL
     * @param resultBuffer a buffer that failures will be written to
     * @return true on success
     */
    private boolean processPurl(BatchLoadRequest request, IAspectString purlXML, StringBuffer resultBuffer) {
        try {
            INKFConvenienceHelper context = request.getContext();

            // Transform the XML fragment to the appropriate SQL statements
            INKFRequest req = context.createSubRequest();
            req.setURI("active:xsltc");
            req.addArgument("operand", purlXML);
            req.addArgument("operator", "ffcpl:/sql/db/batchload.xsl");
            IURRepresentation iur = context.issueSubRequest(req);

            // Insert the list of maintainers into the sql statements
            StringBuffer maintainerSB = new StringBuffer("<sed>");
            for (String key : request.getMaintainers()) {
                String maintainerID = request.getMaintainerID(key);
                if (maintainerID != null) {
                    maintainerSB.append(generateSedPattern("@@MAINTAINER-" + key + "@@", maintainerID));
                }
            }
            maintainerSB.append(generateSedPattern("@@CURRENTUSER@@", request.getMaintainerID(request.getCurrentUser())));
            maintainerSB.append("</sed>");

            req = context.createSubRequest();
            req.setURI("active:sed");
            req.addArgument("operator", new StringAspect(maintainerSB.toString()));
            req.addArgument("operand", iur);
            iur = context.issueSubRequest(req);

            // Submit the batch
            req = context.createSubRequest("active:sqlBatch");
            req.addArgument("operand", iur);
            iur = context.issueSubRequest(req);
        } catch (Throwable t) {
            resultBuffer.append("<failure><message>Failed SQL Batch -- Most likely duplicate</message>" + purlXML.getString() + "</failure>");
            return false;
        }
        return true;
    }

    private String generateSedPattern(String pattern, String value) {
        return "<pattern><regex>" + pattern +
                "</regex><replace>" + value +
                "</replace></pattern>";
    }
}
