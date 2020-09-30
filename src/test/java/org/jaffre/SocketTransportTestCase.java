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


package org.jaffre;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.example.services.Greeting;
import org.example.services.GreetingService;
import org.example.services.ThrowException;
import org.example.services.ThrowExceptionService;
import org.jaffre.client.spi.SocketJaffreClient;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.SocketJaffreConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class SocketTransportTestCase extends JaffreTestCaseBase
{
	@BeforeEach
	public void setUp()
	{
		System.setProperty("org.jaffre.loggerClass", TestLogger.class.getName());
	}


	@AfterEach
	public void tearDown() throws Exception
	{
		Thread.sleep(100);
		TestLogger.clear();
	}


	@Test
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
		final JaffreServer          l_server;
		final SocketJaffreConnector l_connector;

		l_server = new DefaultJaffreServer();

		l_server.registerInterface(ThrowException.class, new ThrowExceptionService());

		l_connector = new SocketJaffreConnector();

		l_connector.setServer(l_server);
		l_connector.setBindingAddress("localhost");
		l_connector.setPort(0);

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
			l_client.setServicePort(l_connector.getLocalPort());

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


	@Test
	public void testKeepAlive() throws Exception
	{
		_testKeepAlive(true);
	}


	@Test
	public void testNoKeepAlive() throws Exception
	{
		_testKeepAlive(false);
	}


	private void _testKeepAlive(boolean p_bKeepAlive)
		throws Exception
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
		l_connector.setPort(0);

		l_connector.setCoreThreadPoolSize(1);

		// start the server
		l_connector.start();
		Thread.sleep(20);

		assertTrue(l_connector.getLocalPort() > 0);

		try
		{
			// setup the client
			final SocketJaffreClient l_client;
			final Greeting           l_interface;

			l_client = new SocketJaffreClient();

			l_client.setServiceAddress("localhost");
			l_client.setServicePort(l_connector.getLocalPort());
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
