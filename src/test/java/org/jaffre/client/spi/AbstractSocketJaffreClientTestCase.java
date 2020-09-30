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


package org.jaffre.client.spi;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.jaffre.spi.DefaultJaffreCallFrameSerializer;
import org.jaffre.spi.DefaultJaffreReturnFrameSerializer;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class AbstractSocketJaffreClientTestCase extends JaffreTestCaseBase
{
	@Test
	public void testConfigure()
		throws UnknownHostException
	{
		final AbstractSocketJaffreClient l_client;

		l_client = new AbstractSocketJaffreClient()
		{
			@Override
			protected Object invokeImpl(Class<?> p_interface, Object p_proxy, Method p_method, Object[] p_args)
			{
				throw new AssertionFailedError("invokeImpl called.");
			}
		};

		assertTrue(l_client.getCallFrameSerializer() instanceof DefaultJaffreCallFrameSerializer);
		l_client.setCallFrameSerializer(null);
		assertNull(l_client.getCallFrameSerializer());

		assertTrue(l_client.getReturnFrameSerializer() instanceof DefaultJaffreReturnFrameSerializer);
		l_client.setReturnFrameSerializer(null);
		assertNull(l_client.getReturnFrameSerializer());

		assertNull(l_client.getServiceInetAddress());
		l_client.setServiceAddress("10.1.1.1");
		assertEquals("10.1.1.1", l_client.getServiceInetAddress().getHostAddress());
		l_client.setServiceInetAddress(InetAddress.getByName("10.1.1.2"));
		assertEquals("10.1.1.2", l_client.getServiceInetAddress().getHostAddress());

		assertEquals(-1, l_client.getServicePort());
		l_client.setServicePort(8815);
		assertEquals(8815, l_client.getServicePort());

		assertTrue(l_client.isKeepAlive());
		l_client.setKeepAlive(false);
		assertFalse(l_client.isKeepAlive());

		assertFalse(l_client.isSendCookies());
		l_client.setSendCookies(true);
		assertTrue(l_client.isSendCookies());

		assertFalse(l_client.isAcceptCookies());
		l_client.setAcceptCookies(true);
		assertTrue(l_client.isAcceptCookies());
	}


	@Test
	public void testBadCofigurationParams()
	{
		final AbstractSocketJaffreClient l_client;

		l_client = new AbstractSocketJaffreClient()
		{
			@Override
			protected Object invokeImpl(Class<?> p_interface, Object p_proxy, Method p_method, Object[] p_args)
			{
				throw new AssertionFailedError("invokeImpl called.");
			}
		};

		assertJCE(() -> l_client.setServicePort(-1), "Illegal port number -1.");
		assertJCE(() -> l_client.setServicePort(65536), "Illegal port number 65536.");
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testGetProxy() throws Exception
	{
		final AbstractSocketJaffreClient l_client;
		final Callable<String>           l_proxy;

		l_client = new AbstractSocketJaffreClient()
		{
			@Override
			protected Object invokeImpl(Class<?> p_interface, Object p_proxy, Method p_method, Object[] p_args)
			{
				assertTrue(Callable.class.isAssignableFrom(p_interface));

				return "testGetProxy:returnValue";
			}
		};

		assertIAE(() -> l_client.getProxy(String.class), "java.lang.String is not an interface.");

		l_proxy = l_client.getProxy(Callable.class);

		assertEquals("testGetProxy:returnValue", l_proxy.call());

		l_client.dispose();
	}
}
