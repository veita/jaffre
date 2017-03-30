/* $Id: AbstractByteBufferInputStream.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.io;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * <p>This class is not synchronized.</p>
 * @author Alexander Veit
 */
public abstract class AbstractByteBufferInputStream extends InputStream
{
	protected ByteBuffer m_buffer;


	/**
	 * Create an input stream that reads a <code>java.nio.ByteBuffer</code>.
	 * <p>The given buffer will be read and will be passed in calls to the
	 * {@link #read(ByteBuffer)} in order to be refilled.</p>
	 * @param p_buffer The buffer to be read.
	 * @param p_bContainsData <code>true</code> if the buffer already contains
	 *    data and is prepared for subsequent relative <code>get</code>
	 *    operations, or <code>false</code> if the buffer does not contain any
	 *    data that should be read by this input stream.
	 */
	public AbstractByteBufferInputStream(ByteBuffer p_buffer, boolean p_bContainsData)
	{
		m_buffer = p_buffer;

		if (!p_bContainsData)
			m_buffer.clear().flip();
	}


	public AbstractByteBufferInputStream(int p_iCapacity)
	{
		this(ByteBuffer.allocate(p_iCapacity), false);

		if (p_iCapacity <= 0) // < 0 is already checked by allocate
		{
			throw new IllegalArgumentException
				(p_iCapacity + " is not a valid buffer capacity.");
		}
	}


	/**
	 * Returns an estimate of the number of bytes that can be read (or skipped over)
	 * from this input stream without blocking by the next invocation of a method
	 * for this input stream.
	 * <p>The value is equal to the number of bytes remaining in the underlying buffer,
	 * or <code>1</code> if the underlying buffer contains no data when this method is
	 * called.</p>
	 * @return The estimate number of bytes that can be read (or skipped over) from
	 *    this input stream
	 * @see java.nio.Buffer#remaining()
	 */
	@Override
	public int available() throws IOException
	{
		return m_buffer.hasRemaining() ? m_buffer.remaining() : 1;
	}


	/**
	 * Marks are not supported by this stream.
	 * @return Always <code>false</code>;
	 */
	@Override
	public boolean markSupported()
	{
		return false;
	}


	/**
	 * @throws UnsupportedOperationException Marks are not supported by this stream.
	 */
	@Override
	public void mark(int p_iReadlimit)
	{
		throw new UnsupportedOperationException("Marks are not supported by this stream.");
	}


	/**
	 * @throws UnsupportedOperationException Marks are not supported by this stream.
	 */
	@Override
	public void reset() throws IOException
	{
		throw new UnsupportedOperationException("Marks are not supported by this stream.");
	}


	@Override
	public int read() throws IOException
	{
		if (!m_buffer.hasRemaining())
		{
			if (_read(m_buffer) == -1)
				return -1;
		}

		return m_buffer.get() & 0x000000FF;
	}


	@Override
	public int read(byte[] p_buf, int p_iOffs, int p_iLen) throws IOException
	{
		int l_iPos;
		int l_iLen;

		if (p_iOffs < 0 || p_iOffs > p_buf.length || p_iLen < 0 ||
		    p_iOffs + p_iLen > p_buf.length || p_iOffs + p_iLen < 0)
		{
			throw new IndexOutOfBoundsException();
		}

		l_iPos = p_iOffs;
		l_iLen = p_iLen;

		read:
		while (true)
		{
			final int l_iRemaining;

			if (!m_buffer.hasRemaining())
			{
				if (_read(m_buffer) == -1)
					break read;
			}

			l_iRemaining = m_buffer.remaining();

			if (l_iRemaining >= l_iLen)
			{
				m_buffer.get(p_buf, l_iPos, l_iLen);

				l_iLen = 0;

				break read;
			}
			else
			{
				m_buffer.get(p_buf, l_iPos, l_iRemaining);

				l_iPos += l_iRemaining;
				l_iLen -= l_iRemaining;
			}
		}

		return p_iLen == l_iLen ? -1 : p_iLen - l_iLen;
	}


	@Override
	public int read(byte[] p_buf) throws IOException
	{
		return read(p_buf, 0, p_buf.length);
	}


	@Override
	public long skip(long p_lSkip) throws IOException
	{
		long l_lSkip;

		if (p_lSkip <= 0)
			return 0L;

		l_lSkip = p_lSkip;

		skip:
		while (true)
		{
			final int l_iRemaining;

			l_iRemaining = m_buffer.remaining();

			if (l_iRemaining == 0)
			{
				if (_read(m_buffer) == -1)
					break skip;
			}
			else if (l_iRemaining <= l_lSkip)
			{
				m_buffer.clear();

				l_lSkip -= l_iRemaining;

				if (_read(m_buffer) == -1)
					break skip;
			}
			else // l_iRemaining > l_lSkip
			{
				m_buffer.position(m_buffer.position() + (int)l_lSkip);

				l_lSkip = 0L;

				break skip;
			}
		}

		assert l_lSkip >= 0;

		return p_lSkip - l_lSkip;
	}


	/**
	 * Prepare the given buffer for input. Then call {@link #read(ByteBuffer)}
	 * and ensure that the implementing class satisfies the contract for
	 * {@link #read(ByteBuffer)}.
	 * @param p_buffer The buffer.
	 * @return The number of bytes read, or <code>-1</code> if no
	 *    more input is available.
	 * @throws IOException If an I/O error occurred, or if {@link #read(ByteBuffer)}
	 *    returned <code>0</code>.
	 * @see #read(ByteBuffer)
	 */
	private int _read(ByteBuffer p_buffer) throws IOException
	{
		final int l_iRead;

		p_buffer.compact();

		if ((l_iRead = read(p_buffer)) == 0)
		{
			throw new IOException
				("No bytes were read even though the end of input has not been reached.");
		}

		assert p_buffer.remaining() >= l_iRead;

		return l_iRead;
	}


	/**
	 * Read bytes into the given input buffer.
	 * <p>Note:</p>
	 * <ul>
	 * <li>The input buffer passed is compacted.</li>
	 * <li>Implementors <b>must</b> ensure that this method will never return 0.</li>
	 * <li>Also, this method <b>must</b> prepare the input buffer for subsequent relative
	 * <code>get</code> operations.</li>
	 * </ul>
	 * @param p_buffer The buffer.
	 * @return The number of bytes read, or <code>-1</code> if no
	 *    more input is available.
	 * @throws IOException If an I/O error occurred.
	 */
	public abstract int read(ByteBuffer p_buffer)
		throws IOException;


	/**
	 * This method does nothing.
	 */
	@Override
	public void close() throws IOException
	{
		// nothing to do
	}
}
