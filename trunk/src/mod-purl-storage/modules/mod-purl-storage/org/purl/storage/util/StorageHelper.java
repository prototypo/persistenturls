package org.purl.storage.util;

import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.IXDA;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;

import java.util.Map;
import java.util.HashMap;

public class StorageHelper {
	/**
	 * This method cleans up URIs associated with PURL data
	 * prior to storage in the database.
	 * 
	 * @param url
	 * @return cleaned up version of the url
	 */
	public static String convertForStorage(String url) {
		String retValue = url;
		
		// Replace single quotations with double single
		// quotations to avoid SQL problems
		if(retValue.contains("'")) {
			retValue = retValue.replaceAll("'", "''");
		}
		
		// We need to escape + signs in URI values to avoid
		// confusing NetKernel active URI handling
		if(retValue.contains("%2B")) {
			retValue = retValue.replaceAll("%2B", "+");
		}
		
		return retValue;
	}



    private static Map<String, String> generateMaintainerMap(INKFConvenienceHelper context,  String key, IXDAReadOnlyIterator maintainerItor) throws Exception {
        Map<String, String> maintainerMap = new HashMap();
        //groupMaintainerItor = paramXDA.readOnlyIterator("//maintainers/gid");

        while(maintainerItor.hasNext()) {
            maintainerItor.next();
            String maintainer = maintainerItor.getText(".", true);
            String z_id = maintainerMap.get(maintainer);

            if(z_id == null) {
                INKFRequest req = context.createSubRequest("active:purl-storage-query-" + key);
                req.addArgument("uri", "ffcpl:/" + key + "/" + maintainer);
                req.setAspectClass(IAspectXDA.class);
                IAspectXDA maintainerXDA = (IAspectXDA)context.issueSubRequestForAspect(req);
                z_id = maintainerXDA.getXDA().getText("/" + key + "/z_id", true);
                maintainerMap.put(maintainer, z_id);
            }
        }

        return maintainerMap;
    }


    public static Map<String, String> generateGroupMap(INKFConvenienceHelper context, IXDA paramXDA) throws Exception {
        return generateMaintainerMap(context, "group", paramXDA.readOnlyIterator("//gid"));
    }

    public static Map<String, String> generateUserMap(INKFConvenienceHelper context, IXDA paramXDA) throws Exception {
        return generateMaintainerMap(context, "user", paramXDA.readOnlyIterator("//uid"));
    }
}
