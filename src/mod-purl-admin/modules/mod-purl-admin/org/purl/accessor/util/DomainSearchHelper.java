package org.purl.accessor.util;

import java.util.HashMap;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;

public class DomainSearchHelper extends AbstractSearchHelper {

    private static Map<String,String> keywordBasisMap = new HashMap<String,String>();
    
    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("name", "/domain/name");
        keywordBasisMap.put("maintainers", "/domain/maintainers/uid");
        keywordBasisMap.put("writers", "/domain/writers/uid");
        keywordBasisMap.put("id", "/domain/id");
    }

    public DomainSearchHelper() {
        super(keywordBasisMap);
    }
    
    public String processKeyword(INKFConvenienceHelper context, String key, String value) {
        String newValue = value;
        
        if(key.equals("id") && newValue.startsWith("/")) {
            newValue = newValue.substring(1);
        }
        
        return super.processKeyword(context, key, newValue);
    }

}
