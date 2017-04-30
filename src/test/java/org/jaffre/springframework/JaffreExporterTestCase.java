/*
 * (C) Copyright 2008-2017 Alexander Veit
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


package org.jaffre.springframework;


import java.io.File;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.test.JaffreTestCaseBase;
import org.test.PackageFile;


/**
 * @author Alexander Veit
 */
public final class JaffreExporterTestCase extends JaffreTestCaseBase
{
	public void test() throws Exception
	{
		final File                            l_fileAppContext;
		final FileSystemXmlApplicationContext l_appCtx;

		l_fileAppContext = PackageFile.get("test-resources/appcontext01.xml");
		assertTrue(l_fileAppContext.isFile());

		l_appCtx = new FileSystemXmlApplicationContext(l_fileAppContext.getPath());

		l_appCtx.start();

		assertEquals(6, l_appCtx.getBeanDefinitionCount());
		assertNotNull(l_appCtx.getBean("jaffreClient"));
		assertNotNull(l_appCtx.getBean("jaffreServer"));
		assertNotNull(l_appCtx.getBean("jaffreConnector"));
		assertNotNull(l_appCtx.getBean("jaffreExporter"));
		assertNotNull(l_appCtx.getBean("throwException"));
		assertNotNull(l_appCtx.getBean("greeting"));

		l_appCtx.stop();
		l_appCtx.close();
	}
}
