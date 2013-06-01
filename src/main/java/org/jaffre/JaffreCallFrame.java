/* $Id: JaffreCallFrame.java 394 2009-03-21 20:28:26Z  $
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


import java.io.Serializable;
import java.util.Arrays;


/**
 * @author Alexander Veit
 */
public final class JaffreCallFrame implements Serializable
{
	private static final long serialVersionUID = 3888811708046652806L;

	private static final Class<?>[] NO_TYPES = new Class<?>[0];

	private static final Object[] NO_ARGS = new Object[0];

	private transient int m_iHashCode;

	/** Bit flags. */
	private int f = 0; // must be the first field in the class definition

	/** The interface to be called. */
	private final Class<?> i;

	/** The method to be called. */
	private final String m;

	/** The parameter types. */
	private final Class<?>[] t;

	/** The arguments. */
	private final Object[] a;

	/** User data, e.g. cookies. */
	private Object u;


	public JaffreCallFrame(Class<?>   p_clsInterface,
	                       String     p_strMethodName,
	                       Class<?>[] p_parameterTypes,
	                       Object[]   p_args)
	{
		this(p_clsInterface, p_strMethodName, p_parameterTypes, p_args, null);
	}


	public JaffreCallFrame(Class<?>   p_clsInterface,
	                       String     p_strMethodName,
	                       Class<?>[] p_parameterTypes,
	                       Object[]   p_args,
	                       Object     p_userData)
	{
		final Class<?>[] l_parameterTypes;
		final Object[]   l_args;

		if (p_clsInterface == null)
			throw new IllegalArgumentException("No interface class.");

		if (!p_clsInterface.isInterface())
			throw new IllegalArgumentException(p_clsInterface.getName() + " is not an interface.");

		if (p_strMethodName == null)
			throw new IllegalArgumentException("No method name.");

		if (p_parameterTypes != null && p_parameterTypes.length > 0)
			l_parameterTypes = p_parameterTypes;
		else
			l_parameterTypes = NO_TYPES;

		if (p_args != null && p_args.length > 0)
			l_args = p_args;
		else
			l_args = NO_ARGS;

		if (l_parameterTypes.length != l_args.length)
		{
			throw new IllegalArgumentException
				("The number of parameters does not match the number of arguments.");
		}

		i = p_clsInterface;
		m = p_strMethodName;
		t = l_parameterTypes;
		a = l_args;
		u = p_userData;
	}


	public Class<?> getInterface()
	{
		return i;
	}


	public String getMethodName()
	{
		return m;
	}


	public boolean hasParameters()
	{
		assert t != null;

		return t.length > 0;
	}


	public Class<?>[] getParameterTypes()
	{
		assert t != null;

		return t;
	}


	public Object[] getParameters()
	{
		assert a != null;

		return a;
	}


	public boolean hasUserData()
	{
		return u != null;
	}


	public Object getUserData()
	{
		return u;
	}


	public void setUserData(Object p_userData)
	{
		u = p_userData;
	}


	public int getFlags()
	{
		return f;
	}


	public void setFlags(int p_iFlags)
	{
		f = p_iFlags;
	}


	public boolean isInOnly()
	{
		return (f & JAFFRE_FLAG.MEP_IN_ONLY) == JAFFRE_FLAG.MEP_IN_ONLY;
	}


	public void setInOnly()
	{
		f = (f & ~JAFFRE_FLAG.MASK_MEP) | JAFFRE_FLAG.MEP_IN_ONLY;
	}


	public boolean isInOut()
	{
		return (f & JAFFRE_FLAG.MEP_IN_OUT) == JAFFRE_FLAG.MEP_IN_OUT;
	}


	public void setInOut()
	{
		f = (f & ~JAFFRE_FLAG.MASK_MEP) | JAFFRE_FLAG.MEP_IN_OUT;
	}


	/**
	 * Check if the sender wishes to keep the existing connection alive.
	 * @return <code>true</code> if the sender wishes to keep the
	 *    connection alive, or <code>false</code> otherwise.
	 */
	public boolean isKeepAlive()
	{
		return (f & JAFFRE_FLAG.CONNECTION_KEEP_ALIVE) == JAFFRE_FLAG.CONNECTION_KEEP_ALIVE;
	}


	/**
	 * Check if the sender wishes to close the existing connection.
	 * @return The negation of {@link #isKeepAlive()}.
	 */
	public boolean isClose()
	{
		return !isKeepAlive();
	}


	/**
	 * Set the keep-alive flag.
	 * @param p_bKeepAlive <code>true</code> if the sender wishes to keep
	 *    a connection alive, or <code>false</code> otherwise.
	 */
	public void setKeepAlive(boolean p_bKeepAlive)
	{
		if (p_bKeepAlive)
			f = f | JAFFRE_FLAG.CONNECTION_KEEP_ALIVE;
		else
			f = f & ~JAFFRE_FLAG.CONNECTION_KEEP_ALIVE;
	}


	@Override
	public boolean equals(Object p_other)
	{
		final JaffreCallFrame l_other;

		if (p_other == this)
			return true;

		if (!(p_other instanceof JaffreCallFrame))
			return false;

		l_other = (JaffreCallFrame)p_other;

		return
			f == l_other.f &&
			i.equals(l_other.i) &&
			m.equals(l_other.m) &&
			Arrays.equals(t, l_other.t) &&
			Arrays.equals(a, l_other.a) &&
			(u == null ? l_other.u == null : u.equals(l_other.u));
	}


	@Override
	public int hashCode()
	{
		if (m_iHashCode != 0)
			return m_iHashCode;

		m_iHashCode =
			f ^ i.hashCode() ^ m.hashCode() ^ t.hashCode() ^
			a.hashCode() ^ (u == null ? 0 : u.hashCode());

		return m_iHashCode;
	}
}
