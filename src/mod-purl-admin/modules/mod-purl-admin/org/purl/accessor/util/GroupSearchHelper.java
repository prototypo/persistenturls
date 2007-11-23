package org.purl.accessor.util;

import java.util.HashMap;
import java.util.Map;

public class GroupSearchHelper extends AbstractSearchHelper {

    private static Map<String,String> keywordBasisMap = new HashMap<String,String>();
    
    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("name", "/group/name");
        keywordBasisMap.put("maintainers", "/group/maintainers/uid");
        keywordBasisMap.put("writers", "/group/writers/uid");
        keywordBasisMap.put("id", "/group/id");
    }

    public GroupSearchHelper() {
        super(keywordBasisMap);
    }
}