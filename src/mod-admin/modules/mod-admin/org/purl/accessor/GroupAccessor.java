package org.purl.accessor;

import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import com.ten60.netkernel.urii.aspect.StringAspect;

public class GroupAccessor extends NKFAccessorImpl {
	public GroupAccessor() {
		super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);

	}

	@Override
	public void processRequest(INKFConvenienceHelper context) throws Exception {
		String path=context.getThisRequest().getArgument("path");
		INKFResponse resp = null;

		String[] parts=path.split("/");
		for(int i = 0; i < parts.length; i++) {
			System.out.println(parts[i]);
		}

		resp = context.createResponseFrom(new StringAspect("<hello>group</hello>"));
		resp.setMimeType("text/xml");
		context.setResponse(resp);
	}

}
