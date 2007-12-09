package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;

public class DomainResolver extends URIResolver {
    @Override
    public String getURI(INKFConvenienceHelper context) {
        String retValue = null;

        try {
            String path = NKHelper.getArgument(context, "path");
            if(path.startsWith("ffcpl:/")) {
                path = path.substring(19); // Skip over ffcpl:/admin/domain
            }
            
            retValue = getURI(path).toLowerCase();
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }

        return retValue;
    }

    @Override
    public String getURI(String id) {
        StringBuffer sb = new StringBuffer("ffcpl:/domain");
        if(!id.startsWith("/")) {
            sb.append("/");
        }
        
        sb.append(id);
        return sb.toString();
    }

}
