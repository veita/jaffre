/* $Id: JAFFRE_FLAG.java 394 2009-03-21 20:28:26Z  $
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
public final class JAFFRE_FLAG
{
	/** <code>0x00000000</code> */
	public static final int NO_FLAGS = 0x00000000;

	/** <code>0xFFFF0000</code> */
	public static final int MASK_CUSTOM = 0xFFFF0000;

	/** <code>0x00000001</code> */
	public static final int CONNECTION_KEEP_ALIVE = 0x00000001;

	/** <code>0x00000100</code> */
	public static final int MEP_IN_OUT = 0x00000100;

	/** <code>0x00000200</code> */
	public static final int MEP_IN_ONLY = 0x00000200;

	/** <code>{@link #MEP_IN_OUT} | {@link #MEP_IN_ONLY}</code> */
	public static final int MASK_MEP = MEP_IN_OUT | MEP_IN_ONLY;


	private JAFFRE_FLAG()
	{
	}
}
