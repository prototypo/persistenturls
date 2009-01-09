importClass(Packages.org.ten60.netkernel.layer1.representation.StringAspect);

aspect = context.sourceAspect("ffcpl:/src/helloworld.txt", StringAspect);
message = aspect.getString();
message = message.concat(" from JavaScript");
aspect = new StringAspect(message);
response = context.createResponseFrom(aspect);
response.setMimeType("text/plain");
response.setCacheable();
