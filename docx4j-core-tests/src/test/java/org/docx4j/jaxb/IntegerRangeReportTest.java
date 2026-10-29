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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * CR-066 D2, D3, D8: a part's save and its load each log one WARN line naming the part, with the count of integer
 * attribute values outside Word's 16-bit range and the first few of them with Word's reading; the values are
 * written as held and read as written. A document with none logs nothing at WARN.
 */
public class IntegerRangeReportTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	private final List<ILoggingEvent> events = new ArrayList<ILoggingEvent>();
	private final List<ch.qos.logback.classic.Logger> loggers = new ArrayList<ch.qos.logback.classic.Logger>();
	private final List<Level> previousLevels = new ArrayList<Level>();
	private AppenderBase<ILoggingEvent> appender;

	@Before
	public void captureTheLog() {
		appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(ILoggingEvent event) {
				events.add(event);
			}
		};
		for (Class<?> c : new Class<?>[] { JaxbXmlPart.class, JaxbXmlPartXPathAware.class }) {
			ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(c);
			loggers.add(logger);
			previousLevels.add(logger.getLevel());
			logger.setLevel(Level.INFO);
			if (appender.getContext() == null) {
				appender.setContext(logger.getLoggerContext());
				appender.start();
			}
			logger.addAppender(appender);
		}
	}

	@After
	public void releaseTheLog() {
		for (int i = 0; i < loggers.size(); i++) {
			loggers.get(i).detachAppender(appender);
			loggers.get(i).setLevel(previousLevels.get(i));
		}
		appender.stop();
	}

	private List<String> warnings(String containing) {
		List<String> found = new ArrayList<String>();
		for (ILoggingEvent event : events) {
			if (event.getLevel().equals(Level.WARN) && event.getFormattedMessage().contains(containing)) {
				found.add(event.getFormattedMessage());
			}
		}
		return found;
	}

	private static WordprocessingMLPackage document(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>" + body + "</w:body></w:document>")));
		return pkg;
	}

	@Test
	public void aSaveLogsOneWarnPerPartAndWritesTheValuesAsHeld() throws Exception {
		WordprocessingMLPackage pkg = document(
				"<w:p><w:pPr><w:spacing w:before=\"1205003\"/><w:ind w:left=\"7205001\"/></w:pPr>"
				+ "<w:r><w:rPr><w:sz w:val=\"70000\"/></w:rPr><w:t>wide</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:spacing w:before=\"65535\"/><w:ind w:left=\"32767\"/></w:pPr></w:p>"); // inside the ranges
		events.clear();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);

		List<String> warnings = warnings("outside Word's 16-bit range");
		assertEquals("one line, for the main document part: " + warnings, 1, warnings.size());
		String line = warnings.get(0);
		assertTrue(line, line.startsWith("/word/document.xml: "));
		assertTrue("the ports' shared code, searchable: " + line, line.contains("value.out_of_range: 3 integer attribute values outside Word's 16-bit range"));
		assertTrue(line, line.contains("7205001 (int16 -32768..32767; Word reads -3959)"));
		assertTrue(line, line.contains("70000 (uint16 0..65535; Word reads 4464)"));
		assertTrue(line, line.contains("1205003 (int16 or uint16 by attribute (ST_TwipsMeasure) -32768..65535; Word changes it"));

		// the file holds the numbers as given (D2: never a rewrite at save)
		WordprocessingMLPackage back = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
		String xml = XmlUtils.marshaltoString(back.getMainDocumentPart().getJaxbElement());
		assertTrue(xml, xml.contains("w:left=\"7205001\""));
		assertTrue(xml, xml.contains("w:before=\"1205003\""));
		assertTrue(xml, xml.contains("w:val=\"70000\""));
	}

	@Test
	public void aLoadLogsOneWarnPerPartAndHoldsTheValuesAsWritten() throws Exception {
		WordprocessingMLPackage pkg = document("<w:p><w:pPr><w:ind w:left=\"7205001\" w:right=\"-32769\"/></w:pPr></w:p>");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		events.clear();

		WordprocessingMLPackage back = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
		back.getMainDocumentPart().getContent(); // the part is read lazily

		List<String> warnings = warnings("outside Word's 16-bit range");
		assertEquals("one line, for the main document part: " + warnings, 1, warnings.size());
		assertTrue(warnings.get(0), warnings.get(0).startsWith("/word/document.xml: value.out_of_range: 2 integer attribute values outside"));
		assertTrue(warnings.get(0), warnings.get(0).contains("-32769 (int16 -32768..32767; Word reads 32767)"));
		String xml = XmlUtils.marshaltoString(back.getMainDocumentPart().getJaxbElement());
		assertTrue("held as written, D3", xml.contains("w:left=\"7205001\"") && xml.contains("w:right=\"-32769\""));
	}

	@Test
	public void aDocumentWithinRangeLogsNothing() throws Exception {
		WordprocessingMLPackage pkg = document("<w:p><w:pPr><w:spacing w:before=\"65535\" w:line=\"-32768\"/><w:ind w:left=\"32767\" w:hanging=\"32768\"/></w:pPr>"
				+ "<w:r><w:rPr><w:sz w:val=\"65535\"/><w:w w:val=\"601\"/><w:bdr w:val=\"single\" w:sz=\"255\" w:space=\"31\"/></w:rPr><w:t>x</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgNumType w:start=\"2147483648\"/></w:sectPr>");
		events.clear();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray())).getMainDocumentPart().getContent();
		assertEquals(events.toString(), 0, warnings("outside Word").size());
	}
}
