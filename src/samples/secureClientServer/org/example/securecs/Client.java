/* $Id: Client.java 394 2009-03-21 20:28:26Z  $
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


package org.example.securecs;


import org.example.simplecs.Remote;
import org.jaffre.client.spi.SSLSocketJaffreClient;


/**
 * @author Alexander Veit
 */
public class Client
{
	private void _run(String p_strServiceAddress, int p_iPort)
	{
		try
		{
			final SSLSocketJaffreClient l_client;
			final Remote                l_proxy;

			l_client = new SSLSocketJaffreClient();

			l_client.setServiceAddress(p_strServiceAddress);
			l_client.setServicePort(p_iPort);
			l_client.setTrustStore("src/samples/secureClientServer/truststore");
			l_client.setTrustStoreType("JCEKS");
			l_client.setTrustStorePassword("secret");

			l_proxy = l_client.getProxy(Remote.class);

			System.out.println(l_proxy.logAndEcho("First call."));
			System.out.println(l_proxy.logAndEcho("Second call."));
			System.out.println(l_proxy.logAndEcho("Third call."));

			l_client.dispose();
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

		if (p_args.length != 2)
		{
			System.err.println(
				"USAGE: org.example.securecs.Client" +
				" <server-addr> <port>");

			System.exit(1);
		}

		l_strServerAddress = p_args[0];
		l_iPort            = Integer.parseInt(p_args[1]);

		new Client()._run(l_strServerAddress, l_iPort);
	}
}
