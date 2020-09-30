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


import org.example.services.Greeting;
import org.example.services.GreetingService;
import org.jaffre.client.JaffreClient;
import org.jaffre.client.spi.LocalVmJaffreClient;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.LocalVmJaffreConnector;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class LocalVmTransportTestCase extends JaffreTestCaseBase
{
	@Override
	protected void tearDown() throws Exception
	{
		System.gc();
		assertNull(LocalVmJaffreConnector.getInstance().getServer());

		super.tearDown();
	}


	public void test()
	{
		// setup the server
		final JaffreServer l_server;

		l_server = new DefaultJaffreServer();

		l_server.registerInterface(Greeting.class, new GreetingService());

		LocalVmJaffreConnector.getInstance().setServer(l_server);

		// setup the client
		final JaffreClient l_client;
		final Greeting   l_greeting;

		l_client   = new LocalVmJaffreClient();
		l_greeting = l_client.getProxy(Greeting.class);

		assertEquals("Hello world!", l_greeting.greet());

		l_client.dispose();
	}
}
