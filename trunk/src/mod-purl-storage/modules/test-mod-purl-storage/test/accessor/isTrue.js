importPackage(Packages.com.ten60.netkernel.urii.aspect);
importPackage(Packages.java.lang);

ba = context.sourceAspect("this:param:param", IAspectBoolean);
resp = context.createResponseFrom(ba);
resp.setExpired();
context.setResponse(resp);
