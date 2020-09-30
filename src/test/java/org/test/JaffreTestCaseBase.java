/*
 * (C) Copyright 2008-2017 Alexander Veit
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


package org.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.jaffre.JaffreConfigurationException;
import org.opentest4j.AssertionFailedError;


/**
 * Base class for all tests of this project.
 * @author Alexander Veit
 */
public abstract class JaffreTestCaseBase
{
	/**
	 * Executes code that might throw an exception.
	 */
	public static interface RunnableEx
	{
		public void run()
			throws Exception;
	}


	/**
	 * Assert that an <code>{@link IllegalArgumentException}</code> occurs.
	 * @param p_run The code to be executed.
	 * @return The exception message.
	 */
	protected static String assertIAE(RunnableEx p_run)
	{
		return _assertException(IllegalArgumentException.class, p_run, null);
	}


	/**
	 * Assert that an <code>{@link IllegalArgumentException}</code> occurs.
	 * Optionally check the exception message.
	 * @param p_run The code to be executed.
	 * @param p_strMsg The expected exception message, or <code>null</code>
	 *    if the message should not be checked.
	 * @return The exception message.
	 */
	protected static String assertIAE(RunnableEx p_run, String p_strMsg)
	{
		return _assertException(IllegalArgumentException.class, p_run, p_strMsg);
	}


	/**
	 * Assert that an <code>{@link IllegalStateException}</code> occurs.
	 * @param p_run The code to be executed.
	 * @return The exception message.
	 */
	protected static String assertISE(RunnableEx p_run)
	{
		return _assertException(IllegalStateException.class, p_run, null);
	}


	/**
	 * Assert that an <code>{@link IllegalStateException}</code> occurs.
	 * Optionally check the exception message.
	 * @param p_run The code to be executed.
	 * @param p_strMsg The expected exception message, or <code>null</code>
	 *    if the message should not be checked.
	 * @return The exception message.
	 */
	protected static String assertISE(RunnableEx p_run, String p_strMsg)
	{
		return _assertException(IllegalStateException.class, p_run, p_strMsg);
	}


	/**
	 * Assert that an <code>{@link IOException}</code> occurs.
	 * @param p_run The code to be executed.
	 * @return The exception message.
	 */
	protected static String assertIOE(RunnableEx p_run)
	{
		return _assertException(IOException.class, p_run, null);
	}


	/**
	 * Assert that an <code>{@link IOException}</code> occurs.
	 * Optionally check the exception message.
	 * @param p_run The code to be executed.
	 * @param p_strMsg The expected exception message, or <code>null</code>
	 *    if the message should not be checked.
	 * @return The exception message.
	 */
	protected static String assertIOE(RunnableEx p_run, String p_strMsg)
	{
		return _assertException(IOException.class, p_run, p_strMsg);
	}


	/**
	 * Assert that an <code>{@link JaffreConfigurationException}</code> occurs.
	 * @param p_run The code to be executed.
	 * @return The exception message.
	 */
	protected static String assertJCE(RunnableEx p_run)
	{
		return _assertException(JaffreConfigurationException.class, p_run, null);
	}


	/**
	 * Assert that an <code>{@link JaffreConfigurationException}</code> occurs.
	 * Optionally check the exception message.
	 * @param p_strMsg The expected exception message, or <code>null</code>
	 *    if the message should not be checked.
	 * @param p_run The code to be executed.
	 * @return The exception message.
	 */
	protected static String assertJCE(RunnableEx p_run, String p_strMsg)
	{
		return _assertException(JaffreConfigurationException.class, p_run, p_strMsg);
	}


	private static String _assertException(Class<? extends Throwable> p_classEx,
	                                       RunnableEx                 p_run,
	                                       String                     p_strExpectedMsg)
	{
		try
		{
			p_run.run();

			throw new AssertionFailedError("A " + p_classEx.getName() + " must occur.");
		}
		catch (AssertionError l_e)
		{
			throw l_e;
		}
		catch (Throwable l_e)
		{
			if (!p_classEx.isInstance(l_e))
				assertEquals(p_classEx.getName(), _stackTrace(l_e));

			if (p_strExpectedMsg != null)
				assertEquals(p_strExpectedMsg, l_e.getMessage());

			return l_e.getMessage();
		}
	}


	private static String _stackTrace(Throwable p_e)
	{
		final StringWriter l_sw;

		l_sw = new StringWriter(1024);

		p_e.printStackTrace(new PrintWriter(l_sw, true));

		return l_sw.toString();
	}


	protected static void assertArraysEquals(Object[] p_array1, Object[] p_array2)
	{
		assertTrue(Arrays.equals(p_array1, p_array2));
	}
}
