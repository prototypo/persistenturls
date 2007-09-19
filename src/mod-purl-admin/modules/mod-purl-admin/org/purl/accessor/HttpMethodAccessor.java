package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class HttpMethodAccessor extends NKFAccessorImpl {

    private static Map<String,String> typeActiveURIMap = null;
    private static Map<String,String> methodMethodMap = null;

    static {
        typeActiveURIMap = new HashMap<String,String> ();
        typeActiveURIMap.put("domain", "active:purl-domain");
        typeActiveURIMap.put("group", "active:purl-group");
        typeActiveURIMap.put("user", "active:purl-user");
        typeActiveURIMap.put("purl", "active:purl");
        typeActiveURIMap.put("purls", "active:purls");

        methodMethodMap = new HashMap<String,String> ();
        methodMethodMap.put("GET", "http:GET");
        methodMethodMap.put("POST", "http:POST");
        methodMethodMap.put("PUT", "http:PUT");
        methodMethodMap.put("DELETE", "http:DELETE");
    }


    public HttpMethodAccessor() {
        super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
    }

    @Override
    public void processRequest(INKFConvenienceHelper context) throws Exception {
        String method = (((IAspectString)context.sourceAspect("literal:method", IAspectString.class)).getString());
        String type = context.getThisRequest().getArgument("type");
        String path = context.getThisRequest().getArgument("path");

        System.out.println("CWU: " + context.getCWU());
        System.out.println("URI: " + context.getThisRequest().getURI());
        System.out.println("param:" + context.exists("this:param"));
        System.out.println("param:param" + context.exists("this:param:param"));
        System.out.println("param:param2" + context.exists("this:param:param2"));

        String uriPrefix = typeActiveURIMap.get(type);
        StringBuffer sb = new StringBuffer(uriPrefix);
        sb.append("+method@http:");
        sb.append(method);
        sb.append("+path@");
        sb.append(path);

        System.out.println(sb.toString());

        INKFRequest request = context.createSubRequest();
        request.setURI(uriPrefix);
        request.addArgument("method", methodMethodMap.get(method));
        request.addArgument("path", path);

        if(context.exists("this:param:param")) {
            request.addArgument("param", context.source("this:param:param"));
        }

        if(context.exists("this:param:param2")) {
            request.addArgument("param", context.source("this:param:param2"));
        }

        IURRepresentation res=context.issueSubRequest(request);
        INKFResponse resp = context.createResponseFrom(res);
        context.setResponse(resp);
    }
}
