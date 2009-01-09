package org.ten60.netkernel.tutorial;
import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.StringAspect;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

public class HelloWorld extends NKFAccessorImpl
{
  // Call super class constructor and indicate that this
  // accessor is threadsafe and will only accept SOURCE requests
  public HelloWorld()
  { super(SAFE_FOR_CONCURRENT_USE, INKFRequestReadOnly.RQT_SOURCE);
  }

  public void processRequest(INKFConvenienceHelper context) throws Exception
  {
    // Create an immutable StringAspect wrapper for our text message
    IURAspect aspect = new StringAspect("Hello World");

   // Create a response object containing the StringAspect
    INKFResponse response = context.createResponseFrom(aspect);

    // Set metadata indicating the MIME type of the response
    response.setMimeType("text/plain");

    // Set metadata indicating the response can be cached
    response.setCacheable();

    // When the method returns the response is sent to the client
  }
}
