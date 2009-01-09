#######################################
# Count the occurance of words
# in the supplied operand argument
# (C) 2006 PJRodgers, 1060 Research Ltd
#######################################
require 'java'

module Java
	include_package 'org.ten60.netkernel.layer1.representation'	
end

#Source operand as string
sa=$context.sourceAspect("this:param:operand", Java::JavaClass.for_name("com.ten60.netkernel.urii.aspect.IAspectString"))

#Generate Frequency Table
freq = Hash.new(0)
sa.getString.split(/\W+/).each do |word|
   freq[word] += 1
end

#Serialize to pretty printed XML
s="<occurance>\n"
for word in freq.keys.sort!
s+=<<END_OF_STRING
	<item>
		<word>#{word}</word>
		<freq>#{freq[word]}</freq>
	</item>
END_OF_STRING
end
s+="</occurance>"

#Construct a string aspect and return
sa= Java::StringAspect.new s
resp=$context.createResponseFrom sa
resp.setMimeType "text/xml"