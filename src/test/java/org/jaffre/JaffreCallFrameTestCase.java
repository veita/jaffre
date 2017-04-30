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


import java.util.function.IntFunction;

import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class JaffreCallFrameTestCase extends JaffreTestCaseBase
{
	public void testConstructorIllegalArgumentException() throws Exception
	{
		new JaffreCallFrame(Runnable.class, "run", null, null);

		assertIAE(
			() -> new JaffreCallFrame(null, "run", null, null),
			"No interface class.");

		assertIAE(
			() -> new JaffreCallFrame(String.class, "run", null, null),
			"java.lang.String is not an interface.");

		assertIAE(
			() -> new JaffreCallFrame(Runnable.class, null, null, null),
			"No method name.");

		assertIAE(
			() -> new JaffreCallFrame(IntFunction.class, "apply", new Class[] {int.class}, new Object[0]),
			"The number of parameters does not match the number of arguments.");
	}


	public void testSimple() throws Exception
	{
		final JaffreCallFrame l_frame;

		l_frame = new JaffreCallFrame
			(IntFunction.class, "apply", new Class[] {int.class}, new Object[] {785});

		assertEquals(0, l_frame.getFlags());
		assertFalse(l_frame.isInOnly());
		assertFalse(l_frame.isInOut());
		assertEquals(l_frame.isClose(), !l_frame.isKeepAlive());
		assertFalse(l_frame.isKeepAlive());
		assertTrue(l_frame.isClose());

		l_frame.setFlags(JAFFRE_FLAG.MEP_IN_ONLY);
		assertTrue(l_frame.isInOnly());
		assertEquals(JAFFRE_FLAG.MEP_IN_ONLY, l_frame.getFlags());

		l_frame.setFlags(JAFFRE_FLAG.MEP_IN_OUT);
		assertTrue(l_frame.isInOut());
		assertEquals(JAFFRE_FLAG.MEP_IN_OUT, l_frame.getFlags());

		l_frame.setFlags(JAFFRE_FLAG.CONNECTION_KEEP_ALIVE);
		assertEquals(l_frame.isClose(), !l_frame.isKeepAlive());
		assertTrue(l_frame.isKeepAlive());
		assertFalse(l_frame.isClose());
		assertEquals(JAFFRE_FLAG.CONNECTION_KEEP_ALIVE, l_frame.getFlags());

		assertEquals(IntFunction.class, l_frame.getInterface());
		assertEquals("apply", l_frame.getMethodName());

		assertTrue(l_frame.hasParameters());
		assertArraysEquals(new Class[] {int.class}, l_frame.getParameterTypes());
		assertArraysEquals(new Object[] {785}, l_frame.getParameters());

		assertFalse(l_frame.hasUserData());
		l_frame.setUserData("test");
		assertTrue(l_frame.hasUserData());
		assertEquals("test", l_frame.getUserData());
	}


	public void testEqualsHashCode() throws Exception
	{
		final JaffreCallFrame l_frame1;
		final JaffreCallFrame l_frame2;

		l_frame1 = new JaffreCallFrame
			(IntFunction.class, "apply", new Class[] {int.class}, new Object[] {785});

		assertTrue("Any object is equal to itself.", l_frame1.equals(l_frame1));
		assertFalse(l_frame1.equals("apply"));
		assertEquals("hashCode must be deterministic.", l_frame1.hashCode(), l_frame1.hashCode());

		l_frame2 = new JaffreCallFrame
			(IntFunction.class, "apply", new Class[] {int.class}, new Object[] {785});

		assertTrue(l_frame1.equals(l_frame2));
		assertTrue(l_frame2.equals(l_frame1));
		assertEquals(l_frame1.hashCode(), l_frame2.hashCode());

		l_frame2.setUserData("test");
		assertFalse(l_frame1.equals(l_frame2));
		assertFalse(l_frame2.equals(l_frame1));
	}
}
