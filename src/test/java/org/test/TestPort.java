/*
 * Created on 02.06.2013
 * (C) Copyright 2003-2013 Alexander Veit
 */


package org.test;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Get new port numbers to avoid problems with the TCP wait state.
 * @author <a href="mailto:alexander.veit@gmx.net">Alexander Veit</a>
 */
public final class TestPort
{
	private static final AtomicInteger ms_port = new AtomicInteger(14711);


	public static int getNext()
	{
		return ms_port.getAndIncrement();
	}


	private TestPort()
	{
	}
}
