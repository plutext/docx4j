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
package org.docx4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.Test;

/**
 * docx4j-fo.properties (17.2.0) is a fallback for docx4j.properties: its keys are taken
 * only where docx4j.properties does not set them, whatever their prefix.
 */
public class Docx4jPropertiesFoFileTest {

	private static InputStream stream(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.ISO_8859_1));
	}

	@Test
	public void mainFileWins() {
		Properties p = Docx4jProperties.load(
				stream("docx4j.convert.out.fo.hyphenate=false\ndocx4j.PageSize=A4\n"),
				stream("docx4j.convert.out.fo.hyphenate=true\ndocx4j.convert.out.fo.kerning=true\n"));
		assertEquals("false", p.getProperty("docx4j.convert.out.fo.hyphenate"));
		assertEquals("true", p.getProperty("docx4j.convert.out.fo.kerning"));
		assertEquals("A4", p.getProperty("docx4j.PageSize"));
		assertEquals(3, p.size());
	}

	@Test
	public void foFileAlone() {
		Properties p = Docx4jProperties.load(null,
				stream("docx4j.convert.out.fo.kerning=true\n"));
		assertEquals("true", p.getProperty("docx4j.convert.out.fo.kerning"));
		assertEquals(1, p.size());
	}

	@Test
	public void neither() {
		Properties p = Docx4jProperties.load(null, null);
		assertEquals(0, p.size());
		assertNull(p.getProperty("docx4j.convert.out.fo.kerning"));
	}

	@Test
	public void keyOutsideThePrefixIsHonoured() {
		Properties p = Docx4jProperties.load(
				stream("docx4j.PageSize=A4\n"),
				stream("docx4j.PageMargins=WIDE\ndocx4j.convert.out.fop.FopConfParser.defaultBaseURI=file:///x\n"));
		assertEquals("WIDE", p.getProperty("docx4j.PageMargins"));
		assertEquals("file:///x", p.getProperty("docx4j.convert.out.fop.FopConfParser.defaultBaseURI"));
	}

	@Test
	public void theLiveTableStillReadsTheTestConfiguration() {
		// docx4j-core-tests' docx4j.properties sets this; no docx4j-fo.properties is on its classpath
		assertEquals("false", Docx4jProperties.getProperty("docx4j.convert.out.fo.hyphenate"));
	}
}
