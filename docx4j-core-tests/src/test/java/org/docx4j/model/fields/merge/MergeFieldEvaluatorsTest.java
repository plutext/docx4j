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
package org.docx4j.model.fields.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.FieldContext;
import org.docx4j.model.fields.FieldEvaluators;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Test;

/** The merge-field evaluators on their own (CR-067 phase 2), and NEXT in MailMerger (as in MailMergerWithNext). */
public class MergeFieldEvaluatorsTest {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	@After
	public void reset() {
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
	}

	@Test
	public void mergeFieldsReadTheContextsRecord() throws Exception {
		FieldContext ctx = FieldContext.of(WordprocessingMLPackage.createPackage()).language("en-US");
		assertNull("outside a merge, the cached result is kept", FieldEvaluators.evaluate(" MERGEFIELD Name ", ctx));
		assertNull(FieldEvaluators.evaluate(" MERGEREC ", ctx));
		assertNull(FieldEvaluators.evaluate(" MERGESEQ ", ctx));
		Map<String, String> record = new HashMap<String, String>();
		record.put("Name", "Ann");
		record.put("Middle", "");
		record.put("Amount", "150");
		ctx.mergeRecord(record).mergeRecordNumber(3).mergeSequence(2);
		assertEquals("Ann", FieldEvaluators.evaluate(" MERGEFIELD Name ", ctx));
		assertEquals("ANN", FieldEvaluators.evaluate(" MERGEFIELD Name \\* Upper ", ctx));
		assertEquals("Dear Ann,", FieldEvaluators.evaluate(" MERGEFIELD Name \\b \"Dear \" \\f \",\" ", ctx));
		assertEquals("", FieldEvaluators.evaluate(" MERGEFIELD Middle ", ctx));
		assertEquals("nothing around an empty value", "", FieldEvaluators.evaluate(" MERGEFIELD Middle \\b \"(\" \\f \")\" ", ctx));
		assertEquals("", FieldEvaluators.evaluate(" MERGEFIELD Nope ", ctx));
		assertEquals("case does not matter", "Ann", FieldEvaluators.evaluate(" MERGEFIELD name ", ctx));
		assertEquals("$150.00", FieldEvaluators.evaluate(" MERGEFIELD Amount \\# \"$#,##0.00\" ", ctx));
		assertEquals("3", FieldEvaluators.evaluate(" MERGEREC ", ctx));
		assertEquals("2", FieldEvaluators.evaluate(" MERGESEQ ", ctx));
		assertEquals("big", FieldEvaluators.evaluate(" IF 150 >= 100 big small ", ctx));
	}

	@Test
	public void askAndFillinTakeTheAnswersElseTheDefaults() throws Exception {
		FieldContext ctx = FieldContext.of(WordprocessingMLPackage.createPackage()).language("en-US");
		assertEquals("", FieldEvaluators.evaluate(" ASK Greeting \"Greeting?\" \\d \"Hello\" \\o ", ctx));
		assertEquals("Hello", ctx.bookmarks().get("greeting"));
		assertEquals("Hello", FieldEvaluators.evaluate(" REF Greeting ", ctx));
		assertEquals("World", FieldEvaluators.evaluate(" FILLIN \"Your name?\" \\d \"World\" \\o ", ctx));
		assertEquals("", FieldEvaluators.evaluate(" FILLIN \"No default?\" ", ctx));
		ctx.answers().put("Greeting", "G'day");
		ctx.answers().put("Your name?", "Jason");
		assertEquals("", FieldEvaluators.evaluate(" ASK Greeting \"Greeting?\" \\d \"Hello\" ", ctx));
		assertEquals("G'day", ctx.bookmarks().get("greeting"));
		assertEquals("Jason", FieldEvaluators.evaluate(" FILLIN \"Your name?\" \\d \"World\" ", ctx));
		assertNull(FieldEvaluators.evaluate(" ASK ", ctx));
	}

	/** NEXT in MailMerger: the next record continues the same letter, as MailMergerWithNext did it alone until 17.3.5. */
	@Test
	public void nextAdvancesTheRecordWithinALetter() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + "><w:fldSimple w:instr=\" MERGEFIELD Name \"><w:r><w:t>«Name»</w:t></w:r></w:fldSimple>"
				+ "<w:r><w:t xml:space=\"preserve\"> and </w:t></w:r><w:fldSimple w:instr=\" NEXT \"><w:r><w:t/></w:r></w:fldSimple>"
				+ "<w:fldSimple w:instr=\" MERGEFIELD Name \"><w:r><w:t>«Name»</w:t></w:r></w:fldSimple>"
				+ "<w:r><w:t xml:space=\"preserve\"> (rec </w:t></w:r><w:fldSimple w:instr=\" MERGEREC \"><w:r><w:t>0</w:t></w:r></w:fldSimple>"
				+ "<w:r><w:t xml:space=\"preserve\"> seq </w:t></w:r><w:fldSimple w:instr=\" MERGESEQ \"><w:r><w:t>0</w:t></w:r></w:fldSimple><w:r><w:t>)</w:t></w:r></w:p>"));
		List<Map<DataFieldName, String>> data = new ArrayList<Map<DataFieldName, String>>();
		for (String name : new String[] { "Ann", "Bob", "Cat" }) {
			Map<DataFieldName, String> m = new HashMap<DataFieldName, String>();
			m.put(new DataFieldName("Name"), name);
			data.add(m);
		}
		WordprocessingMLPackage merged = new MailMerger(pkg).getConsolidatedResultCrude(data, false);
		String text = org.docx4j.model.text.PlainText.of(org.docx4j.model.text.Texts.of(merged, new org.docx4j.model.text.TextOptions()));
		assertEquals("Ann and Bob (rec 2 seq 1)\nCat and  (rec 4 seq 2)\n", text);
	}
}
