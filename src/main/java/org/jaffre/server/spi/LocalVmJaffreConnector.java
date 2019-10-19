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


package org.jaffre.server.spi;


import java.lang.ref.WeakReference;

import org.jaffre.server.JaffreConnector;
import org.jaffre.server.JaffreServer;


/**
 * @author Alexander Veit
 */
public final class LocalVmJaffreConnector implements JaffreConnector
{
	private static final LocalVmJaffreConnector ms_instance = new LocalVmJaffreConnector();

	private volatile WeakReference<JaffreServer> m_refServer;


	private LocalVmJaffreConnector()
	{
	}


	public static LocalVmJaffreConnector getInstance()
	{
		return ms_instance;
	}


	@Override
	public synchronized void setServer(JaffreServer p_server)
	{
		if (m_refServer != null && m_refServer.get() != null)
			throw new IllegalStateException("Server already registered.");

		m_refServer = new WeakReference<>(p_server);
	}


	@Override
	public synchronized JaffreServer getServer()
	{
		return m_refServer == null ? null : m_refServer.get();
	}


	/**
	 * The implementation of this method does nothing.
	 */
	@Override
	public void start()
	{
		// nothing to do
	}


	/**
	 * This method always returns <code>true</code>.
	 * @return Always <code>true</code>.
	 */
	@Override
	public boolean isRunning()
	{
		return true;
	}


	/**
	 * The implementation of this method does nothing.
	 */
	@Override
	public void stop()
	{
		// nothing to do
	}
}
