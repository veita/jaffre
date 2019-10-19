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
public interface Logger
{
	public boolean isTraceEnabled();

	public void trace(String p_strMessage);

	public void trace(String p_strMessage, Throwable p_e);


	public boolean isDebugEnabled();

	public void debug(String p_strMessage);

	public void debug(String p_strMessage, Throwable p_e);


	public boolean isInfoEnabled();

	public void info(String p_strMessage);

	public void info(String p_strMessage, Throwable p_e);


	public boolean isWarnEnabled();

	public void warn(String p_strMessage);

	public void warn(String p_strMessage, Throwable p_e);


	public boolean isErrorEnabled();

	public void error(String p_strMessage);

	public void error(String p_strMessage, Throwable p_e);
}
