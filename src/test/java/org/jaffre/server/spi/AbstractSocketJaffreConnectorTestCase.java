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


package org.jaffre.server.spi;


import java.net.InetAddress;

import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class AbstractSocketJaffreConnectorTestCase extends JaffreTestCaseBase
{
	public void testConfigureStoppedState() throws Exception
	{
		final AbstractSocketJaffreConnector l_connector;

		l_connector = new AbstractSocketJaffreConnector()
		{
			@Override
			public void start()
			{
				fail();
			}

			@Override
			public boolean isRunning()
			{
				return false;
			}

			@Override
			public void stop()
			{
				fail();
			}
		};

		assertNull(l_connector.getBindingInetAddress());
		l_connector.setBindingAddress("10.1.1.1");
		assertEquals("10.1.1.1", l_connector.getBindingInetAddress().getHostAddress());

		l_connector.setBindingInetAddress(InetAddress.getByName("10.1.1.2"));
		assertEquals("10.1.1.2", l_connector.getBindingInetAddress().getHostAddress());

		l_connector.setPort(8815);
		assertEquals(8815, l_connector.getPort());

		l_connector.setCoreThreadPoolSize(101);
		assertEquals(101, l_connector.getCoreThreadPoolSize());

		l_connector.setMaxThreadPoolSize(1331);
		assertEquals(1331, l_connector.getMaxThreadPoolSize());

		l_connector.setStopTimeout(12345);
		assertEquals(12345, l_connector.getStopTimeout());

		assertNull(l_connector.getServer());
		l_connector.setServer(new DefaultJaffreServer());
		assertNotNull(l_connector.getServer());
	}


	public void testConfigureStarted() throws Exception
	{
		final AbstractSocketJaffreConnector l_connector;

		l_connector = new AbstractSocketJaffreConnector()
		{
			@Override
			public void start()
			{
				fail();
			}

			@Override
			public boolean isRunning()
			{
				return true;
			}

			@Override
			public void stop()
			{
				fail();
			}
		};

		l_connector.setStopTimeout(12); // can be set after the connector has been started

		assertISE(() -> l_connector.setBindingAddress("10.1.1.1"));
		assertISE(() -> l_connector.setBindingInetAddress(InetAddress.getByName("10.1.1.2")));
		assertISE(() -> l_connector.setPort(8815));
		assertISE(() -> l_connector.setCoreThreadPoolSize(101));
		assertISE(() -> l_connector.setMaxThreadPoolSize(1331));
		assertISE(() -> l_connector.setServer(new DefaultJaffreServer()));
	}


	public void testBadCofigurationParams() throws Exception
	{
		final AbstractSocketJaffreConnector l_connector;

		l_connector = new AbstractSocketJaffreConnector()
		{
			@Override
			public void start()
			{
				fail();
			}

			@Override
			public boolean isRunning()
			{
				return false;
			}

			@Override
			public void stop()
			{
				fail();
			}
		};

		assertJCE(() -> l_connector.setPort(-1), "Illegal port number -1.");
		assertJCE(() -> l_connector.setPort(65536), "Illegal port number 65536.");
		assertJCE(() -> l_connector.setCoreThreadPoolSize(0), "0 is not a valid thread pool size.");
		assertJCE(() -> l_connector.setMaxThreadPoolSize(0), "0 is not a valid maximum thread pool size.");
		assertJCE(() -> l_connector.setStopTimeout(-1L), "Negative timeout value.");
	}
}
