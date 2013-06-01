/* $Id: SomeTestMethodsService.java 394 2009-03-21 20:28:26Z  $
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


package org.example.services;


import org.jaffre.Logger;
import org.jaffre.LoggerFactory;


/**
 * @author Alexander Veit
 */
public class SomeTestMethodsService
{
	private static final Logger ms_log = LoggerFactory.getLogger(SomeTestMethodsService.class);

	public SomeTestMethodsService()
	{
	}

	public int add(int p_iA, int p_iB)
	{
		return p_iA + p_iB;
	}

	public Object echo(Object p_obj)
	{
		return p_obj;
	}

	public String echo(String p_str)
	{
		return p_str;
	}

	public void log(String p_str)
	{
		ms_log.info(p_str);
	}

	public void throwException(String p_strExceptionClass) throws Exception
	{
		final Class<?> l_class;

		l_class = Class.forName(p_strExceptionClass);

		throw (Exception)(l_class.newInstance());
	}
}
