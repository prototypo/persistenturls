package org.purl.accessor.group;

import org.purl.accessor.util.AbstractSearchHelper;

import java.util.HashMap;
import java.util.Map;

public class GroupSearchHelper extends AbstractSearchHelper {

    private static Map<String,String> keywordBasisMap = new HashMap<String,String>();
    
    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("name", "/group/name");
        keywordBasisMap.put("maintainers", "/group/maintainers/uid");
        keywordBasisMap.put("members", "/group/members/uid");
        keywordBasisMap.put("g_id", "/group/id");
    }

    public GroupSearchHelper() {
        super(keywordBasisMap);
    }
}
