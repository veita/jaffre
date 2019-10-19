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


package org.jaffre.io;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


/**
 * This is a helper class for writing adapters between classical <code>java.io</code>
 * stream based output and <code>java.nio</code> based output.
 * <p>Write operations are buffered in an internal <code>java.nio.ByteBuffer</code>.
 * If this buffer gets full, or {@link #flush()} or {@link #close()} are being called,
 * the method {@link #write(ByteBuffer, WRITE_MODE)}, that must be implemented by derived
 * classes, is being called to consume pending data in the buffer.<br>
 * Data not consumed by {@link #write(ByteBuffer, WRITE_MODE)} will again be passed in
 * subsequent write operations. So the implementor is responsible for avoiding infinite
 * loops.</p>
 * <p>This class is not synchronized.</p>
 * @author Alexander Veit
 */
public abstract class AbstractByteBufferOutputStream extends OutputStream
{
	public enum WRITE_MODE {WRITE, FLUSH, CLOSE};

	protected ByteBuffer m_buffer;

	private final byte[] m_oneByte = new byte[1];


	public AbstractByteBufferOutputStream(int p_iCapacity)
	{
		if (p_iCapacity <= 0)
		{
			throw new IllegalArgumentException
				(p_iCapacity + " is not a valid buffer capacity.");
		}

		m_buffer = ByteBuffer.allocate(p_iCapacity);
	}


	public AbstractByteBufferOutputStream(ByteBuffer p_buffer)
	{
		m_buffer = p_buffer;
	}


	@Override
	public void write(int p_iByte) throws IOException
	{
		m_oneByte[0] = (byte)p_iByte;

		write(m_oneByte, 0, 1);
	}


	@Override
	public void write(byte[] p_buf, int p_iOffs, int p_iLen) throws IOException
	{
		int l_iOffs;
		int l_iToBeWritten;

		if (p_iOffs < 0 || p_iOffs > p_buf.length || p_iLen < 0 ||
		    p_iOffs + p_iLen > p_buf.length || p_iOffs + p_iLen < 0)
		{
			throw new IndexOutOfBoundsException();
		}

		l_iOffs        = p_iOffs;
		l_iToBeWritten = p_iLen;

		while (l_iToBeWritten > 0)
		{
			final int l_iFree;

			l_iFree = m_buffer.remaining();

			if (l_iToBeWritten < l_iFree)
			{
				m_buffer.put(p_buf, l_iOffs, l_iToBeWritten);

				l_iOffs        += l_iToBeWritten;
				l_iToBeWritten  = 0;
			}
			else
			{
				m_buffer.put(p_buf, l_iOffs, l_iFree);

				l_iOffs        += l_iFree;
				l_iToBeWritten -= l_iFree;

				m_buffer.flip();
				write(m_buffer, WRITE_MODE.WRITE);
				m_buffer.compact();
			}
		}

		assert l_iToBeWritten == 0;
	}


	/**
	 * @param p_buffer The buffer that contains the data to be written.
	 * @param p_mode <ul>
	 *    <li>{@link WRITE_MODE#WRITE} if this method was called by
	 *    {@link #write(int)}, {@link #write(byte[])}, or
	 *    {@link #write(byte[], int, int)}, or</li>
	 *    <li>{@link WRITE_MODE#FLUSH} if this method was called
	 *    by {@link #flush()}, or</li>
	 *    <li>{@link WRITE_MODE#CLOSE} if this method was called
	 *    by {@link #close()}.</li>
	 *    </ul>
	 * @throws IOException If an I/O error occurs.
	 */
	public abstract void write(ByteBuffer p_buffer, WRITE_MODE p_mode)
		throws IOException;


	/**
	 * Flushes this output stream and forces any buffered output bytes
     * to be written out.
     * <p>{@link #write(ByteBuffer, WRITE_MODE)} will be called with {@link WRITE_MODE#FLUSH}
     * as second parameter.</p>
     * @throws IOException If an I/O error occurred, or if the call to
     *    {@link #write(ByteBuffer, WRITE_MODE)} did not consume all pending data.
	 */
	@Override
	public void flush() throws IOException
	{
		m_buffer.flip();
		write(m_buffer, WRITE_MODE.FLUSH);

		// put the buffer in a defined state even though we may throw an exception
		if (m_buffer.hasRemaining())
		{
			m_buffer.compact();

			throw new IOException("Output data could not be written.");
		}
		else
		{
			m_buffer.compact();
		}
	}


	/**
	 * Closes this output stream and forces any buffered output bytes
     * to be written out.
     * <p>{@link #write(ByteBuffer, WRITE_MODE)} will be called with {@link WRITE_MODE#CLOSE}
     * as second parameter.</p>
     * @throws IOException If an I/O error occurred, or if the call to
     *    {@link #write(ByteBuffer, WRITE_MODE)} did not consume all pending data.
	 */
	@Override
	public void close() throws IOException
	{
		m_buffer.flip();
		write(m_buffer, WRITE_MODE.CLOSE);

		// put the buffer in a defined state even though we may throw an exception
		if (m_buffer.hasRemaining())
		{
			m_buffer.compact();

			throw new IOException("Output data could not be written.");
		}
		else
		{
			m_buffer.compact();
		}
	}
}
