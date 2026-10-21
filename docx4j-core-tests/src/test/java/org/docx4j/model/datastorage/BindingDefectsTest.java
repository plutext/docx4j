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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tag;
import org.junit.Test;

/**
 * Three of the six binding defects docx4j-portfolio found by reading the code (2026-10-22, its
 * text-extraction paper section 12a): the stored date's Z dropped with the character before it,
 * a missing condition id failing with a NullPointerException, and an XSLT finisher asked for without
 * the binding step failing the same way. (The fourth, the picture path returning the base64 String
 * where its run was meant, is a one-token fix in {@code BindingTraverserNonXSLT.xpathInjectImage}.)
 */
public class BindingDefectsTest {

	private static void assertDay(int year, int month, int day, int hour, Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		assertEquals(year, c.get(Calendar.YEAR));
		assertEquals(month, c.get(Calendar.MONTH) + 1);
		assertEquals(day, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(hour, c.get(Calendar.HOUR_OF_DAY));
	}

	/** The stored value's nominal fields, as Word shows them, whatever the zone marker: the 22nd stays the 22nd. */
	@Test
	public void storedDatesKeepTheirNominalDay() {
		assertDay(2026, 10, 22, 0, BindingTraverserXSLT.parseStoredDate("2026-10-22T00:00:00Z"));
		assertDay(2026, 10, 22, 0, BindingTraverserXSLT.parseStoredDate("2026-10-22Z")); // was 2 October
		assertDay(2026, 10, 22, 10, BindingTraverserXSLT.parseStoredDate("2026-10-22T10:00:00+10:00"));
		assertDay(2026, 10, 22, 10, BindingTraverserXSLT.parseStoredDate("2026-10-22T10:00:00.123Z"));
		assertDay(2026, 10, 22, 0, BindingTraverserXSLT.parseStoredDate("2026-10-22"));
		assertDay(2026, 10, 22, 23, BindingTraverserXSLT.parseStoredDate(" 2026-10-22T23:59:59 "));
	}

	@Test(expected = java.time.format.DateTimeParseException.class)
	public void aValueThatIsNoDateIsReported() {
		BindingTraverserXSLT.parseStoredDate("next Tuesday");
	}

	/** A top-level control naming a condition the conditions part does not define: a Docx4JException that says so (REQ-030), not a NullPointerException. */
	@Test
	public void aMissingConditionIsAClearError() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
				System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/invoice.docx"));
		int retagged = 0;
		for (Object o : pkg.getMainDocumentPart().getContent()) {
			if (!(XmlUtils.unwrap(o) instanceof SdtElement)) continue;
			SdtElement sdt = (SdtElement) XmlUtils.unwrap(o);
			Tag tag = sdt.getSdtPr() == null ? null : sdt.getSdtPr().getTag();
			if (tag != null && tag.getVal().equals("od:condition=c6")) {
				tag.setVal("od:condition=nope");
				retagged++;
			}
		}
		assertEquals("the body-level condition control in the fixture", 1, retagged);
		try {
			new OpenDoPEHandler(pkg).preprocess();
			fail("expected a Docx4JException");
		} catch (Docx4JException e) {
			assertTrue(e.getMessage(), e.getMessage().startsWith("Missing condition nope"));
			assertTrue(e.getMessage(), e.getMessage().contains("REQ-030"));
		}
	}

	/** The same inside a repeat, where the preprocess step's own check of descendant conditions reports it. */
	@Test
	public void aMissingConditionInsideARepeatIsAClearError() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
				System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/nested-condition-rptposcon.docx"));
		final List<SdtElement> controls = new java.util.ArrayList<SdtElement>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (XmlUtils.unwrap(o) instanceof SdtElement) controls.add((SdtElement) XmlUtils.unwrap(o));
				return null;
			}
		});
		int retagged = 0;
		for (SdtElement sdt : controls) {
			Tag tag = sdt.getSdtPr() == null ? null : sdt.getSdtPr().getTag();
			if (tag != null && tag.getVal().startsWith("od:condition=")) {
				tag.setVal("od:condition=nope");
				retagged++;
				break;
			}
		}
		assertEquals("a condition control in the fixture", 1, retagged);
		try {
			new OpenDoPEHandler(pkg).preprocess();
			fail("expected a Docx4JException");
		} catch (Docx4JException e) {
			// the preprocess step's own check of the tags against the conditions part reports it first
			// ("Required condition 'nope' is missing"); processBindingRoleIfAny's guard is behind it
			assertTrue(e.getMessage(), e.getMessage().contains("nope") && e.getMessage().contains("missing"));
		}
	}

	/** An xsltProvider without FLAG_BIND_BIND_XML: a Docx4JException naming the flag, not a NullPointerException. */
	@Test
	public void aFinisherWithoutTheBindingStepIsAClearError() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		XsltProvider provider = new XsltProvider() {
			@Override
			public javax.xml.transform.Templates getFinisherXslt(String filename) {
				return null;
			}
		};
		try {
			Docx4J.bind(pkg, null, Docx4J.FLAG_BIND_REMOVE_SDT, null, provider, "finisher.xslt", null);
			fail("expected a Docx4JException");
		} catch (Docx4JException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("FLAG_BIND_BIND_XML"));
		}
	}

	/** Nothing about the fixture itself changed: the handler still runs it with the tag as written. */
	@Test
	public void theFixtureStillPreprocesses() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(
				System.getProperty("user.dir") + "/src/test/resources/OpenDoPE/nested-condition-rptposcon.docx"));
		List<Object> before = pkg.getMainDocumentPart().getContent();
		assertTrue(before.size() > 0);
		new OpenDoPEHandler(pkg).preprocess();
	}
}
