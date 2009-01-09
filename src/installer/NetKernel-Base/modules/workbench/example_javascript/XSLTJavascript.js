//Create and issue XSLT request
sr=context.createSubRequest();
sr.setURI("active:xslt");
sr.addArgument("operand","../support/resources/lear.xml");
sr.addArgument("operator","../support/resources/act_to_html.xsl");
rep=context.issueSubRequest(sr);
    
//Create response
resp=context.createResponseFrom(rep);

//Issue response
context.setResponse(resp)