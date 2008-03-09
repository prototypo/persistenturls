/******************************************************************************
 (c) Copyright 2002 - $Date: 2007/09/17 08:49:56 $ 1060 Research Ltd
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
 * File:          $RCSfile: ConnectionPoolAspect.java,v $
 * Version:       $Name:  $ $Revision: 1.12 $
 * Last Modified: $Date: 2007/09/17 08:49:56 $
 *****************************************************************************/
package org.ten60.rdbms.representation;
import java.sql.*;
import java.util.*;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.util.*;
/**
 *
 * @author  tab
 */
public class ConnectionPoolAspect implements IAspectTransactedDBConnectionPool
{
    private final String mDriver;
    private final String mConnectionString;
    private final Properties mProperties;

    private final int mPoolSize;
	private final int mDefaultQueryTimeout;
	private final int mQueryTimeoutMethod;
    private final List mAvailablePool = new ArrayList();
    private final List mUsedPool = new ArrayList();
	private static final int RETRY_COUNT=2;
    
    /** Creates a new instance of ConnectionPoolAspect */
    public ConnectionPoolAspect(String aDriver, String aConnection, Properties aProperties, int aPoolSize, int aDefaultQueryTimeout, int aQueryTimeoutMethod)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {	mDriver = aDriver;
		mConnectionString = aConnection;
		mProperties=aProperties;
		mPoolSize=aPoolSize;
		mDefaultQueryTimeout=aDefaultQueryTimeout;
		mQueryTimeoutMethod=aQueryTimeoutMethod;
    }
	
	public boolean equal(Object aOther)
	{	return false;
	}
	
	public int hashCode()
	{	return mConnectionString.hashCode()%mDriver.hashCode();
	}
	
	
	
	public void cleanup()
	{	for (Iterator i=mAvailablePool.iterator(); i.hasNext(); )
		{	Connection c = (Connection)i.next();
			try
			{	c.close();
			}	catch (SQLException e)
			{	SysLogger.log(SysLogger.WARNING, this, "Exception whilst closing database connection in pool ["+mConnectionString+"]");
				e.printStackTrace();
			}
		}
		if (!mUsedPool.isEmpty())
		{	SysLogger.log(SysLogger.WARNING, this, "Connections still marked as in use when finalizing pool ["+mConnectionString+"]");
			for (Iterator i=mUsedPool.iterator(); i.hasNext(); )
		{	Connection c = (Connection)i.next();
			try
			{	c.close();
			}	catch (SQLException e)
			{	
			}
		}
		}
	}
    
    private synchronized Connection innerAcquireConnection() throws NetKernelException,SQLException
    {	Connection result;
		int availablePoolSize = mAvailablePool.size();
		if (availablePoolSize>0)
		{   result = (Connection)mAvailablePool.remove(availablePoolSize-1);
			mUsedPool.add(result);
		}
		else if (mUsedPool.size()<mPoolSize)
		{   result = innerCreateNewConnection();
			mUsedPool.add(result);
		}
		else
		{   throw new NetKernelException("Too Many Concurrent Connections",">"+mPoolSize+" connections requested for ["+mConnectionString,null);
		}
		return result;
    }
    
    private Connection innerCreateNewConnection() throws SQLException
    {	return DriverManager.getConnection(mConnectionString,mProperties);
    }
	
	private synchronized void innerReleaseConnection(Connection aConnection)
	{	if (mUsedPool.remove(aConnection))
		{	mAvailablePool.add(aConnection);
		}
		else
		{	SysLogger.log(SysLogger.WARNING, this, "Bad releaseConnection() in pool ["+mConnectionString+"]");
		}
	}
    
    public Connection acquireConnection() throws NetKernelException
    {	Connection result=null;
		int count=RETRY_COUNT;
		while(true)
		{	try
			{	result = innerAcquireConnection();
				result.setAutoCommit(true);
				break;
			}
			catch (SQLException e)
			{	if (result!=null)
				{	mUsedPool.remove(result);
					result=null;
				}
				if ((count--)<=0)
				{	NetKernelException ex= new NetKernelException("Error Obtaining Connection","on connection ["+mConnectionString+"]",null);
					ex.addCause(e);
					throw ex;
				}
			}
		}
		return result;
    }
	
    public void releaseConnection(Connection aConnection)
    {	innerReleaseConnection(aConnection);
    }

    public IAspectDBConnectionPool getTransactedConnection() throws NetKernelException
    {	TransactedConnection result=null;
		Connection c=null;
		int count=RETRY_COUNT;
		while(true)
		{	try
			{	c = innerAcquireConnection();
				c.setAutoCommit(false);
				result = new TransactedConnection(c,this);
				break;
			}
			catch (SQLException e)
			{	if (c!=null)
				{	mUsedPool.remove(c);
					c=null;
				}
				if ((count--)<=0)
				{	NetKernelException ex= new NetKernelException("Error Obtaining Connection","on connection ["+mConnectionString+"]",null);
					ex.addCause(e);
					throw ex;
				}
			}
		}
		return result;	
    }

	public void commitConnection(IAspectDBConnectionPool aConnection) throws NetKernelException
    {	if (aConnection instanceof TransactedConnection)
		{	TransactedConnection tc = (TransactedConnection)aConnection;
			Connection c = tc.acquireConnection();
			try
			{	c.commit();
			}
			catch (SQLException e)
			{	throw new NetKernelException("Error during Commit","on connection ["+mConnectionString+"]",null);
			}	
			finally
			{	releaseConnection(c);
			}
		}
		else
		{	SysLogger.log(SysLogger.WARNING, this, "Bad commitConnection() in pool ["+mConnectionString+"]");
		}
    }
    
    public void rollbackConnection(IAspectDBConnectionPool aConnection) throws NetKernelException
    {	if (aConnection instanceof TransactedConnection)
		{	TransactedConnection tc = (TransactedConnection)aConnection;
			Connection c = tc.acquireConnection();
			try
			{	c.rollback();
			}
			catch (SQLException e)
			{	throw new NetKernelException("Error during Rollback","on connection ["+mConnectionString+"]",null);
			}	
			finally
			{	releaseConnection(c);
			}
		}
		else
		{	SysLogger.log(SysLogger.WARNING, this, "Bad rollbackConnection() in pool ["+mConnectionString+"]");
		}
    }
	
	public int getDefaultQueryTimeout()
	{	return mDefaultQueryTimeout;
	}
	
	public int getQueryTimeoutMethod()
	{	return mQueryTimeoutMethod;
	}
	
}