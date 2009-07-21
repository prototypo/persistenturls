package org.purl.accessor.domain;

import java.util.HashMap;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.purl.accessor.util.AbstractSearchHelper;

public class DomainSearchHelper extends AbstractSearchHelper {

    private static Map<String,String> keywordBasisMap = new HashMap<String,String>();
    
    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("name", "/domain/name");
        keywordBasisMap.put("maintainers", "/domain/maintainers/uid");
        keywordBasisMap.put("writers", "/domain/writers/uid");
        keywordBasisMap.put("d_id", "/domain/id");
    }

    public DomainSearchHelper() {
        super(keywordBasisMap);
    }
    

}
