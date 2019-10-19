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


package org.jaffre.client;


/**
 * @author Alexander Veit
 */
public interface JaffreClient
{
	/**
	 * Create a Jaffre proxy for the given interface.
	 * @param <T> The interface.
	 * @param p_interface The interface class.
	 * @return A Jaffre client that implements the given interface.
	 */
	public <T> T getProxy(Class<T> p_interface);


	/**
	 * Free all resources that were allocated by this Jaffre client.
	 */
	public abstract void dispose();
}
