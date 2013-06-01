/* $Id: Server.java 399 2009-04-01 20:54:50Z  $
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


package org.example.sessioncs;


import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.Subject;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreCookie;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.server.JaffreServer;
import org.jaffre.server.spi.DefaultJaffreServer;
import org.jaffre.server.spi.SocketJaffreConnector;


/**
 * @author Alexander Veit
 */
public final class Server
{
	private final Map<String, Subject> m_sessions = new HashMap<String, Subject>();


	private class RemoteImpl implements Remote
	{
		@Override
		public String login()
		{
			final String l_strSessionCookie;

			l_strSessionCookie = UUID.randomUUID().toString();

			m_sessions.put(l_strSessionCookie, new Subject());

			System.out.println("New session " + l_strSessionCookie + ".");

			return l_strSessionCookie;
		}

		@Override
		public void logout(String p_strSessionCookie)
		{
			m_sessions.remove(p_strSessionCookie);

			System.out.println("Logged out session " + p_strSessionCookie + ".");
		}

		@Override
		public String logAndEcho(String p_str)
		{
			System.out.println(JaffreCookie.get() + ": " + p_str);

			return "ECHO " + JaffreCookie.get() + ": " + p_str;
		}

		@Override
		public void shutdownServer()
		{
			synchronized (Server.this)
			{
				Server.this.notify();
			}
		}
	}


	private class SessionJaffreServer extends DefaultJaffreServer
	{
		private SessionJaffreServer()
		{
			setAcceptCookies(true);
			setSendCookies(true);
		}

		@Override
		public JaffreReturnFrame process(final JaffreCallFrame p_call, final Object p_extParam)
		{
			final String l_strSessionCookie;

			l_strSessionCookie = (String)p_call.getUserData();

			if (l_strSessionCookie != null && m_sessions.containsKey(l_strSessionCookie))
			{
				return Subject.doAsPrivileged
					(m_sessions.get(l_strSessionCookie), new PrivilegedAction<JaffreReturnFrame>()
				{
					@Override
					public JaffreReturnFrame run()
					{
						return SessionJaffreServer.super.process(p_call, p_extParam);
					}
				}, null);
			}
			else
			{
				return super.process(p_call, p_extParam);
			}
		}
	}


	private void _run(String p_strBindAddress, int p_iPort, int p_iNumThreads)
	{
		try
		{
			// register a service endpoint
			final JaffreServer l_server;

			l_server = new SessionJaffreServer();

			l_server.registerInterface(new RemoteImpl());

			// start the socket connector
			final SocketJaffreConnector l_connector;

			l_connector = new SocketJaffreConnector();

			l_connector.setServer(l_server);
			l_connector.setBindingAddress(p_strBindAddress);
			l_connector.setPort(p_iPort);
			l_connector.setCoreThreadPoolSize(p_iNumThreads);

			l_connector.start();

			synchronized (this)
			{
				wait();

				System.out.println("The server received a shutdown request.");
			}

			l_connector.stop();
		}
		catch (Exception l_e)
		{
			l_e.printStackTrace();
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
				"USAGE: org.example.simplecs.Server" +
				" <server-addr> <port> <num-threads>");

			System.exit(1);
		}

		l_strBindAddress = p_args[0];
		l_iPort          = Integer.parseInt(p_args[1]);
		l_iNumThreads    = Integer.parseInt(p_args[2]);

		new Server()._run(l_strBindAddress, l_iPort, l_iNumThreads);
	}
}
