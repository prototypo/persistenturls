#Create module to import java packages into
module Java
	include_package 'java.lang'
	include_package 'org.ten60.netkernel.layer1.representation'
end

#Print hello world from Ruby and Java
print "Hello From Ruby\n"
Java::System.out.println("Hello From Java Too\n")

#Construct a string aspect and return
sa= Java::StringAspect.new "Hello World"
resp=$context.createResponseFrom(sa)
resp.setMimeType("text/plain");
