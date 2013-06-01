/* $Id: JaffreConnector.java 394 2009-03-21 20:28:26Z  $
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


package org.jaffre.server;


/**
 * @author Alexander Veit
 */
public interface JaffreConnector
{
	/**
	 * Connect this connector with a server.
	 * @param p_server The server to connect with.
	 */
	public void setServer(JaffreServer p_server);

	/**
	 * Get the server this connector is connected to.
	 * @return The server this connector is connected with,
	 *    or <code>null</code>.
	 */
	public JaffreServer getServer();

	/**
	 * Start this connector.
	 */
	public void start();

	/**
	 * Check if the connector is ronning.
	 * @return <code>true</code> if the connector is running,
	 *    or <code>false</code> otherwise.
	 */
	public boolean isRunning();

	/**
	 * Stop this connector.
	 */
	public void stop();
}
