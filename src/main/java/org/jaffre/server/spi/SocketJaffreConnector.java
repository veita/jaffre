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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreConfigurationException;
import org.jaffre.JaffreReturnFrame;
import org.jaffre.JaffreSerializeException;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;
import org.jaffre.server.JaffreServerException;
import org.jaffre.util.JaffreUtil;


/**
 * @author Alexander Veit
 */
public class SocketJaffreConnector extends AbstractSocketJaffreConnector
{
	private static final Logger ms_log = LoggerFactory.getLogger(SocketJaffreConnector.class);

	private boolean m_bRunning = false;

	private final ThreadGroup m_threadGroup = new ThreadGroup("SocketJaffreConnectorThreadGroup");

	private final ThreadFactory m_threadFactory = new ConnectorThreadFactory();

	private volatile boolean m_bRun = false;

	private AtomicInteger m_intRunning = new AtomicInteger(0);

	private AtomicInteger m_intEngaged = new AtomicInteger(0);


	private Selector m_selector;

	private ServerSocketChannel m_channel;


	private int m_iBufferSize = 8192;


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

			l_sbuf.append("SocketJaffreConnectorThread-");
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
							try (final Socket l_socket = l_channel.socket())
							{
								// start new workers if neccessary and allowed
								if (m_intEngaged.incrementAndGet() >= m_intRunning.get())
								{
									if (m_intRunning.get() < getMaxThreadPoolSize())
										_startWorkerThread();
								}

								_process(l_socket);
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
					if (m_selector.select() > 0) // TODO i.e. == 1 ?????
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

							if (l_channel == null)
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
		 * @param p_socket The socket to read data from.
		 * @throws ClosedChannelException
		 * @throws ClosedByInterruptException If the socket was closed by an interrupt.
		 * @throws IOException If another I/O error occurred.
		 * @throws JaffreSerializeException
		 *    See {@link org.jaffre.JaffreCallFrameSerializer#deserializeCall(InputStream)}.
		 * @throws ClassNotFoundException
		 *    See {@link org.jaffre.JaffreCallFrameSerializer#deserializeCall(InputStream)}.
		 */
		private void _process(Socket p_socket)
			throws ClosedByInterruptException, ClosedChannelException, IOException,
				JaffreSerializeException, ClassNotFoundException
		{
			try (BufferedInputStream  l_in  = new BufferedInputStream(p_socket.getInputStream(), m_iBufferSize);
			     BufferedOutputStream l_out = new BufferedOutputStream(p_socket.getOutputStream(), m_iBufferSize))
			{
				dialog:
				while (m_bRun)
				{
					final JaffreCallFrame   l_frameCall;
					final boolean           l_bKeepAlive;
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
		}
	}


	public SocketJaffreConnector()
	{
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

		try
		{
			m_selector = SelectorProvider.provider().openSelector();
			m_channel  = ServerSocketChannel.open();

			// TODO make socket option SO_REUSEADDR configurable to handle time wait states
			//m_channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
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
		return true;
	}
}
