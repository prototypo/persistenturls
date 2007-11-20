package org.purl.accessor.util;

import java.util.HashMap;
import java.util.Map;

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

}
