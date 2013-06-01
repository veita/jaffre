/* $Id: JaffreNoInterfaceException.java 394 2009-03-21 20:28:26Z  $
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
public final class JaffreNoInterfaceException extends JaffreUncheckedException
{
	private static final long serialVersionUID = -3489813913783026741L;


	public JaffreNoInterfaceException(String p_strMsg)
	{
		super(p_strMsg);
	}
}
