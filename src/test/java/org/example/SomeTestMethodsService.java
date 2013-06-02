/*
 * Created on 02.06.2013
 * (C) Copyright 2003-2013 Alexander Veit
 */


package org.example;


import org.example.services.SomeTestMethods;
import org.jaffre.Logger;
import org.jaffre.LoggerFactory;


/**
 * @author <a href="mailto:alexander.veit@gmx.net">Alexander Veit</a>
 */
public final class SomeTestMethodsService implements SomeTestMethods
{
	private static final Logger ms_log = LoggerFactory.getLogger(SomeTestMethodsService.class);

	@Override
	public int add(int p_iA, int p_iB)
	{
		return p_iA + p_iB;
	}

	@Override
	public Object echo(Object p_obj)
	{
		return p_obj;
	}

	@Override
	public String echo(String p_str)
	{
		return p_str;
	}

	@Override
	public void log(String p_str)
	{
		ms_log.info(p_str);
	}

	@Override
	public void throwException(String p_strExceptionClass) throws Exception
	{
		final Class<?> l_class;

		l_class = Class.forName(p_strExceptionClass);

		throw (Exception)(l_class.newInstance());
	}
}