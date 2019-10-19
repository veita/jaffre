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


package org.jaffre.springframework;


import java.util.List;
import java.util.Map;

import org.jaffre.server.JaffreServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author Alexander Veit
 */
public class JaffreExporter implements ApplicationContextAware, InitializingBean
{
	private ApplicationContext m_appContex;

	private JaffreServer m_server;

	private List<?> m_endpointList;

	private Map<?, ?> m_endpointMap;


	/**
	 * Get the server that's being used by this exporter.
	 * @return The server that's being used.
	 */
	public JaffreServer getServer()
	{
		return m_server;
	}


	/**
	 * Set the server that's to be used by this exporter.
	 * @param p_server The server to be used.
	 */
	public void setServer(JaffreServer p_server)
	{
		m_server = p_server;
	}


	public List<?> getEndpointList()
	{
		return m_endpointList;
	}


	public void setEndpointList(List<?> p_endpointList)
	{
		m_endpointList = p_endpointList;
	}


	public Map<?, ?> getEndpointMap()
	{
		return m_endpointMap;
	}


	public void setEndpointMap(Map<?, ?> p_endpointMap)
	{
		m_endpointMap = p_endpointMap;
	}


	@Override
	public void setApplicationContext(ApplicationContext p_appContext)
		throws BeansException
	{
		m_appContex = p_appContext;
	}


	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (m_server == null)
			throw new IllegalStateException("No server set.");

		if (m_endpointList != null)
		{
			for (final Object l_endpoint : m_endpointList)

				if (l_endpoint instanceof String)
				{
					final Object l_bean;

					l_bean = m_appContex.getBean((String)l_endpoint);

					m_server.registerInterface(l_bean);
				}
				else
				{
					m_server.registerInterface(l_endpoint);
				}
		}

		if (m_endpointMap != null)
		{
			for (final Map.Entry<?, ?> l_entry : m_endpointMap.entrySet())
			{
				final Class<?> l_interface;

				l_interface  = Class.forName((String)l_entry.getKey());

				m_server.registerInterface(l_interface, l_entry.getValue());
			}
		}
	}
}
