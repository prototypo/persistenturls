importPackage(Packages.com.ten60.netkernel.urii.aspect);
importPackage(Packages.java.lang);

sa = context.sourceAspect("this:param:param", IAspectString);
System.out.println("It's: " + sa.getString());
resp = context.createResponseFrom(new BooleanAspect(true));
resp.setExpired();
context.setResponse(resp);