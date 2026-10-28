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
package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.revisions.TrackedChange;
import org.docx4j.model.revisions.TrackedChanges;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Test;

/**
 * The client's zone per package (CR-067 D12; the portfolio's date rule, 2026-10-28): a date field's instant, and
 * a revision's w:date without dateUtc, are shown in OpcPackage.getDateTimeZone(), else the JVM-wide property,
 * else the JVM default; FieldContext.zone overrides per call; the bound-date conversion is a separate opt-in.
 */
public class PackageDateTimeZoneTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	@After
	public void clearProperties() {
		Docx4jProperties.setProperty(OpcPackage.DATE_TIME_ZONE_PROPERTY, "");
		Docx4jProperties.setProperty(OpcPackage.BOUND_DATES_CONVERTED_PROPERTY, "false");
	}

	@Test
	public void theFallbackChain() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		assertNull(pkg.getDateTimeZone());
		assertEquals(ZoneId.systemDefault(), pkg.effectiveDateTimeZone());
		Docx4jProperties.setProperty(OpcPackage.DATE_TIME_ZONE_PROPERTY, "Asia/Tokyo");
		assertEquals("the JVM-wide property", ZoneId.of("Asia/Tokyo"), pkg.effectiveDateTimeZone());
		pkg.setDateTimeZone(ZoneId.of("America/New_York"));
		assertEquals("the package's own wins", ZoneId.of("America/New_York"), pkg.effectiveDateTimeZone());
		FieldContext ctx = FieldContext.of(pkg);
		assertEquals(ZoneId.of("America/New_York"), ctx.getZone());
		assertEquals("the per-call override", ZoneId.of("Europe/Paris"), ctx.zone(ZoneId.of("Europe/Paris")).getZone());
		assertEquals(ZoneId.of("America/New_York"), ctx.zone(null).getZone());
		Docx4jProperties.setProperty(OpcPackage.DATE_TIME_ZONE_PROPERTY, "not a zone");
		pkg.setDateTimeZone(null);
		assertEquals("a bad property is ignored", ZoneId.systemDefault(), pkg.effectiveDateTimeZone());
	}

	@Test
	public void theDateFieldsShowThePackagesZone() throws Exception {
		WordprocessingMLPackage pkg = FieldEvaluatorsTest.pkg();
		org.docx4j.docProps.core.dc.terms.W3CDTF created = new org.docx4j.docProps.core.dc.terms.W3CDTF();
		created.getContent().add("2024-02-03T04:05:06Z");
		pkg.getDocPropsCorePart().getJaxbElement().setCreated(created);
		final Date noon = Date.from(Instant.parse("2024-02-03T12:00:00Z"));
		pkg.setDateTimeZone(ZoneId.of("Asia/Tokyo")); // UTC+9, no daylight time
		FieldContext ctx = FieldContext.of(pkg).clock(() -> noon).language("en-US");
		assertEquals("13:05", FieldEvaluators.evaluate(" CREATEDATE \\@ \"HH:mm\" ", ctx));
		assertEquals("2/3/2024 1:05:00 PM", FieldEvaluators.evaluate(" CREATEDATE ", ctx));
		assertEquals("21:00", FieldEvaluators.evaluate(" DATE \\@ \"HH:mm\" ", ctx));
		assertEquals("21:00", FieldEvaluators.evaluate(" TIME \\@ \"HH:mm\" ", ctx));
		assertEquals("the override", "07:00", FieldEvaluators.evaluate(" DATE \\@ \"HH:mm\" ", ctx.zone(ZoneId.of("America/New_York"))));
		pkg.setDateTimeZone(ZoneId.of("UTC"));
		assertEquals("04:05", FieldEvaluators.evaluate(" CREATEDATE \\@ \"HH:mm\" ", FieldContext.of(pkg)));
	}

	@Test
	public void aRevisionsLocalClockIsReadInThePackagesZone() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + "><w:ins w:id=\"1\" w:author=\"A\" w:date=\"2024-02-03T04:05:06Z\"><w:r><w:t>x</w:t></w:r></w:ins></w:p>"));
		pkg.setDateTimeZone(ZoneId.of("Asia/Tokyo"));
		List<TrackedChange> changes = TrackedChanges.in(pkg);
		assertEquals(1, changes.size());
		assertEquals("w:date is Word's local clock, here Tokyo's: 04:05 there is 19:05Z the day before",
				Instant.parse("2024-02-02T19:05:06Z"), changes.get(0).getDate());
		pkg.setDateTimeZone(ZoneId.of("UTC"));
		assertEquals(Instant.parse("2024-02-03T04:05:06Z"), TrackedChanges.in(pkg).get(0).getDate());
		// dateUtc, where present, is UTC whatever the package's zone
		WordprocessingMLPackage pkg2 = WordprocessingMLPackage.createPackage();
		pkg2.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + " xmlns:w16du=\"http://schemas.microsoft.com/office/word/2023/wordml/word16du\"><w:ins w:id=\"1\" w:author=\"A\" w:date=\"2024-02-03T04:05:06Z\" w16du:dateUtc=\"2024-02-02T19:05:06Z\"><w:r><w:t>x</w:t></w:r></w:ins></w:p>"));
		pkg2.setDateTimeZone(ZoneId.of("Asia/Tokyo"));
		assertEquals(Instant.parse("2024-02-02T19:05:06Z"), TrackedChanges.in(pkg2).get(0).getDate());
	}

	@Test
	public void theBoundDateConversionIsItsOwnOptIn() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		assertFalse("off by default", pkg.isBoundDatesConvertedAsWord());
		pkg.setDateTimeZone(ZoneId.of("Asia/Tokyo"));
		assertFalse("a zone alone does not turn it on", pkg.isBoundDatesConvertedAsWord());
		Docx4jProperties.setProperty(OpcPackage.BOUND_DATES_CONVERTED_PROPERTY, "true");
		assertTrue("the JVM-wide property", pkg.isBoundDatesConvertedAsWord());
		pkg.setBoundDatesConvertedAsWord(Boolean.FALSE);
		assertFalse("the package's own wins", pkg.isBoundDatesConvertedAsWord());
		pkg.setBoundDatesConvertedAsWord(null);
		assertTrue(pkg.isBoundDatesConvertedAsWord());
	}
}
