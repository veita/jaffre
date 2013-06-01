/* $Id: DefaultLogger.java 394 2009-03-21 20:28:26Z  $
 *
 * (C) Copyright 2008-2013 Alexander Veit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */


package org.jaffre.spi;


import java.util.logging.Level;

import org.jaffre.Logger;


/**
 * This default logger uses a <code>java.util.logging.Logger</code>
 * for logging.
 * @author Alexander Veit
 */
public class DefaultLogger implements Logger
{
	private java.util.logging.Logger m_logger;


	public DefaultLogger(Class<?> p_class)
	{
		m_logger = java.util.logging.Logger.getLogger(p_class.getName());
	}


	@Override
	public boolean isErrorEnabled()
	{
		return m_logger.isLoggable(Level.SEVERE);
	}


	@Override
	public void error(String p_strMessage)
	{
		m_logger.log(Level.SEVERE, p_strMessage);
	}


	@Override
	public void error(String p_strMessage, Throwable p_e)
	{
		m_logger.log(Level.SEVERE, p_strMessage, p_e);
	}


	@Override
	public boolean isWarnEnabled()
	{
		return m_logger.isLoggable(Level.WARNING);
	}


	@Override
	public void warn(String p_strMessage)
	{
		m_logger.log(Level.WARNING, p_strMessage);
	}


	@Override
	public void warn(String p_strMessage, Throwable p_e)
	{
		m_logger.log(Level.WARNING, p_strMessage, p_e);
	}


	@Override
	public boolean isInfoEnabled()
	{
		return m_logger.isLoggable(Level.INFO);
	}


	@Override
	public void info(String p_strMessage)
	{
		m_logger.log(Level.INFO, p_strMessage);
	}


	@Override
	public void info(String p_strMessage, Throwable p_e)
	{
		m_logger.log(Level.INFO, p_strMessage, p_e);
	}


	@Override
	public boolean isDebugEnabled()
	{
		return m_logger.isLoggable(Level.FINE);
	}


	@Override
	public void debug(String p_strMessage)
	{
		m_logger.log(Level.FINE, p_strMessage);
	}


	@Override
	public void debug(String p_strMessage, Throwable p_e)
	{
		m_logger.log(Level.FINE, p_strMessage, p_e);
	}


	@Override
	public boolean isTraceEnabled()
	{
		return m_logger.isLoggable(Level.FINEST);
	}


	@Override
	public void trace(String p_strMessage)
	{
		m_logger.log(Level.FINEST, p_strMessage);
	}


	@Override
	public void trace(String p_strMessage, Throwable p_e)
	{
		m_logger.log(Level.FINEST, p_strMessage, p_e);
	}
}

