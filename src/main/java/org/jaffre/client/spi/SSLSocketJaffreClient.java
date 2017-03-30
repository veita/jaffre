/* $Id: SSLSocketJaffreClient.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.client.spi;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreConfigurationException;
import org.jaffre.JaffreNoReturnFrameException;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.JaffreUncheckedException;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.io.AbstractByteBufferInputStream;
import org.jaffre.io.AbstractByteBufferOutputStream;
import org.jaffre.ssl.SSLUtil;
import org.jaffre.util.JaffreUtil;


/**
 * @author Alexander Veit
 */
public class SSLSocketJaffreClient extends AbstractSocketJaffreClient
{
	private static final Logger ms_log = LoggerFactory.getLogger(SSLSocketJaffreClient.class);

	//// network resources
	private SocketChannel m_socketChannel;

	//// I/O objects
	private ByteBuffer m_outAppBuf;

	private ByteBuffer m_outNetBuf;

	private ByteBuffer m_inAppBuf;

	private ByteBuffer m_inNetBuf;

	private OutputStream m_out;

	private InputStream m_in;

	//// SSL context, engine, ...
	private SSLContext m_sslContext;

	private SSLContext m_sslContextCustom;

	private SSLEngine m_sslEngine;

	//// SSL related properties
	private String m_strProtocol = "TLS";

	private String m_strKsType;

	private String m_strKsPath;

	private String m_strKsPassword;

	private String m_strKeyManagerFacAlgorithm = "SunX509";

	private String m_strTsType;

	private String m_strTsPath;

	private String m_strTsPassword;

	private String m_strTrustManagerFacAlgorithm = "SunX509";


	public SSLSocketJaffreClient()
	{
	}


	/**
	 * Set a <code>SSLContext</code> to be used by this connector.
	 * <p>This property can be used to provide a more customized
	 * <code>SSLContext</code> than it is possible by using the
	 * <code>setKeyXxx</code> and <code>setTrustXxx</code>
	 * properties.</p>
	 * @param p_sslContext The <code>SSLContext</code> to be used.
	 */
	public void setSSLContext(SSLContext p_sslContext)
	{
		m_sslContextCustom = p_sslContext;
	}


	public String getProtocol()
	{
		return m_strProtocol;
	}


	public void setProtocol(String p_strProtocol)
	{
		m_strProtocol = p_strProtocol;
	}


	/**
	 * Get the key store path.
	 * <p>Default is the value of the <code>javax.net.ssl.keyStore</code>
	 * system property, if defined.</p>
	 * @return Get the key store path.
	 */
	public String getKeyStore()
	{
		return m_strKsPath;
	}


	/**
	 * Set the trust store path.
	 * <p>Default is the value of the <code>javax.net.ssl.keyStore</code>
	 * system property, if defined.</p>
	 * @param p_strKeyStore The trust store path.
	 */
	public void setKeyStore(String p_strKeyStore)
	{
		m_strKsPath = p_strKeyStore;
	}


	/**
	 * Set the key store password.
	 * <p>Default is the value of the <code>javax.net.ssl.keyStorePassword</code>
	 * system property, if defined.</p>
	 * @param p_strKeyStorePassword The key store password.
	 */
	public void setKeyStorePassword(String p_strKeyStorePassword)
	{
		m_strKsPassword = p_strKeyStorePassword;
	}


	/**
	 * Get the key store type.
	 * <p>Default is the value of the <code>javax.net.ssl.keyStoreType</code>
	 * system property, if defined.
	 * Otherwise <code>java.security.KeyStore.getDefaultType()</code> is
	 * used as default.</p>
	 * @return The key store type.
	 */
	public String getKeyStoreType()
	{
		return m_strKsType;
	}


	/**
	 * Set the key store type.
	 * <p>Default is the value of the <code>javax.net.ssl.keyStoreType</code>
	 * system property, if defined.
	 * Otherwise <code>java.security.KeyStore.getDefaultType()</code> is
	 * used as default.</p>
	 * @param p_strKeyStoreType The key store type.
	 */
	public void setKeyStoreType(String p_strKeyStoreType)
	{
		m_strKsType = p_strKeyStoreType;
	}


	/**
	 * Get the <code>KeyManagerFactory</code> algorithm.
	 * Default is <code>SunX509</code>.
	 * @return The algorithm.
	 */
	public String getKeyManagerFactoryAlgorithm()
	{
		return m_strKeyManagerFacAlgorithm;
	}


	/**
	 * Set the <code>KeyManagerFactory</code> algorithm.
	 * Default is <code>SunX509</code>.
	 * @param p_strKeyManagerFactoryAlgorithm The algorithm.
	 */
	public void setKeyManagerFactoryAlgorithm(String p_strKeyManagerFactoryAlgorithm)
	{
		m_strKeyManagerFacAlgorithm = p_strKeyManagerFactoryAlgorithm;
	}


	/**
	 * Get the trust store path.
	 * <p>Default is the value of the <code>javax.net.ssl.trustStore</code>
	 * system property, if defined.</p>
	 * @return Get the trust store path.
	 */
	public String getTrustStore()
	{
		return m_strTsPath;
	}


	/**
	 * Set the trust store path.
	 * <p>Default is the value of the <code>javax.net.ssl.trustStore</code>
	 * system property, if defined.</p>
	 * @param p_strTrustStore The trust store path.
	 */
	public void setTrustStore(String p_strTrustStore)
	{
		m_strTsPath = p_strTrustStore;
	}


	/**
	 * Set the trust store password.
	 * <p>Default is the value of the <code>javax.net.ssl.trustStorePassword</code>
	 * system property, if defined.</p>
	 * @param p_strTrustStorePassword The trust store password.
	 */
	public void setTrustStorePassword(String p_strTrustStorePassword)
	{
		m_strTsPassword = p_strTrustStorePassword;
	}


	/**
	 * Get the trust store type.
	 * <p>Default is the value of the <code>javax.net.ssl.trustStoreType</code>
	 * system property, if defined.
	 * Otherwise <code>java.security.KeyStore.getDefaultType()</code> is
	 * used as default.</p>
	 * @return The trust store type.
	 */
	public String getTrustStoreType()
	{
		return m_strTsType;
	}


	/**
	 * Set the trust store type.
	 * <p>Default is the value of the <code>javax.net.ssl.trustStoreType</code>
	 * system property, if defined.
	 * Otherwise <code>java.security.KeyStore.getDefaultType()</code> is
	 * used as default.</p>
	 * @param p_strTrustStoreType The trust store type.
	 */
	public void setTrustStoreType(String p_strTrustStoreType)
	{
		m_strTsType = p_strTrustStoreType;
	}


	/**
	 * Get the <code>TrustManagerFactory</code> algorithm.
	 * Default is <code>SunX509</code>.
	 * @return The algorithm.
	 */
	public String getTrustManagerFactoryAlgorithm()
	{
		return m_strTrustManagerFacAlgorithm;
	}


	/**
	 * Set the <code>TrustManagerFactory</code> algorithm.
	 * Default is <code>SunX509</code>.
	 * @param p_strTrustManagerFactoryAlgorithm The algorithm.
	 */
	public void setTrustManagerFactoryAlgorithm(String p_strTrustManagerFactoryAlgorithm)
	{
		m_strTrustManagerFacAlgorithm = p_strTrustManagerFactoryAlgorithm;
	}


	/**
	 * Free all resources that are no longer needed by this client.
	 * <p><i>Note: subsequent method invocations on client interfaces
	 * will acquire new resources, so this method has to be called again.</i></p>
	 */
	@Override
	public synchronized void dispose()
	{
		// properly close the SSL/TLS link
		if (m_socketChannel != null && m_socketChannel.isConnected())
		{
			assert m_sslEngine != null;

			try
			{
				if (!m_sslEngine.isOutboundDone())
				{
					assert !m_sslEngine.isInboundDone();

					SSLUtil.writeClosingMessage
						(m_socketChannel, m_sslEngine, m_outAppBuf, m_outNetBuf);

					SSLUtil.readClosingMessage
						(m_socketChannel, m_sslEngine, m_inAppBuf, m_inNetBuf);
				}
			}
			catch (IOException l_e)
			{
				ms_log.error("Error while exchanging SSL closing messages.", l_e);
			}
		}

		// SSL resources
		m_sslEngine  = null;
		m_sslContext = null; // we keep the custom context, if any

		// I/O objects
		m_outAppBuf = null;
		m_outNetBuf = null;
		m_inAppBuf  = null;
		m_inNetBuf  = null;
		m_out       = null;
		m_in        = null;

		// network resources
		m_socketChannel = JaffreUtil.close(m_socketChannel);
	}


	@Override
	protected synchronized Object invokeImpl(Class<?> p_interface,
	                                         Object   p_proxy,
	                                         Method   p_method,
	                                         Object[] p_args)
		throws Throwable // MUSTFIX avoid UndeclaredThrowableException
	{
		if (m_sslContext == null)
			_setupSSLContext();

		if (m_sslEngine == null)
		{
			final SSLEngine  l_sslEngine;
			final SSLSession l_sslSession;

			l_sslEngine = m_sslContext.createSSLEngine
				(getServiceInetAddress().getHostName(), getServicePort());

			l_sslEngine.setUseClientMode(true);

			l_sslSession = l_sslEngine.getSession();
			m_outAppBuf  = ByteBuffer.allocate(l_sslSession.getApplicationBufferSize());
			m_outNetBuf  = ByteBuffer.allocate(l_sslSession.getPacketBufferSize());
			m_inAppBuf   = ByteBuffer.allocate(l_sslSession.getApplicationBufferSize());
			m_inNetBuf   = ByteBuffer.allocate(l_sslSession.getPacketBufferSize());

			m_out = new AbstractByteBufferOutputStream(m_outAppBuf)
			{
				@Override
				public void write(ByteBuffer p_buffer, WRITE_MODE p_mode) throws IOException
				{
					assert p_buffer == m_outAppBuf;

					SSLUtil.write(m_socketChannel, m_sslEngine, m_outAppBuf, m_outNetBuf);
				}
			};

			m_in = new AbstractByteBufferInputStream(m_inAppBuf, false)
			{
				@Override
				public int read(ByteBuffer p_buffer) throws IOException
				{
					assert p_buffer == m_inAppBuf;

					return SSLUtil.read(m_socketChannel, m_sslEngine, m_inAppBuf, m_inNetBuf);
				}
			};

			m_sslEngine = l_sslEngine;
		}

		// clear all buffers
		m_outAppBuf.clear();
		m_outNetBuf.clear();
		m_inAppBuf.clear();
		m_inNetBuf.clear();

		// initialize the SSL connection if neccessary
		if (m_socketChannel == null || !m_socketChannel.isConnected())
		{
			// create a blocking socket channel
			final SocketChannel l_socketChannel;

			if (m_socketChannel == null)
			{
				l_socketChannel = SocketChannel.open();
				l_socketChannel.configureBlocking(true);
				l_socketChannel.socket().setKeepAlive(isKeepAlive());
			}
			else
			{
				assert m_socketChannel.isOpen();

				l_socketChannel = m_socketChannel;
				m_socketChannel = null;
			}

			l_socketChannel.connect
				(new InetSocketAddress(getServiceInetAddress(), getServicePort()));

			while (!l_socketChannel.finishConnect())
				Thread.yield(); // do something until connect finished

			m_socketChannel = l_socketChannel;

			// initial handshake
			SSLUtil.doHandshake
				(m_socketChannel, m_sslEngine, m_outAppBuf, m_outNetBuf, m_inAppBuf, m_inNetBuf);
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

		try
		{
			// send the call frame
			m_outAppBuf.clear();
			m_outNetBuf.clear();

			getCallFrameSerializer().serialize(l_frameCall, m_out);

			m_out.flush();

			// receive the return frame
			m_inNetBuf.clear();
			m_inAppBuf.flip(); // the deserializer must not read null bytes

			assert !m_inAppBuf.hasRemaining();

			l_frameReturn = getReturnFrameSerializer().deserialize(m_in);

			if (l_frameReturn == null)
				throw new JaffreNoReturnFrameException();

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
				// the server will send it's closing message first
				SSLUtil.readClosingMessage
					(m_socketChannel, m_sslEngine, m_inAppBuf, m_inNetBuf);

				// now the client
				SSLUtil.writeClosingMessage
					(m_socketChannel, m_sslEngine, m_outAppBuf, m_outNetBuf);

				m_sslEngine = null;
				m_outAppBuf = null;
				m_outNetBuf = null;
				m_inAppBuf  = null;
				m_inNetBuf  = null;
				m_out       = null;
				m_in        = null;

				m_socketChannel = JaffreUtil.close(m_socketChannel);
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


	private void _setupSSLContext()
	{
		assert m_sslContext == null;

		if (m_sslContextCustom == null)
		{
			//// we need to create an SSLContext internally
			try
			{
				final KeyManager[]   l_keyManagers;
				final TrustManager[] l_trustManagers;
				final SSLContext     l_sslContext;

				// initialize the key material
				if (m_strKsPath != null)
				{
					final KeyStore          l_ksKeys;
					final char[]            l_passwd;
					final KeyManagerFactory l_kmf;

					l_ksKeys = SSLUtil.loadKeyStore(m_strKsType, m_strKsPath, m_strKsPassword);
					l_passwd = m_strKsPassword != null ? m_strKsPassword.toCharArray() : null;

					l_kmf = KeyManagerFactory.getInstance(m_strKeyManagerFacAlgorithm);
					l_kmf.init(l_ksKeys, l_passwd);

					l_keyManagers = l_kmf.getKeyManagers();
				}
				else
				{
					l_keyManagers = null;
				}

				// initialize the trust material
				if (m_strTsPath != null)
				{
					final KeyStore            l_ksTrust;
					final TrustManagerFactory l_tmf;

					l_ksTrust = SSLUtil.loadKeyStore(m_strTsType, m_strTsPath, m_strTsPassword);

					l_tmf = TrustManagerFactory.getInstance(m_strTrustManagerFacAlgorithm);
					l_tmf.init(l_ksTrust);

					l_trustManagers = l_tmf.getTrustManagers();
				}
				else
				{
					l_trustManagers = null;
				}

				l_sslContext = SSLContext.getInstance(m_strProtocol);

				l_sslContext.init(l_keyManagers, l_trustManagers, null);

				m_sslContext = l_sslContext;
			}
			catch (Exception l_e)
			{
				throw new JaffreConfigurationException("Cannot create SSLContext.", l_e);
			}
		}
		else
		{
			m_sslContext = m_sslContextCustom;
		}
	}


	/*
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize()
	{
		if (m_sslEngine == null || m_sslContext == null || m_socketChannel == null)
			ms_log.warn("Call dispose to cleanup system resources.");

		dispose();
	}
}
