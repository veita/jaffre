/* $Id: SSLSocketJaffreConnector.java 394 2009-03-21 20:28:26Z  $
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


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreConfigurationException;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.JaffreSerializeException;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.io.AbstractByteBufferInputStream;
import org.jaffre.io.AbstractByteBufferOutputStream;
import org.jaffre.server.JaffreServerException;
import org.jaffre.ssl.SSLUtil;
import org.jaffre.util.JaffreUtil;


/**
 * @author Alexander Veit
 */
public class SSLSocketJaffreConnector extends AbstractSocketJaffreConnector
{
	private static final Logger ms_log =
		LoggerFactory.getLogger(SSLSocketJaffreConnector.class);

	private boolean m_bRunning = false;

	private final ThreadGroup m_threadGroup =
		new ThreadGroup("SSLSocketJaffreConnectorThreadGroup");

	private final ThreadFactory m_threadFactory = new ConnectorThreadFactory();

	private volatile boolean m_bRun = false;

	private AtomicInteger m_intRunning = new AtomicInteger(0);

	private AtomicInteger m_intEngaged = new AtomicInteger(0);


	private Selector m_selector;

	private ServerSocketChannel m_channel;


	//// SSL related properties

	private SSLContext m_sslContext;

	private SSLContext m_sslContextCustom;

	private String m_strProtocol = "TLSv1.2";

	private String m_strKsType;

	private String m_strKsPath;

	private String m_strKsPassword;

	private String m_strKeyManagerFacAlgorithm = "SunX509";

	private String m_strTsType;

	private String m_strTsPath;

	private String m_strTsPassword;

	private String m_strTrustManagerFacAlgorithm = "SunX509";

	private boolean m_bNeedClientAuth = false;


	/**
	 * The standard thread factory for this connector.
	 */
	private final class ConnectorThreadFactory implements ThreadFactory
	{
		private AtomicInteger m_intCount = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable p_runnable)
		{
			final Thread        l_thread;
			final StringBuilder l_sbuf;
			final InetAddress   l_inetAddr;
			final int           l_iPort;

			l_inetAddr = getBindingInetAddress();
			l_iPort    = getPort();

			assert l_inetAddr != null;
			assert l_iPort >= 0 && l_iPort <= 0xFFFF;

			l_sbuf = new StringBuilder(128);

			l_sbuf.append("SSLSocketJaffreConnectorThread-");
			l_sbuf.append(l_inetAddr.getHostAddress());
			l_sbuf.append(':');
			l_sbuf.append(l_iPort);
			l_sbuf.append('-');
			l_sbuf.append(m_intCount.incrementAndGet());

			l_thread = new Thread(m_threadGroup, p_runnable, l_sbuf.toString());

			l_thread.setDaemon(true);

			return l_thread;
		}
	}


	/**
	 * The connector's worker thread implementation.
	 */
	private final class ConnectorRunnable implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				m_intRunning.incrementAndGet();

				while (_shouldRun())
				{
					try (final SocketChannel l_channel = _select())
					{
						if (l_channel != null)
						{
							try
							{
								// start new workers if neccessary and allowed
								if (m_intEngaged.incrementAndGet() >= m_intRunning.get())
								{
									if (m_intRunning.get() < getMaxThreadPoolSize())
										_startWorkerThread();
								}

								_process(l_channel);
							}
							finally
							{
								m_intEngaged.decrementAndGet();
							}
						}
					}
					catch (ClosedByInterruptException l_e)
					{
						assert !_shouldRun();

						ms_log.debug("Socket closed by interrupt.", l_e);
					}
					catch (Throwable l_e)
					{
						ms_log.error("An unexpected error occurred.", l_e);
					}
				}
			}
			finally
			{
				m_intRunning.decrementAndGet();
			}

			ms_log.debug("Exiting " + Thread.currentThread().getName() + ".");
		}


		/**
		 * @return <code>true</code> if the connector's <code>stop()</code>
		 *    method was not already invoked and the server-socket channel's
		 *    selector is open.
		 */
		private boolean _shouldRun()
		{
			assert m_selector != null;

			return m_bRun && m_selector.isOpen();
		}


		/**
		 * @return A selected server-socket channel.
		 * @throws ClosedSelectorException
		 *    If the server-socket channel's selector was closed.
		 */
		private SocketChannel _select() throws ClosedSelectorException
		{
			assert m_selector != null;

			synchronized (m_selector)
			{
				if (!_shouldRun())
					return null;

				try
				{
					if (m_selector.select() > 0) // i.e. == 1
					{
						final Iterator<SelectionKey> l_it;

						l_it = m_selector.selectedKeys().iterator();

						if (l_it.hasNext())
						{
							final SelectionKey        l_skey;
							final ServerSocketChannel l_ssc;
							final SocketChannel       l_channel;

							l_skey = l_it.next();

							l_it.remove();

							l_ssc = (ServerSocketChannel)l_skey.channel();

							assert l_ssc == m_channel;

							l_channel = l_ssc.accept();

							if (l_channel != null)
								l_channel.configureBlocking(true);
							else
								ms_log.error("Selected a null channel.");

							return l_channel;
						}
						else
						{
							return null;
						}
					}
					else
					{
						// no keys selected
						return null;
					}
				}
				catch (IOException l_e)
				{
					ms_log.error("Error while selecting channels.", l_e);

					return null;
				}
			}
		}


		/**
		 * @param p_channel The socket channel.
		 * @throws ClosedChannelException
		 * @throws ClosedByInterruptException If the socket was closed by an interrupt.
		 * @throws IOException If another I/O error occurred.
		 * @throws JaffreSerializeException
		 *    See {@link org.jaffre.JaffreCallFrameSerializer#deserializeCall(InputStream)}.
		 * @throws ClassNotFoundException
		 *    See {@link org.jaffre.JaffreCallFrameSerializer#deserializeCall(InputStream)}.
		 */
		private void _process(final SocketChannel p_channel)
			throws ClosedByInterruptException, ClosedChannelException, IOException,
				JaffreSerializeException, ClassNotFoundException
		{
			final SocketAddress l_socketAddr;
			final SSLEngine     l_sslEngine;

			// create and configure the SSLEngine
			l_socketAddr = p_channel.socket().getRemoteSocketAddress();

			if (l_socketAddr instanceof InetSocketAddress)
			{
				final InetSocketAddress l_inetSocketAddr;

				l_inetSocketAddr = (InetSocketAddress)l_socketAddr;

				l_sslEngine = m_sslContext.createSSLEngine
					(l_inetSocketAddr.getHostName(), l_inetSocketAddr.getPort());
			}
			else
			{
				l_sslEngine = m_sslContext.createSSLEngine();
			}

			l_sslEngine.setUseClientMode(false);
			l_sslEngine.setNeedClientAuth(m_bNeedClientAuth);

			final SSLSession l_session;
			final ByteBuffer l_outAppBuf;
			final ByteBuffer l_outNetBuf;
			final ByteBuffer l_inAppBuf;
			final ByteBuffer l_inNetBuf;

			l_session   = l_sslEngine.getSession();
			l_outAppBuf = ByteBuffer.allocate(l_session.getApplicationBufferSize());
			l_outNetBuf = ByteBuffer.allocate(l_session.getPacketBufferSize());
			l_inAppBuf  = ByteBuffer.allocate(l_session.getApplicationBufferSize());
			l_inNetBuf  = ByteBuffer.allocate(l_session.getPacketBufferSize());

			SSLUtil.doHandshake
				(p_channel, l_sslEngine, l_outAppBuf, l_outNetBuf, l_inAppBuf, l_inNetBuf);

			InputStream  l_in;
			OutputStream l_out;

			l_in  = null;
			l_out = null;

			try
			{
				l_in = new AbstractByteBufferInputStream(l_inAppBuf, false)
				{
					@Override
					public int read(ByteBuffer p_buffer) throws IOException
					{
						assert p_buffer == l_inAppBuf;

						return SSLUtil.read(p_channel, l_sslEngine, l_inAppBuf, l_inNetBuf);
					}
				};

				l_out = new BufferedOutputStream(new AbstractByteBufferOutputStream(l_outAppBuf)
				{
					@Override
					public void write(ByteBuffer p_buffer, WRITE_MODE p_mode) throws IOException
					{
						assert p_buffer == l_outAppBuf;

						SSLUtil.write(p_channel, l_sslEngine, l_outAppBuf, l_outNetBuf);
					}
				});

				if (m_bRun)
					ms_log.debug("Begin dialog.");

				dialog:
				while (m_bRun)
				{
					final JaffreCallFrame   l_frameCall;
					final boolean         l_bKeepAlive;
					final JaffreReturnFrame l_frameReturn;

					l_frameCall = getCallFrameSerializer().deserialize(l_in);

					if (l_frameCall == null)
						break dialog;

					l_bKeepAlive = l_frameCall.isKeepAlive();

					l_frameReturn = getServer().process(l_frameCall, null);

					if (l_frameCall.isInOut())
					{
						l_frameReturn.setKeepAlive(l_bKeepAlive);

						getReturnFrameSerializer().serialize(l_frameReturn, l_out);
						l_out.flush();
					}

					if (!l_bKeepAlive || !canKeepAlive())
						break dialog;
				}
			}
			finally
			{
				// exchange closing messages with the client
				SSLUtil.writeClosingMessage(p_channel, l_sslEngine, l_outAppBuf, l_outNetBuf);
				SSLUtil.readClosingMessage(p_channel, l_sslEngine, l_inAppBuf, l_inNetBuf);

				JaffreUtil.close(l_in);
				JaffreUtil.close(l_out);

				ms_log.debug("End dialog.");
			}
		}
	}


	public SSLSocketJaffreConnector()
	{
		m_strKsPath     = System.getProperty("javax.net.ssl.keyStore");
		m_strKsPassword = System.getProperty("javax.net.ssl.keyStorePassword");
		m_strKsType     = System.getProperty("javax.net.ssl.keyStoreType");
		m_strTsPath     = System.getProperty("javax.net.ssl.trustStore");
		m_strTsPassword = System.getProperty("javax.net.ssl.trustStorePassword");
		m_strTsType     = System.getProperty("javax.net.ssl.trustStoreType");
	}


	/**
	 * Get the number of threads that are currently handling
	 * connections.
	 * @return The number of threads that are currently
	 *    handling connections.
	 */
	public int getNumRunningThreads()
	{
		return m_intRunning.get();
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


	public boolean isNeedClientAuth()
	{
		return m_bNeedClientAuth;
	}


	public void setNeedClientAuth(boolean p_bNeedClientAuth)
	{
		m_bNeedClientAuth = p_bNeedClientAuth;
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
	 * Otherwise {@link java.security.KeyStore#getDefaultType()} is
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
	 * Otherwise {@link java.security.KeyStore#getDefaultType()} is
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
	 * Otherwise {@link java.security.KeyStore#getDefaultType()} is
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
	 * Otherwise {@link java.security.KeyStore#getDefaultType()} is
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


	/*
	 * @see org.jaffre.server.JaffreConnector#start()
	 */
	@Override
	public synchronized void start()
	{
		final InetAddress l_inetAddr;
		final int         l_iPort;

		assert !m_bRun;
		assert getCoreThreadPoolSize() > 0;

		if (m_bRunning)
			throw new IllegalStateException("The connector is already running.");

		if (getServer() == null)
			throw new JaffreConfigurationException("No Jaffre server.");

		l_inetAddr = getBindingInetAddress();

		if (l_inetAddr == null)
			throw new JaffreConfigurationException("No binding address.");

		l_iPort = getPort();

		if (l_iPort < 0 || l_iPort > 0xFFFF)
			throw new JaffreConfigurationException("Illegal port number " + l_iPort + ".");

		_setupSSLContext();

		try
		{
			m_selector = SelectorProvider.provider().openSelector();
			m_channel  = ServerSocketChannel.open();

			m_channel.configureBlocking(false);
			m_channel.socket().bind(new InetSocketAddress(l_inetAddr, l_iPort));
			m_channel.register(m_selector, SelectionKey.OP_ACCEPT, null);
		}
		catch (IOException l_e)
		{
			throw new JaffreServerException("Cannot start connector.", l_e);
		}

		m_bRun = true;

		for (int i = 0, l_iCnt = getCoreThreadPoolSize(); i < l_iCnt; i++)
			_startWorkerThread();

		m_bRunning = true;
	}


	/*
	 * @see org.jaffre.server.JaffreConnector#isRunning()
	 */
	@Override
	public boolean isRunning()
	{
		return m_bRunning;
	}


	/*
	 * @see org.jaffre.server.JaffreConnector#stop()
	 */
	@Override
	public synchronized void stop()
	{
		assert m_bRun;

		if (!m_bRunning)
			throw new IllegalStateException("The connector is not running.");

		m_bRun = false;

		try
		{
			final long l_lTimeLimit;

			m_threadGroup.interrupt();

			l_lTimeLimit = System.currentTimeMillis() + getStopTimeout();

			try
			{
				while (System.currentTimeMillis() < l_lTimeLimit && m_threadGroup.activeCount() > 0)
					Thread.sleep(10);
			}
			catch (InterruptedException l_e)
			{
				// don't wait any longer
			}

			JaffreUtil.close(m_channel);

			_destroySSLContext();
		}
		finally
		{
			m_bRunning = false;
		}
	}


	/**
	 * Start a new worker thread.
	 */
	private void _startWorkerThread()
	{
		m_threadFactory.newThread(new ConnectorRunnable()).start();
	}


	protected boolean canKeepAlive()
	{
		return true; // TODO
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


	private void _destroySSLContext()
	{
		m_sslContext = null;
	}
}
