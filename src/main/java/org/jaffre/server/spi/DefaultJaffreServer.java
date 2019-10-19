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


package org.jaffre.server.spi;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreCookie;
import org.jaffre.JaffreCookieSupport;
import org.jaffre.JaffreNoInterfaceException;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.server.EndpointRegistry;
import org.jaffre.server.JaffreServer;


/**
 * @author Alexander Veit
 */
public class DefaultJaffreServer implements JaffreServer, JaffreCookieSupport
{
	private static final Logger ms_log = LoggerFactory.getLogger(DefaultJaffreServer.class);

	protected EndpointRegistry m_epr = new EndpointRegistryImpl();

	private boolean m_bLogInOutExceptions = true;

	private boolean m_bLogInOnlyExceptions = true;

	private boolean m_bSendCookies = false;

	private boolean m_bAcceptCookies = false;


	public DefaultJaffreServer()
	{
	}


	/*
	 * @see org.jaffre.JaffreCookieSupport#isAcceptCookies()
	 */
	@Override
	public boolean isAcceptCookies()
	{
		return m_bAcceptCookies;
	}


	/*
	 * @see org.jaffre.JaffreCookieSupport#setAcceptCookies(boolean)
	 */
	@Override
	public void setAcceptCookies(boolean p_bAcceptCookies)
	{
		m_bAcceptCookies = p_bAcceptCookies;
	}


	/*
	 * @see org.jaffre.JaffreCookieSupport#isSendCookies()
	 */
	@Override
	public boolean isSendCookies()
	{
		return m_bSendCookies;
	}


	/*
	 * @see org.jaffre.JaffreCookieSupport#setSendCookies(boolean)
	 */
	@Override
	public void setSendCookies(boolean p_bSendCookies)
	{
		m_bSendCookies = p_bSendCookies;
	}


	public boolean isLogInOutExceptions()
	{
		return m_bLogInOutExceptions;
	}


	public void setLogInOutExceptions(boolean p_bLogInOutExceptions)
	{
		m_bLogInOutExceptions = p_bLogInOutExceptions;
	}


	public boolean isLogInOnlyExceptions()
	{
		return m_bLogInOnlyExceptions;
	}


	public void setLogInOnlyExceptions(boolean p_bLogInOnlyExceptions)
	{
		m_bLogInOnlyExceptions = p_bLogInOnlyExceptions;
	}


	/*
	 * @see org.jaffre.server.JaffreServer#registerInterface(java.lang.Object)
	 */
	@Override
	public void registerInterface(Object p_obj)
	{
		m_epr.registerEndpoint(p_obj);
	}


	/*
	 * @see org.jaffre.server.JaffreServer#registerInterface(java.lang.Class, java.lang.Object)
	 */
	@Override
	public void registerInterface(Class<?> p_interface, Object p_obj)
	{
		m_epr.registerEndpoint(p_interface, p_obj);
	}


	/**
	 * Process a call frame.
	 * @param p_call The call frame to be processed.
	 * @param p_extParam A parameter reserved for implementors that need to pass
	 *    additional information to this method. Not used in this implementation.
	 * @return A return frame that contains the result of the method call.
	 */
	@Override
	public JaffreReturnFrame process(JaffreCallFrame p_call, Object p_extParam)
	{
		try
		{
			final Class<?>          l_clsInterface;
			final Object            l_objStub;
			final Method            l_method;
			final Object            l_objResult;
			final JaffreReturnFrame l_return;

			l_clsInterface = p_call.getInterface();

			l_objStub = getEndpointInterface(l_clsInterface);

			l_method = l_clsInterface.getMethod
				(p_call.getMethodName(), p_call.getParameterTypes());

			if (isAcceptCookies())
				JaffreCookie.set(p_call.getUserData());

			l_objResult = l_method.invoke(l_objStub, p_call.getParameters());

			l_return = new JaffreReturnFrame(l_objResult, false);

			if (isSendCookies())
				l_return.setUserData(JaffreCookie.get());

			return l_return;
		}
		catch (Throwable l_e)
		{
			final Throwable l_t;

			if (l_e instanceof InvocationTargetException)
				l_t = l_e.getCause();
			else
				l_t = l_e;

			if ((p_call.isInOnly() && m_bLogInOnlyExceptions) ||
			    (p_call.isInOut() && m_bLogInOutExceptions))
			{
				final StringBuilder l_sbuf = new StringBuilder(128);

				l_sbuf.append("Error in call to ");
				l_sbuf.append(p_call.getInterface().getName());
				l_sbuf.append('#');
				l_sbuf.append(p_call.getMethodName());
				l_sbuf.append('.');

				ms_log.error(l_sbuf.toString(), l_t);
			}

			return new JaffreReturnFrame(l_t, true);
		}
		finally
		{
			if (isAcceptCookies() || isSendCookies())
				JaffreCookie.clear();
		}
	}


	@Override
	public <T> T getEndpointInterface(Class<T> p_interface)
	{
		final T l_endpoint;

		if (p_interface == null)
			throw new IllegalArgumentException("No interface specified.");

		l_endpoint = m_epr.getStub(p_interface);

		if (l_endpoint == null)
		{
			throw new JaffreNoInterfaceException
				(p_interface.getName() + " is not a registered service endpoint interface.");
		}

		return l_endpoint;
	}
}
