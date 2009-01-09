req = context.createSubRequest();
req.setURI("active:xslt");
uris = [ "operator" : "../support/resources/act_to_html.xsl",
         "operand" : "../support/resources/lear.xml" ]
uris.each { key, value -> req.addArgument(key,value) }
result = context.issueSubRequest(req);
resp = context.createResponseFrom(result);
context.setResponse(resp);