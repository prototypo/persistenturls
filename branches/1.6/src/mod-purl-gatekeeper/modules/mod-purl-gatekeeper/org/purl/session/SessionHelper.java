package org.purl.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import com.ten60.netkernel.urii.aspect.IAspectBoolean;

public class SessionHelper {

    private static Map<String, Set<String>> methodURIRequiresSessionMap
    = new HashMap<String, Set<String>>();
    private static Map<String, Set<String>> methodURINoSessionMap
    = new HashMap<String, Set<String>>();

    static {
        // TODO: Read this in from a configuration file
        Set<String> uriSet = new HashSet<String>();
        uriSet.add("ffcpl:/user"); 
        uriSet.add("ffcpl:/admin/pending");
        methodURIRequiresSessionMap.put("GET", uriSet);
        uriSet = new HashSet<String>();
        uriSet.add("ffcpl:/admin/purl");
        uriSet.add("ffcpl:/admin/purls");
        uriSet.add("ffcpl:/admin/group");
        uriSet.add("ffcpl:/admin/domain");        
        methodURIRequiresSessionMap.put("POST", uriSet);
        uriSet = new HashSet<String>(); 
        uriSet.add("ffcpl:/admin/user");
        uriSet.add("ffcpl:/admin/purl");
        uriSet.add("ffcpl:/admin/purls");
        uriSet.add("ffcpl:/admin/group");
        uriSet.add("ffcpl:/admin/domain");        
        methodURIRequiresSessionMap.put("PUT", uriSet);
        uriSet = new HashSet<String>();
        uriSet.add("ffcpl:/admin/user");
        uriSet.add("ffcpl:/admin/purl");
        uriSet.add("ffcpl:/admin/group");
        uriSet.add("ffcpl:/admin/domain");        
        methodURIRequiresSessionMap.put("DELETE", uriSet);
        // GETs that don't require Sessions
        uriSet = new HashSet<String>();
        uriSet.add("ffcpl:/purl");
        uriSet.add("ffcpl:/admin/user");        
        uriSet.add("ffcpl:/admin/targetpurl");
        uriSet.add("ffcpl:/admin/targetpurls");
        uriSet.add("ffcpl:/admin/group");
        uriSet.add("ffcpl:/admin/domain");        
        methodURINoSessionMap.put("GET", uriSet);
        // POSTs that don't require Sessions
        uriSet = new HashSet<String>();
        uriSet.add("ffcpl:/admin/user");
        methodURINoSessionMap.put("POST", uriSet);
        // PUTs that don't require Sessions                
        uriSet = new HashSet<String>();        
        methodURINoSessionMap.put("PUT", uriSet);
        // DELETEs that don't require Sessions                        
        uriSet = new HashSet<String>();        
        methodURINoSessionMap.put("DELETE", uriSet);                
    }

    public static boolean requiresSession(INKFConvenienceHelper context, String method, String uri) {
        boolean retValue = true;

        Set<String> set = methodURIRequiresSessionMap.get(method);

        if(set != null) {
            retValue = set.contains(uri);
        } else {
            // TODO: Handle this
        }

        return retValue;
    }
    
    public static boolean validUser(INKFConvenienceHelper context, String user) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-user-valid");
            req.addArgument("uri", "ffcpl:/user/" + user); 
            req.setAspectClass(IAspectBoolean.class);
            IAspectBoolean result = (IAspectBoolean) context.issueSubRequestForAspect(req);
            retValue = result.isTrue();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
    
    public static boolean isUserAdmin(INKFConvenienceHelper context, String user) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-query-user");
            req.addArgument("uri", "ffcpl:/user/" + user); 
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA result = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = result.getXDA().isTrue("/user/@admin='true'");
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return retValue;
    }
}
