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


/**
 * @author Alexander Veit
 */
public class JaffreSerializeException extends JaffreUncheckedException
{
	private static final long serialVersionUID = 5233592996366091440L;


	public JaffreSerializeException(String p_strMsg)
	{
		super(p_strMsg);
	}


	public JaffreSerializeException(String p_strMsg, Throwable p_cause)
	{
		super(p_strMsg, p_cause);
	}
}
