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
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * CR-058 (1): a bound date control is formatted as Word formats it. {@code w:dateFormat} is Word's date
 * picture language, the DATE field's {@code \@} switch (ECMA-376 17.5.2.9, 17.16.4.1), displayed in the
 * language of {@code w:lid} (17.5.2.20); a stored value that is not a date is shown as it is. The fixture
 * is docx4j-portfolio's bind-typed-formats probe (one control per format, surfaces {@code DFn_STALE});
 * the expectations follow the CR's decisions D1 to D3 and the field converter already measured against
 * Word ({@code DateFieldLanguageTest}), checked against Word 365's run of the probe (2026-10-23, CR §8):
 * Word agrees on every format, with two differences the CR's D2 (revised) and D6 record: without a
 * {@code w:lid} Word uses its own editing language, not the run's; and Word shows a stored {@code dateTime}
 * in the machine's time zone, where docx4j shows the nominal time (so DF2 is "3:05 PM" here and was
 * "2:05 AM" on a machine at UTC+11). An empty value shows the placeholder, as an empty text binding does.
 *
 * <p>Until 17.3.5 {@code xpathDateRun} translated {@code dddd} only and formatted with
 * {@code SimpleDateFormat} in the JVM's locale: {@code ddd} came out as the day of the year ("295"),
 * {@code am/pm} failed the control ({@code [missing!]}), a German {@code w:lid} gave English names, and
 * a value that is not a date became today's date in red.</p>
 */
public class BoundDateFormatTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";
	private static final String FIXTURE = "typed-controls-dates.docx";

	private static String bind(String implementation) throws Exception {
		return bind(implementation, null);
	}

	/** Bound, after {@code prepare} has had the loaded package (a styles part, a property). */
	private static String bind(String implementation, java.util.function.Consumer<WordprocessingMLPackage> prepare) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
					System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/" + FIXTURE));
			if (prepare != null) prepare.accept(pkg);
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, Docx4J.FLAG_BIND_BIND_XML);
			return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	/** The control with this alias, as marshalled. */
	private static String control(String xml, String alias) {
		Matcher m = Pattern.compile("<w:sdt>(?:(?!<w:sdt>).)*?<w:alias w:val=\"" + alias + "\"/>.*?</w:sdt>", Pattern.DOTALL).matcher(xml);
		assertTrue("control " + alias + " in " + xml, m.find());
		return m.group();
	}

	/** The control's text: its {@code w:t} contents joined. */
	private static String shown(String control) {
		int at = control.indexOf("<w:sdtContent>");
		assertTrue(control, at >= 0);
		Matcher m = Pattern.compile("<w:t(?: [^>]*)?>(.*?)</w:t>", Pattern.DOTALL).matcher(control.substring(at));
		StringBuilder sb = new StringBuilder();
		while (m.find()) sb.append(m.group(1));
		return sb.toString();
	}

	private static void shows(String implementation, String xml, String alias, String expected) {
		String control = control(xml, alias);
		assertEquals(implementation + " " + alias + ": " + control, expected, shown(control));
		assertFalse(implementation + " " + alias + " is red: " + control, control.contains("FF0000"));
	}

	private static void wordsFormatting(String implementation) throws Exception {
		String xml = bind(implementation);
		shows(implementation, xml, "DF1", "Thu 22 Oct 2026");           // ddd: the weekday, not the day of the year
		shows(implementation, xml, "DF2", "3:05 PM");                   // am/pm: upper case, as Word; nominal time (D6)
		shows(implementation, xml, "DF3", "12:05 PM");                  // h at noon is 12, not 0 (Word: "11:05 PM" at UTC+11)
		shows(implementation, xml, "DF4", "15:05");
		shows(implementation, xml, "DF5", "22 Oktober 2026");           // w:lid de-DE on any JVM
		shows(implementation, xml, "DF6", "Thursday, 22 October 2026");
		// no w:lid: not the run's fr-FR (Word uses its editing language; D2 revised); the fixture has no document
		// default language either, so the JVM's locale, as today
		shows(implementation, xml, "DF7", new java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.getDefault(java.util.Locale.Category.FORMAT))
				.format(new java.util.GregorianCalendar(2026, java.util.Calendar.OCTOBER, 22).getTime()));
		shows(implementation, xml, "DF8", "not-a-date");                // D3: the stored text, not today's date in red
		shows(implementation, xml, "DF9", "2026-13-45");
		String df10 = control(xml, "DF10");                             // empty: the placeholder, as for an empty text binding
		assertTrue(implementation + " DF10 placeholder: " + df10, df10.contains("<w:showingPlcHdr/>"));
		assertFalse(implementation + " DF10 is red: " + df10, df10.contains("FF0000"));
		assertFalse(implementation + " DF10 shows a date: " + df10, shown(df10).contains("2026"));
		shows(implementation, xml, "DF11", "2026-10-22");
		shows(implementation, xml, "DF12", "Day 22 of October");
		shows(implementation, xml, "DF13", "Okt. 2026");                // Word's abbreviation, no CLDR period
	}

	@Test
	public void boundDatesAreFormattedAsWordFormatsThemXSLT() throws Exception {
		wordsFormatting("BindingTraverserXSLT");
	}

	@Test
	public void boundDatesAreFormattedAsWordFormatsThemNonXSLT() throws Exception {
		wordsFormatting("BindingTraverserNonXSLT");
	}

	/** The formats Java already reads as Word does, and that no locale touches: the fixture binds today. */
	@Test
	public void localeFreeFormatsAgreeToday() throws Exception {
		for (String implementation : new String[] { "BindingTraverserXSLT", "BindingTraverserNonXSLT" }) {
			String xml = bind(implementation);
			shows(implementation, xml, "DF4", "15:05");
			shows(implementation, xml, "DF11", "2026-10-22");
		}
	}

	/**
	 * No {@code w:lid}: the document's default language ({@code w:docDefaults}), the nearest thing a
	 * server has to the editing language Word used (D2 revised); the run's language is not consulted.
	 */
	@Test
	public void withoutALidTheDocumentDefaultLanguageIsUsed() throws Exception {
		java.util.function.Consumer<WordprocessingMLPackage> germanDefaults = pkg -> {
			try {
				org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart styles =
						new org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart();
				styles.setJaxbElement((org.docx4j.wml.Styles) XmlUtils.unmarshalString(
						"<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
						+ "<w:docDefaults><w:rPrDefault><w:rPr><w:lang w:val=\"de-DE\"/></w:rPr></w:rPrDefault></w:docDefaults></w:styles>"));
				pkg.getMainDocumentPart().addTargetPart(styles);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		for (String implementation : new String[] { "BindingTraverserXSLT", "BindingTraverserNonXSLT" }) {
			String xml = bind(implementation, germanDefaults);
			shows(implementation, xml, "DF7", "22 Oktober 2026");   // no w:lid, run fr-FR: the document's German
			shows(implementation, xml, "DF5", "22 Oktober 2026");   // its own w:lid still wins
			shows(implementation, xml, "DF6", "Thursday, 22 October 2026");
		}
	}

	/**
	 * D6: a stored {@code dateTime} carrying {@code Z} is shown at its nominal time by default; with
	 * {@code docx4j.model.datastorage.BindingHandler.dateTimeZone} set, it is converted to that zone, as
	 * Word converts to the machine's (the probe's Word 365 run at UTC+11, 2026-10-23: DF2 "2:05 AM",
	 * DF3 "11:05 PM", DF4 "02:05"; DF1 and the date-only DF11 unshifted).
	 */
	@Test
	public void theDateTimeZonePropertyConvertsAsWordDoes() throws Exception {
		Docx4jProperties.setProperty(BindingTraverserXSLT.DATE_TIME_ZONE_PROPERTY, "Australia/Sydney");
		try {
			for (String implementation : new String[] { "BindingTraverserXSLT", "BindingTraverserNonXSLT" }) {
				String xml = bind(implementation);
				shows(implementation, xml, "DF1", "Thu 22 Oct 2026");
				shows(implementation, xml, "DF2", "2:05 AM");
				shows(implementation, xml, "DF3", "11:05 PM");
				shows(implementation, xml, "DF4", "02:05");
				shows(implementation, xml, "DF11", "2026-10-22");
			}
		} finally {
			Docx4jProperties.setProperty(BindingTraverserXSLT.DATE_TIME_ZONE_PROPERTY, "");
		}
	}
}
