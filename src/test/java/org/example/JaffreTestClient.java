/* $Id: $
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
import org.jaffre.client.spi.SocketJaffreClient;


/**
 * @author Alexander Veit
 */
public final class JaffreTestClient
{
	private static class TestRunnable implements Runnable
	{
		private final String m_strServiceAddress;

		final int m_iNumLoops;

		final int m_iPort;

		private TestRunnable(String p_strServiceAddress, int p_iPort, int p_iNumLoops)
		{
			m_strServiceAddress = p_strServiceAddress;
			m_iNumLoops         = p_iNumLoops;
			m_iPort             = p_iPort;
		}

		@Override
		public void run()
		{
			try
			{
				final SocketJaffreClient l_client;
				final SomeTestMethods  l_interface;
				long                   l_lTime;

				l_client = new SocketJaffreClient();

				l_client.setServiceAddress(m_strServiceAddress);
				l_client.setServicePort(m_iPort);

				l_interface = l_client.getProxy(SomeTestMethods.class);

				l_lTime = System.nanoTime();

				for (int i = 0; i < m_iNumLoops; i++)
				{
					if (!"Hallo Welt!".equals(l_interface.echo("Hallo Welt!")))
						throw new Exception();
				}

				l_lTime = (System.nanoTime() - l_lTime) / 1000000;

				l_client.dispose();

				System.out.println(l_lTime + " ms");
			}
			catch (Exception l_e)
			{
				l_e.printStackTrace();
			}
		}
	}


	public static void main(String[] p_args)
	{
		final String l_strServerAddress;
		final int    l_iPort;
		final int    l_iNumThreads;
		final int    l_iNumLoops;

		if (p_args.length != 4)
		{
			System.err.println(
				"USAGE: org.example.JaffreTestClient" +
				" <server-addr> <port> <num-threads> <num-loops>");

			System.exit(1);
		}

		l_strServerAddress = p_args[0];
		l_iPort            = Integer.parseInt(p_args[1]);
		l_iNumThreads      = Integer.parseInt(p_args[2]);
		l_iNumLoops        = Integer.parseInt(p_args[3]);

		for (int i = 0; i < l_iNumThreads; i++)
			new Thread(new TestRunnable(l_strServerAddress, l_iPort, l_iNumLoops)).start();
	}
}
