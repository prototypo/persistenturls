#######################################
# Test Harness for occurs.rb
# (C) 2006 PJRodgers, 1060 Research Ltd
#######################################
require 'java'

req=$context.createSubRequest "active:ruby"
req.addArgument "operator", "occurs.rb" 
req.addArgument "operand",  "occurance-test.txt"
result=$context.issueSubRequest req

resp=$context.createResponseFrom result
