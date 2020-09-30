/*
 * (C) Copyright 2008-2017 Alexander Veit
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


package org.jaffre.spi;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jaffre.JaffreCallFrame;
import org.junit.jupiter.api.Test;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class DefaultJaffreCallFrameSerializerTestCase extends JaffreTestCaseBase
{
	@Test
	public void testSerializeIllegalArgumentException01()
	{
		assertIAE(
			() -> new DefaultJaffreCallFrameSerializer().serialize(null, null),
			"No call frame to serialize.");
	}


	@Test
	public void testSerializeIllegalArgumentException02()
	{
		final JaffreCallFrame l_frame;

		l_frame = new JaffreCallFrame(Runnable.class, "run", null, null);

		assertIAE(
			() -> new DefaultJaffreCallFrameSerializer().serialize(l_frame, null),
			"No output stream.");
	}


	@Test
	public void testDeserializeIllegalArgumentException()
	{
		assertIAE(
			() -> new DefaultJaffreCallFrameSerializer().deserialize(null),
			"No input stream.");
	}


	@Test
	public void testSerializeDeserialize()
		throws Exception
	{
		final JaffreCallFrame                  l_frame;
		final DefaultJaffreCallFrameSerializer l_ser;
		final ByteArrayOutputStream            l_baos;

		l_frame = new JaffreCallFrame(Runnable.class, "run", null, null);
		l_ser   = new DefaultJaffreCallFrameSerializer();
		l_baos  = new ByteArrayOutputStream();

		l_ser.serialize(l_frame, l_baos);

		final ByteArrayInputStream l_bais;
		final JaffreCallFrame      l_deser;

		l_bais  = new ByteArrayInputStream(l_baos.toByteArray());
		l_deser = l_ser.deserialize(l_bais);

		assertEquals(l_frame, l_deser);
	}
}
