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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * Jason's two rulings of 2026-10-29 (CR-067 section 14.7; WD 0.41). (1) Under {@code fields: cached} the fields Word never
 * stores: a list number (LISTNUM, AUTONUM, AUTONUMLGL, AUTONUMOUT) gives nothing, as Word's own plain text does, with no
 * {@code field.no_result} warning; a SYMBOL is computed in every mode, a named departure from Word's converter, which
 * omits it. Measured against Word's plain-text save of the never-updated phase 3 probe. (2) The inspector reports a
 * DATE, TIME or SAVEDATE field ({@code fields.auto_update}) and the print-time triggers ({@code fields.print_update}).
 */
public class FieldsNeverStoredAndTriggersTest {

	static WordprocessingMLPackage load(String path) throws Exception {
		try (InputStream is = ResourceUtils.getResource(path)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	static Map<String, String> cases(String text) {
		Map<String, String> m = new LinkedHashMap<String, String>();
		for (String line : text.replace("\r", "").split("\n")) {
			int i = line.indexOf(" = ");
			if (i > 0 && line.matches("^[A-Z]\\d\\d_[A-Z0-9_]+ = .*")) m.put(line.substring(0, i), line.substring(i + 3).trim());
		}
		return m;
	}

	@Test
	public void listNumbersGiveNothingAndSymbolsTheirCharacterUnderCached() throws Exception {
		String wordText;
		try (InputStream is = ResourceUtils.getResource("fields/cr067-phase3/phase3-probe-r1-word365.txt")) {
			wordText = new String(is.readAllBytes(), Charset.forName("windows-1252"));
		}
		Map<String, String> word = cases(wordText);
		WordprocessingMLPackage pkg = load("fields/cr067-phase3/phase3-probe-r1.docx");
		TextResult result = Texts.of(pkg, new TextOptions());
		Map<String, String> ours = cases(PlainText.of(result));
		// the list numbers: nothing, as Word's plain text (its save of the never-updated probe)
		for (String t : new String[] { "L01_LISTNUM", "L03_LEVEL2", "L09_LEGAL", "L16_NUMBERDEFAULT", "A01_AUTONUM", "A05_LGL_H1", "A11_OUT_H1", "A15_AUTONUM_AFTER" }) {
			assertEquals(t + " (Word)", "", word.get(t).replace("heading text", "").trim());
			assertEquals(t, "", ours.get(t).replace("heading text", "").trim());
		}
		// SYMBOL: the character, computed in every mode (Word's converter omits it: the named departure)
		assertEquals("", word.get("S01_DECIMAL"));
		assertEquals("©", ours.get("S01_DECIMAL"));
		assertEquals("€", ours.get("S05_UNICODE"));
		assertEquals("", ours.get("S03_SYMBOL_FONT"));
		assertEquals("###", ours.get("S09_OVER_255"));
		// the stored results of everything else stay (the probe stores OLD)
		assertEquals("OLD", ours.get("N01_FILENAME"));
		// no field.no_result for a list number: the omission is the rule (the probe's 21 XE marks, begin-end fields with no
		// separate, warn in both modes alike); the block model carries the field with no result
		TextResult updatedResult = Texts.of(load("fields/cr067-phase3/phase3-probe-r1.docx"), new TextOptions().fields(TextOptions.Fields.UPDATE));
		assertEquals(noResultWarnings(updatedResult), noResultWarnings(result));
		int listFields = 0, withResult = 0;
		for (TextBlock b : result.blocks()) for (TextField f : b.getFields()) {
			String instr = f.getInstr() == null ? "" : f.getInstr().trim().toUpperCase();
			if (instr.startsWith("LISTNUM") || instr.startsWith("AUTONUM")) { listFields++; if (f.getResult() != null) withResult++; }
		}
		assertTrue(listFields > 30);
		assertEquals(0, withResult);
		// under update, computed as before
		Map<String, String> updated = cases(PlainText.of(Texts.of(load("fields/cr067-phase3/phase3-probe-r1.docx"), new TextOptions().fields(TextOptions.Fields.UPDATE))));
		assertEquals("i)", updated.get("L01_LISTNUM"));
		assertEquals("2.1.14.", updated.get("L09_LEGAL"));
	}

	static int noResultWarnings(TextResult r) {
		int n = 0;
		for (TextWarning w : r.warnings()) if (TextWarning.FIELD_NO_RESULT.equals(w.getKind())) n++;
		return n;
	}

	@Test
	public void theInspectorNamesTheTriggers() throws Exception {
		Inspection i = Inspect.of(load("fields/cr067-fields-fun/one-file-many-readings.docx"));
		List<String> auto = new ArrayList<String>(), print = new ArrayList<String>();
		for (InspectFinding f : i.findings()) {
			if (InspectFinding.FIELDS_AUTO_UPDATE.equals(f.getKind())) auto.add(f.getExcerpt().trim());
			if (InspectFinding.FIELDS_PRINT_UPDATE.equals(f.getKind())) print.add(f.getExcerpt().trim());
		}
		assertEquals(1, auto.size());
		assertTrue(auto.get(0), auto.get(0).startsWith("DATE"));
		assertEquals(1, print.size());
		assertTrue(print.get(0), print.get(0).startsWith("PAGE"));
		// a document with none raises neither
		Inspection none = Inspect.of(load("fields/cr067-iffield/iffield-probe-r2.docx"));
		for (InspectFinding f : none.findings()) {
			assertFalse(f.getKind(), InspectFinding.FIELDS_AUTO_UPDATE.equals(f.getKind()));
		}
	}
}
