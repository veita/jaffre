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
 * A logger that does nothing.
 * @author Alexander Veit
 */
public class NoopLogger implements Logger
{
	public NoopLogger()
	{
	}


	/**
	 * @return Always <code>false</code>.
	 * @see org.jaffre.Logger#isErrorEnabled()
	 */
	@Override
	public boolean isErrorEnabled()
	{
		return false;
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void error(String p_strMessage, Throwable p_e)
	{
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void error(String p_strMessage)
	{
	}


	/**
	 * @return Always <code>false</code>.
	 * @see org.jaffre.Logger#isWarnEnabled()
	 */
	@Override
	public boolean isWarnEnabled()
	{
		return false;
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void warn(String p_strMessage, Throwable p_e)
	{
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void warn(String p_strMessage)
	{
	}


	/**
	 * @return Always <code>false</code>.
	 * @see org.jaffre.Logger#isInfoEnabled()
	 */
	@Override
	public boolean isInfoEnabled()
	{
		return false;
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void info(String p_strMessage, Throwable p_e)
	{
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void info(String p_strMessage)
	{
	}


	/**
	 * @return Always <code>false</code>.
	 * @see org.jaffre.Logger#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled()
	{
		return false;
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void debug(String p_strMessage, Throwable p_e)
	{
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void debug(String p_strMessage)
	{
	}


	/**
	 * @return Always <code>false</code>.
	 * @see org.jaffre.Logger#isTraceEnabled()
	 */
	@Override
	public boolean isTraceEnabled()
	{
		return false;
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void trace(String p_strMessage, Throwable p_e)
	{
	}


	/**
	 * This method does nothing.
	 */
	@Override
	public void trace(String p_strMessage)
	{
	}
}
