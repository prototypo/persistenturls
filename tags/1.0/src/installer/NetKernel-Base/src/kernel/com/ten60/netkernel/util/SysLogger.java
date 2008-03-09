/******************************************************************************
 * (c) Copyright 2002,2003, 1060 Research Ltd
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
 * File:          $RCSfile: SysLogger.java,v $
 * Version:       $Name:  $ $Revision: 1.7 $
 * Last Modified: $Date: 2005/09/12 16:36:00 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.w3c.dom.*;

/**
 *  Wrapper class around the Logger class.
 *
 *  Avoids having to fiddle with the properties file (set all logging
 *  allowed and filter message through this class).  Also allows check
 *  to see if logging allowed before wasting time calling log.
 *
 *  Optionally allows logging of Documents and methods.
 *
 *  Is a singleton wrapper.
 *
 *  Allows use of parameters in logging expressions.  Dynamic configuration
 *  of logging.
 */
public class SysLogger
{
	/** The core Logger class */
	private static Logger logger;
	
	public static final char NEWLINE_SUBSTITUTE='\u0011';
	
	/** Controls whether the calling method is written (true) to the log file.  */
	public final static boolean logMethods = true;
	
	/** variables to dynamically control the logging */
	private static boolean[] mLogLevelEnable = new boolean[8];
	
	/** map of log text to integer level*/
	private static Map sMap=new HashMap(7);
	static
	{
		sMap.put("debug", new Integer(SysLogger.DEBUG));
		sMap.put("fine", new Integer(SysLogger.FINE));
		sMap.put("info", new Integer(SysLogger.INFO));
		sMap.put("warning", new Integer(SysLogger.WARNING));
		sMap.put("severe", new Integer(SysLogger.SEVERE));
		sMap.put("application", new Integer(SysLogger.APPLICATION));
		sMap.put("container", new Integer(SysLogger.CONTAINER));
		sMap.put("cache", new Integer(SysLogger.CACHE));
	}
	
	
	// levels of logging message
	public final static int FINE  = 0;
	public final static int INFO = 1;
	public final static int WARNING = 2;
	public final static int SEVERE = 3;
	public final static int DEBUG = 4;
	public final static int APPLICATION = 5;
	public final static int CONTAINER = 6;
	public final static int CACHE = 7;
	
	static
	{
		LogManager manager = LogManager.getLogManager();
		try
		{    manager.readConfiguration();
		}
		catch(Exception e)
		{	e.printStackTrace();
		}
		logger	    = Logger.getLogger("com.ten60.netkernel.util.SysLogger");
		resetHandlers();
	}
	
	private static int sSevere=0;
	private static int sWarning=0;
	
	
	
	/**
	 *  Creates a new instance of SysLogger
	 */
	private SysLogger()
	{
	}
	
	/**
	 *  Indicates whether a log message of the specified level will be logged.
	 *
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 *  @return  true if a log message of the specified level will be logged.
	 */
	public static boolean shouldLog(int level, Object callingObject)
	{
		return logCheck(level, callingObject);
	}
	
	/**
	 *  Determines whether logging of a particular level is required
	 *  Avoids wasting time creating objects that are not required to be logged
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 *  @return  true if the log message should be recorded.
	 */
	private static boolean logCheck(int level, Object callingObject)
	{
		boolean logIt = mLogLevelEnable[level];
		return logIt;
	}
	
	/**Add a handler to this logger*/
	public static void addHandler(Handler h)
	{	h.setLevel(Level.ALL);
		logger.addHandler(h);
	}
	
	/**Get a handler identified by class name*/
	public static Handler getHandler(String classname)
	{ Handler[] h=logger.getHandlers();
	  for(int i=0;i<h.length;i++)
	  {
		  if(h[i].getClass().getName().equals(classname)) return h[i];
	  }
	  return null;
	}
	
	/**
	 *  Toggle whether to log messages of a certain level.
	 *  @param level  The logging level.
	 *  @param on  If true, log messages for the specified level will be recorded.
	 */
	public static void setLoggingFor(int level, boolean on)
	{	mLogLevelEnable[level]=on;
		Level l = Level.ALL;
		if (mLogLevelEnable[DEBUG]) l = Level.ALL;
		else if (mLogLevelEnable[CACHE]) l = Level.ALL;
		else if (mLogLevelEnable[FINE]) l = Level.FINE;
		else if (mLogLevelEnable[INFO]) l = Level.INFO;
		else if (mLogLevelEnable[APPLICATION]) l = Level.INFO;
		else if (mLogLevelEnable[CONTAINER]) l = Level.INFO;
		else if (mLogLevelEnable[WARNING]) l = Level.WARNING;
		else if (mLogLevelEnable[SEVERE]) l = Level.SEVERE;
		logger.setLevel(l);
	}
	
	/** Reset and remove Log Handlers */
	public static void resetHandlers()
	{
		//Remove all Handlers
		Handler[] h=logger.getHandlers();
		for(int i=0;i<h.length;i++) logger.removeHandler(h[i]);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		
	}
	
	/**
	 *  Determines the method the calling object was executing when the log message
	 *  was reported.  This is expensive to report so it can be toggled on/off by
	 *  by the logMethod flag.
	 *  @param  callingObject The object that has requested that a log message be
	 *  recorded.
	 *  @return  The name of the method.
	 */
	private static String getCallingMethod(Object callingObject)
	{
		String name	  = callingObject.getClass().getName();
		String method = null;
		
		Exception	  e = new Exception();
		StringWriter  sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		
		String str =  sw.toString();
		
		int i = str.indexOf('(', str.indexOf('(')+1);
		int j = i;
		while(str.charAt(j) != '.' && j>0)
		{
			j--;
		}
		method = str.substring(j+1, i);
		
		return method;
	}
	
	/**
	 *  Adds a log entry with one parameter.
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 *  @param msg  The message to record.
	 *  @param p1  The first paramter to add to the message
	 */
	public static void log1(int level, Object callingObject, String msg, Object p1)
	{
		StringBuffer buf = new StringBuffer(msg);
		replaceParameters(buf, new Object[]{p1});
		log(level, callingObject, buf.toString());
	}
	
	/**
	 *  Adds a log entry with two parameters.
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 *  @param msg  The message to record.
	 *  @param p1  The first paramter to add to the message
	 *  @param p2  The second paramter to add to the message
	 */
	public static void log2(int level, Object callingObject, String msg, Object p1, Object p2)
	{
		StringBuffer buf = new StringBuffer(msg);
		//replaceParameter(buf, 1, p1);
		//replaceParameter(buf, 2, p2);
		replaceParameters(buf, new Object[]{p1,p2});
		log(level, callingObject, buf.toString());
	}
	
	/**
	 *  Adds a log entry with three parameters.
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 *  @param msg  The message to record.
	 *  @param p1  The first paramter to add to the message
	 *  @param p2  The second paramter to add to the message
	 *  @param p3  The third paramter to add to the message
	 */
	public static void log3(int level, Object callingObject, String msg, Object p1, Object p2, Object p3)
	{
		StringBuffer buf = new StringBuffer(msg);
		replaceParameters(buf, new Object[]{p1,p2,p3});
		log(level, callingObject, buf.toString());
	}
	
	/**
	 *  Adds a log entry with four parameters.
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 *  @param msg  The message to record.
	 *  @param p1  The first paramter to add to the message
	 *  @param p2  The second paramter to add to the message
	 *  @param p3  The third paramter to add to the message
	 *  @param p4  The fourth paramter to add to the message
	 */
	public static void log4(int level, Object callingObject, String msg, Object p1, Object p2, Object p3, Object p4)
	{
		StringBuffer buf = new StringBuffer(msg);
		replaceParameters(buf, new Object[]{p1,p2,p3,p4});
		log(level, callingObject, buf.toString());
	}
	
	
	private static void replaceParameters(StringBuffer aMessage, Object[] aValues)
	{	int location[] = new int[aValues.length];
		for (int i=aValues.length-1; i>=0; i--)
		{	String param = "%" + (i+1);
			int j = aMessage.indexOf(param);
			location[i]=j;
		}
		for (int i=aValues.length-1; i>=0; i--)
		{	int l=location[i];
			String sub=aValues[i]==null?"<null>":aValues[i].toString();
			if (l>=0)
			{	aMessage.replace(l,l+2, sub);
				int diff=sub.length()-2;
				for (int j=i-1; j>=0; j--)
				{	if (location[j]>l)
					{	location[j]+=diff;
					}
				}
			}
		}
	}
		
	
	/**
	 *  This will always attempt to log.  Call shouldLog first to see if the message
	 *  should be logged according to the SysLogger settings.
	 *  @param msg  The log message to be recorded.
	 *  @param level  The log level for the message.
	 *  @param callingObject  The object requesting the log message be recorded
	 */
	public static final void log(int level, Object callingObject, String _msg)
	{
		if(!logCheck(level, callingObject))
			return;
		
		//replace newlines in msg with spaces
		_msg = _msg.replace('\n', NEWLINE_SUBSTITUTE);
		
		String s;
		String className  = callingObject.getClass().getName();
		String methodName = null;
		
		// logging methods is expensive, so optional
		if(logMethods)
		{
			methodName = getCallingMethod(callingObject);
		}
		synchronized(logger)
		{
			
			switch (level)
			{
				case CACHE:
				case FINE:
				case DEBUG:
					logger.logp(Level.FINE, className, methodName, _msg);
					break;
				case INFO:
				case CONTAINER:
				case APPLICATION:
					logger.logp(Level.INFO, className, methodName, _msg);
					break;
				case WARNING:
					logger.logp(Level.WARNING, className, methodName, _msg);
					sWarning++;
					break;
				case SEVERE:
					logger.logp(Level.SEVERE, className, methodName, _msg);
					sSevere++;
					break;
				default:
					logger.logp(Level.INFO, className, methodName, "Attempting to log invalid type");
					break;
			}
		}
	}
	
	
	public static void config(String aBasePath, XMLReadable config)
	{	SysLogger.resetHandlers();
		try
		{	List n=config.getNodes("/system/log/handler");
			Iterator i=n.iterator();
			int j=1;
			while(i.hasNext())
			{	XMLReadable xr=new XMLReadable((Node)i.next());
				try
				{	XMLReadable instance=new XMLReadable((Node)xr.getNodes("instance").get(0));
					Handler h=(Handler)Utils.constructFromXML(instance);
					h.setLevel(Level.ALL);
					String classname=xr.getText("formatterClass");
					if(!classname.equals(""))
					{	Class c=Class.forName(classname);
						java.util.logging.Formatter f=(java.util.logging.Formatter)c.newInstance();
						h.setFormatter(f);
					}
					SysLogger.addHandler(h);
				}
				catch(Throwable e)
				{   System.err.println("LOG handler "+j+" couldn't be loaded");
					e.printStackTrace();
				}
				j++;
			}
			resetLevels();
			n=config.getNodes("/system/log/level");
			Node parent=(Node)n.get(0);
			Node l=XMLUtils.getFirstChildElement(parent);
			while(l!=null)
			{	String id=l.getNodeName();
				Integer level=(Integer)sMap.get(id);
				SysLogger.setLoggingFor(level.intValue(), true);
				l=XMLUtils.getNextSiblingElement(l);
			}
		}
		catch(Exception e)
		{	System.err.println("Problem configuring Loggers");
			e.printStackTrace();
		}
	}
	
	public static void resetStats()
	{	sSevere=0;
		sWarning=0;
	}
	
	private static void resetLevels()
	{	//Turn off all levels
		SysLogger.setLoggingFor(SysLogger.DEBUG, false);
		SysLogger.setLoggingFor(SysLogger.FINE, false);
		SysLogger.setLoggingFor(SysLogger.INFO, false);
		SysLogger.setLoggingFor(SysLogger.WARNING, false);
		SysLogger.setLoggingFor(SysLogger.SEVERE, false);
		SysLogger.setLoggingFor(SysLogger.APPLICATION, false);
		SysLogger.setLoggingFor(SysLogger.CONTAINER, false);
		SysLogger.setLoggingFor(SysLogger.CACHE, false);
	}
	
	public static int getErrorCount()
	{	return sSevere;
	}
	public static int getWarningCount()
	{	return sWarning;
	}
	
}