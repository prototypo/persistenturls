/******************************************************************************
 (c) Copyright 2002 - $Date: 2007/09/17 09:35:44 $ 1060 Research Ltd
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
 * File:          $RCSfile: ResultSetAspect2.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2007/09/17 09:35:44 $
 *****************************************************************************/
package org.ten60.rdbms.representation;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.util.NetKernelException;
import com.ten60.netkernel.urii.aspect.*;
import org.ten60.netkernel.layer1.representation.*;
import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.rdbms.accessor.RDBMSAccessorImpl2;
import java.io.*;
import java.util.TimerTask;
import java.sql.*;

/**
 *	Aspect that can initiate a query and return a java.sql ResultSet over it
 * @author  tab
 */
public class ResultSetAspect2 implements IAspectResultSet
{
	private String mQuery;
	private String mConnectionURI;
	private IAspectDBConnectionPool mPool;
	private ResultSet mResultSet;
	private RDBMSAccessorImpl2 mAccessor;
	
	/** Creates a new instance of ResultSetAspect */
	public ResultSetAspect2(String aQuery, String aConnectionURI, RDBMSAccessorImpl2 aAccessor)
	{	mQuery=aQuery;
		mConnectionURI=aConnectionURI;
		mAccessor=aAccessor;
	}
	
	/** get a fresh result set from this aspect. This method may throw an NKFExceptionblock if result set is currently
	 * in use.
	 * @exception SQLException thrown if connection to db fails or query is invalid
	 * @exception NKFException thrown if result set is currently in use or if db configuration not found
	 */
	public synchronized ResultSet getResultSet(INKFConvenienceHelper context) throws SQLException, NKFException
	{
		if (mResultSet!=null)
		{	throw new NKFException("Concurrent ResultSet Access","An org.ten60.rdbms.representation.IAspectResultSet must be released be reusing",mQuery);
		}
		mPool = (IAspectDBConnectionPool)context.sourceAspect(mConnectionURI,IAspectDBConnectionPool.class);
		Connection c = null;
		Statement s=null;
		TimerTask tt=null;
		try
		{	c=mPool.acquireConnection();
			s=c.createStatement();
			tt=mAccessor.configureStatementTimeout(s, mPool);
			s.setQueryTimeout(mPool.getDefaultQueryTimeout());
			mResultSet=s.executeQuery(mQuery);
			return mResultSet;
		}
		catch (SQLException e)
		{	if (s!=null)
			{	
				s.close();
			}
			if (c!=null)
			{	mPool.releaseConnection(c);
			}
			throw e;
		}
		catch (NetKernelException e)
		{	NKFException e2= new NKFException(e.getId(),e.getMessage(),null);
			if (e.getCause()!=null)
			{	e2.addCause(e.getCause());
			}
			throw e2;
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}			
		}
			
	}

	/** release result set for use by another
	 */
	public synchronized void release()
	{	if (mResultSet!=null)
		{	Statement s = null;
			try
			{	s=mResultSet.getStatement();
				mResultSet.close();
			}
			catch (SQLException e) { /* silently ignore */ }
			Connection c=null;
			try
			{	if (s!=null)
				{	c=s.getConnection();
					s.close();
				}
			}
			catch (SQLException e) { /* silently ignore */ }
			if (c!=null)
			{	mPool.releaseConnection(c);
			}
		}
		mResultSet=null;
		mPool=null;
	}
}