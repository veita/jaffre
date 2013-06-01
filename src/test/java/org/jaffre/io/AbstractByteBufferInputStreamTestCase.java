/* $Id: AbstractByteBufferInputStreamTestCase.java 394 2009-03-21 20:28:26Z  $
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;


/**
 * @author Alexander Veit
 */
public class AbstractByteBufferInputStreamTestCase extends TestCase
{
	private static final String TEXT =
		"Java is a programming language originally developed by Sun Microsystems and" +
		" released in 1995 as a core component of Sun Microsystems' Java platform." +
		" The language derives much of its syntax from C and C++ but has a simpler" +
		" object model and fewer low-level facilities. Java applications are typically" +
		" compiled to bytecode that can run on any Java virtual machine (JVM) regardless" +
		" of computer architecture.";


	public void testReadEOF() throws Exception
	{
		final AbstractByteBufferInputStream l_in;

		l_in = new AbstractByteBufferInputStream(10)
		{
			@Override
			public int read(ByteBuffer p_buffer) throws IOException
			{
				p_buffer.flip();
				return -1;
			}
		};

		assertEquals(-1, l_in.read());
		assertEquals(-1, l_in.read());
		assertEquals(-1, l_in.read(new byte[10]));
		assertEquals(-1, l_in.read(new byte[10]));
	}


	public void testSkipEOF() throws Exception
	{
		final AbstractByteBufferInputStream l_in;

		l_in = new AbstractByteBufferInputStream(10)
		{
			@Override
			public int read(ByteBuffer p_buffer) throws IOException
			{
				p_buffer.flip();
				return -1;
			}
		};

		assertEquals(0, l_in.skip(1));
		assertEquals(0, l_in.skip(0));
		assertEquals(0, l_in.skip(1));
	}


	public void testReadSingleByte() throws Exception
	{
		byte[] l_buf;
		String l_str;

		l_buf = "".getBytes();
		assertTrue(Arrays.equals(l_buf, _testReadSingleByte(l_buf)));

		l_buf = "Hello World! \u00FF".getBytes("ISO-8859-1");
		assertTrue(Arrays.equals(l_buf, _testReadSingleByte(l_buf)));

		l_str = TEXT + TEXT;
		l_str = l_str + l_str;
		l_str = l_str + l_str;
		l_str = l_str + l_str;
		l_buf = l_str.getBytes();

		for (int i = 0; i < 100; i++)
			assertTrue(Arrays.equals(l_buf, _testReadSingleByte(l_buf)));
	}


	private byte[] _testReadSingleByte(final byte[] p_buf) throws Exception
	{
		final int                           MIN_BUFFER_SIZE;
		final AbstractByteBufferInputStream l_in;
		final AtomicInteger                 l_intPos;
		int                                 l_iRead;
		ByteArrayOutputStream               l_baos;

		MIN_BUFFER_SIZE = 4;

		l_intPos = new AtomicInteger(0);

		l_in = new AbstractByteBufferInputStream(MIN_BUFFER_SIZE + (int)(64 * Math.random()))
		{
			@Override
			public int read(ByteBuffer p_buffer) throws IOException
			{
				final int l_iPos;
				final int l_iChunk;

				assertEquals(0, p_buffer.position());
				assertEquals(p_buffer.capacity(), p_buffer.limit());

				l_iPos = l_intPos.get();

				if (l_iPos >= p_buf.length)
				{
					p_buffer.flip();
					return -1;
				}

				l_iChunk = Math.min
					(p_buf.length - l_iPos, 1 + (int)(MIN_BUFFER_SIZE * Math.random()));

				p_buffer.put(p_buf, l_iPos, l_iChunk).flip();

				l_intPos.addAndGet(l_iChunk);

				return l_iChunk;
			}
		};

		l_baos = new ByteArrayOutputStream();

		while ((l_iRead = l_in.read()) != -1)
			l_baos.write(l_iRead);

		return l_baos.toByteArray();
	}


	public void testReadByte() throws Exception
	{
		byte[] l_buf;
		String l_str;

		l_buf = "".getBytes();
		assertTrue(Arrays.equals(l_buf, _testReadByte(l_buf)));

		l_buf = "Hello World!".getBytes();
		assertTrue(Arrays.equals(l_buf, _testReadByte(l_buf)));

		l_str = TEXT + TEXT;
		l_str = l_str + l_str;
		l_str = l_str + l_str;
		l_str = l_str + l_str;
		l_buf = l_str.getBytes();

		for (int i = 0; i < 100; i++)
			assertTrue(Arrays.equals(l_buf, _testReadByte(l_buf)));
	}


	private byte[] _testReadByte(final byte[] p_buf) throws Exception
	{
		final int                           MIN_BUFFER_SIZE;
		final AbstractByteBufferInputStream l_in;
		final AtomicInteger                 l_intPos;
		int                                 l_iRead;
		byte[]                              l_buf;
		ByteArrayOutputStream               l_baos;

		MIN_BUFFER_SIZE = 4;

		l_intPos = new AtomicInteger(0);

		l_in = new AbstractByteBufferInputStream(MIN_BUFFER_SIZE + (int)(64 * Math.random()))
		{
			@Override
			public int read(ByteBuffer p_buffer) throws IOException
			{
				final int l_iPos;
				final int l_iChunk;

				assertEquals(0, p_buffer.position());
				assertEquals(p_buffer.capacity(), p_buffer.limit());

				l_iPos = l_intPos.get();

				if (l_iPos >= p_buf.length)
				{
					p_buffer.flip();
					return -1;
				}

				l_iChunk = Math.min
					(p_buf.length - l_iPos, 1 + (int)(MIN_BUFFER_SIZE * Math.random()));

				p_buffer.put(p_buf, l_iPos, l_iChunk).flip();

				l_intPos.addAndGet(l_iChunk);

				return l_iChunk;
			}
		};

		l_buf  = new byte[13];
		l_baos = new ByteArrayOutputStream();

		while ((l_iRead = l_in.read(l_buf, 0, 1 + (int)(13 * Math.random()))) != -1)
			l_baos.write(l_buf, 0, l_iRead);

		return l_baos.toByteArray();
	}
}
