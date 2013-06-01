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


import java.io.IOException;

import junit.framework.TestCase;

import org.example.services.Greeting;
import org.example.services.GreetingService;
import org.example.services.ThrowException;
import org.example.services.ThrowExceptionService;
import org.jaffre.client.spi.SocketJaffreClient;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.SocketJaffreConnector;


/**
 * @author Alexander Veit
 */
public final class SocketTransportTestCase extends TestCase
{
	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		System.setProperty("org.jaffre.loggerClass", TestLogger.class.getName());
	}


	@Override
	public void tearDown() throws Exception
	{
		Thread.sleep(100);
		TestLogger.clear();

		super.tearDown();
	}


	public void testException() throws Exception
	{
		TestLogger.push(new TestLogger.DefaultTestLogger()
		{
			@Override
			public void error(String p_strMessage, Throwable p_e)
			{
				if (!"A test exception.".equals(p_e.getMessage()))
					throw new RuntimeException(p_strMessage, p_e);
			}
		});

		// setup the server
		final JaffreServer l_server;

		l_server = new DefaultJaffreServer();

		l_server.registerInterface(ThrowException.class, new ThrowExceptionService());

		final SocketJaffreConnector l_connector;

		l_connector = new SocketJaffreConnector();

		l_connector.setServer(l_server);
		l_connector.setBindingAddress("localhost");
		l_connector.setPort(9876);

		l_connector.setCoreThreadPoolSize(1);

		// start the server
		l_connector.start();
		Thread.sleep(20);

		try
		{
			// setup the client
			final SocketJaffreClient l_client;
			final ThrowException   l_interface;

			l_client = new SocketJaffreClient();

			l_client.setServiceAddress("localhost");
			l_client.setServicePort(9876);

			l_interface = l_client.getProxy(ThrowException.class);

			try
			{
				// make the remote call
				l_interface.throwException("dummy");

				fail("An IOEXception must occur.");
			}
			catch (IOException l_e)
			{
				assertEquals(
					ThrowExceptionService.class.getName(),
					l_e.getStackTrace()[0].getClassName());
			}
			finally
			{
				l_client.dispose();
			}
		}
		finally
		{
			l_connector.stop();
			Thread.sleep(20);
		}
	}


	public void testKeepAlive() throws Exception
	{
		_testKeepAlive(9877, true);
	}


	public void testNoKeepAlive() throws Exception
	{
		_testKeepAlive(9878, false);
	}


	public void _testKeepAlive(int p_iPort, boolean p_bKeepAlive) throws Exception
	{
		TestLogger.push(new TestLogger.DefaultTestLogger()
		{
			@Override
			public void error(String p_strMessage, Throwable p_e)
			{
				throw new RuntimeException(p_strMessage, p_e);
			}
		});

		// setup the server
		final JaffreServer l_server;

		l_server = new DefaultJaffreServer();

		l_server.registerInterface(Greeting.class, new GreetingService());

		final SocketJaffreConnector l_connector;

		l_connector = new SocketJaffreConnector();

		l_connector.setServer(l_server);
		l_connector.setBindingAddress("localhost");
		l_connector.setPort(p_iPort);

		l_connector.setCoreThreadPoolSize(1);

		// start the server
		l_connector.start();
		Thread.sleep(20);

		try
		{
			// setup the client
			final SocketJaffreClient l_client;
			final Greeting           l_interface;

			l_client = new SocketJaffreClient();

			l_client.setServiceAddress("localhost");
			l_client.setServicePort(p_iPort);
			l_client.setKeepAlive(p_bKeepAlive);

			l_interface = l_client.getProxy(Greeting.class);

			try
			{
				// make the remote call
				assertEquals("Hello world!", l_interface.greet());
			}
			finally
			{
				l_client.dispose();
			}
		}
		finally
		{
			l_connector.stop();
			Thread.sleep(100);
		}
	}
}
