package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.purl.accessor.ResourceFilter;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

/***
 * A reusable class for applying XSLT transformations to a 
 * resource as it is being returned.
 *  
 * @author brian
 *
 */
public class XSLTResourceFilter implements ResourceFilter {
	
	private String filter;
	
	public XSLTResourceFilter(String filter) {
		this.filter = filter;
	}

    public IURAspect filter(INKFConvenienceHelper context, IURAspect iur) {
        IURAspect retValue = null;

        try {
            INKFRequest req = context.createSubRequest();
            req.setURI("active:xslt");
            req.addArgument("operand", iur);
            req.addArgument("operator", filter);
            req.setAspectClass(IAspectString.class);
            retValue = context.issueSubRequestForAspect(req);
        } catch(NKFException nfe) {
            // TODO: return something other than the raw user
            nfe.printStackTrace();
        }

        return retValue;
    }

}
