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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreCallFrameSerializer;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.JaffreReturnFrameSerializer;
import org.jaffre.spi.DefaultJaffreCallFrameSerializer;
import org.jaffre.spi.DefaultJaffreReturnFrameSerializer;
import org.junit.jupiter.api.Test;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class AbstractSocketJaffreConnectorTestCase extends JaffreTestCaseBase
{
	private static final JaffreCallFrameSerializer CALL = new JaffreCallFrameSerializer()
	{
		@Override
		public void serialize(JaffreCallFrame p_frame, OutputStream p_out)
		{
			throw new AssertionError();
		}

		@Override
		public JaffreCallFrame deserialize(InputStream p_in)
		{
			throw new AssertionError();
		}
	};


	private static final JaffreReturnFrameSerializer RTRN = new JaffreReturnFrameSerializer()
	{
		@Override
		public void serialize(JaffreReturnFrame p_frame, OutputStream p_out)
		{
			throw new AssertionError();
		}

		@Override
		public JaffreReturnFrame deserialize(InputStream p_in)
		{
			throw new AssertionError();
		}
	};


	@Test
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

			@Override
			public int getLocalPort()
			{
				return -1;
			}
		};

		assertEquals(1000L, l_connector.getStopTimeout());
		assertJCE(() -> l_connector.setStopTimeout(-1L));
		l_connector.setStopTimeout(65432L);
		assertEquals(65432L, l_connector.getStopTimeout());

		assertNull(l_connector.getBindingInetAddress());
		l_connector.setBindingAddress("127.0.0.123");
		assertEquals("127.0.0.123", l_connector.getBindingInetAddress().getHostAddress());

		assertEquals(0, l_connector.getPort());
		assertJCE(() -> l_connector.setPort(-1));
		l_connector.setPort(4711);
		assertEquals(4711, l_connector.getPort());

		assertEquals(4, l_connector.getCoreThreadPoolSize());
		assertJCE(() -> l_connector.setCoreThreadPoolSize(-1));
		l_connector.setCoreThreadPoolSize(7);
		assertEquals(7, l_connector.getCoreThreadPoolSize());

		assertEquals(10, l_connector.getMaxThreadPoolSize());
		assertJCE(() -> l_connector.setMaxThreadPoolSize(-1));
		l_connector.setMaxThreadPoolSize(11);
		assertEquals(11, l_connector.getMaxThreadPoolSize());

		assertNull(l_connector.getServer());
		l_connector.setServer(new DefaultJaffreServer());
		assertNotNull(l_connector.getServer());

		assertTrue(l_connector.getCallFrameSerializer() instanceof DefaultJaffreCallFrameSerializer);
		l_connector.setCallFrameSerializer(CALL);
		assertSame(CALL, l_connector.getCallFrameSerializer());

		assertTrue(l_connector.getReturnFrameSerializer() instanceof DefaultJaffreReturnFrameSerializer);
		l_connector.setReturnFrameSerializer(RTRN);
		assertSame(RTRN, l_connector.getReturnFrameSerializer());
	}


	@Test
	public void testConfigureStarted()
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

			@Override
			public int getLocalPort()
			{
				return -1;
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


	@Test
	public void testBadCofigurationParams()
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

			@Override
			public int getLocalPort()
			{
				return -1;
			}
		};

		assertJCE(() -> l_connector.setPort(-1), "Illegal port number -1.");
		assertJCE(() -> l_connector.setPort(65536), "Illegal port number 65536.");
		assertJCE(() -> l_connector.setCoreThreadPoolSize(0), "0 is not a valid thread pool size.");
		assertJCE(() -> l_connector.setMaxThreadPoolSize(0), "0 is not a valid maximum thread pool size.");
		assertJCE(() -> l_connector.setStopTimeout(-1L), "Negative timeout value.");
	}
}
