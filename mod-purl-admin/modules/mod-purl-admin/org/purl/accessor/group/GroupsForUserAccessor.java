package org.purl.accessor.group;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.netkernel.xml.representation.DOMXDAAspect;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.DOMXDA;
import org.ten60.netkernel.xml.xda.IXDA;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.BooleanAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GroupsForUserAccessor extends NKFAccessorImpl {

    private static StringAspect TEMPLATE_ASPECT = new StringAspect("<groups></groups>");
    
    public GroupsForUserAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE|INKFRequestReadOnly.RQT_SINK|INKFRequestReadOnly.RQT_EXISTS|INKFRequestReadOnly.RQT_DELETE);
    }

    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        // TODO : Cache results for a user
        int requestType = context.getThisRequest().getRequestType();

        String user = context.getThisRequest().getArgument("user").substring(5);
        String file = "ffcpl:/groups-for-user/" + user;
        
        INKFResponse resp = null;

        try {
            INKFRequest req = context.createSubRequest("active:lock");
            req.addArgument("operand", file);
            context.issueSubRequest(req);

            switch(requestType) {
            case INKFRequestReadOnly.RQT_EXISTS:
                BooleanAspect ba = new BooleanAspect(context.exists(file));
                resp = context.createResponseFrom(ba);
                context.setResponse(resp);
                break;
            case INKFRequestReadOnly.RQT_SOURCE:
                IURRepresentation iur = context.source(file);
                resp = context.createResponseFrom(iur);
                resp.setMimeType("text/xml");
                break;        
            case INKFRequestReadOnly.RQT_SINK:
                IAspectXDA groupFile = null;
                IAspectXDA xda = (IAspectXDA) context.sourceAspect(INKFRequestReadOnly.URI_SYSTEM, IAspectXDA.class);
                
                if(context.exists(file)) {
                    groupFile = (IAspectXDA) context.sourceAspect(file, IAspectXDA.class);
                } else {
                    groupFile = (IAspectXDA) context.transrept(TEMPLATE_ASPECT, IAspectXDA.class);
                }
                
                String group = xda.getXDA().getText("/groups/group/@id", true);
                if(!groupFile.getXDA().isTrue("/groups/group[@id=\"" + group +"\"]")) {
                    IXDA modGroupFile = groupFile.getClonedXDA();
                    modGroupFile.append(xda.getXDA(), "/groups/group", "/groups");
                    context.sinkAspect(file, new DOMXDAAspect((DOMXDA)modGroupFile));
                }
                
                break;
            case INKFRequestReadOnly.RQT_DELETE:
                context.delete(file);
                resp = context.createResponseFrom(new BooleanAspect(true));
                break;                       
            }

            if(resp!=null) {
                context.setResponse(resp);
            }
        } finally {
            INKFRequest req = context.createSubRequest("active:unlock");
            req.addArgument("operand", file);
            context.issueSubRequest(req);
        }
    }

}
