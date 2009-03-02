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
 * File:          $RCSfile: ConsoleFormatter.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/09/13 14:32:15 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import java.util.logging.*;

/** A custom formatter for simple tidy output on the console
 * @author tab
 */
public class ConsoleFormatter extends java.util.logging.Formatter
{
	/** Format the given log record and return the formatted string.
	 * <p>
	 * The resulting formatted String will normally include a
	 * localized and formated version of the LogRecord's message field.
	 * The Formatter.formatMessage convenience method can (optionally)
	 * be used to localize and format the message field.
	 *
	 * @param record the log record to be formatted.
	 * @return the formatted log record
	 *
	 */
	public String format(LogRecord record)
	{	StringBuffer result = new StringBuffer(record.getMessage().length()+64);
		if (record.getLevel()==Level.WARNING || record.getLevel() ==Level.SEVERE)
		{	result.append(record.getLevel().getName());
			result.append(' ');
			result.append(record.getMessage().replace(SysLogger.NEWLINE_SUBSTITUTE,'\n'));
			result.append(" in ");
			result.append(record.getSourceClassName());
			result.append('\n');
		}
		else
		{	result.append(record.getMessage().replace(SysLogger.NEWLINE_SUBSTITUTE,'\n'));
			result.append('\n');
		}	
		return result.toString();
	}
}
