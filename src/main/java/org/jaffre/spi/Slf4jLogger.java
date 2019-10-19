/*
 * (C) Copyright 2008-2019 Alexander Veit
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


import org.jaffre.Logger;


/**
 * This logger uses a SLF4J logger for logging.
 * @author Alexander Veit
 */
public class Slf4jLogger implements Logger
{
	private final org.slf4j.Logger m_logger;


	public Slf4jLogger(Class<?> p_class)
	{
		m_logger = org.slf4j.LoggerFactory.getLogger(p_class);
	}


	@Override
	public boolean isErrorEnabled()
	{
		return m_logger.isErrorEnabled();
	}


	@Override
	public void error(String p_strMessage)
	{
		m_logger.error(p_strMessage);
	}


	@Override
	public void error(String p_strMessage, Throwable p_e)
	{
		m_logger.error(p_strMessage, p_e);
	}


	@Override
	public boolean isWarnEnabled()
	{
		return m_logger.isWarnEnabled();
	}


	@Override
	public void warn(String p_strMessage)
	{
		m_logger.warn(p_strMessage);
	}


	@Override
	public void warn(String p_strMessage, Throwable p_e)
	{
		m_logger.warn(p_strMessage, p_e);
	}


	@Override
	public boolean isInfoEnabled()
	{
		return m_logger.isInfoEnabled();
	}


	@Override
	public void info(String p_strMessage)
	{
		m_logger.info(p_strMessage);
	}


	@Override
	public void info(String p_strMessage, Throwable p_e)
	{
		m_logger.info(p_strMessage, p_e);
	}


	@Override
	public boolean isDebugEnabled()
	{
		return m_logger.isDebugEnabled();
	}


	@Override
	public void debug(String p_strMessage)
	{
		m_logger.debug(p_strMessage);
	}


	@Override
	public void debug(String p_strMessage, Throwable p_e)
	{
		m_logger.debug(p_strMessage, p_e);
	}


	@Override
	public boolean isTraceEnabled()
	{
		return m_logger.isTraceEnabled();
	}


	@Override
	public void trace(String p_strMessage)
	{
		m_logger.trace(p_strMessage);
	}


	@Override
	public void trace(String p_strMessage, Throwable p_e)
	{
		m_logger.trace(p_strMessage, p_e);
	}
}
