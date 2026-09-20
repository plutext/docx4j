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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * What a conversion says about the document's fonts (CR-017 phase 4): one line per
 * document font, INFO where the font itself or a metric clone draws it and WARN with an
 * action where a user could do something, behind
 * {@code docx4j.fonts.report.log=summary|full|off}.
 *
 * <p>Until 17.2.0 a conversion said nothing but a DEBUG line per mapping and FOP's own
 * "font not found" warning, which names no action.</p>
 */
public class FontReportLogTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String SANS = "Liberation Sans";

	private final List<ILoggingEvent> events = new ArrayList<ILoggingEvent>();
	private ch.qos.logback.classic.Logger logger;
	private AppenderBase<ILoggingEvent> appender;
	private Level previousLevel;

	@BeforeClass
	public static void fonts() {
		new IdentityPlusMapper();
		Assume.assumeTrue("Liberation not on the classpath", PhysicalFonts.get(SANS) != null);
	}

	@Before
	public void captureTheLog() {
		logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FontsAnalysis.class);
		previousLevel = logger.getLevel();
		logger.setLevel(Level.INFO);
		appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(ILoggingEvent event) {
				events.add(event);
			}
		};
		appender.setContext(logger.getLoggerContext());
		appender.start();
		logger.addAppender(appender);
	}

	@After
	public void releaseTheLog() {
		logger.detachAppender(appender);
		appender.stop();
		logger.setLevel(previousLevel);
		Docx4jProperties.setProperty(FontsAnalysis.LOG_PROPERTY, "summary");
	}

	/** Two fonts: one this machine has, one it does not and cannot substitute. */
	private static WordprocessingMLPackage twoFonts() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>"
						+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"" + SANS + "\" w:hAnsi=\"" + SANS
						+ "\"/></w:rPr><w:t>installed</w:t></w:r></w:p>"
						+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Docx4j Log Narrow\" w:hAnsi=\"Docx4j Log Narrow\"/>"
						+ "</w:rPr><w:t>condensed, so deliberately unmapped</w:t></w:r></w:p>"
						+ "</w:body></w:document>")));
		pkg.setFontMapper(new IdentityPlusMapper());
		return pkg;
	}

	private int lines(Level level) {
		int n = 0;
		for (ILoggingEvent event : events) {
			if (event.getLevel().equals(level)) n++;
		}
		return n;
	}

	@Test
	public void summaryIsOneLinePerFontWithTheGradeInIt() throws Exception {

		WordprocessingMLPackage pkg = twoFonts();
		Docx4jProperties.setProperty(FontsAnalysis.LOG_PROPERTY, "summary");
		FontsAnalysis.logReport(pkg);

		int fonts = FontsAnalysis.analyse(pkg).getEntries().size();
		assertEquals("one line per document font", fonts, events.size());
		for (ILoggingEvent event : events) {
			assertFalse("a summary line is one line: " + event.getFormattedMessage(),
					event.getFormattedMessage().contains("\n"));
		}
		// the installed font at INFO, the one a user could act on at WARN
		assertTrue("nothing at INFO", lines(Level.INFO) > 0);
		assertTrue("nothing at WARN", lines(Level.WARN) > 0);
		boolean action = false;
		for (ILoggingEvent event : events) {
			if (event.getLevel().equals(Level.WARN) && event.getFormattedMessage().contains("install ")) {
				action = true;
			}
		}
		assertTrue("the WARN line says what to do", action);
	}

	/** Once per conversion, not once per run or once per font mapping: logging twice is
	 *  two conversions' worth, and nothing accumulates between them. */
	@Test
	public void itSaysTheSameThingEachTimeItIsAsked() throws Exception {
		WordprocessingMLPackage pkg = twoFonts();
		FontsAnalysis.logReport(pkg);
		int first = events.size();
		events.clear();
		FontsAnalysis.logReport(pkg);
		assertEquals(first, events.size());
	}

	@Test
	public void fullLogsTheWholeReportAsOneEvent() throws Exception {
		WordprocessingMLPackage pkg = twoFonts();
		Docx4jProperties.setProperty(FontsAnalysis.LOG_PROPERTY, "full");
		FontsAnalysis.logReport(pkg);
		assertEquals(1, events.size());
		assertTrue(events.get(0).getFormattedMessage().contains("the machine that saved it"));
	}

	@Test
	public void offLogsNothing() throws Exception {
		WordprocessingMLPackage pkg = twoFonts();
		Docx4jProperties.setProperty(FontsAnalysis.LOG_PROPERTY, "off");
		FontsAnalysis.logReport(pkg);
		assertEquals(0, events.size());
	}

	/** Not a WordprocessingMLPackage, and nothing is said (rather than thrown). */
	@Test
	public void anotherPackageTypeIsIgnored() throws Exception {
		FontsAnalysis.logReport(org.docx4j.openpackaging.packages.PresentationMLPackage.createPackage());
		FontsAnalysis.logReport(null);
		assertEquals(0, events.size());
	}
}
