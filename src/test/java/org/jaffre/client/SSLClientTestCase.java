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


package org.jaffre.client;


import java.net.UnknownHostException;

import org.example.services.SomeTestMethods;
import org.example.services.SomeTestMethodsService;
import org.jaffre.client.spi.SSLSocketJaffreClient;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.SSLSocketJaffreConnector;
import org.test.JaffreTestCaseBase;
import org.test.TestPort;


/**
 * @author Alexander Veit
 */
public final class SSLClientTestCase extends JaffreTestCaseBase
{
	private SSLSocketJaffreConnector m_connector;

	private SSLSocketJaffreClient m_client;


	@Override
	public void setUp() throws Exception
	{
	}


	@Override
	public void tearDown() throws Exception
	{
		m_connector.stop();
		m_client.dispose();
	}


	private void setUpClientAndServer(int p_iPort)
		throws UnknownHostException, InterruptedException
	{
		// server
		final JaffreServer             l_server;
		final SSLSocketJaffreConnector l_connector;

		l_server = new DefaultJaffreServer();

		l_server.registerInterface(new SomeTestMethodsService());

		l_connector = new SSLSocketJaffreConnector();

		l_connector.setServer(l_server);
		l_connector.setBindingAddress("localhost");
		l_connector.setPort(p_iPort);
		l_connector.setKeyStore("resources/keystore");
		l_connector.setKeyStoreType("JCEKS");
		l_connector.setKeyStorePassword("secret");
		l_connector.setTrustStore("resources/truststore");
		l_connector.setTrustStoreType("JCEKS");
		l_connector.setTrustStorePassword("secret");

		l_connector.start();

		m_connector = l_connector;

		Thread.sleep(500L);

		// client
		final SSLSocketJaffreClient l_client;

		l_client = new SSLSocketJaffreClient();

		l_client.setServiceAddress("localhost");
		l_client.setServicePort(p_iPort);
		l_client.setKeyStoreType("JCEKS");
		l_client.setKeyStore("resources/keystore");
		l_client.setKeyStorePassword("secret");
		l_client.setTrustStoreType("JCEKS");
		l_client.setTrustStore("resources/truststore");
		l_client.setTrustStorePassword("secret");

		m_client = l_client;
	}


	public void testMultiDispose() throws Exception
	{
		setUpClientAndServer(TestPort.getNext());

		m_client.dispose();
		m_client.dispose();
		m_client.dispose();
	}


	public void testCallThenClientDispose() throws Exception
	{
		final SomeTestMethods l_interface;
		final String          l_strIn;
		final String          l_strOut;

		setUpClientAndServer(TestPort.getNext());

		l_interface = m_client.getProxy(SomeTestMethods.class);

		l_strIn  = "The rain in spain stays mainly in the plain.";
		l_strOut = l_interface.echo(l_strIn);

		assert(l_strIn.equals(l_strOut));

		m_client.dispose();
	}


	public void testCallNoKeepAlive() throws Exception
	{
		final SomeTestMethods l_interface;
		final String          l_strIn;
		final String          l_strOut;

		setUpClientAndServer(TestPort.getNext());

		m_client.setKeepAlive(false);

		l_interface = m_client.getProxy(SomeTestMethods.class);

		l_strIn  = "The rain in spain stays mainly in the plain.";
		l_strOut = l_interface.echo(l_strIn);

		assert(l_strIn.equals(l_strOut));

		m_client.dispose();
	}
}
