importPackage(Packages.org.ten60.netkernel.xml.representation);
importPackage(Packages.java.lang);
var debug=false;

//Source application deployment resource
xoa=context.sourceAspect("ffcpl:/etc/netkernel-control-rollback-previous-state.xml", IAspectXmlObject);

//Rollback system to last good state
context.sinkAspect("netkernel:module-list", xoa);

//Hot Restart System - this is asynchronous and will not commence until the completion of this process.
context.source("active:hotRestart");

//Return response
resp=context.createResponseFrom(xoa);
resp.setMimeType("text/xml");
context.setResponse(resp);
