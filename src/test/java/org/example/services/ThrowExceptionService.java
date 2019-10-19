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


package org.example.services;


import java.io.IOException;


/**
 * @author Alexander Veit
 */
public class ThrowExceptionService implements ThrowException
{
	public ThrowExceptionService()
	{
	}


	/**
	 * @throws An <code>java.io.IOException</code> with
	 *   message <code>"A test exception."</code>
	 */
	@Override
	public void throwException(String p_strExceptionClass)
		throws IOException
	{
		throw new IOException("A test exception.");
	}
}
