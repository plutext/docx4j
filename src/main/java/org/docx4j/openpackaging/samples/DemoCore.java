/*
 * Copyright (c) 2006, Wygwam
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * - Neither the name of Wygwam nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without 
 * specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.docx4j.openpackaging.samples;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Core helper for demos.
 * 
 * @author Julien Chable, CDubet
 * @version 1.0
 */
public class DemoCore {

	private String testRootPath; // Test root path

	/**
	 * All sample document are normally located at this
	 * place.
	 */
	private static String pathRootProject; // Project root path
	
	/**
	 * Demo logger
	 */
	private static Logger logger = Logger.getLogger("org.openxml4j.demo");

	static {
		pathRootProject = System.getProperty("user.dir") + File.separator;

		// Log4j configuration
		PropertyConfigurator.configure(pathRootProject + File.separator
				+ "config.log4j");
	}

	/**
	 * Constructor. Initialize the demo.
	 * 
	 */
	public DemoCore() {
		init();
	}
	
	/**
	 * Initialize the test root path
	 */
	public void init() {
		String packageName = getClass().getPackage().getName();
		// replace . by /
		String sep = File.separator;
		if (sep.equals("\\")) {
			sep = "\\\\";
		}
		testRootPath = pathRootProject + File.separator + "src" + File.separator
				+ packageName.replaceAll("\\.", sep) + File.separator;
	}
	
	// Accessors
	
	/**
	 * Gets the test root path.
	 * 
	 * @return The test root path.
	 */
	public String getTestRootPath() {
		return testRootPath;
	}

	/**
	 * Sets the test root path.
	 * @param testRoot
	 */
	public void setTestRootPath(String testRoot) {
		this.testRootPath = testRoot;
	}

	/**
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}
}