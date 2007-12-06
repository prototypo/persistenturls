/******************************************************************************
 (c) Copyright 2002 - $Date: 2007/10/17 11:08:47 $ 1060 Research Ltd
 *
 * This Software is licensed to You, the licensee, for use under the terms of
 * the 1060 Public License v1.0. Please read and agree to the 1060 Public
 * License v1.0 [www.1060research.com/license] before using or redistributing
 * this software.
 *
 * In summary the 1060 Public license has the following conditions.
 * A. You may use the Software free of charge provided you agree to the terms
 * laid out in the 1060 Public License v1.0
 * B. You are only permitted to use the Software with components or applications
 * that provide you with OSI Certified Open Source Code [www.opensource.org], or
 * for which licensing has been approved by 1060 Research Limited.
 * You may write your own software for execution by this Software provided any
 * distribution of your software with this Software complies with terms set out
 * in section 2 of the 1060 Public License v1.0
 * C. You may redistribute the Software provided you comply with the terms of
 * the 1060 Public License v1.0 and that no warranty is implied or given.
 * D. If you find you are unable to comply with this license you may seek to
 * obtain an alternative license from 1060 Research Limited by contacting
 * license@1060research.com or by visiting www.1060research.com
 *
 * NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC
 * LICENSE V1.0 FOR DETAILS
 *
 * THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ
 * THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license
 *
 * File:          $RCSfile: ConfigToRDBMSConnection.java,v $
 * Version:       $Name:  $ $Revision: 1.18 $
 * Last Modified: $Date: 2007/10/17 11:08:47 $
 *****************************************************************************/
package org.ten60.rdbms.transreptor;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.aspect.IAspectBinaryStream;
import com.ten60.netkernel.urrequest.URRequest;

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.NKFTransreptorImpl;
import org.ten60.netkernel.layer1.util.SuperStackClassLoader;

import org.ten60.netkernel.xml.representation.*;
import org.ten60.netkernel.xml.xda.*;

import org.ten60.rdbms.representation.*;
import java.sql.*;
import java.util.*;

/**
 * ConfigToRDBMSConnection- a transreptor to make a database configuration accessible as a data
 * base connection.
 * @author  tab
 */
public class ConfigToRDBMSConnection extends NKFTransreptorImpl
{
	private Map mCreatedConnections = Collections.synchronizedMap(new WeakHashMap());
	private static List sDriversToDeregister = Collections.synchronizedList(new ArrayList());
	
	public boolean supports(IURRepresentation aFrom, Class aTo)
	{	return (aFrom.hasAspect(IAspectXDA.class) || aFrom.hasAspect(IAspectBinaryStream.class)) 
			&& aTo.isAssignableFrom(ConnectionPoolAspect.class);
	}
	
	public void destroy()
	{	for (Iterator i=mCreatedConnections.keySet().iterator(); i.hasNext(); )
		{	ConnectionPoolAspect cpa = (ConnectionPoolAspect)i.next();
			cpa.cleanup();
		}
		
		for (Iterator i=sDriversToDeregister.iterator(); i.hasNext(); )
		{	Driver d = (Driver)i.next();
			try
			{	DriverManager.deregisterDriver(d);
			}
			catch (SQLException e)
			{	e.printStackTrace();
			}
		}
		sDriversToDeregister.clear();
		
	}
	
	protected void transrepresent(INKFConvenienceHelper context) throws Exception
	{	IXDAReadOnly from = ((IAspectXDA)context.sourceAspect(INKFRequestReadOnly.URI_SYSTEM, IAspectXDA.class)).getXDA();

		String jdbcDriver = from.getText("rdbms/jdbcDriver", true);
		String jdbcConnection = from.getText("rdbms/jdbcConnection",true);
		int poolsize=1;
		if(from.isTrue("rdbms/poolSize"))
		{	poolsize=Integer.parseInt(from.getText("rdbms/poolSize",true));
		}
		
		int defaultQueryTimeout=0; // no timeout
		if(from.isTrue("rdbms/defaultQueryTimeout"))
		{	defaultQueryTimeout=Integer.parseInt(from.getText("rdbms/defaultQueryTimeout",true));
		}
		
		int queryTimeoutMethod=IAspectDBConnectionPool.TIMEOUT_METHOD_SET;
		if(from.isTrue("rdbms/queryTimeoutMethod"))
		{	String methodString=from.getText("rdbms/queryTimeoutMethod",true);
			if (methodString.equals("TIMER"))
			{	queryTimeoutMethod=IAspectDBConnectionPool.TIMEOUT_METHOD_TIMER;
			}
		}
		
		
		Properties props = new Properties();
		for (IXDAReadOnlyIterator i=from.readOnlyIterator("rdbms/*"); i.hasNext(); )
		{	i.next();
			String name=i.eval("name()").getStringValue();
			if (!(name.equals("jdbcDriver") || name.equals("jdbcConnection") || name.equals("poolSize") || name.equals("defaultQueryTimeout")))
			{	String value=i.getText(".", false);
				props.put(name,value);
			}
		}
		
		// initialise JDBC driver
		URRequest request = context.getKernelHelper().getThisKernelRequest();
		SuperStackClassLoader cl = new SuperStackClassLoader(request);
		Class c = cl.loadClass(jdbcDriver);
		Driver d = (Driver)c.newInstance();
		d=new DriverShim(d);
		sDriversToDeregister.add(d);
		DriverManager.registerDriver(d);
		
		ConnectionPoolAspect cpa=new ConnectionPoolAspect(jdbcDriver,jdbcConnection,props,poolsize,defaultQueryTimeout,queryTimeoutMethod);
		INKFResponse resp = context.createResponseFrom(cpa);
		resp.setCreationCost(1024);
		context.setResponse(resp);
		mCreatedConnections.put(cpa,null);
	}
}

/** Thanks to Nick Sayer nsayer @ kfu.com for the solution to
 * jdbcs inability to load driver from alternate classloaders
*/
class DriverShim implements Driver {
	private Driver driver;
	DriverShim(Driver d) {
		this.driver = d;
	}
	public boolean acceptsURL(String u) throws SQLException {
		return this.driver.acceptsURL(u);
	}
	public Connection connect(String u, Properties p) throws SQLException {
		return this.driver.connect(u, p);
	}
	public int getMajorVersion() {
		return this.driver.getMajorVersion();
	}
	public int getMinorVersion() {
		return this.driver.getMinorVersion();
	}
	public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
		return this.driver.getPropertyInfo(u, p);
	}
	public boolean jdbcCompliant() {
		return this.driver.jdbcCompliant();
	}
}
