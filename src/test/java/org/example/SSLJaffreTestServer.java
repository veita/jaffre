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


import org.example.services.SomeTestMethods;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.SSLSocketJaffreConnector;


/**
 * @author Alexander Veit
 */
public class SSLJaffreTestServer
{
	private static final Logger ms_log = LoggerFactory.getLogger(SSLJaffreTestServer.class);


	private static class SomeTestMethodsService implements SomeTestMethods
	{
		@Override
		public int add(int p_iA, int p_iB)
		{
			return p_iA + p_iB;
		}

		@Override
		public Object echo(Object p_obj)
		{
			return p_obj;
		}

		@Override
		public String echo(String p_str)
		{
			return p_str;
		}

		@Override
		public void log(String p_str)
		{
			ms_log.info(p_str);
		}

		@Override
		public void throwException(String p_strExceptionClass) throws Exception
		{
			final Class<?> l_class;

			l_class = Class.forName(p_strExceptionClass);

			throw (Exception)(l_class.newInstance());
		}
	}


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

		try
		{
			final JaffreServer l_server;

			l_server = new DefaultJaffreServer();

			l_server.registerInterface(new SomeTestMethodsService());

			// ...
			SSLSocketJaffreConnector l_connector;

			l_connector = new SSLSocketJaffreConnector();

			l_connector.setServer(l_server);
			l_connector.setBindingAddress(l_strBindAddress);
			l_connector.setPort(l_iPort);
			l_connector.setCoreThreadPoolSize(l_iNumThreads);
			l_connector.setMaxThreadPoolSize(l_iNumThreads);
			l_connector.setKeyStore("resources/keystore");
			l_connector.setKeyStoreType("JCEKS");
			l_connector.setKeyStorePassword("secret");
			l_connector.setTrustStore("resources/truststore");
			l_connector.setTrustStoreType("JCEKS");
			l_connector.setTrustStorePassword("secret");
//			l_connector.setNeedClientAuth(true);

			l_connector.start();

			synchronized (l_server)
			{
				l_server.wait();
			}
		}
		catch (Exception l_e)
		{
			l_e.printStackTrace();
		}
	}
}

