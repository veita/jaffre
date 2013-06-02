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


package org.jaffre.server.spi;


import java.util.UUID;

import junit.framework.TestCase;

import org.example.services.Greeting;
import org.jaffre.client.spi.SocketJaffreClient;
import org.jaffre.server.JaffreServer;
import org.test.TestPort;


/**
 * @author Alexander Veit
 */
public final class SocketJaffreConnectorTestCase extends TestCase
{
//  deactived for Java < 1.7
//	public void testLifeCycle() throws Exception
//	{
//		final SocketJaffreConnector l_connector;
//
//		l_connector = new SocketJaffreConnector();
//
//		l_connector.setServer(new DefaultJaffreServer());
//		l_connector.setBindingAddress("localhost");
//		l_connector.setPort(TestPort.getNext());
//
//		l_connector.setCoreThreadPoolSize(7);
//		Thread.sleep(20);
//		assertEquals(0, l_connector.getNumRunningThreads());
//
//		l_connector.start();
//		Thread.sleep(20);
//		assertEquals(7, l_connector.getNumRunningThreads());
//
//		l_connector.stop();
//		Thread.sleep(20);
//		assertEquals(0, l_connector.getNumRunningThreads());
//
//		l_connector.start();
//		Thread.sleep(20);
//		assertEquals(7, l_connector.getNumRunningThreads());
//
//		l_connector.stop();
//		Thread.sleep(20);
//		assertEquals(0, l_connector.getNumRunningThreads());
//	}


	public void test() throws Exception
	{
		final int    l_iPort;
		final String l_strGreeting;

		l_iPort       = TestPort.getNext();
		l_strGreeting = UUID.randomUUID().toString();

		// setup the server
		final JaffreServer l_server;

		l_server = new DefaultJaffreServer();

		l_server.registerInterface(Greeting.class, new Greeting()
		{
			@Override
			public String greet()
			{
				return l_strGreeting;
			}
		});

		// start a connector connector
		final SocketJaffreConnector l_connector;

		l_connector = new SocketJaffreConnector();

		l_connector.setServer(l_server);
		l_connector.setBindingAddress("localhost");
		l_connector.setPort(l_iPort);

		l_connector.start();

		Thread.sleep(100);

		// setup the client
		final SocketJaffreClient l_client;
		final Greeting           l_greeting;

		l_client = new SocketJaffreClient();

		l_client.setServiceAddress("localhost");
		l_client.setServicePort(l_iPort);

		// do the test
		l_greeting = l_client.getProxy(Greeting.class);

		assertEquals(l_strGreeting, l_greeting.greet());

		// shutdown
		l_client.dispose();

		Thread.sleep(100);

		l_connector.stop();

		Thread.sleep(100);
	}
}
