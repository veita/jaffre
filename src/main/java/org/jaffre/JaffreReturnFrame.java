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


package org.jaffre;


import java.io.Serializable;


/**
 * @author Alexander Veit
 */
public final class JaffreReturnFrame implements Serializable
{
	private static final long serialVersionUID = -2099972322546120988L;

	private transient int m_iHashCode;

	/** A flag indicating an exception result. */
	private final boolean e; // must be the first field in the class definition

	/** Bit flags. */
	private int f = JAFFRE_FLAG.NO_FLAGS; // must be the second field in the class definition

	/** The result object. */
	private final Object r;

	/** User data, e.g. cookies. */
	private Object u;


	public JaffreReturnFrame(Object p_objResult, boolean p_bExceptionResult)
	{
		u = null;
		r = p_objResult;
		e = p_bExceptionResult;
	}


	public Object getResult()
	{
		return r;
	}


	public boolean isExceptionResult()
	{
		return e;
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
	 *    the connection alive, or <code>false</code> otherwise.
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
		final JaffreReturnFrame l_other;

		if (p_other == this)
			return true;

		if (!(p_other instanceof JaffreReturnFrame))
			return false;

		l_other = (JaffreReturnFrame)p_other;

		return
			e == l_other.e &&
			f == l_other.f &&
			(r == null ? l_other.r == null : r.equals(l_other.r)) &&
			(u == null ? l_other.u == null : u.equals(l_other.u));
	}


	@Override
	public int hashCode()
	{
		if (m_iHashCode != 0)
			return m_iHashCode;

		m_iHashCode =
			(e ? 1 : 0) ^
			f ^
			(r == null ? 0 : r.hashCode()) ^
			(u == null ? 0 : u.hashCode());

		return m_iHashCode;
	}
}
