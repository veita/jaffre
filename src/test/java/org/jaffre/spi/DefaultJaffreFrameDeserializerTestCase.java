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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class DefaultJaffreFrameDeserializerTestCase extends JaffreTestCaseBase
{
	public void testSerializeNull() throws Exception
	{
		ByteArrayOutputStream l_bos;
		ObjectOutputStream    l_oos;
		final byte[]          l_buf;

		l_bos = new ByteArrayOutputStream();
		l_oos = new ObjectOutputStream(l_bos);

		l_oos.writeObject(null);

		l_buf = l_bos.toByteArray();

		assertTrue(l_buf.length > 0);

		ByteArrayInputStream l_bin;
		ObjectInputStream    l_oin;

		l_bin = new ByteArrayInputStream(l_buf);
		l_oin = new ObjectInputStream(l_bin);

		assertNull(l_oin.readObject());
	}
}
