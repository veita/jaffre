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


package org.jaffre.server.spi;


import java.util.concurrent.atomic.AtomicBoolean;

import org.jaffre.JAFFRE_FLAG;
import org.jaffre.JaffreCallFrame;
import org.jaffre.JaffreNoInterfaceException;
import org.jaffre.JaffreReturnFrame;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class DefaultJaffreServerTestCase extends JaffreTestCaseBase
{
	public void testGetterSetter()
	{
		final DefaultJaffreServer l_server;

		l_server = new DefaultJaffreServer();

		assertFalse(l_server.isAcceptCookies());
		l_server.setAcceptCookies(true);
		assertTrue(l_server.isAcceptCookies());

		assertFalse(l_server.isSendCookies());
		l_server.setSendCookies(true);
		assertTrue(l_server.isSendCookies());

		assertTrue(l_server.isLogInOnlyExceptions());
		l_server.setLogInOnlyExceptions(false);
		assertFalse(l_server.isLogInOnlyExceptions());

		assertTrue(l_server.isLogInOutExceptions());
		l_server.setLogInOutExceptions(false);
		assertFalse(l_server.isLogInOutExceptions());
	}


	public void testNoInterface()
	{
		final DefaultJaffreServer   l_server;
		final JaffreCallFrame       l_call;
		final JaffreReturnFrame     l_return;

		l_server = new DefaultJaffreServer();

		l_call = new JaffreCallFrame(Runnable.class, "run", null, null);

		l_return = l_server.process(l_call, null);

		assertTrue(l_return.isExceptionResult());
		assertTrue(l_return.getResult() instanceof JaffreNoInterfaceException);
		assertEquals(JAFFRE_FLAG.NO_FLAGS, l_return.getFlags());
	}


	public void testCallRunnable()
	{
		final DefaultJaffreServer   l_server;
		final AtomicBoolean         l_bWasHere;
		final JaffreCallFrame       l_call;
		final JaffreReturnFrame     l_return;

		l_server = new DefaultJaffreServer();

		l_bWasHere = new AtomicBoolean(false);

		l_server.registerInterface((Runnable)() -> l_bWasHere.set(true));

		l_call = new JaffreCallFrame(Runnable.class, "run", null, null);

		assertFalse(l_bWasHere.get());

		l_return = l_server.process(l_call, null);

		assertTrue(l_bWasHere.get());

		assertFalse(l_return.isExceptionResult());
		assertNull(l_return.getResult());
		assertEquals(JAFFRE_FLAG.NO_FLAGS, l_return.getFlags());
	}
}
