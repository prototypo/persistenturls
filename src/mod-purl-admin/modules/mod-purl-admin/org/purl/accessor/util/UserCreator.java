package org.purl.accessor.util;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

/**
 * A ResourceCreator instance to fill out a user instance
 * from parameters that were passed in.
 *
 * @author brian
 *
 */
public class UserCreator implements ResourceCreator {

    public IURAspect createResource(INKFConvenienceHelper context, IAspectNVP params) throws NKFException {
        IAspectXDA config = (IAspectXDA)  context.sourceAspect("ffcpl:/etc/PURLConfig.xml", IAspectXDA.class);
        IXDAReadOnly configXDA = config.getXDA();
        StringAspect retValue = null;

        try {
            boolean createUser = false;

            if(configXDA.isTrue("/purl-config/allowUserAutoCreation")) {
                createUser = true;
            }

            StringBuffer sb = new StringBuffer( "<user admin=\"false\">" );
            sb.append("<id>");
            if(params.getValue("id") != null) {
            	sb.append(params.getValue("id"));
            } else {
            	sb.append(NKHelper.getLastSegment(context));
            }
            sb.append("</id>");
            sb.append("<name>");
            String name = DataHelper.cleanseInput(params.getValue("name"));
            sb.append(name);
            sb.append("</name>");
            sb.append("<affiliation>");
            sb.append(DataHelper.cleanseInput(params.getValue("affiliation")));
            sb.append("</affiliation>");
            sb.append("<email>");
            sb.append(params.getValue("email"));
            sb.append("</email>");
            sb.append("<password>");
            sb.append(NKHelper.getMD5Value(context, params.getValue("passwd")));
            sb.append("</password>");
            sb.append("<hint>");
            sb.append(DataHelper.cleanseInput(params.getValue("hint")));
            sb.append("</hint>");
            sb.append("<justification>");
            sb.append(DataHelper.cleanseInput(params.getValue("justification")));
            sb.append("</justification>");
            sb.append("</user>");
            retValue = new StringAspect(sb.toString());
        } catch (XPathLocationException e) {
            // TODO What should the error code be?
            throw new PURLException("Unable to create user: " + NKHelper.getLastSegment(context), 500);
        }

        return retValue;
    }
}
