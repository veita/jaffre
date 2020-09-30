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


package org.jaffre.server.spi;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class SSLSocketJaffreConnectorTestCase extends JaffreTestCaseBase
{
	@Test
	public void testGetterSetter()
	{
		final SSLSocketJaffreConnector l_connector;

		l_connector = new SSLSocketJaffreConnector();

		assertEquals(0, l_connector.getNumRunningThreads());

		assertEquals("TLSv1.2", l_connector.getProtocol());
		l_connector.setProtocol("TLS");
		assertEquals("TLS", l_connector.getProtocol());

		assertEquals("SunX509", l_connector.getKeyManagerFactoryAlgorithm());
		l_connector.setKeyManagerFactoryAlgorithm("kmfa");
		assertEquals("kmfa", l_connector.getKeyManagerFactoryAlgorithm());

		assertEquals("SunX509", l_connector.getTrustManagerFactoryAlgorithm());
		l_connector.setTrustManagerFactoryAlgorithm("tmfa");
		assertEquals("tmfa", l_connector.getTrustManagerFactoryAlgorithm());

		assertFalse(l_connector.isNeedClientAuth());
		l_connector.setNeedClientAuth(true);
		assertTrue(l_connector.isNeedClientAuth());

		l_connector.setKeyStorePassword("secret");
		l_connector.setKeyStorePassword(null);

		assertNull(l_connector.getKeyStore());
		l_connector.setKeyStore("path/to/the.ks");
		assertEquals("path/to/the.ks", l_connector.getKeyStore());

		assertNull(l_connector.getKeyStoreType());
		l_connector.setKeyStoreType("kst");
		assertEquals("kst", l_connector.getKeyStoreType());

		l_connector.setTrustStorePassword("secret");
		l_connector.setTrustStorePassword(null);

		assertNull(l_connector.getTrustStore());
		l_connector.setTrustStore("path/to/the.ts");
		assertEquals("path/to/the.ts", l_connector.getTrustStore());

		assertNull(l_connector.getTrustStoreType());
		l_connector.setTrustStoreType("tst");
		assertEquals("tst", l_connector.getTrustStoreType());
	}
}
