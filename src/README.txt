NetKernel Modules
-----------------
0) This write up assumes that you have NetKernel 3.2 installed somewhere. For now, please make sure that the version you have installed matches the following md5 hash:

MD5 (/Users/brian/Desktop/1060-NetKernel-SE-DK-3.2.0.jar) = c01a30fbe5844b9ef5bd4291cec058f2

1) Next do an svn checkout if you haven't done one already.

2) You will need to customize one property in the build.xml. In the "init" target, please point netkernel.home.dir to wherever you have NetKernel installed.

3) Once you have pointed the Ant build file to the correct place for NetKernel, you should be able to do an "ant deploy".

4) We also need to hand edit two files in your NetKernel installation to bootstrap the rest of the templates. We need to expose the mod-control module to NetKernel and the front end fulcrum.

Please edit the etc/deployedModules.xml in your NetKernel installation directory to include the following line (replacing <svn-co-dir> with wherever you checked it out):

<module>file:<svn-co-dir>/zepheira/trunk/customers/OCLC/PURL2/src/deploy/mod-control-0.0.1.jar</module>

Then, edit modules/mod-fulcrum-frontend/module.xml to include the following line (look for the section that says "Add your modules below here..." for a good place to put it):

<import>
    <uri>urn:org:purl:session</uri>
</import>

At this point, you should be able to start up NetKernel. Ignore the following error, but you shouldn't see anything else wrong at this point:

WARNING <ex>
    <id>com.ten60.netkernel.util.NetKernelException</id>
    <message>Problems with Module Linking</message>
    <stack>
        <level>com.ten60.netkernel.module.ModuleManager.reparseModules() line:210</level>
        <level>com.ten60.netkernel.module.ModuleManager.start() line:106</level>
        <level>com.ten60.netkernel.container.Container.startComponent() line:340</level>
        <level>com.ten60.netkernel.container.Container.startComponents() line:293</level>
    </stack>
    <ex>
        <id>failed to parse module</id>
        <requestid>urn:org:ten60:netkernel:fulcrum:frontend</requestid>
        <stack>
            <level>com.ten60.netkernel.module.ModuleDefinition.parseMappings() line:320</level>
            <level>com.ten60.netkernel.module.ModuleManager.reparseModules() line:207</level>
            <level>com.ten60.netkernel.module.ModuleManager.start() line:106</level>
            <level>com.ten60.netkernel.container.Container.startComponent() line:340</level>
        </stack>
        <ex>
            <id>imported module not found </id>
            <message>imported module not found </message>
            <requestid>urn:org:purl:session</requestid>
            <stack>
                <level>com.ten60.netkernel.module.ModuleManager.getModule() line:275</level>
                <level>com.ten60.netkernel.module.ModuleDefinition.parseMappings() line:253</level>
                <level>com.ten60.netkernel.module.ModuleManager.reparseModules() line:207</level>
                <level>com.ten60.netkernel.module.ModuleManager.start() line:106</level>
            </stack>
        </ex>
    </ex>
</ex>

5) At this point, we can kick NetKernel to read in the modules to deploy from our build. These are found in the mod-control-0.0.1.jar as etc/appDeployment.xml. You will not have to perform this step again until you update the modules. Keep in mind that if you change the definition of a module and want to be able to rollback (see below), you'll need to update the module's version in version.properties and the module definition. We'll eventually eliminate this 

Hit the following URL (preferably in FireFox):

http://localhost:1060/ep+name@control_reload

Verify that the resulting XML document includes the following module definitions (customized):

<module>
file:/usr/local/svn/zepheira/trunk/customers/OCLC/PURL2/src/deploy/mod-control-0.0.1.jar
</module>

<module>
file:/usr/local/svn/zepheira/trunk/customers/OCLC/PURL2/src/deploy/mod-session-0.0.1.jar
</module>

<module>
file:/usr/local/svn/zepheira/trunk/customers/OCLC/PURL2/src/deploy/mod-template-0.0.1.jar
</module>

<module>
file:/usr/local/svn/zepheira/trunk/customers/OCLC/PURL2/src/deploy/test-mod-template-0.0.1.jar
</module>

If you want to test the roll back capability, hit the following URL:

http://localhost:1060/ep+name@control_panic

Verify that the only one of those modules you still see is:

<module>
file:/usr/local/svn/zepheira/trunk/customers/OCLC/PURL2/src/deploy/mod-control-0.0.1.jar
</module>

Don't forget to his this URL again if you rolled back:

http://localhost:1060/ep+name@control_reload

6) Let's try to run our XUnit tests. Hit the following URL:

http://localhost:1060/ep+name@app_xunit

You should see "PURLs Template Library Tests". Select the Run button and make sure that you only see green test results. There should be three PURLs tests run.

7) Now that we've verified things are installed correctly, let's hit a URL exposed from the mod-template module (the template is the important part, the something part can be anything):

http://localhost:8080/template/something

This request should be intercepted and require you to enter a user name and password. "test" and "test" should suffice. Once you submit the user name and password, you should see the following:

<hello>template</hello>

More detailed breakdown of the modules:
=======================================

mod-control:
------------
This module manages deploying our modules into the NetKernel instance. It is the only module that needs to be part of the the etc/deployedModules.xml and Front end fulcrum before we begin. All other modules are deployed by manipulating the config/appDeployment.xml and rebuilding.

The entrypoints we were hitting in the walkthrough above are defined in the mod-control/modules/mod-control/entrypoints.xml.

There are two:

<name>control_reload</name>
<uri>ffcpl:/netkernel-control/reload</uri>

and

<name>control_panic</name>
<uri>ffcpl:/netkernel-control/panic</uri>

These are mapped in the mod-control's module.xml to:

<rule>
	<match>ffcpl:/netkernel-control/reload</match>
	<to>active:javascript+operator@ffcpl:/resources/reload.js</to>
</rule>
<rule>
	<match>ffcpl:/netkernel-control/panic</match>
	<to>active:javascript+operator@ffcpl:/resources/panic.js</to>
</rule>

These two JavaScript files store off the current list of modules, adds in any new ones defined in the module's /etc/appDeployment.xml file. This is generated at build time. It is copied out of the root directory's config/appDeployment.xml and is tokenized with the version information contained in the various module version.properties property files.

After manipulating the module list, it kicks NetKernel to do a hot restart so that the changes are immediately applied.

mod-session:
------------
This module manages sessions for URIs that we want to protect with an authentication requirement.

If you recall, we had to import this module's interface into the front end fulcrum even before we deployed. There is probably a programmatic way to reload the front end fulcrum's module.xml, I just haven't taken care of that yet.

This module currently only exports the following entries:

<export>
	<uri>
		<match>ffcpl:/(template)/.*</match>
		<match>ffcpl:/etc/HTTPBridgeConfig.xml</match>
	</uri>
</export>

If we wanted to export the foo and bar URIs as well, it would look like:

<export>
	<uri>
		<match>ffcpl:/(template|foo|bar)/.*</match>
		<match>ffcpl:/etc/HTTPBridgeConfig.xml</match>
	</uri>
</export>

So, the session module has to re-export the URIs from other modules that you want to wrap with an authentication check. For example, the template URI is taken from mod-template. This is verified by checking the mod-session module.xml toward the bottom and noting the following import:

<import>
	<uri>urn:org:purl:template</uri>
</import>

The rewrite rules look like this:

<rule>
	<match>(ffcpl:/(template)/(?!login|static/).*)</match>
	<to>active:gk+uri@$1</to>
</rule>
<rule>
	<match>.*?(/login/.*?.bsh.*)</match>
	<to>active:beanshell+operator@ffcpl:/resources$1</to>
</rule>
<rule>
	<match>(.*(template)/(?!static).*)</match>
	<to>active:sessionmapper+uri@$e1</to>
</rule>
<rule>
	<match>ffcpl:/(template)(/static.*?)(\+.*)</match>
	<to>ffcpl:/resources$1</to>
</rule>

Any URI beginning with the word "template" ("template|foo|bar" in the future) will get rerouted to the URI GateKeeper module. The static and login directories under resources are not routed through the GateKeeper.

The GateKeeper is configured via the /etc/GateKeeperPolicy.xml which defines a zone:

<zone>
	<match>.*/(template)/(?!login/).*</match>
	<isValidURI>active:beanshell+operator@ffcpl:/resources/sessionvalidator.bsh</isValidURI>
	<loginURI>ffcpl:/resources/static/login.html</loginURI>
</zone>

It validates the URI in question by calling the sessionvalidator.bsh file. If that returns false (as in there is not a valid, authenticated session for this user), it will return the login.html page which will submit back into the resources/login/login-submit.bsh which will verify the credentials, create a session and attach it to the URI for this user. It then rewrites to the originally-requested URI.

The resources/login/logout.bsh file shows how the session credentials are invalidated. This can be called explicitly on logout or via a cron job.

mod-template:
-------------

This module is a sample module that exports a variety of URIs:

<export>
	<uri>
		<match>ffcpl:/template/.*</match>
		<match>ffcpl:/template/initialize</match>
		<match>active:template\+.*</match>
		<match>ffcpl:/etc/HTTPBridgeConfig.xml</match>
		<match>ffcpl:/entrypoints.xml</match>
		<match>ffcpl:/etc/CRONConfig.xml</match>
	</uri>
</export>

We rewrite the ffcpl:/template URIs to active:template requests. But, because other modules might in theory want to issue active:template requests, we export that URI as well. active:template requests get rewritten to the TemplateAccessor which responds differently whether it is called with the template/initialize or something else.

Of note here is the use of a CRON configuration. In the entrypoints.xml file, we see:

<entrypoint>
  <name>template_cron_config</name>
  <uri>ffcpl:/etc/CRONConfig.xml</uri>
  <indexable>
    <title>config for kicking the Template Accessor</title>
    <desc></desc>
    <keywords/>
  </indexable>
  <categories>cron hidden</categories>
  <group>Tests</group>
  <icon/>
</entrypoint>

This is a cron entrypoint that defines kicking off a job. It is a useful pattern to start either recurring or singleshot requests this way when a module is loaded.

Our /etc/CRONConfig.xml is simple:

<job>
	<uri>active:template+path@ffcpl:/template/initialize</uri>
	<name>Kick the template accessor</name>
	<desc>Kick the template accessor</desc>
	<simple>
		<startTime></startTime>
		<endTime></endTime>
		<repeatCount>0</repeatCount>
		<repeatInterval>1000</repeatInterval>
	</simple>
</job>


test-mod-template:
------------------

This is a companion module that wouldn't be deployed to a production system necessarily (note: it currently is, but we will address that). It contains unit tests for the mod-template module. This pairing/naming convention is a useful pattern to follow.

We indicate that there are unit tests to run with the following entrypoint:

<entrypoints>
  <entrypoint>
    <name>test-mod-template</name>
    <uri>ffcpl:/test/mod-template-test/xunit</uri>
    <indexable>
      <title>PURLs Library Tests</title>
      <desc>PURLs Template unit-test suite</desc>
      <keywords/>
    </indexable>
    <categories>xunit</categories>
    <group>OCLC</group>
    <icon/>
  </entrypoint>
 </entrypoints>

In the module.xml file, we rewrite requests to ffcpl:/test/mod-template-test/xunit* to the active:xunit accessor:

<rule>
	<match>ffcpl:/test/mod-template-test/xunit(.*)</match>
	<to>active:xunit+testlist@ffcpl:/test/xunit/testlist.xml$1</to>
</rule>

Tests are defined in the testlist.xml file. This indicates that there is a single test group in this module:

<group title="Template Accessor Tests">
  <uri>ffcpl:/test/xunit/testlist-template.xml</uri>
</group>

The testlist-template.xml file (badly named, noted) identifies the following tests which first trigger behavior and then attempt XPath assertions against the results. You should attempt to change the expected results, save them, rerun the tests (remember, this URL: http://localhost:1060/ep+name@app_xunit) and verify that they break. Please fix the unit tests if you break them.

<testlist  title="Template Accessor Unit Tests">
    <desc>
	<div>
	<b>Template</b> accessor unit tests for the <b>PURLs</b> lib.
	</div>
    </desc>
    <test>
        <uri>active:beanshell+operator@ffcpl:/test/accessor/test.bsh</uri>
		<assert>
			<xpath>count(//hello) = 1</xpath>
		</assert>
    </test>
    <test>
      <uri>active:javascript+operator@ffcpl:/test/accessor/template-test1.js</uri>
		<assert>
			<xpath>count(//hello) = 1</xpath>
		</assert>
    </test>
    <test>
      <uri>active:javascript+operator@ffcpl:/test/accessor/template-test1.js</uri>
		<assert>
			<xpath>//hello = 'template'</xpath>
		</assert>
    </test>
</testlist>

These tests may seem verbose, but the same pattern will happen over and over so it should be easy to cut and paste them.