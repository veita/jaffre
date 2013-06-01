/* $Id: SSLClientTestCase.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.client;


import junit.framework.TestCase;

import org.example.services.SomeTestMethods;
import org.jaffre.client.spi.SSLSocketJaffreClient;


/**
 * @author Alexander Veit
 */
public final class SSLClientTestCase extends TestCase
{
	private SSLSocketJaffreClient m_client;


	@Override
	public void setUp() throws Exception
	{
		final SSLSocketJaffreClient l_client;

		l_client = new SSLSocketJaffreClient();

		l_client.setServiceAddress("localhost");
		l_client.setServicePort(1443);
		l_client.setKeyStoreType("JCEKS");
		l_client.setKeyStore("resources/keystore");
		l_client.setKeyStorePassword("secret");
		l_client.setTrustStoreType("JCEKS");
		l_client.setTrustStore("resources/truststore");
		l_client.setTrustStorePassword("secret");
		//l_client.setKeepAlive(false);

		m_client = l_client;
	}


	@Override
	public void tearDown() throws Exception
	{
		m_client.dispose();
	}


	public void testMultiDispose() throws Exception
	{
		m_client.dispose();
		m_client.dispose();
		m_client.dispose();
	}


	public void testCallThenClientDispose() throws Exception
	{
		final SomeTestMethods l_interface;
		final String          l_strIn;
		final String          l_strOut;

		l_interface = m_client.getProxy(SomeTestMethods.class);

		l_strIn  = "The rain in spain stays always in the plain.";
		l_strOut = l_interface.echo(l_strIn);

		assert(l_strIn.equals(l_strOut));

		m_client.dispose();
	}


	public void testCallNoKeepAlive() throws Exception
	{
		final SomeTestMethods l_interface;
		final String          l_strIn;
		final String          l_strOut;

		m_client.setKeepAlive(false);

		l_interface = m_client.getProxy(SomeTestMethods.class);

		l_strIn  = "The rain in spain stays always in the plain.";
		l_strOut = l_interface.echo(l_strIn);

		assert(l_strIn.equals(l_strOut));

		m_client.dispose();
	}
}
