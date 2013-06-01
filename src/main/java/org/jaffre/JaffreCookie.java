/* $Id: JaffreCookie.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre;


/**
 * @author Alexander Veit
 */
public final class JaffreCookie
{
	private static final ThreadLocal<Object> ms_cookie = new ThreadLocal<Object>();


	private JaffreCookie()
	{
	}


	public static <T> T get()
	{
		@SuppressWarnings("unchecked")
		final T l_cookie = (T)ms_cookie.get();

		return l_cookie;
	}


	public static void set(Object p_cookie)
	{
		ms_cookie.set(p_cookie);
	}


	public static void clear()
	{
		ms_cookie.set(null);
	}
}
