package org.purl.accessor.user;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.XPathLocationException;
import org.purl.accessor.util.URIResolver;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectBoolean;

public class UserHelper {
    private static URIResolver userResolver = new UserResolver();
    
    public static boolean isValidUser(INKFConvenienceHelper context, String userURI) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-user-valid");
            req.setAspectClass(IAspectBoolean.class);
            req.addArgument("uri", userURI);
            retValue = ((IAspectBoolean) context.issueSubRequestForAspect(req)).isTrue();
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }
    
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
    
    public static boolean isAdminUser(INKFConvenienceHelper context, String user) {
        boolean retValue = false;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-query-user");
            req.addArgument("uri", userResolver.getURI(user));
            req.setAspectClass(IAspectXDA.class);
            IAspectXDA userXDA = (IAspectXDA) context.issueSubRequestForAspect(req);
            retValue = userXDA.getXDA().isTrue("/user[@admin='true']");
        } catch (NKFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 

        return retValue;
    }
    
    public static IURRepresentation getGroupsForUser(INKFConvenienceHelper context, String user) {
        IURRepresentation retValue = null;
        
        try {
            INKFRequest req = context.createSubRequest("active:purl-storage-groups-for-user");
            req.addArgument("uri", userResolver.getURI(user));
            retValue = context.issueSubRequest(req);
        } catch(NKFException nfe) {
            nfe.printStackTrace();
        }
        
        return retValue;
    }
}
