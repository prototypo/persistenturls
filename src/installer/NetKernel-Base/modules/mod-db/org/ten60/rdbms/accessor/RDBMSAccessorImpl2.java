/******************************************************************************
  (c) Copyright 2002 - $Date: 2007/12/05 12:06:31 $ 1060 Research Ltd

  This Software is licensed to You, the licensee, for use under the terms of   
  the 1060 Public License v1.0. Please read and agree to the 1060 Public       
  License v1.0 [www.1060research.com/license] before using or redistributing   
  this software.                                                               

  In summary the 1060 Public license has the following conditions.             
  A. You may use the Software free of charge provided you agree to the terms   
  laid out in the 1060 Public License v1.0                                     
  B. You are only permitted to use the Software with components or applications
  that provide you with OSI Certified Open Source Code [www.opensource.org], or
  for which licensing has been approved by 1060 Research Limited.              
  You may write your own software for execution by this Software provided any  
  distribution of your software with this Software complies with terms set out 
  in section 2 of the 1060 Public License v1.0                                 
  C. You may redistribute the Software provided you comply with the terms of   
  the 1060 Public License v1.0 and that no warranty is implied or given.       
  D. If you find you are unable to comply with this license you may seek to    
  obtain an alternative license from 1060 Research Limited by contacting       
  license@1060research.com or by visiting www.1060research.com                 

  NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC  
  LICENSE V1.0 FOR DETAILS                                                     

  THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ     
  THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license      

  File:          $RCSfile: RDBMSAccessorImpl2.java,v $
  Version:       $Name:  $ $Revision: 1.16 $
  Last Modified: $Date: 2007/12/05 12:06:31 $
 *****************************************************************************/

package org.ten60.rdbms.accessor;


import com.ten60.netkernel.urii.*;
import org.ten60.netkernel.xml.xda.*;
import org.ten60.netkernel.xml.representation.*;
import org.ten60.netkernel.xml.util.*;
import org.ten60.netkernel.xml.xahelper.*;

//import org.ten60.netkernel.layer1.meta.*;
import com.ten60.netkernel.urii.aspect.*;

import org.ten60.rdbms.util.*;
import org.ten60.rdbms.representation.*;
import org.ten60.netkernel.layer1.representation.MonoRepresentationImpl;
import org.ten60.netkernel.layer1.representation.ByteArrayAspect;
import org.ten60.netkernel.layer1.util.Utils;
import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import java.sql.*;

/**
 *	The RDBMS Accessor class. Implements sqlxxxxx family of accessors
 * @author  tab
 */
public class RDBMSAccessorImpl2 extends NKFAccessorImpl
{
	public static final String ARG_CONFIGURATION = "configuration";
	public static final String DEFAULT_CONFIG="ffcpl:/etc/ConfigRDBMS.xml";
	
	/** Creates a new instance of RDBMSAccessorImpl */
	public RDBMSAccessorImpl2()
	{	super(0,true,INKFRequestReadOnly.RQT_SOURCE);
	}
	
	public void processRequest(INKFConvenienceHelper context) throws Exception
	{	
		String configURI;
		if (context.getThisRequest().argumentExists(ARG_CONFIGURATION))
		{	configURI= "this:param:"+ARG_CONFIGURATION;
		}
		else
		{	configURI=DEFAULT_CONFIG;
		}
		
		if (context.getThisRequest().getActiveType().equals("sqlQuery") && 
			IAspectResultSet.class.isAssignableFrom(context.getThisRequest().getAspectClass()))
		{
			IXDAReadOnly operand = ((IAspectXDA)context.sourceAspect("this:param:operand",IAspectXDA.class)).getXDA();
			IAspectDBConnectionPool pool = (IAspectDBConnectionPool)context.sourceAspect(configURI,IAspectDBConnectionPool.class);

			String query = operand.getText(".",true);
			IURAspect resultantAspect = new ResultSetAspect2(query,pool,this);
			INKFResponse response = context.createResponseFrom(resultantAspect);
			response.setCreationCost(4);
			response.setUsageCost(32);
			context.setResponse(response);
		}
		else
		{	IAspectDBConnectionPool connectionPool = (IAspectDBConnectionPool)context.sourceAspect(configURI,IAspectDBConnectionPool.class);
			Connection c = connectionPool.acquireConnection();
			int queryTimeout = connectionPool.getDefaultQueryTimeout();
			try
			{	IURAspect resultantAspect=execute(c,context,connectionPool);
				INKFResponse response = context.createResponseFrom(resultantAspect);
				response.setCreationCost(32);
				context.setResponse(response);
			}
			finally
			{	connectionPool.releaseConnection(c);
			}
		}
	}
	
	private IURAspect execute(Connection aConnection, INKFConvenienceHelper context, IAspectDBConnectionPool aPool) throws Exception
	{	String action = context.getThisRequest().getActiveType();
		IXDAReadOnly operand = ((IAspectXDA)context.sourceAspect("this:param:operand",IAspectXDA.class)).getXDA();
		IURAspect resultantAspect;
		if (action.equals("sqlQueryBlob"))
		{	resultantAspect = sqlQueryBlob(aConnection,operand,aPool);
		}
		else if (action.equals("sqlUpdateBlob"))
		{	IAspectReadableBinaryStream source=(IAspectReadableBinaryStream)context.sourceAspect("this:param:param",IAspectReadableBinaryStream.class);
			resultantAspect = sqlUpdateBlob(aConnection,operand,source,aPool);
		}
		else if (action.equals("sqlQueryClob"))
		{	resultantAspect = sqlQueryClob(aConnection,operand,aPool);
		}
		else if (action.equals("sqlUpdateClob"))
		{	IAspectString source=(IAspectString)context.sourceAspect("this:param:param",IAspectString.class);
			resultantAspect = sqlUpdateClob(aConnection,operand,source,aPool);
		}
		else if (action.equals("sqlQuery"))
		{	resultantAspect = sqlQuery(aConnection,operand,context.getThisRequest().getAspectClass(),aPool);
		}
		else if (action.equals("sqlBooleanQuery"))
		{	resultantAspect = sqlBooleanQuery(aConnection,operand,aPool);
		}
		else if (action.equals("sqlUpdate"))
		{	resultantAspect = sqlUpdate(aConnection,operand,aPool);
		}
		else if (action.equals("sqlBatch"))
		{	resultantAspect = sqlBatch(aConnection,operand,aPool);
		}
		else if (action.equals("sqlProc"))
		{	resultantAspect = sqlProc(aConnection,operand,aPool);
		}
		else
		{	throw new NKFException("unknown sql operation");
		}
		return resultantAspect;
	}
	
	private IURAspect sqlQueryBlob(Connection aConnection, IXDAReadOnly aOperand, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		Statement s=aConnection.createStatement();
		TimerTask tt=configureStatementTimeout(s,aPool);
		try
		{	ResultSet rs=s.executeQuery(sql);
			try
			{	if(rs.getMetaData().getColumnCount()>1) throw new Exception("Ambiguous columns - refine SQL query to return only the Blob column");
				if(!rs.next())
				{	throw new Exception("SQL query returned no rows - refine SQL to return a single row");
				}
				
				ByteArrayOutputStream baos=new ByteArrayOutputStream(2048);
				InputStream is=rs.getBinaryStream(1);
				Utils.pipe(is, baos);
				return new ByteArrayAspect(baos);
			}
			finally
			{	rs.close();
			}
		}
		finally
		{	s.close();
		}
	}
	
	private IURAspect sqlUpdateBlob(Connection aConnection, IXDAReadOnly aOperand, IAspectReadableBinaryStream aSource, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		PreparedStatement ps=aConnection.prepareStatement(sql);
		TimerTask tt=configureStatementTimeout(ps,aPool);
		try
		{	ps.setBinaryStream(1, aSource.getInputStream(), aSource.getContentLength());
			ps.executeUpdate();
			return new org.ten60.netkernel.layer1.representation.VoidAspect();		
		}
		finally
		{	ps.close();
		}
	}
	
	/** Make DB query from xml supplied SQL query statement, Return a best match aspect*/
	public IURAspect sqlQuery(Connection aConnection, IXDAReadOnly aOperand, Class aAspectClass, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		Statement s=aConnection.createStatement();
		TimerTask tt=configureStatementTimeout(s, aPool);
		ResultSet rs=null;
		try
		{	
			IURAspect result;
			rs=s.executeQuery(sql);
			if (StringAspect.class.isAssignableFrom(aAspectClass))
			{	String unparsed = SQLXMLUtil.resultsetToXMLUnparsed(rs,"results", "row");
				result = new StringAspect(unparsed);
			}
			else 
			{	DOMXDA parsed = SQLXMLUtil.resultsetToXML(rs,"results", "row");
				result = new DOMXDAAspect(parsed);
			}
			return result;
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			s.close();
			if (rs!=null)
			{	rs.close();
			}
		}
	}
	
	/** Make DB query from xml supplied SQL query statement, Return boolean */
	public BooleanAspect sqlBooleanQuery(Connection aConnection, IXDAReadOnly aOperand, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		Statement s=aConnection.createStatement();
		TimerTask tt=configureStatementTimeout(s,aPool);
		ResultSet rs=null;
		try
		{	rs=s.executeQuery(sql);
			return new BooleanAspect(rs.next());
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			s.close();
			if(rs!=null)
			{	rs.close();				
			}
		}
	}
	
	/** Make DB Update (INSERT, UPDATE, DELETE) from xml supplied SQL query statement, Return XMLized updated-rows count*/
	public IURAspect sqlUpdate(Connection aConnection, IXDAReadOnly aOperand, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		Statement s=aConnection.createStatement();
		TimerTask tt=configureStatementTimeout(s,aPool);
		try
		{	int rows=s.executeUpdate(sql);
			DOMXDA doc=new DOMXDA(XMLUtils.getInstance().newDocument(),false);
			doc.appendPath("/", "updated-rows", Integer.toString(rows));
			return new DOMXDAAspect(doc);
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			s.close();
		}
	}
	
	/** Do SQL Batch from supplied mutiple SQL query document, Return XMLized batch-status with rows affected count*/
	public IURAspect sqlBatch(Connection aConnection, IXDAReadOnly aOperand, IAspectDBConnectionPool aPool) throws Exception
	{	Statement s=aConnection.createStatement();
		TimerTask tt=configureStatementTimeout(s,aPool);
		try
		{	IXDAReadOnlyIterator it=aOperand.readOnlyIterator("sql");
			while(it.hasNext())
			{	it.next();
				String sql=it.getText(".", false);
				s.addBatch(sql);
			}
			int[] rows=s.executeBatch();
			DOMXDA doc=new DOMXDA(XMLUtils.getInstance().newDocument(),false);
			doc.appendPath("/", "batch-status", null);
			for(int i=0; i<rows.length;i++)
			{	doc.appendPath("/batch-status", "update", Integer.toString(rows[i]));
			}
			return new DOMXDAAspect(doc);
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			s.close();
		}
	}
	
	private IURAspect sqlQueryClob(Connection aConnection, IXDAReadOnly aOperand, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		Statement s=aConnection.createStatement();
		TimerTask tt=configureStatementTimeout(s,aPool);
		try
		{	ResultSet rs=s.executeQuery(sql);
			try
			{	if(rs.getMetaData().getColumnCount()>1) throw new Exception("Ambiguous columns - refine SQL query to return only the Clob column");
				if(!rs.next())
				{	throw new Exception("SQL query returned no rows - refine SQL to return a single row");
				}
				Clob b=rs.getClob(1);
				StringWriter sw = new StringWriter(2048);
				Reader r = b.getCharacterStream();
				pipe(r,sw);
				return new StringAspect(sw.toString());
			}
			finally
			{	rs.close();
			}
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			s.close();
		}
	}

	private IURAspect sqlProc(Connection aConnection, IXDAReadOnly aOperand, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		CallableStatement stmnt = aConnection.prepareCall(sql);
		TimerTask tt=configureStatementTimeout(stmnt,aPool);
		try
		{	//execute
			boolean returnedResultSet=stmnt.execute();
			
			//find result sets in statement
			StringBuffer sb=new StringBuffer(1024);
			sb.append("<resultsets>");

			while(true)
			{	if(! returnedResultSet )
				{	int updateCount = stmnt.getUpdateCount();
					if(updateCount == -1)
					{	break;
					}
				}
				else
				{	ResultSet rs = stmnt.getResultSet();
					SQLXMLUtil.resultsetToXMLUnparsed(sb, rs, "results", "row");
					rs.close();
				}

				if(!stmnt.getMoreResults()) break;
			}
		  
			sb.append("</resultsets>");
			return new StringAspect(sb.toString());

		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			stmnt.close();
		}
		
	}
	
	
	private IURAspect sqlUpdateClob(Connection aConnection, IXDAReadOnly aOperand, IAspectString aSource, IAspectDBConnectionPool aPool) throws Exception
	{	String sql=aOperand.getText(".", true);
		PreparedStatement ps=aConnection.prepareStatement(sql);
		TimerTask tt=configureStatementTimeout(ps,aPool);
		try
		{	ps.setCharacterStream(1, aSource.getReader(), aSource.getString().length());
			ps.executeUpdate();
			return new org.ten60.netkernel.layer1.representation.VoidAspect();		
		}
		finally
		{	if (tt!=null)
			{	tt.cancel();
			}
			ps.close();
		}
	}	
	
	/** copy an reader to a writer and flush streams when finished
	 * @throws IOException if there are any problems
	 */
	public static void pipe(Reader aInput, Writer aOutput) throws IOException
	{	char c[] = new char[256];
		int n;
		try
		{	while ( (n=aInput.read(c))>0 )
			{	aOutput.write(c,0,n);
			}
		}
		finally
		{	aOutput.flush();
		}
	}
	
	
	private Timer mTimer;
	
	/** Will return a timer task registered with a timer to cancel the given statement if it doesn't
	 complete within the timeout period specified in the connectionpool if this connection pool is
	 configured to use the TIMER timeout method. Otherwise if using the SET method the
	 statement will be configured with a query timeout will return null.
	 **/
	public TimerTask configureStatementTimeout(Statement aStatement, IAspectDBConnectionPool aConnection) throws SQLException
	{	TimerTask tt=null;
		if (aConnection.getQueryTimeoutMethod()==IAspectDBConnectionPool.TIMEOUT_METHOD_TIMER)
		{	synchronized(this)
			{	if (mTimer==null)
				{	mTimer=new Timer();
				}
			}
			final Statement statement=aStatement;
			tt=new TimerTask()
			{	public void run()
				{	try
					{	statement.cancel();
					}
					catch (SQLException e)
					{ // nothing we can do
					}
				}
			};
			mTimer.schedule(tt,1000L*(long)aConnection.getDefaultQueryTimeout());
		}
		else if (aConnection.getQueryTimeoutMethod()==IAspectDBConnectionPool.TIMEOUT_METHOD_SET)
		{	aStatement.setQueryTimeout(aConnection.getDefaultQueryTimeout());
		}
		return tt;
	}
}