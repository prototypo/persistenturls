importPackage(Packages.java.lang);
importPackage(Packages.com.ten60.netkernel.urii.aspect);
importPackage(Packages.org.ten60.netkernel.xml.representation);

req = context.createSubRequest("active:purl-search");
req.addArgument("path", "ffcpl:/purl/something");
req.addArgument("index", "ffcpl:/index/purls");
req.addArgument("query", new StringAspect("<query>foo</query>"));
req.setAspectClass(IAspectXDA);

r=context.issueSubRequest(req);

resp=context.createResponseFrom(r);
resp.setMimeType("text/xml");