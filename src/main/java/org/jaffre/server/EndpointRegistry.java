/* $Id: EndpointRegistry.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.server;


/**
 * @author Alexander Veit
 */
public interface EndpointRegistry
{
	/**
	 * Register a service endpoint.
	 * @param p_obj The object that implements the endpoint.
	 */
	public void registerEndpoint(Object p_obj);


	/**
	 * Register a service endpoint.
	 * @param p_interface The endpoint interface.
	 * @param p_obj The object that implements the endpoint interface.
	 */
	public void registerEndpoint(Class<?> p_interface, Object p_obj);


	public <T> T getStub(Class<T> p_interface);
}
