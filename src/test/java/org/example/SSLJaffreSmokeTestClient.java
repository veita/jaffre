/* $Id: SSLJaffreSmokeTestClient.java 398 2009-04-01 20:52:42Z  $
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
import org.jaffre.client.spi.SSLSocketJaffreClient;


/**
 * @author Alexander Veit
 */
public final class SSLJaffreSmokeTestClient
{
	public static void run(String p_strServiceAddress, int p_iPort)
	{
		try
		{
			final SSLSocketJaffreClient l_client;
			final SomeTestMethods       l_interface;
			long                        l_lTime;
			String                      l_strTest;

			l_client = new SSLSocketJaffreClient();

			l_client.setServiceAddress(p_strServiceAddress);
			l_client.setServicePort(p_iPort);
			l_client.setKeepAlive(true);

			l_interface = l_client.getProxy(SomeTestMethods.class);

			l_lTime = System.nanoTime();

			l_strTest = "A";

			for (int i = 0; i < 15; i++)
				l_strTest = l_strTest + l_strTest;

			for (int i = 0; i < 20000; i++)
			{
				final String l_strIn;
				final String l_strOut;

				l_strIn = l_strTest.substring(0, i);

				try
				{
					l_strOut = l_interface.echo(l_strIn);

					if (!l_strIn.equals(l_strOut))
						throw new Exception();
				}
				catch (Exception l_e)
				{
					System.out.println("i = " + i);

					throw l_e;
				}
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


	public static void main(String[] p_args)
	{
		final String l_strServerAddress;
		final int    l_iPort;

		if (p_args.length != 4)
		{
			System.err.println(
				"USAGE: org.example.SSLJaffreSmokeTestClient" +
				" <server-addr> <port> <num-threads> <num-loops>");

			System.exit(1);
		}

		l_strServerAddress = p_args[0];
		l_iPort            = Integer.parseInt(p_args[1]);

		run(l_strServerAddress, l_iPort);
		run(l_strServerAddress, l_iPort);
	}
}
