/* $Id: PackageFile.java 394 2009-03-21 20:28:26Z  $
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


package org.test;


import java.io.File;


/**
 * @author Alexander Veit
 */
public final class PackageFile
{
	private static final String BASE_DIR;

	static
	{
		BASE_DIR = System.getProperty("test.baseDir", "src/test/java");
	}

	private PackageFile()
	{
	}


	/**
	 * @param p_strPath The path relative to the caller class' package directory.
	 * @return The file.
	 */
	public static final File get(String p_strPath)
	{
		final StackTraceElement l_ste = new Throwable().getStackTrace()[1];

		return _get(l_ste.getClassName(), p_strPath);
	}


	public static final File get(Class<?> p_class, String p_strPath)
	{
		return _get(p_class.getName(), p_strPath);
	}


	public static final File get(Object p_obj, String p_strPath)
	{
		return _get(p_obj.getClass().getName(), p_strPath);
	}


	/**
	 * @param p_strPath The path relative to the caller class' package directory.
	 * @return The file.
	 */
	private static final File _get(String p_strClassName, String p_strPath)
	{
		final File l_dirBase;
		String     l_strPackagePath;
		final File l_dirPackage;
		final File l_file;

		l_dirBase = new File(BASE_DIR);

		if (!l_dirBase.isDirectory())
			throw new RuntimeException("The directory " + BASE_DIR + " does not exist.");

		l_strPackagePath = p_strClassName.replaceAll("\\.", "/");
		l_strPackagePath = l_strPackagePath.substring(0, l_strPackagePath.lastIndexOf('/'));

		l_dirPackage = new File(l_dirBase, l_strPackagePath);

		if (!l_dirPackage.isDirectory())
			throw new RuntimeException("The directory " + l_dirPackage.getPath() + " does not exist.");

		l_file = new File(l_dirPackage, p_strPath);

		if (!l_dirPackage.exists())
			throw new RuntimeException("The file " + l_file.getPath() + " does not exist.");

		return l_file;
	}
}
