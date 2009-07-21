importPackage(Packages.java.lang);
importPackage(Packages.com.ten60.netkernel.urii.aspect);
importPackage(Packages.org.ten60.netkernel.xml.representation);

req = context.createSubRequest("active:template");
req.addArgument("path", "ffcpl:/template/something");
req.setAspectClass(IAspectXDA);

r=context.issueSubRequest(req);

resp=context.createResponseFrom(r);
resp.setMimeType("text/xml");