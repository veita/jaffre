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


import java.util.concurrent.atomic.AtomicReference;

import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class JaffreCookieTestCase extends JaffreTestCaseBase
{
	private static final String COOKIE = "cookie";

	private static final String OTHER_COOKIE = "otherCookie";


	public void testLifeCycle() throws Exception
	{
		assertNull(JaffreCookie.get());

		try
		{
			final AtomicReference<Boolean> l_ref;
			final Thread                   l_thread;

			JaffreCookie.set(COOKIE);
			assertSame(COOKIE, JaffreCookie.get());

			l_ref = new AtomicReference<>();
			assertNull(l_ref.get());

			l_thread = new Thread("otherThread")
			{
				@Override
				public void run()
				{
					if (JaffreCookie.get() == null)
						l_ref.set(Boolean.TRUE);
					else
						l_ref.set(Boolean.FALSE);

					JaffreCookie.set(OTHER_COOKIE);
					assertSame(OTHER_COOKIE, JaffreCookie.get());
				}
			};

			l_thread.start();
			l_thread.join();

			assertSame(Boolean.TRUE, l_ref.get());
			assertSame(COOKIE, JaffreCookie.get());
		}
		finally
		{
			JaffreCookie.clear();
		}

		assertNull(JaffreCookie.get());
	}
}
