/* $Id: AbstractByteBufferOutputStreamTestCase.java 394 2009-03-21 20:28:26Z  $
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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;


/**
 * @author Alexander Veit
 */
public final class AbstractByteBufferOutputStreamTestCase extends TestCase
{
	private static final String TEXT =
		"Java is a programming language originally developed by Sun Microsystems and" +
		" released in 1995 as a core component of Sun Microsystems' Java platform." +
		" The language derives much of its syntax from C and C++ but has a simpler" +
		" object model and fewer low-level facilities. Java applications are typically" +
		" compiled to bytecode that can run on any Java virtual machine (JVM) regardless" +
		" of computer architecture.";


	public void testNotConsumed() throws IOException
	{
		final OutputStream  l_out;
		final AtomicInteger l_intCnt;

		l_intCnt = new AtomicInteger(0);

		l_out = new AbstractByteBufferOutputStream(16)
		{
			@Override
			public void write(ByteBuffer p_buffer, WRITE_MODE p_mode) throws IOException
			{
				l_intCnt.incrementAndGet();
			}
		};

		l_out.write("0123456789".getBytes());
		assertEquals(0, l_intCnt.intValue());

		l_out.write("ABCDEF".getBytes());
		assertEquals(1, l_intCnt.intValue());

		try
		{
			l_out.close();

			fail("An IOException must occur.");
		}
		catch (IOException l_e)
		{
			assertEquals("Output data could not be written.", l_e.getMessage());
		}

		assertEquals(2, l_intCnt.intValue());
	}


	public void testConsumed() throws IOException
	{
		final ByteBuffer    l_buffer;
		final byte[]        l_buf;
		final OutputStream  l_out;
		final AtomicInteger l_intCnt;

		l_buffer = ByteBuffer.allocate(16);
		l_buf    = new byte[16];
		l_intCnt = new AtomicInteger(0);

		l_out = new AbstractByteBufferOutputStream(l_buffer)
		{
			@Override
			public void write(ByteBuffer p_buffer, WRITE_MODE p_mode) throws IOException
			{
				assertSame(l_buffer, p_buffer);

				if (l_intCnt.get() == 1)
				{
					assertSame(WRITE_MODE.CLOSE, p_mode);
					assertFalse(p_buffer.hasRemaining());
				}
				else
				{
					assertSame(WRITE_MODE.WRITE, p_mode);
					assertEquals("Unexpected integer value", 0, l_intCnt.get());

					p_buffer.get(l_buf, 0, 16);
				}

				l_intCnt.incrementAndGet();
			}
		};

		l_out.write("0123456789".getBytes("US-ASCII"));
		assertEquals(0, l_intCnt.intValue());

		l_out.write("ABCDEF".getBytes("US-ASCII"));
		assertEquals(1, l_intCnt.intValue());

		l_out.close();
		assertEquals(2, l_intCnt.intValue());
		assertTrue(Arrays.equals("0123456789ABCDEF".getBytes("US-ASCII"), l_buf));
	}


	public void testSimple() throws IOException
	{
		final ByteBuffer            l_buffer;
		final ByteArrayOutputStream l_baos;
		final OutputStream          l_out;
		final byte[]                l_in;

		l_buffer = ByteBuffer.allocate(16);
		l_baos   = new ByteArrayOutputStream();

		l_out = new AbstractByteBufferOutputStream(l_buffer)
		{
			@Override
			public void write(ByteBuffer p_buffer, WRITE_MODE p_mode) throws IOException
			{
				while (p_buffer.hasRemaining())
					l_baos.write(p_buffer.get());
			}
		};

		l_in = TEXT.getBytes("UTF-8");

		for (int i = 0; i < l_in.length;)
		{
			int l_iLen = 1 + (i % 3);

			if (i + l_iLen > l_in.length)
				l_iLen = l_in.length - i;

			l_out.write(l_in, i, l_iLen);

			i += l_iLen;
		}

		l_out.close();

		assertTrue(Arrays.equals(l_in, l_baos.toByteArray()));
	}
}
