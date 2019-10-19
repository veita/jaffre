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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author Alexander Veit
 */
public interface JaffreCallFrameSerializer
{
	/**
	 * Serialize a <code>JaffreCallFrame</code>.
	 * <p><i>Note: this method must be thread-safe.</i></p>
	 * @param p_frame The frame to be serialized.
	 * @param p_out The output stream.
	 * @throws IllegalArgumentException
	 *    If <code>p_frame</code> or <code>p_out</code> is <code>null</code>.
	 * @throws IOException If an I/O error occurred.
	 * @throws JaffreSerializeException
	 *    If the frame could not be serialized for some reason.
	 */
	public void serialize(JaffreCallFrame p_frame, OutputStream p_out)
		throws IOException, JaffreSerializeException;


	/**
	 * Deserialize a <code>JaffreCallFrame</code>.
	 * <p><i>Note: this method must be thread-safe.</i></p>
	 * @param p_in The input stream.
	 * @return The deserialized frame, or <code>null</code> if no more
	 *    frames are available on the input stream.
	 * @throws IOException If an I/O error occurred.
	 * @throws ClassNotFoundException If the class of a serialized object
	 *    cannot be found.
	 * @throws JaffreSerializeException If the deserialized object was not a
	 *    <code>JaffreCallFrame</code>.
	 */
	public JaffreCallFrame deserialize(InputStream p_in)
		throws IOException, ClassNotFoundException, JaffreSerializeException;
}
