package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.aspect.IAspectBoolean;

public class UserHelper {
    public static boolean isValidUser(INKFConvenienceHelper context, String userURI) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("purl-storage-user-valid");
            req.setAspectClass(IAspectBoolean.class);
            req.addArgument("uri", userURI);
            retValue = ((IAspectBoolean) context.issueSubRequestForAspect(req)).isTrue();
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }
}
