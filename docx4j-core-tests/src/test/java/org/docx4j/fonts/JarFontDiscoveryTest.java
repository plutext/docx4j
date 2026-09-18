/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.fonts;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Assume;
import org.junit.Test;

/**
 * Font jars are discovered from every classpath root that has a {@code fonts/} folder,
 * not only the first.  Until 17.1.1 {@code PhysicalFonts.getFontUrls} used
 * {@code ClassLoader.getResource}, which answers with one root, so of two font jars
 * sharing the prefix (croscore and crosextra both use {@code fonts/}) only one was ever
 * walked - and on a headless box, where the jars are the whole font supply, Times New
 * Roman, Arial and Courier New then had no substitute.  CR-016 phase 0c.
 *
 * <p>The test module has one font jar (Liberation, under {@code fonts/}).  A second root
 * is made by copying one of its files under {@code target/test-classes/fonts/}, which is
 * also on the classpath; discovery must then see the files of both roots.
 */
public class JarFontDiscoveryTest {

	@Test
	public void everyRootWithTheFolderIsWalked() throws Exception {

		ClassLoader cl = PhysicalFonts.class.getClassLoader();
		URL jarFont = cl.getResource("fonts/LiberationSans-Regular.ttf");
		if (jarFont == null || !"jar".equals(jarFont.getProtocol())) {
			// the jar lays its fonts out under a subfolder; find any ttf under fonts/
			jarFont = firstTtfUnder(cl, "fonts");
		}
		/*
		 * The last assertion below is that a jar: root was walked, so the font module has
		 * to be on the classpath as a jar.  It is when the modules are installed and this
		 * one is tested on its own - which is how a release is built - and it is NOT under
		 * `mvn test -pl docx4j-core-tests -am`, the recipe CLAUDE.md gives: the reactor
		 * puts the font module's own target/classes *directory* on the classpath instead,
		 * so every discovered face has a file: URI and the test can only fail.  That is a
		 * fact about the build, not about getFontUrls, so it is assumed rather than
		 * asserted.  (CR-001 batch 48)
		 */
		Assume.assumeTrue("no font jar on the test classpath: the font module is here as a directory,"
				+ " which happens under `mvn test -am`; install the modules and run this one alone",
				jarFont != null && "jar".equals(jarFont.getProtocol()));

		// a second classpath root with the same folder: the test-classes directory
		URL testClasses = cl.getResource("docx4j.properties");
		Assume.assumeTrue("test-classes not found on the classpath", testClasses != null
				&& "file".equals(testClasses.getProtocol()));
		File dir = new File(new File(testClasses.toURI()).getParentFile(), "fonts/second-root");
		dir.mkdirs();
		File copy = new File(dir, "CopyOfAJarFont.ttf");
		try (InputStream is = jarFont.openStream()) {
			Files.copy(is, copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		int found = PhysicalFonts.discoverJarFonts("fonts");

		// the jar's twelve faces and the copy; with getResource, one root or the other
		assertTrue("discovery walked one root only: " + found + " font file(s) found", found >= 2);
		boolean copySeen = false;
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (pf.getEmbeddedURI() != null && pf.getEmbeddedURI().toString().endsWith("CopyOfAJarFont.ttf")) copySeen = true;
		}
		assertTrue("the directory root was not walked", copySeen);
		boolean jarSeen = false;
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (pf.getEmbeddedURI() != null && "jar".equals(pf.getEmbeddedURI().getScheme())) jarSeen = true;
		}
		assertTrue("the jar root was not walked", jarSeen);
	}

	private static URL firstTtfUnder(ClassLoader cl, String prefix) throws Exception {
		for (java.util.Enumeration<URL> roots = cl.getResources(prefix); roots.hasMoreElements(); ) {
			URL root = roots.nextElement();
			if (!"jar".equals(root.getProtocol())) continue;
			String spec = root.toString();
			String jarPath = spec.substring(spec.indexOf("file:"), spec.indexOf("!/"));
			try (java.util.jar.JarFile jar = new java.util.jar.JarFile(new File(new java.net.URI(jarPath)))) {
				for (java.util.Enumeration<java.util.jar.JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
					String name = e.nextElement().getName();
					if (name.startsWith(prefix + "/") && name.toLowerCase().endsWith(".ttf")) {
						return new URL(spec.substring(0, spec.indexOf("!/") + 2) + name);
					}
				}
			}
		}
		return null;
	}
}
