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
package org.docx4j.model.datastorage.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * #572: VariablePrepare joins the runs a ${key} placeholder is split across, whatever their formatting, so
 * variableReplace finds the key; the value takes the formatting of the run where ${ begins. Until 17.3.5 it joined
 * only runs formatted alike, so a placeholder Word had split across runs formatted differently wasn't replaced.
 */
public class VariablePrepareSplitKeyTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String SMALL = "<w:rPr><w:color w:val=\"FF0000\"/><w:sz w:val=\"20\"/><w:szCs w:val=\"20\"/></w:rPr>";
	private static final String RED = "<w:rPr><w:color w:val=\"FF0000\"/></w:rPr>";

	private static String run(String rPr, String text) {
		return "<w:r>" + rPr + "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	/** The paragraph, prepared and replaced, as XML */
	private static String replaced(String content) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + ">" + content + "</w:p>"));
		VariablePrepare.prepare(pkg);
		Map<String, String> values = new HashMap<String, String>();
		values.put("first_name", "Ann");
		pkg.getMainDocumentPart().variableReplace(values);
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getContent().get(0), true, false);
	}

	/** #572's paragraph */
	@Test
	public void aPlaceholderAcrossRunsFormattedDifferently() throws Exception {
		String p = replaced(run("", "FIRST NAME       ") + run(SMALL, "${") + "<w:proofErr w:type=\"spellStart\"/>"
				+ run(SMALL, "first_") + run(RED, "name") + "<w:proofErr w:type=\"spellEnd\"/>" + run(RED, "}"));
		assertTrue(p, p.contains(">FIRST NAME       <"));
		assertTrue(p, p.contains(SMALL + "<w:t>Ann</w:t>") || p.contains(SMALL + "<w:t xml:space=\"preserve\">Ann</w:t>"));
		assertFalse(p, p.contains("first_") || p.contains("name<") || p.contains("${"));
	}

	/** The text after the }, in the run holding it, keeps its own run and formatting */
	@Test
	public void theTextAfterTheBraceKeepsItsFormatting() throws Exception {
		String p = replaced(run(SMALL, "Dear ${first") + run(RED, "_name}, hello"));
		assertTrue(p, p.contains(">Dear Ann<"));
		assertTrue(p, p.contains(RED + "<w:t xml:space=\"preserve\">, hello</w:t>"));
	}

	@Test
	public void aDollarAndABraceInDifferentRuns() throws Exception {
		String p = replaced(run(SMALL, "$") + run(RED, "{first_name}"));
		assertTrue(p, p.contains(">Ann<") && !p.contains("first_name"));
	}

	/** Without a closing }, or past a run holding more than text, nothing is joined */
	@Test
	public void leftAloneWithoutAClose() throws Exception {
		String unclosed = replaced(run(SMALL, "costs ${") + run(RED, "100"));
		assertTrue(unclosed, unclosed.contains(">costs ${<") && unclosed.contains(">100<"));
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		P p = (P) XmlUtils.unmarshalString("<w:p " + W + ">" + run(SMALL, "${first") + "<w:r>" + RED + "<w:tab/><w:t>_name}</w:t></w:r></w:p>");
		pkg.getMainDocumentPart().getContent().add(p);
		VariablePrepare.prepare(pkg);
		assertEquals(2, ((P) pkg.getMainDocumentPart().getContent().get(0)).getContent().size());
	}
}
