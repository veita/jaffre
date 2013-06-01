/* $Id: DefaultJaffreReturnFrameSerializerTestCase.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.spi;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.jaffre.JaffreReturnFrame;


/**
 * @author Alexander Veit
 */
public final class DefaultJaffreReturnFrameSerializerTestCase extends TestCase
{
	public void test() throws Exception
	{
		final JaffreReturnFrame                  l_frame;
		final DefaultJaffreReturnFrameSerializer l_ser;
		final ByteArrayOutputStream              l_baos;

		l_frame = new JaffreReturnFrame("Hello world!", false);
		l_ser   = new DefaultJaffreReturnFrameSerializer();
		l_baos  = new ByteArrayOutputStream();

		l_ser.serialize(l_frame, l_baos);

		final ByteArrayInputStream l_bais;
		final JaffreReturnFrame    l_deser;

		l_bais  = new ByteArrayInputStream(l_baos.toByteArray());
		l_deser = l_ser.deserialize(l_bais);

		assertEquals(l_frame, l_deser);
	}
}
