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


package org.jaffre.spi;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;

import org.jaffre.JaffreReturnFrame;
import org.jaffre.JaffreReturnFrameSerializer;
import org.jaffre.JaffreSerializeException;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;


/**
 * @author Alexander Veit
 */
public class DefaultJaffreReturnFrameSerializer implements JaffreReturnFrameSerializer
{
	private static final Logger ms_log =
		LoggerFactory.getLogger(DefaultJaffreReturnFrameSerializer.class);


	public DefaultJaffreReturnFrameSerializer()
	{
	}


	@Override
	public void serialize(JaffreReturnFrame p_frame, OutputStream p_out)
		throws IOException
	{
		final ObjectOutputStream l_oos;

		if (p_frame == null)
			throw new IllegalArgumentException("No return frame to serialize.");

		if (p_out == null)
			throw new IllegalArgumentException("No output stream.");

		l_oos = new ObjectOutputStream(p_out);

		l_oos.writeObject(p_frame);
	}


	@Override
	public JaffreReturnFrame deserialize(InputStream p_in)
		throws IOException, ClassNotFoundException, JaffreSerializeException
	{
		final ObjectInputStream l_ois;
		final Object            l_obj;

		if (p_in == null)
			throw new IllegalArgumentException("No input stream.");

		try
		{
			l_ois = new ObjectInputStream(p_in);
		}
		catch (EOFException l_e)
		{
			// no more frames available
			if (ms_log.isDebugEnabled())
				ms_log.debug("No more return frames to deserialize.", l_e);

			return null;
		}
		catch (ClosedChannelException l_e)
		{
			// no more frames available
			if (ms_log.isDebugEnabled())
				ms_log.debug("No more return frames to deserialize.", l_e);

			return null;
		}

		l_obj = l_ois.readObject();

		if (!(l_obj instanceof JaffreReturnFrame))
			throw new JaffreSerializeException("Unexpected object deserialized: " + l_obj + ".");

		return (JaffreReturnFrame)l_obj;
	}
}
