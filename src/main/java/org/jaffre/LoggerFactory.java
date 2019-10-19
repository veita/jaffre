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


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.jaffre.spi.DefaultLogger;


/**
 * @author Alexander Veit
 */
public final class LoggerFactory
{
	public static Logger getLogger(Class<?> p_cls)
	{
		final String l_strLoggerClass;
		Logger       l_logger;
		Throwable    l_exception;

		l_strLoggerClass = System.getProperty("org.jaffre.loggerClass");
		l_logger         = null;
		l_exception      = null;

		if (l_strLoggerClass != null)
		{
			try
			{
				final Class<?> l_clsLogger;
				Constructor<?> l_ctorDefault;
				Constructor<?> l_ctorClass;

				l_clsLogger   = Class.forName(l_strLoggerClass);
				l_ctorDefault = null;
				l_ctorClass   = null;

				find_ctor:
				for (final Constructor<?> l_ctor : l_clsLogger.getConstructors())
				{
					final Class<?>[] l_paramTypes;

					l_paramTypes = l_ctor.getParameterTypes();

					if (l_paramTypes.length == 1 && l_paramTypes[0].equals(Class.class) &&
					    ((l_ctor.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC))
					{
						l_ctorClass = l_ctor;

						break find_ctor;
					}
					else if (l_paramTypes.length == 0 &&
					    ((l_ctor.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC))
					{
						l_ctorDefault = l_ctor;
					}
				}

				final Object l_instance;

				if (l_ctorClass != null)
					l_instance = l_ctorClass.newInstance(p_cls);
				else if (l_ctorDefault != null)
					l_instance = l_ctorDefault.newInstance();
				else
					throw new NoSuchMethodException("No appropriate logger constructor.");

				if (!(l_instance instanceof Logger))
				{
					throw new ClassCastException
						(l_strLoggerClass + " is not an instance of " + Logger.class.getName() + ".");
				}

				l_logger = (Logger)l_instance;
			}
			catch (Exception l_e)
			{
				l_exception = l_e;
			}
		}

		if (l_logger == null)
			l_logger = new DefaultLogger(p_cls);

		if (l_exception != null)
			l_logger.error("Cannot use logger class " + l_strLoggerClass + ".", l_exception);

		return l_logger;
	}
}
