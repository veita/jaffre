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


package org.jaffre.server;


import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreNoInterfaceException;
import org.jaffre.JaffreReturnFrame;


/**
 * @author Alexander Veit
 */
public interface JaffreServer
{
	/**
	 * Register an endpoint interface.
	 * <p>An implementation normally would choose a suitable interface implemented
	 * by the endpoint and then call {@link #registerInterface(Class, Object)}
	 * with the choosen interface and the given endpoint as arguments.</p>
	 * @param p_obj A service endpoint that implements an interface.
	 * @see #registerInterface(Class, Object)
	 */
	public void registerInterface(Object p_obj);


	/**
	 * Register an endpoint interface.
	 * @param p_interface The endpoint interface.
	 * @param p_obj A service endpoint that implements <code>p_interface</code>.
	 */
	public void registerInterface(Class<?> p_interface, Object p_obj);


	/**
	 * Process a call frame.
	 * @param p_call The call frame to be processed.
	 * @param p_extParam A parameter reserved for implementors that need
	 *    to pass additional information to this method.
	 * @return A return frame that contains the result of the method call.
	 */
	public JaffreReturnFrame process(JaffreCallFrame p_call, Object p_extParam);


	/**
	 * Support for local transport.
	 * @param <T> The type of interface requested from this service.
	 * @param p_interface The interface class.
	 * @return The requested service endpoint interface.
	 * @throws JaffreNoInterfaceException
	 *    If <code>p_interface</code> is not a registered service endpoint interface.
	 */
	public <T> T getEndpointInterface(Class<T> p_interface)
		throws JaffreNoInterfaceException;
}