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


package org.jaffre.client.spi;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.net.SocketFactory;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreCookie;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.JaffreUncheckedException;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.util.JaffreUtil;


/**
 * @author Alexander Veit
 */
public class SocketJaffreClient extends AbstractSocketJaffreClient
{
	private static final Logger ms_log = LoggerFactory.getLogger(SocketJaffreClient.class);

	private SocketFactory m_socketFac;

	private Socket m_socket;

	private int m_iBufferSize = 8192;

	private BufferedOutputStream m_out;

	private BufferedInputStream m_in;


	public SocketJaffreClient()
	{
	}


	/**
	 * Get the currently used socket factory.
	 * @return The currently used socket factory, or <code>null</code>
	 *    if the default factory is used.
	 */
	public synchronized SocketFactory getSocketFactory()
	{
		return m_socketFac;
	}


	/**
	 * Set the socket factory to be used by this client.
	 * @param p_socketFac The socket factory that should be used, or
	 *    <code>null</code> if the default factory should be used.
	 */
	public synchronized void setSocketFactory(SocketFactory p_socketFac)
	{
		m_socketFac = p_socketFac;
	}


	/**
	 * Free all network resources that are no longer needed by this client.
	 * <p><i>Note: subsequent method invocations on client interfaces
	 * will acquire new resources, so this method has to be called again.</i></p>
	 */
	@Override
	public synchronized void dispose()
	{
		m_out    = JaffreUtil.close(m_out);
		m_in     = JaffreUtil.close(m_in);
		m_socket = JaffreUtil.close(m_socket);
	}


	@Override
	protected synchronized Object invokeImpl(Class<?> p_interface,
	                                         Object   p_proxy,
	                                         Method   p_method,
	                                         Object[] p_args)
		throws Throwable
	{
		// create the client socket
		if (m_socket == null)
		{
			try
			{
				final Socket l_socket;

				l_socket = SocketFactory.getDefault().createSocket();

				l_socket.setKeepAlive(isKeepAlive());
				l_socket.bind(null);
				l_socket.connect(new InetSocketAddress(getServiceInetAddress(), getServicePort()));

				assert l_socket.getKeepAlive() == isKeepAlive();

				m_socket = l_socket;

				m_out = new BufferedOutputStream(l_socket.getOutputStream(), m_iBufferSize);
				m_in  = new BufferedInputStream(l_socket.getInputStream(), m_iBufferSize);
			}
			catch (SocketException l_e)
			{
				throw new JaffreUncheckedException(l_e);
			}
			catch (IOException l_e)
			{
				throw new JaffreUncheckedException(l_e);
			}
		}

		// invoke the method on the server and receive the result
		boolean                 l_bKeepAlive;
		final JaffreCallFrame   l_frameCall;
		final JaffreReturnFrame l_frameReturn;

		l_frameCall = new JaffreCallFrame(p_interface,
		                                  p_method.getName(),
		                                  p_method.getParameterTypes(),
		                                  p_args);

		l_bKeepAlive = isKeepAlive();

		l_frameCall.setKeepAlive(l_bKeepAlive);
		l_frameCall.setInOut();

		if (isSendCookies())
			l_frameCall.setUserData(JaffreCookie.get());

		try
		{
			getCallFrameSerializer().serialize(l_frameCall, m_out);

			m_out.flush();

			l_frameReturn = getReturnFrameSerializer().deserialize(m_in);

			if (isAcceptCookies())
				JaffreCookie.set(l_frameReturn.getUserData());

			// we close the connection if the client is not configured to
			// keep connections alive, or the server wishes to do so
			l_bKeepAlive = l_bKeepAlive && l_frameReturn.isKeepAlive();
		}
		catch (ClassNotFoundException l_e)
		{
			throw new JaffreUncheckedException(l_e);
		}
		catch (IOException l_e)
		{
			throw new JaffreUncheckedException(l_e);
		}
		finally
		{
			if (!l_bKeepAlive)
			{
				m_out    = JaffreUtil.close(m_out);
				m_in     = JaffreUtil.close(m_in);
				m_socket = JaffreUtil.close(m_socket);
			}
		}

		// return the result to the caller, or re-throw the exception that
		// was thrown on the server
		if (!l_frameReturn.isExceptionResult())
		{
			return l_frameReturn.getResult();
		}
		else
		{
			final Throwable l_e;

			l_e = (Throwable)l_frameReturn.getResult();

			if (JaffreUtil.isDeclaredThrowable(l_e, p_method))
				throw l_e;
			else
				throw new JaffreUncheckedException(l_e);
		}
	}


	/*
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize()
	{
		if (m_socket != null || m_in != null || m_out != null)
			ms_log.warn("Call dispose to cleanup system resources.");

		dispose();
	}
}
