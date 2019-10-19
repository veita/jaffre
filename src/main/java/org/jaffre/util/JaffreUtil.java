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


package org.jaffre.util;


import java.io.Closeable;
import java.lang.reflect.Method;
import java.net.Socket;


/**
 * @author Alexander Veit
 */
public final class JaffreUtil
{
	/**
	 * @param <T> The closable type.
	 * @param p_closee The closeable object to be closed.
	 * @return Always <code>null</code>.
	 */
	public static <T extends Closeable> T close(T p_closee)
	{
		if (p_closee != null)
		{
			try
			{
				p_closee.close();
			}
			catch (Throwable l_t)
			{
				assert false;
			}
		}

		return null;
	}


	/**
	 * @param <T> The closable type.
	 * @param p_closee The socket to be closed.
	 * @return Always <code>null</code>.
	 */
	public static <T extends Socket> T close(T p_closee)
	{
		if (p_closee != null)
		{
			try
			{
				p_closee.close();
			}
			catch (Throwable l_t)
			{
				assert false;
			}
		}

		return null;
	}


	/**
	 * Check if the given <code>java.lang.Throwable</code> is a declared
	 * throwable of the given method.
	 * @param p_e The throwable.
	 * @param p_method The method.
	 * @return <code>true</code> if <code>p_method</code> declares to throw
	 *    <code>p_e</code>, or <code>false</code> otherwise.
	 * @throws IllegalArgumentException
	 *    If either of the method parameters is <code>null</code>.
	 */
	public static boolean isDeclaredThrowable(Throwable p_e, Method p_method)
	{
		final Class<? extends Throwable> l_clsThrowable;

		if (p_e == null)
			throw new IllegalArgumentException("No throwable.");

		if (p_method == null)
			throw new IllegalArgumentException("No method.");

		l_clsThrowable = p_e.getClass();

		for (final Class<?> l_clsException : p_method.getExceptionTypes())
		{
			if (l_clsException.isAssignableFrom(l_clsThrowable))
				return true;
		}

		return false;
	}
}
