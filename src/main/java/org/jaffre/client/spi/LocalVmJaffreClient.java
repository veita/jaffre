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


package org.jaffre.client.spi;


import org.jaffre.client.JaffreClient;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.LocalVmJaffreConnector;


/**
 * @author Alexander Veit
 */
public final class LocalVmJaffreClient implements JaffreClient
{
	public LocalVmJaffreClient()
	{
	}


	@Override
	public <T> T getProxy(Class<T> p_interface)
	{
		final LocalVmJaffreConnector l_connector;
		final JaffreServer           l_server;

		l_connector = LocalVmJaffreConnector.getInstance();
		l_server    = l_connector.getServer();

		if (l_server == null)
			throw new IllegalStateException("No local VM server.");

		return l_server.getEndpointInterface(p_interface);
	}


	@Override
	public void dispose()
	{
	}
}
