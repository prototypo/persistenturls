package org.purl.accessor.purl;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;
import org.purl.accessor.util.*;
import org.purl.accessor.purl.PURLAllowableResource;
import org.purl.accessor.util.PURLException;
import org.purl.accessor.purl.PURLResourceStorage;
import org.purl.accessor.purl.PURLURIResolver;
import org.purl.accessor.domain.DomainResolver;
import org.purl.accessor.AllowableResource;
import org.purl.accessor.ResourceStorage;
import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.DOMXDAAspect;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.*;

import java.util.ArrayList;
import java.util.List;

public class PURLSAccessor extends NKFAccessorImpl {

    private URIResolver purlResolver;
    private URIResolver domainResolver;
    private ResourceStorage purlStorage;
    private AllowableResource purlAllowableResource;

    public PURLSAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);

        purlResolver = new PURLURIResolver();
        domainResolver = new DomainResolver();
        purlStorage = new PURLResourceStorage();
        purlAllowableResource = new PURLAllowableResource(purlStorage, purlResolver, domainResolver);
    }

    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        INKFResponse resp;

        String methodArg = context.getThisRequest().getArgument("method");
        String method = ((StringAspect) context.sourceAspect(methodArg, IAspectString.class)).getString();
        IAspectXDA xdaParam = (IAspectXDA) context.sourceAspect("this:param:param", IAspectXDA.class);

        if (method.equals("POST")) {
            resp = doPost(context, xdaParam);
        } else {
            resp = errorMessage(context, "Invalid Batch PURL HTTP Method: " + method, 400);
        }

        resp.setMimeType("text/xml");
        context.setResponse(resp);
    }

    private INKFResponse doPost(INKFConvenienceHelper context, IAspectXDA xdaParam) throws Exception {
        INKFResponse resp = null;

        if (isValidBatchXML(context, xdaParam)) {
            try {

                int total = getPurlCount(xdaParam);
                // Validate the incoming purls.  Make any modifications and compile a list of rejections
                // TODO DCF The validation done here and in the BatchLoadRequest should be consolidated
                // TODO DCF This is a very roundabout way of compiling the list of failures.  Fix it after a good nights sleep
                StringBuffer rejected = new StringBuffer();
                rejected.append("<failures>");
                xdaParam = checkPURLs(context, xdaParam, rejected);
                rejected.append("</failures>");

                IXDAReadOnly rejectedXDA = ((IAspectXDA)context.transrept(new StringAspect(rejected.toString()), IAspectXDA.class)).getXDA();
                INKFRequest req = context.createSubRequest("active:purl-storage-batch-load");
                req.addArgument("param", xdaParam);
                req.addArgument("currentuser", "data:text/plain," + NKHelper.getUser(context));
                IURRepresentation iur = context.issueSubRequest(req);

                IXDAReadOnly loadResults = ((IAspectXDA)context.transrept(iur, IAspectXDA.class)).getXDA();

                int failed = 0;

                StringBuffer failures = new StringBuffer();

                IXDAReadOnlyIterator it = rejectedXDA.readOnlyIterator("/failures/failure");
                while (it.hasNext()) {
                    it.next();
                    failed++;
                    failures.append(it.toString());
                }

                it = loadResults.readOnlyIterator("/purl-batch/failure");
                while (it.hasNext()) {
                    it.next();
                    failed++;
                    failures.append(it.toString());
                }

                StringBuffer sb = new StringBuffer("<purl-batch total=\"" + total + "\" numCreated=\"");
                sb.append(total-failed);
                sb.append("\" failed=\"" + failed + "\">");
                sb.append(failures);
                sb.append("</purl-batch>");

                resp = context.createResponseFrom(new StringAspect(sb.toString()));

            } catch (PURLException pe) {
                resp = errorMessage(context, pe.getMessage(), pe.getResponseCode());
            } catch (Throwable t) {
                resp = errorMessage(context, "Error Processing Batchload", 500);
            }
        } else {
            resp = errorMessage(context, "Batch Load does not validate against schema.", 400);
        }
        return resp;
    }

    private int getPurlCount(IAspectXDA xdaParam) throws Exception {
        return Integer.valueOf(xdaParam.getXDA().eval("count(/purls/purl)").getStringValue()).intValue();
    }

    private IAspectXDA checkPURLs(INKFConvenienceHelper context, IAspectXDA batchXDA, StringBuffer rejected) throws PURLException {
        IXDA retValue = null;
        try {
            IXDAReadOnlyIterator xdaROItor = batchXDA.getXDA().readOnlyIterator("/purls/purl");
            retValue = batchXDA.getClonedXDA();
            List<String> deleteQueue = new ArrayList();
            while (xdaROItor.hasNext()) {
                try {
                    xdaROItor.next();

                    String result = checkPURL(context, xdaROItor);
                    if (result.startsWith(("<failure>"))) {
                        rejected.append(result);
                        deleteQueue.add(0,xdaROItor.getCurrentXPath());
                        continue;
                    } else {
                        retValue.setText(xdaROItor.getCurrentXPath() + "/@id", result);
                    }

                    result = checkForClone(context, xdaROItor);
                    if (result.startsWith(("<failure>"))) {
                        rejected.append(result);
                        deleteQueue.add(0,xdaROItor.getCurrentXPath());
                        continue;
                    } else if (result.length() > 0) {
                        retValue.replace(((IAspectXDA) context.transrept(new StringAspect(result), IAspectXDA.class)).getXDA(), "/purl", xdaROItor.getCurrentXPath());
                    }

                    result = checkForChain(context, xdaROItor);
                    if (result.startsWith("<failure>")) {
                        rejected.append(result);
                        deleteQueue.add(0,xdaROItor.getCurrentXPath());
                        continue;
                    } else if (result.length() > 0) {
                        DOMXDAAspect targetShimDOMXDA = (DOMXDAAspect) context.transrept(new StringAspect(result), DOMXDAAspect.class);
                        retValue.append(targetShimDOMXDA.getXDA(), "/", xdaROItor.getCurrentXPath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    rejected.append("<failure><message>Unable to process PURL</message>" + xdaROItor.toString() + "</failure>");
                    deleteQueue.add(0,xdaROItor.getCurrentXPath());
                }
            }
            for (String path : deleteQueue) {
                retValue.delete(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DOMXDAAspect((DOMXDA) retValue);
    }


    private String checkPURL(INKFConvenienceHelper context, IXDAReadOnlyIterator xdaROItor) throws Exception {
        String p_id = xdaROItor.getText("@id", true);
		if (xdaROItor.isTrue("target/@url")) {
        	String target = xdaROItor.getText("target/@url", true);
	        if (target.length() > 4000) {
	            return "<failure><message>Target URL too long</message>" + xdaROItor.toString() + "</failure>";
	        }
		}
        if (!purlAllowableResource.allow(context, purlResolver.getURI(p_id))) {
            return "<failure><message>"+purlAllowableResource.getDenyMessage(context, purlResolver.getURI(p_id)) + "</message>" + xdaROItor.toString() + "</failure>";
        }
        return purlResolver.getDisplayName(p_id);
    }

    private String checkForClone(INKFConvenienceHelper context, IXDAReadOnlyIterator xdaROItor) throws Exception {

        if ("clone".equals(xdaROItor.getText("@type", true))) {

            String basepurl = xdaROItor.getText("basepurl/@path", true);
            String baseURI = purlResolver.getURI(basepurl);

            if (!purlStorage.resourceExists(context, baseURI)) {
                return "<failure><message>" + "Invalid cloned PURL: " + basepurl + " for PURL:"
                        + xdaROItor.getText("@id", true) + "</message>" + xdaROItor.toString() + "</failure>";
            }

            IURAspect iur = purlStorage.getResource(context, baseURI);
            IAspectXDA oldPurlXDAOrig = (IAspectXDA) context.transrept(iur, IAspectXDA.class);
            IXDAReadOnly xdaRO = oldPurlXDAOrig.getXDA();
            String type = xdaRO.getText("/purl/type", true);


            StringBuffer sb = new StringBuffer("<purl id=\"");
            sb.append(xdaROItor.getText("@id", true));
            sb.append("\" type=\"");
            sb.append(type);
            sb.append("\">");
            sb.append("<maintainers>");
            IXDAReadOnlyIterator maintainersItor = xdaRO.readOnlyIterator("maintainers/uid");

            while (maintainersItor.hasNext()) {
                maintainersItor.next();
                sb.append("<maintainer id=\"");
                sb.append(maintainersItor.getText(".", true));
                sb.append("\"/>");
            }

            sb.append("</maintainers>");

            if (isNumber(type)) {
                int numType = Integer.valueOf(type).intValue();

                switch (numType) {
                    case 301:
                    case 302:
                    case 307:
                        sb.append("<target url=\"");
                        sb.append(xdaRO.getText("/purl/target/url", true));
                        sb.append("\"/>");
                        break;
                    case 303:
                        sb.append("<seealso url=\"");
                        sb.append(xdaRO.getText("/purl/target/url", true));
                        sb.append("\"/>");
                        break;
                    case 404:
                    case 410:
                        break;
                }
            } else {
                // TODO: Figure out what needs to be done here
            }

            sb.append("</purl>");
            return sb.toString();
        }
        return "";
    }

    private String checkForChain(INKFConvenienceHelper context, IXDAReadOnlyIterator xdaROItor) throws Exception {
        if ("chain".equals(xdaROItor.getText("@type", true))) {
            String basepurl = xdaROItor.getText("basepurl/@path", true);
            String baseURI = purlResolver.getURI(basepurl);
            if (purlStorage.resourceExists(context, baseURI)) {
                return "<target url=\"" + basepurl + "\"/>";

            } else {
                return "<failure><message>" + "Invalid chained PURL: " + basepurl + " for PURL:"
                        + xdaROItor.getText("@id", true) + "</message>" + xdaROItor.toString() + "</failure>";

            }
        }
        return "";
    }


    private boolean isNumber(String number) {
        boolean retValue = true;
        int idx = 0;

        if (number != null) {
            int len = number.length();

            while (retValue && idx < len) {
                retValue = Character.isDigit(number.charAt(idx++));
            }
        }

        return retValue;
    }

    private boolean isValidBatchXML(INKFConvenienceHelper context, IAspectXDA xdaParam) throws NKFException, XPathLocationException {
        INKFRequest req = context.createSubRequest("active:validateRNG");
        req.addArgument("operand", xdaParam);
        req.addArgument("operator", "ffcpl:/schemas/batchPurls.rng");
        req.setAspectClass(IAspectXDA.class);
        IAspectXDA xda = (IAspectXDA) context.issueSubRequestForAspect(req);
        IXDAReadOnly xdaRO = xda.getXDA();
        return xdaRO.isTrue("/b/text()='t'");
    }

    private INKFResponse errorMessage(INKFConvenienceHelper context, String message, int code) throws Exception {
        StringAspect sa = new StringAspect("<error message=\"" + message + "\"/>");
        IURRepresentation iur = NKHelper.setResponseCode(context, sa, code);
        return context.createResponseFrom(iur);
    }
}