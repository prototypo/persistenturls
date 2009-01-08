#!/bin/sh

# Compile the load test code.
# TODO: Move this to ant.
javac -cp lib/ant-junit.jar:lib/com.noelios.restlet.ext.httpclient_3.1.jar:lib/com.noelios.restlet.jar:lib/org.apache.commons.codec.jar:lib/org.apache.commons.httpclient.jar:lib/org.apache.commons.logging.jar:lib/org.restlet.jar:lib/resolver.jar:lib/serializer.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/xmlunit-1.1.jar: org/purl/test/MassivePurlLoadTest.java 

# Run the load test with a 30 second delay.
java -cp lib/ant-junit.jar:lib/com.noelios.restlet.ext.httpclient_3.1.jar:lib/com.noelios.restlet.jar:lib/org.apache.commons.codec.jar:lib/org.apache.commons.httpclient.jar:lib/org.apache.commons.logging.jar:lib/org.restlet.jar:lib/resolver.jar:lib/serializer.jar:lib/xercesImpl.jar:lib/xml-apis.jar:lib/xmlunit-1.1.jar: org.purl.test.MassivePurlLoadTest -d 30000
