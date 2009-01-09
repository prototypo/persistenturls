package org.ten60.netkernel.tutorial;
import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

public class HelloWorldSubRequest extends NKFAccessorImpl
{
  // Call super class constructor and indicate that this
  // accessor is threadsafe and will only accept SOURCE requests
  public HelloWorldSubRequest()
  { super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
  }

  public void processRequest(INKFConvenienceHelper context) throws Exception
  {
    // Create a request to obtain a portion of the message
    INKFRequest subrequest = context.createSubRequest();

    // Set the logical address of the message resource
    subrequest.setURI("ffcpl:/src/helloworld.txt");
    
    // Set the type of resource representation desired
    subrequest.setAspectClass(StringAspect.class);

    // Issue the request into the logical address space
    IURAspect aspect = context.issueSubRequestForAspect(subrequest);

    // Unwrap the text message
    String message = ((StringAspect)aspect).getString();

    // Append our portion of the final message
    message = message.concat(" from Java");

    // Create an immutable StringAspect wrapper for our text message
    aspect = new StringAspect(message);

    // Create a response object containing the StringAspect
	  INKFResponse response = context.createResponseFrom(aspect);

	  // Set metadata indicating the MIME type of the response
	  response.setMimeType("text/plain");

	  // Tell NetKernel that this can be kept in the internal cache
	  response.setCacheable();

  // When the method returns the response is sent to the client
  }
}