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


package org.jaffre.ssl;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import org.jaffre.Logger;
import org.jaffre.LoggerFactory;


/**
 * @author Alexander Veit
 */
public final class SSLUtil
{
	private static final Logger ms_log = LoggerFactory.getLogger(SSLUtil.class);

	private SSLUtil()
	{
	}


	/**
	 * Load a keystore.
	 * @param p_strType The keystore type, or <code>null</code> if
	 *    the default keystore type should be used.
	 * @param p_strPath The path to the keystore.
	 * @param p_strPassphrase The keystore passphrase, or <code>null</code>.
	 * @return The loaded keystore.
	 * @throws KeyStoreException If no provider supports the requested
	 *    keystore type.
	 * @throws NoSuchAlgorithmException If the algorithm used to check
     *    the integrity of the keystore cannot be found.
	 * @throws CertificateException If any of the certificates in the
     *    keystore could not be loaded.
	 * @throws IOException If an I/O error occurred.
	 */
	public static KeyStore loadKeyStore(String p_strType, String p_strPath, String p_strPassphrase)
		throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		final KeyStore l_ksKeyStore;

		if (p_strType != null)
			l_ksKeyStore = KeyStore.getInstance(p_strType);
		else
			l_ksKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		try (InputStream l_in = new BufferedInputStream(new FileInputStream(p_strPath)))
		{
			l_ksKeyStore.load
				(l_in, p_strPassphrase != null ? p_strPassphrase.toCharArray() : null);
		}

		return l_ksKeyStore;
	}


	/**
	 * Perform a SSL handshake.
	 * <p>The buffers passed to this method must be clear.</p>
	 * <p><code>p_outAppBuf</code>, <code>p_outNetBuf</code>, and
	 * <code>p_inAppBuf</code> are clear when this method returns.</p>
	 * <p>The buffer <code>p_inNetBuf</code> is compacted and may contain
	 * input data that are not handshake but payload data.</p>
	 * @param p_channel The socket channel.
	 * @param p_sslEngine The SSL engine to be used.
	 * @param p_outAppBuf The application output buffer.
	 * @param p_outNetBuf The network output buffer.
	 * @param p_inAppBuf The application input buffer.
	 * @param p_inNetBuf The network input buffer.
	 * @throws IOException If an I/O error occurred.
	 */
	public static void doHandshake(SocketChannel p_channel,
	                               SSLEngine     p_sslEngine,
	                               ByteBuffer    p_outAppBuf,
	                               ByteBuffer    p_outNetBuf,
	                               ByteBuffer    p_inAppBuf,
	                               ByteBuffer    p_inNetBuf)
		throws IOException
	{
		final long l_lTimeStart;

		assert p_outAppBuf.position() == 0 && p_outAppBuf.limit() == p_outAppBuf.capacity();
		assert p_outNetBuf.position() == 0 && p_outNetBuf.limit() == p_outNetBuf.capacity();
		assert p_inAppBuf.position() == 0 && p_inAppBuf.limit() == p_inAppBuf.capacity();
		assert p_inNetBuf.position() == 0 && p_inNetBuf.limit() == p_inNetBuf.capacity();

		if (ms_log.isDebugEnabled())
		{
			ms_log.debug("Begin SSL handshake.");

			l_lTimeStart = System.currentTimeMillis();
		}
		else
		{
			l_lTimeStart = 0;
		}

		assert p_sslEngine.getHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING :
			p_sslEngine.getHandshakeStatus().toString();

		p_sslEngine.beginHandshake();

		handshake:
		while (true)
		{
			final HandshakeStatus l_hss;

			l_hss = p_sslEngine.getHandshakeStatus();

			switch (l_hss)
			{
			case FINISHED:
				ms_log.debug("SSL handshake status FINISHED.");
				break handshake;

			case NOT_HANDSHAKING:
				ms_log.debug("SSL handshake status NOT_HANDSHAKING.");
				break handshake;

			case NEED_UNWRAP:
				ms_log.debug("SSL handshake status NEED_UNWRAP.");
				_handshakeUnwrap(p_channel, p_sslEngine, p_inAppBuf, p_inNetBuf);
				break;

			case NEED_WRAP:
				ms_log.debug("SSL handshake status NEED_WRAP.");
				_handshakeWrap(p_channel, p_sslEngine, p_outAppBuf, p_outNetBuf);
				break;

			case NEED_TASK:
				ms_log.debug("SSL handshake status NEED_TASK.");
				p_sslEngine.getDelegatedTask().run();
				break;

			default:
				ms_log.error("SSL handshake: unexpected status " + l_hss + ".");
				throw new AssertionError("Unexpected handshake status " + l_hss + ".");
			}
		}

		// clear the buffers that need to be cleared
		// p_inNetBuf was alread compacted by _handshakeUnwrap
		p_outAppBuf.clear();
		p_outNetBuf.clear();
		p_inAppBuf.clear();

		if (ms_log.isDebugEnabled())
		{
			final long l_lDiff;

			l_lDiff = System.currentTimeMillis() - l_lTimeStart;

			ms_log.debug("Finished SSL handshake in " + l_lDiff + " ms.");
		}
	}


	private static SSLEngineResult _handshakeWrap(SocketChannel p_channel,
	                                              SSLEngine     p_sslEngine,
	                                              ByteBuffer    p_outAppBuf,
	                                              ByteBuffer    p_outNetBuf)
		throws IOException
	{
		final SSLEngineResult l_res;

		// generate handshaking data
		p_outNetBuf.clear();

		l_res = p_sslEngine.wrap(p_outAppBuf, p_outNetBuf);

		switch (l_res.getStatus())
		{
		case OK:
			p_outNetBuf.flip();

			// send the handshaking data to the peer
			while (p_outNetBuf.hasRemaining())
			{
				p_channel.write(p_outNetBuf);

				if (p_outNetBuf.hasRemaining()) // TODO really useful?
					Thread.yield();
			}

			p_outNetBuf.clear();
			break;

		case BUFFER_OVERFLOW:
			ms_log.error("SSL handshake write status BUFFER_OVERFLOW.");
			throw new AssertionError("SSL write status BUFFER_OVERFLOW.");

		case BUFFER_UNDERFLOW:
			ms_log.error("SSL handshake write status BUFFER_UNDERFLOW.");
			throw new AssertionError("SSL write status BUFFER_UNDERFLOW.");

		case CLOSED:
			ms_log.error("SSL handshake write status CLOSED.");
			throw new AssertionError("SSL write status CLOSED.");

		default:
			ms_log.error("SSL handshake write: unexpected status " + l_res.getStatus() + ".");

			throw new AssertionError
				("SSL handshake write: unexpected status " + l_res.getStatus() + ".");
		}

		return l_res;
	}


	/**
	 * <p>The buffer <code>p_inAppBuf</code> is flipped an contains decrypted data when
	 * this method returns.</p>
	 * <p>The buffer <code>p_inNetBuf</code> is compacted when this method returns.</p>
	 * @param p_inAppBuf The application input buffer.
	 * @param p_inNetBuf The network input buffer.
	 * @throws IOException If an I/O error occurred.
	 */
	private static void _handshakeUnwrap(SocketChannel p_channel,
	                                     SSLEngine     p_sslEngine,
	                                     ByteBuffer    p_inAppBuf,
	                                     ByteBuffer    p_inNetBuf)
		throws IOException
	{
		while (true)
		{
			if (p_inNetBuf.position() == 0)
			{
				final int l_iRead;

				l_iRead = p_channel.read(p_inNetBuf);

				if (l_iRead == -1)
					throw new ClosedChannelException();

				if (l_iRead == 0)
					throw new AssertionError("Unexpected 0 byte read in blocking channel mode.");
			}
			else
			{
				final SSLEngineResult l_res;

				p_inNetBuf.flip();
				p_inAppBuf.clear();

				l_res = p_sslEngine.unwrap(p_inNetBuf, p_inAppBuf);

				p_inNetBuf.compact();
				p_inAppBuf.clear();

				// check status
				switch (l_res.getStatus())
				{
				case OK:
					ms_log.debug("SSL handshake read status OK.");
					return;

				case BUFFER_UNDERFLOW:
					ms_log.debug("SSL handshake read status BUFFER_UNDERFLOW.");
					Thread.yield(); // wait for more data to arrive
					break;

				case BUFFER_OVERFLOW:
					ms_log.error("SSL handshake read status BUFFER_OVERFLOW.");
					throw new AssertionError("SSL read status BUFFER_OVERFLOW.");

				case CLOSED:
					ms_log.error("SSL handshake read status CLOSED.");
					throw new AssertionError("SSL read status CLOSED.");

				default:
					ms_log.error("SSL handshake read: unexpected status " + l_res.getStatus() + ".");

					throw new AssertionError
						("SSL handshake read: unexpected status " + l_res.getStatus() + ".");
				}
			}
		}
	}


	/**
	 * Read data from the socket.
	 * <p>The buffer <code>p_inAppBuf</code> is flipped and contains
	 * decrypted data when this method returns.</p>
	 * <p>The buffer <code>p_inNetBuf</code> is compacted when this
	 * method returns. The caller must not modify the contents of this
	 * buffer between two subsequent calls of this method</p>
	 * @param p_channel The socket channel.
	 * @param p_sslEngine The SSL engine to be used.
	 * @param p_inAppBuf A compacted buffer that will receive decrypted data.
	 * @param p_inNetBuf An buffer that will receive network data.
	 * @throws IOException If an I/O error occurred.
	 * @return The number of bytes read, or -1 if no more input is available.
	 */
	public static int read(SocketChannel p_channel,
	                       SSLEngine     p_sslEngine,
	                       ByteBuffer    p_inAppBuf,
	                       ByteBuffer    p_inNetBuf)
		throws IOException
	{
		final int l_iBytesBefore;
		final int l_iBytesAfter;

		// the buffers must be compacted
		assert p_inAppBuf.limit() == p_inAppBuf.capacity();
		assert p_inNetBuf.limit() == p_inNetBuf.capacity();

		l_iBytesBefore = p_inAppBuf.position();

		while (true)
		{
			final int             l_iRead;
			final SSLEngineResult l_res;

			p_inNetBuf.flip();

			l_res = p_sslEngine.unwrap(p_inNetBuf, p_inAppBuf);

			p_inNetBuf.compact();

			// check status
			switch (l_res.getStatus())
			{
			case OK:
				l_iBytesAfter = p_inAppBuf.position();

				p_inAppBuf.flip();

				return l_iBytesAfter - l_iBytesBefore;

			case BUFFER_UNDERFLOW:
				// read data
				l_iRead = p_channel.read(p_inNetBuf);

				if (l_iRead == -1)
				{
					assert p_inNetBuf.position() == 0;
					return -1;
				}

				break;

			case CLOSED:
				// the client sent an SSL close message
				return -1;

			case BUFFER_OVERFLOW:
				throw new AssertionError("SSL read status BUFFER_OVERFLOW.");

			default:
				throw new AssertionError
					("SSL read: unexpected status " + l_res.getStatus() + ".");
			}
		}
	}


	/**
	 * Write data to the socket.
	 * @param p_channel The socket channel.
	 * @param p_sslEngine The SSL engine to be used.
	 * @param p_outAppBuf The application output buffer.
	 * @param p_outNetBuf The network output buffer.
	 * @throws IOException If an I/O error occurred.
	 */
	public static void write(SocketChannel p_channel,
	                         SSLEngine     p_sslEngine,
	                         ByteBuffer    p_outAppBuf,
	                         ByteBuffer    p_outNetBuf)
		throws IOException
	{
		assert p_outNetBuf.limit() == p_outNetBuf.capacity();

		while (p_outAppBuf.hasRemaining())
		{
			final SSLEngineResult l_res;

			// encrypt data
			l_res = p_sslEngine.wrap(p_outAppBuf, p_outNetBuf);

			switch (l_res.getStatus())
			{
			case OK:
			case BUFFER_OVERFLOW:
				p_outNetBuf.flip();

				// send the encrypted data to the peer
				while (p_outNetBuf.hasRemaining()) // coward loop
				{
					p_channel.write(p_outNetBuf);

					if (p_outNetBuf.hasRemaining())
						Thread.yield(); // should not occur in blocking mode
				}

				p_outNetBuf.clear();
				break;

			case BUFFER_UNDERFLOW:
				throw new AssertionError("SSL write status BUFFER_UNDERFLOW.");

			case CLOSED:
				throw new AssertionError("SSL write status CLOSED.");

			default:
				throw new AssertionError
					("SSL write: unexpected status " + l_res.getStatus() + ".");
			}
		}
	}


	/**
	 * Clear the output buffers and send a SSL close message to the peer.
	 * <p>The output buffers are cleared when this method returns.</p>
	 * @param p_channel The socket channel to write to.
	 * @param p_sslEngine The SSL engine to be used.
	 * @param p_outAppBuf The application output buffer.
	 * @param p_outNetBuf The network output buffer.
	 * @throws IOException If an I/O error occurred.
	 */
	public static void writeClosingMessage(SocketChannel p_channel,
                                           SSLEngine     p_sslEngine,
                                           ByteBuffer    p_outAppBuf,
                                           ByteBuffer    p_outNetBuf)
		throws IOException
	{
		assert !p_sslEngine.isOutboundDone();

		ms_log.debug("SSL write closing message.");

		p_outAppBuf.clear();
		p_outNetBuf.clear();

		p_sslEngine.closeOutbound();
		assert p_sslEngine.getHandshakeStatus() == HandshakeStatus.NEED_WRAP;

		while (!p_sslEngine.isOutboundDone())
		{
			final SSLEngineResult l_res;

			l_res = p_sslEngine.wrap(p_outAppBuf, p_outNetBuf);

			assert l_res.getStatus() == Status.OK || l_res.getStatus() == Status.CLOSED;

			p_outNetBuf.flip();

			while (p_outNetBuf.hasRemaining())
			{
				p_channel.write(p_outNetBuf);

				if (p_outNetBuf.hasRemaining())
					Thread.yield(); // should not occur in blocking mode
			}

			p_outNetBuf.clear();
		}

		p_outAppBuf.clear();
	}


	/**
	 * Receive a SSL close message from the peer.
	 * <p>The input buffers are cleared when this method returns.</p>
	 * @param p_channel The socket channel to read from.
	 * @param p_sslEngine The SSL engine to be used.
	 * @param p_inAppBuf The application input buffer.
	 * @param p_inNetBuf The network input buffer.
	 * @throws IOException If an I/O error occurred.
	 */
	public static void readClosingMessage(SocketChannel p_channel,
	                                      SSLEngine     p_sslEngine,
	                                      ByteBuffer    p_inAppBuf,
	                                      ByteBuffer    p_inNetBuf)
		throws IOException
	{
		ms_log.debug("SSL read closing message.");

		p_inAppBuf.clear();

		read_loop:
		while (!p_sslEngine.isInboundDone())
		{
			final int             l_iRead;
			final SSLEngineResult l_res;

			p_inNetBuf.flip();
			l_res = p_sslEngine.unwrap(p_inNetBuf, p_inAppBuf);
			p_inNetBuf.compact();
			p_inAppBuf.clear();

			switch (l_res.getStatus())
			{
			case OK:
			case BUFFER_UNDERFLOW:
				l_iRead = p_channel.read(p_inNetBuf);

				if (l_iRead == -1)
					break read_loop;

				break;

			case CLOSED:
				break read_loop;

			default:
				throw new AssertionError
					("SSL read closing message: unexpected status " + l_res.getStatus() + ".");
			}
		}

		p_sslEngine.closeInbound();

		p_inAppBuf.clear();
		p_inNetBuf.clear();
	}
}
