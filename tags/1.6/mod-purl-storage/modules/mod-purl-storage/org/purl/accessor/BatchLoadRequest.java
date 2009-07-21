package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.IURRepresentation;

/**
 *
 */
public class BatchLoadRequest {
    private INKFConvenienceHelper context;
    private IAspectXDA xdaParam;
    private String currentUser;


    private Map<String, String> maintainerMap = new HashMap<String, String>();

    public BatchLoadRequest(INKFConvenienceHelper context) throws Exception {
        // TODO: Authenticate the batch format
        // TODO: THIS IS DANGEROUS. WE NEED TO DETERMINE WHAT THE TRANSACTIONAL PROPERTIES OF BATCH LOADS ARE.


        this.context = context;
        parseParams();
        initializeMaintainers();
        currentUser = ((IAspectString) context.sourceAspect("this:param:currentuser", IAspectString.class)).getString();
    }

    public String getCurrentUser() throws Exception {
        return currentUser;
    }

    public Set<String> getMaintainers() {
        return maintainerMap.keySet();
    }

    public String getMaintainerID(String maintainer) {
        return maintainerMap.get(maintainer);
    }

    public int getPurlCount() throws Exception {
        return Integer.valueOf(xdaParam.getXDA().eval("count(/purls/purl)").getStringValue()).intValue();
    }

    public IXDAReadOnly getPurlXDA() {
        return xdaParam.getXDA();
    }

    public INKFConvenienceHelper getContext() {
        return context;
    }

    private void parseParams() throws Exception {
        IURRepresentation iur = context.source("this:param:param");
        INKFRequest req = null;
        INKFResponse resp = null;

        // If the batch file contains any apostrophes, escape them for processing below
        req = context.createSubRequest("active:SQLEscapeXML");
        req.addArgument("operand", iur);
        iur = context.issueSubRequest(req);

        xdaParam = (IAspectXDA) context.transrept(iur, IAspectXDA.class);
    }

    // We'll maintain a local copy of any maintainer info to avoid unnecessary
    // kernel request scheduling
    private void initializeMaintainers() throws Exception {
        String currentUser = getCurrentUser();
        parseMaintainers(false);
        parseMaintainers(true);

        if (!maintainerMap.containsKey(currentUser)) {
            addMaintainer(currentUser, false);
        }
    }

    private void parseMaintainers(boolean group) throws Exception {
        String path = group ? "//maintainers/gid" : "//maintainers/uid";
        IXDAReadOnlyIterator maintainerItor = xdaParam.getXDA().readOnlyIterator(path);

        while (maintainerItor.hasNext()) {
            maintainerItor.next();
            String maintainer = maintainerItor.getText(".", true);
            String z_id = maintainerMap.get(maintainer);

            if (z_id == null) {
                addMaintainer(maintainer, group);
            }
        }
    }

    private boolean addMaintainer(String maintainer, boolean group) {
        try {
            String type = group ? "group" : "user";
            INKFRequest req = context.createSubRequest("active:purl-storage-query-" + type);
            req.addArgument("uri", "ffcpl:/" + type + "/" + maintainer);
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA maintainerXDA = (IAspectXDA) context.issueSubRequestForAspect(req);
            String z_id = maintainerXDA.getXDA().getText("/" + type + "/z_id", true);
            maintainerMap.put(maintainer, z_id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
