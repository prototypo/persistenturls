package org.purl.accessor.user;

import org.purl.accessor.util.AbstractSearchHelper;

import java.util.HashMap;
import java.util.Map;

public class UserSearchHelper extends AbstractSearchHelper {

    private static Map<String,String> keywordBasisMap = new HashMap<String,String>();
    
    static {
        // TODO: Turn this into a configuration file
        keywordBasisMap.put("name", "/user/name");
        keywordBasisMap.put("affiliation", "/user/affiliation");
        keywordBasisMap.put("email", "/user/email");
        keywordBasisMap.put("userid", "/user/id");
    }

    public UserSearchHelper() {
        super(keywordBasisMap);
    }
}
