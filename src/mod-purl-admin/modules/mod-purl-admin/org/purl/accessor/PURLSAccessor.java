package org.purl.accessor;

import java.util.ArrayList;
import java.util.List;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.layer1.representation.NVPAspect;
import org.ten60.netkernel.layer1.representation.NVPImpl;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class PURLSAccessor extends NKFAccessorImpl {

	public PURLSAccessor() {
		super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
	}
	@Override
	public void processRequest(INKFConvenienceHelper context) throws Exception {
//		String path=context.getThisRequest().getArgument("path");
        IAspectXDA xdaParam = (IAspectXDA) context.sourceAspect("this:param:param", IAspectXDA.class);

        // Validate the input document against the batch schema
        INKFRequest req = context.createSubRequest("active:validateRNG");
        req.addArgument("operand", xdaParam);
        req.addArgument("operator", "ffcpl:/schemas/batchPurls.rng");
        req.setAspectClass(IAspectXDA.class);
        IAspectXDA xda = (IAspectXDA)context.issueSubRequestForAspect(req);
        IXDAReadOnly xdaRO = xda.getXDA();
        INKFResponse resp = null;

        // TODO: Make this more declarative
        // TODO: What's the best way to do this?
        if(xdaRO.isTrue("/b/text()='t'")) {
            List<String> createdPurlList = new ArrayList<String>();
            IXDAReadOnlyIterator xdaROItor = xdaParam.getXDA().readOnlyIterator("/purls/purl");
            while(xdaROItor.hasNext()) {
                xdaROItor.next();

                //TODO: Handle transactions

                NVPImpl nvp = new NVPImpl();

                String pid = xdaROItor.getText("@id", true);
                String type = xdaROItor.getText("@type", true);
                IXDAReadOnlyIterator maintainerXdaROItor = xdaParam.getXDA().readOnlyIterator( xdaROItor.getCurrentXPath() + "/maintainers/maintainer");
                StringBuffer sb = new StringBuffer();
                while(maintainerXdaROItor.hasNext()) {
                    maintainerXdaROItor.next();
                    if(sb.length()>0) {
                        sb.append(",");
                    }
                    sb.append(maintainerXdaROItor.getText("@id", true));
                }

                nvp.addNVP("maintainers", sb.toString());
                nvp.addNVP("type", type);

                if(type.equals("303")) {
                    String seeAlso = xdaROItor.getText("seealso/@url", true);
                    nvp.addNVP("seealso", seeAlso);
                } else if(type.equals("clone") || type.equals("chain")) {
                    String basepurl = xdaROItor.getText("basepurl/@path", true);
                    nvp.addNVP("basepurl", basepurl);
                } else if(!type.equals("404") && !type.equals("410")) {
                    String target = xdaROItor.getText("target/@url", true);
                    nvp.addNVP("target", target);
                }

                req=context.createSubRequest("active:purl");
                req.addArgument("path", "ffcpl:" + pid );
                req.addArgument("method", context.source(context.getThisRequest().getArgument("method")));
                req.addArgument("params", new NVPAspect(nvp));
                req.addArgument("requestURL", context.getThisRequest().getArgument("requestURL"));
                IURRepresentation iur=context.issueSubRequest(req);
                //IAspectString sa = (IAspectString) context.transrept(iur,IAspectString.class);
                //System.out.println(sa.getString());
                createdPurlList.add(pid);
            }

            StringBuffer sb = new StringBuffer("<purl-batch-success numCreated=\"");
            sb.append(createdPurlList.size());
            sb.append("\"/>");

            resp = context.createResponseFrom(new StringAspect(sb.toString()));
        } else {
            StringAspect result = (StringAspect) context.transrept(xda, IAspectString.class);
            StringBuffer sb = new StringBuffer("<purl-batch-error>");
            IXDAReadOnlyIterator xdaROItor = xdaRO.readOnlyIterator("/b/errors/error");
            while(xdaROItor.hasNext()) {
                xdaROItor.next();
                sb.append("<error xpath=\"");
                sb.append(xdaROItor.getText("@xpath", true));
                sb.append("\">");
                sb.append(xdaROItor.getText(".", true));
                sb.append("</error>");
            }

            sb.append("</purl-batch-error>");
            StringAspect sa = new StringAspect(sb.toString());
            resp = context.createResponseFrom(sa);
        }

		resp.setMimeType("text/xml");
		context.setResponse(resp);
	}

}