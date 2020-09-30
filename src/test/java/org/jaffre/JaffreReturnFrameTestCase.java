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


package org.jaffre;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class JaffreReturnFrameTestCase extends JaffreTestCaseBase
{
	@Test
	public void testSimple()
	{
		final JaffreReturnFrame l_frame;

		l_frame = new JaffreReturnFrame("the result value", false);

		assertEquals(JAFFRE_FLAG.NO_FLAGS, l_frame.getFlags());
		assertEquals(l_frame.isClose(), !l_frame.isKeepAlive());
		assertFalse(l_frame.isKeepAlive());
		assertTrue(l_frame.isClose());

		l_frame.setFlags(JAFFRE_FLAG.CONNECTION_KEEP_ALIVE);
		assertEquals(l_frame.isClose(), !l_frame.isKeepAlive());
		assertTrue(l_frame.isKeepAlive());
		assertFalse(l_frame.isClose());
		assertEquals(JAFFRE_FLAG.CONNECTION_KEEP_ALIVE, l_frame.getFlags());

		assertEquals("the result value", l_frame.getResult());

		assertFalse(l_frame.isExceptionResult());

		assertFalse(l_frame.hasUserData());
		l_frame.setUserData("test");
		assertTrue(l_frame.hasUserData());
		assertEquals("test", l_frame.getUserData());
	}


	@Test
	public void testEqualsHashCode()
	{
		final JaffreReturnFrame l_frame1;
		final JaffreReturnFrame l_frame2;

		l_frame1 = new JaffreReturnFrame("the result value", false);

		assertTrue(l_frame1.equals(l_frame1), "Any object is equal to itself.");
		assertFalse(l_frame1.equals("apply"));
		assertEquals(l_frame1.hashCode(), l_frame1.hashCode(), "hashCode must be deterministic.");

		l_frame2 = new JaffreReturnFrame("the result value", false);

		assertTrue(l_frame1.equals(l_frame2));
		assertTrue(l_frame2.equals(l_frame1));
		assertEquals(l_frame1.hashCode(), l_frame2.hashCode());

		l_frame2.setUserData("test");
		assertFalse(l_frame1.equals(l_frame2));
		assertFalse(l_frame2.equals(l_frame1));
	}
}
