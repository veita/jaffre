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


package org.test;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Get new port numbers to avoid problems with the TCP wait state.
 * @author Alexander Veit
 */
public final class TestPort
{
	private static final AtomicInteger ms_port = new AtomicInteger(14711);


	public static int getNext()
	{
		return ms_port.getAndIncrement();
	}


	private TestPort()
	{
	}
}
