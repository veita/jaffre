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


import org.example.services.DerivedEchoService;
import org.example.services.Echo;
import org.example.services.EchoService;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class EndpointRegistryImplTestCase extends JaffreTestCaseBase
{
	public void testClassImplementsInterface() throws Exception
	{
		final EndpointRegistryImpl l_registry;
		final Echo                 l_service;

		l_registry = new EndpointRegistryImpl();
		l_service  = new EchoService();

		l_registry.registerEndpoint(l_service);

		assertSame(l_service, l_registry.getStub(Echo.class));
	}


	public void testSubclassedClassImplementsInterface() throws Exception
	{
		final EndpointRegistryImpl l_registry;
		final Echo                 l_service;

		l_registry = new EndpointRegistryImpl();
		l_service  = new DerivedEchoService();

		l_registry.registerEndpoint(l_service);

		assertSame(l_service, l_registry.getStub(Echo.class));
	}
}
