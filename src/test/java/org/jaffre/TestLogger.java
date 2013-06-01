/* $Id: $
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

package org.jaffre;


import java.util.Stack;

import org.jaffre.spi.NoopLogger;


/**
 * @author Alexander Veit
 */
public class TestLogger implements Logger
{
	private static final TestLogger ms_instance = new TestLogger();

	private final DefaultTestLogger m_default = new DefaultTestLogger();

	private Stack<Logger> m_stack = new Stack<Logger>();


	public static class DefaultTestLogger extends NoopLogger
	{
		public DefaultTestLogger()
		{
		}


		/**
		 * @return Always <code>true</code>.
		 * @see org.jaffre.Logger#isErrorEnabled().
		 */
		@Override
		public boolean isErrorEnabled()
		{
			return true;
		}


		/**
		 * Always throws a <code>RuntimeException</code> with
		 * the given exception as cause.
		 */
		@Override
		public void error(String p_strMessage, Throwable p_e)
		{
			throw new RuntimeException(p_strMessage, p_e);
		}


		/**
		 * Always throws a <code>RuntimeException</code>.
		 */
		@Override
		public void error(String p_strMessage)
		{
			throw new RuntimeException(p_strMessage);
		}


		/**
		 * @return Always <code>true</code>.
		 * @see org.jaffre.Logger#isWarnEnabled().
		 */
		@Override
		public boolean isWarnEnabled()
		{
			return true;
		}


		/**
		 * Always throws a <code>RuntimeException</code> with
		 * the given exception as cause.
		 */
		@Override
		public void warn(String p_strMessage, Throwable p_e)
		{
			throw new RuntimeException(p_strMessage, p_e);
		}


		/**
		 * Always throws a <code>RuntimeException</code>.
		 */
		@Override
		public void warn(String p_strMessage)
		{
			throw new RuntimeException(p_strMessage);
		}
	}


	public TestLogger()
	{
	}


	public static void push(Logger p_logger)
	{
		ms_instance.m_stack.push(p_logger);
	}


	public static Logger pop()
	{
		return ms_instance.m_stack.pop();
	}


	public static void clear()
	{
		ms_instance.m_stack.clear();
	}


	private Logger _delegate()
	{
		if (ms_instance.m_stack.isEmpty())
			return ms_instance.m_default;
		else
			return ms_instance.m_stack.peek();
	}


	/*
	 * @see org.jaffre.Logger#isErrorEnabled()
	 */
	@Override
	public boolean isErrorEnabled()
	{
		return _delegate().isErrorEnabled();
	}


	/*
	 * @see org.jaffre.Logger#error(java.lang.String)
	 */
	@Override
	public void error(String p_strMessage)
	{
		_delegate().error(p_strMessage);
	}


	/*
	 * @see org.jaffre.Logger#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(String p_strMessage, Throwable p_e)
	{
		_delegate().error(p_strMessage, p_e);
	}


	/*
	 * @see org.jaffre.Logger#isWarnEnabled()
	 */
	@Override
	public boolean isWarnEnabled()
	{
		return _delegate().isWarnEnabled();
	}


	/*
	 * @see org.jaffre.Logger#warn(java.lang.String)
	 */
	@Override
	public void warn(String p_strMessage)
	{
		_delegate().warn(p_strMessage);
	}


	/*
	 * @see org.jaffre.Logger#warn(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void warn(String p_strMessage, Throwable p_e)
	{
		_delegate().warn(p_strMessage, p_e);
	}


	/*
	 * @see org.jaffre.Logger#isInfoEnabled()
	 */
	@Override
	public boolean isInfoEnabled()
	{
		return _delegate().isInfoEnabled();
	}


	/*
	 * @see org.jaffre.Logger#info(java.lang.String)
	 */
	@Override
	public void info(String p_strMessage)
	{
		_delegate().info(p_strMessage);
	}


	/*
	 * @see org.jaffre.Logger#info(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void info(String p_strMessage, Throwable p_e)
	{
		_delegate().info(p_strMessage, p_e);
	}


	/*
	 * @see org.jaffre.Logger#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled()
	{
		return _delegate().isDebugEnabled();
	}


	/*
	 * @see org.jaffre.Logger#debug(java.lang.String)
	 */
	@Override
	public void debug(String p_strMessage)
	{
		_delegate().debug(p_strMessage);
	}


	/*
	 * @see org.jaffre.Logger#debug(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void debug(String p_strMessage, Throwable p_e)
	{
		_delegate().debug(p_strMessage, p_e);
	}


	/*
	 * @see org.jaffre.Logger#isTraceEnabled()
	 */
	@Override
	public boolean isTraceEnabled()
	{
		return _delegate().isTraceEnabled();
	}


	/*
	 * @see org.jaffre.Logger#trace(java.lang.String)
	 */
	@Override
	public void trace(String p_strMessage)
	{
		_delegate().trace(p_strMessage);
	}


	/*
	 * @see org.jaffre.Logger#trace(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void trace(String p_strMessage, Throwable p_e)
	{
		_delegate().trace(p_strMessage, p_e);
	}
}
