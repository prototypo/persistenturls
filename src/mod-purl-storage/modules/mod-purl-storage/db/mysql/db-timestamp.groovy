import com.ten60.netkernel.urii.aspect.*

req=context.createSubRequest("active:sqlQuery");
req.addArgument("operand", new StringAspect("<sql>select current_timestamp as ts</sql>"));

xda=context.issueSubRequest(req).getAspect()

def date = xda.getXDA().getText("/results/row/ts", true)

resp = context.createResponseFrom(new StringAspect(date))
resp.setExpired()