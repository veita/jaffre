/* $Id: SSLJaffreTestServer.java 394 2009-03-21 20:28:26Z  $
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


package org.example;


import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.SSLSocketJaffreConnector;


/**
 * @author Alexander Veit
 */
public class SSLJaffreTestServer
{
	private static JaffreServer ms_server;


	public static void main(String[] p_args)
	{
		final String l_strBindAddress;
		final int    l_iPort;
		final int    l_iNumThreads;

		if (p_args.length != 3)
		{
			System.err.println(
				"USAGE: org.example.SSLJaffreTestServer" +
				" <server-addr> <port> <num-threads>");

			System.exit(1);
		}

		l_strBindAddress = p_args[0];
		l_iPort          = Integer.parseInt(p_args[1]);
		l_iNumThreads    = Integer.parseInt(p_args[2]);

		run(l_strBindAddress, l_iPort, l_iNumThreads);
	}


	public static void run(String p_strBindAddress,
	                       int    p_iPort,
	                       int    p_iNumThreads)
	{
		try
		{
			final JaffreServer l_server;

			l_server = new DefaultJaffreServer();

			l_server.registerInterface(new SomeTestMethodsService());

			// ...
			final SSLSocketJaffreConnector l_connector;

			l_connector = new SSLSocketJaffreConnector();

			l_connector.setServer(l_server);
			l_connector.setBindingAddress(p_strBindAddress);
			l_connector.setPort(p_iPort);
			l_connector.setCoreThreadPoolSize(p_iNumThreads);
			l_connector.setMaxThreadPoolSize(p_iNumThreads);
			l_connector.setKeyStore("resources/keystore");
			l_connector.setKeyStoreType("JCEKS");
			l_connector.setKeyStorePassword("secret");
			l_connector.setTrustStore("resources/truststore");
			l_connector.setTrustStoreType("JCEKS");
			l_connector.setTrustStorePassword("secret");

			l_connector.start();

			ms_server = l_server;

			synchronized (ms_server)
			{
				ms_server.wait();
			}

			ms_server = null;
		}
		catch (Exception l_e)
		{
			l_e.printStackTrace();
		}
	}


	public static void stop()
	{
		if (ms_server != null)
			ms_server.notifyAll();
	}
}

