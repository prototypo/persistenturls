importPackage(Packages.org.ten60.netkernel.xml.representation);
importPackage(Packages.java.lang);
var debug=false;

//Source application deployment resource
xoa=context.sourceAspect("ffcpl:/etc/appDeployment.xml", IAspectXmlObject);
deployment=new XML(xoa.getXmlObject());

//Source system module-list
xoa=context.sourceAspect("netkernel:module-list", IAspectXmlObject);
mods=new XML(xoa.getXmlObject());

//Save rollback state so we can return to a known good state
context.sinkAspect("ffcpl:/etc/netkernel-control-rollback-previous-state.xml", xoa);

//Remove previous matching modules
i=0;
mods=mods.module;
while(i<mods.length())
{	incr=true;
	s=new String(mods[i]);
	for each ( var match in deployment..match)
	{	if(debug)
		{	System.out.println("Match: "+match+" Module: "+mods[i].toString());
		}
		if(s.indexOf(match) > 0)
		{	System.out.println("Removing Module... "+mods[i].toString());
			delete mods[i];
			incr=false;
		}
	}
	if(incr) i++;
}

if(debug)
{	System.out.println("Modules: \n"+mods.toString());
}

//Register new modules
for each ( var uri in deployment..uri)
{	System.out.println("Installing Module... "+uri.toString());
	mod=<module>{uri.toString()}</module>;
	mods=mods + mod;
}

//Finally put the module list into a root element and create aspect
mods=<modules>{mods}</modules>;
xoa=new XmlObjectAspect(mods.getXmlObject());

//Update the master module list
context.sinkAspect("netkernel:module-list", xoa);

//Hot Restart System - this is asynchronous and will not commence until the completion of this process.
context.source("active:hotRestart");

//Return response
resp=context.createResponseFrom(xoa);
resp.setMimeType("text/xml");
context.setResponse(resp);
