import org.ten60.netkernel.xml.xda.*;
import org.ten60.netkernel.xml.representation.*;
import org.ten60.netkernel.xml.util.*;
import groovy.xml.*;

builder = DOMBuilder.newInstance()
        
root = builder.a(a:5, b:7) {
       b1('textb1') { c1('textc1') }
       b2('textb2')
       b3(a:7)  }
        
d =XMLUtils.newDocument();
d.appendChild(d.importNode(root,true));

resp = context.createResponseFrom(new DOMAspect(d))
resp.setMimeType("text/xml")
context.setResponse(resp)