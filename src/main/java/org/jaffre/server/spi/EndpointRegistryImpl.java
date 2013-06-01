/* $Id: EndpointRegistryImpl.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.server.spi;


import java.io.Externalizable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.server.EndpointRegistry;


/**
 * @author Alexander Veit
 */
public class EndpointRegistryImpl implements EndpointRegistry
{
	private static final Logger ms_log = LoggerFactory.getLogger(EndpointRegistryImpl.class);

	private volatile Map<Class<?>, Object> m_map = new HashMap<Class<?>, Object>();


	@Override
	public void registerEndpoint(Object p_obj)
	{
		final Class<?> l_class;
		final Class<?> l_interface;

		if (p_obj == null)
			throw new IllegalArgumentException("No object.");

		l_class     = p_obj.getClass();
		l_interface = _findInterface(l_class, l_class);

		if (l_interface == null)
		{
			throw new IllegalArgumentException
				(l_class.getName() + " does not implement an interface.");
		}

		_registerEndpoint(l_interface, p_obj);
	}


	private Class<?> _findInterface(Class<?> p_class, Class<?> p_super)
	{
		final Class<?>[] l_interfaces;
		Class<?>         l_interface;

		l_interfaces = p_super.getInterfaces();
		l_interface  = null;

		if (l_interfaces.length != 0)
		{
			for (Class<?> l_if : l_interfaces)
			{
				// ignore some well known interfaces
				if (l_if.equals(Serializable.class) ||
				    l_if.equals(Externalizable.class) ||
				    l_if.equals(Cloneable.class))
				{
					continue;
				}

				if (l_interface != null)
					throw new IllegalArgumentException(p_super.getName() + " implements multiple interfaces.");

				l_interface = l_if;
			}
		}

		if (l_interface != null)
		{
			return l_interface;
		}
		else
		{
			final Class<?> l_super;

			l_super = p_super.getSuperclass();

			if (l_super == null || l_super.equals(Object.class))
				return null;

			return _findInterface(p_class, l_super);
		}
	}


	@Override
	public void registerEndpoint(Class<?> p_interface, Object p_obj)
	{
		if (p_obj == null)
			throw new IllegalArgumentException("No object.");

		if (p_interface == null)
			throw new IllegalArgumentException("No interface.");

		if (!p_interface.isInterface())
			throw new IllegalArgumentException(p_interface.getName() + " is not an interface.");

		_registerEndpoint(p_interface, p_obj);
	}


	private void _registerEndpoint(Class<?> p_interface, Object p_obj)
	{
		final Map<Class<?>, Object> l_map;

		synchronized (this)
		{
			l_map = new HashMap<Class<?>, Object>(m_map);

			if (l_map.containsKey(p_interface))
			{
				throw new IllegalStateException
					(p_interface.getName() + " is already registered as an interface.");
			}

			l_map.put(p_interface, p_obj);

			m_map = l_map;

			if (ms_log.isDebugEnabled())
				ms_log.debug("Registered endpoint with interface " + p_interface.getName() + ".");
		}
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T> T getStub(Class<T> p_interface)
	{
		if (p_interface == null)
			throw new IllegalArgumentException("No interface.");

		if (!p_interface.isInterface())
			throw new IllegalArgumentException(p_interface.getName() + " is not an interface.");

		return (T)m_map.get(p_interface);
	}
}
