package org.purl.accessor.util.group;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import com.ten60.netkernel.urii.aspect.IAspectBoolean;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GroupHelper {
    public static boolean isValidGroup(INKFConvenienceHelper context, String groupURI) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-group-valid");
            req.setAspectClass(IAspectBoolean.class);
            req.addArgument("uri", groupURI);
            retValue = ((IAspectBoolean) context.issueSubRequestForAspect(req)).isTrue();
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }
    
 /*   public static boolean isGroupMember(INKFConvenienceHelper context, String groupURI, String member) {
        
    }
    
    public static boolean isGroupMaintainer(INKFConvenienceHelper context, String groupURI, String maintainer) {
        boolean retValue = false;
        
        try {
            INKFRequest req=context.createSubRequest("active:purl-storage-query-groupmaintainers");
            req.addArgument("param", new StringAspect("<group><id>" + group + "</id></group>"));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA res = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = res.getXDA().isTrue("/maintainers/uid = '" + user + "'") ||
                       res.getXDA().isTrue("/maintainers/gid = '" + user + "'");
        } catch(Exception e) {
         e.printStackTrace();   
        }
        
        return retValue;       
    } */
}
