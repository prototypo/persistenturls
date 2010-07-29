import com.ten60.netkernel.urii.aspect.*

req=context.createSubRequest("active:sqlQuery");
req.addArgument("operand", new StringAspect("<sql>select top 1 current_timestamp as ts from users</sql>"));

xda=context.issueSubRequest(req).getAspect()

def date = xda.getXDA().getText("/results/row/TS", true)

resp = context.createResponseFrom(new StringAspect(date))
resp.setExpired()