/*
 * (C) Copyright 2008-2019 Alexander Veit
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.test.JaffreTestCaseBase;


/**
 * @author Alexander Veit
 */
public final class JaffreUncheckedExceptionTestCase extends JaffreTestCaseBase
{
	@Test
	public void testConstructors()
	{
		final Exception l_cause;

		l_cause = new Exception("cause");

		assertNull(new JaffreUncheckedException().getMessage());
		assertNull(new JaffreUncheckedException().getCause());

		assertEquals("err", new JaffreUncheckedException("err").getMessage());
		assertNull(new JaffreUncheckedException("err").getCause());

		assertEquals("java.lang.Exception: cause", new JaffreUncheckedException(l_cause).getMessage());
		assertSame(l_cause, new JaffreUncheckedException(l_cause).getCause());

		assertEquals("err", new JaffreUncheckedException("err", l_cause).getMessage());
		assertSame(l_cause, new JaffreUncheckedException("err", l_cause).getCause());
	}
}
