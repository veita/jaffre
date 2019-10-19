/*
 * (C) Copyright 2008-2019 Alexander Veit
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
import org.jaffre.server.spi.SocketJaffreConnector;


/**
 * @author Alexander Veit
 */
public class JaffreTestServer
{
	private static final Logger ms_log = LoggerFactory.getLogger(JaffreTestServer.class);


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
				"USAGE: org.example.JaffreTestServer" +
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

			l_server.registerInterface(SomeTestMethods.class, new SomeTestMethodsService());

			// ...
			SocketJaffreConnector l_connector;

			l_connector = new SocketJaffreConnector();

			l_connector.setServer(l_server);
			l_connector.setBindingAddress(l_strBindAddress);
			l_connector.setPort(l_iPort);
			l_connector.setCoreThreadPoolSize(l_iNumThreads);

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
