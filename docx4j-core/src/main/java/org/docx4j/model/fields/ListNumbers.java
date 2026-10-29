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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.NumberFormatter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The numbers Word paints for LISTNUM, AUTONUM, AUTONUMLGL and AUTONUMOUT, which it never writes into the file (a
 * field's stored result stays whatever it was; measured against Word 365, CR-067 section 14.4). They depend on every
 * such field before them, so they are computed here for a whole document at once, over the main document part in
 * document order, both field shapes; a field's number is then looked up by its node (its {@code w:fldSimple}, or the
 * run or {@code w:fldChar} that begins a complex field).
 *
 * <p>The measured rules. LISTNUM: the three named lists (NumberDefault, LegalDefault, OutlineDefault; the name is the
 * argument, NumberDefault when there is none) share one set of nine level counters; a level's count goes up by one, or
 * to {@code \s}; a level started under a parent level that has not counted yet gives the parent 1 (an implicit parent,
 * which a later field at that level continues); counting a level resets the deeper ones. The level is {@code \l}, else
 * the paragraph's own heading level, else the level after the nearest heading paragraph before it (a Heading 2, then a
 * paragraph: level 3), else 1. NumberDefault paints {@code 1) a) i) (1) (a) (i) 1. a. i.}, OutlineDefault
 * {@code I. A. 1. a) (1) (a) (i) (a) (i)}, LegalDefault the counts from level 1 joined by periods with a period after.
 * AUTONUM, AUTONUMLGL and AUTONUMOUT share a second set of counters, one per heading level and one for body text; a
 * field counts at its paragraph's heading level (body text where it has none) and resets the deeper levels, so a
 * heading paragraph with no field changes nothing; AUTONUM paints the count and a period ({@code \s} another
 * separator), AUTONUMLGL the counts of the levels so far joined by periods ({@code \e} drops the last period),
 * AUTONUMOUT the outline form of the count at its level. A paragraph's heading level is its effective outline level,
 * else the number in its style's name ({@code heading 2}) or id ({@code Heading2}).</p>
 *
 * @since 17.3.5 (CR-067 phase 3)
 */
public final class ListNumbers {

	private static final Logger log = LoggerFactory.getLogger(ListNumbers.class);

	/** The fields computed here. */
	public static final Set<String> NAMES = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList("LISTNUM", "AUTONUM", "AUTONUMLGL", "AUTONUMOUT")));

	private static final Pattern HEADING_NAME = Pattern.compile("heading ?([1-9])");

	private static final String[] NUMBER_DEFAULT = { null, "%d)", "%a)", "%i)", "(%d)", "(%a)", "(%i)", "%d.", "%a.", "%i." };
	private static final String[] OUTLINE_DEFAULT = { null, "%I.", "%A.", "%d.", "%a)", "(%d)", "(%a)", "(%i)", "(%a)", "(%i)" };

	private final List<String> results = new ArrayList<String>();
	private final Map<Object, Integer> ordinals = new IdentityHashMap<Object, Integer>();

	private final int[] list = new int[10];
	private final int[] auto = new int[11];
	private int lastHeading;

	private ListNumbers() {
	}

	/** Whether the field name is one computed here. */
	public static boolean isListNumber(String fieldName) {
		return fieldName != null && NAMES.contains(fieldName.toUpperCase(Locale.ROOT));
	}

	/** The numbers of every such field in the package's main document part, in document order. */
	public static ListNumbers of(WordprocessingMLPackage pkg) {
		ListNumbers ln = new ListNumbers();
		if (pkg == null || pkg.getMainDocumentPart() == null) return ln;
		ln.scan(pkg);
		return ln;
	}

	/** The number of the field the node begins (its {@code w:fldSimple}, or a complex field's first run or fldChar), or null. */
	public String resultOf(Object node) {
		Integer i = node == null ? null : ordinals.get(node);
		return i == null ? null : results.get(i);
	}

	/** The number of the field at the ordinal (from 0) among these fields in document order, or null. */
	public String resultAt(int ordinal) {
		return ordinal < 0 || ordinal >= results.size() ? null : results.get(ordinal);
	}

	/** How many such fields the document holds. */
	public int size() {
		return results.size();
	}

	private void scan(WordprocessingMLPackage pkg) {
		final StyleDefinitionsPart styles = pkg.getMainDocumentPart().getStyleDefinitionsPart(false);
		PropertyResolver r = null;
		try {
			r = pkg.getMainDocumentPart().getPropertyResolver();
		} catch (Exception e) {
			log.debug("No property resolver: heading levels by style name only ({})", e.getMessage());
		}
		final PropertyResolver resolver = r;
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			int paragraphLevel;
			int depth;
			StringBuilder instr;
			R beginRun;
			FldChar beginChar;

			@Override
			public List<Object> apply(Object o) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof P) {
					paragraphLevel = levelOf((P) u, styles, resolver);
					if (paragraphLevel > 0) lastHeading = paragraphLevel;
				} else if (u instanceof CTSimpleField) {
					if (depth == 0) record((CTSimpleField) u, ((CTSimpleField) u).getInstr(), paragraphLevel, null);
				} else if (u instanceof R) {
					run((R) u);
				}
				return null;
			}

			private void run(R run) {
				for (Object c : run.getContent()) {
					Object u = XmlUtils.unwrap(c);
					if (u instanceof FldChar) {
						STFldCharType type = ((FldChar) u).getFldCharType();
						if (type == STFldCharType.BEGIN) {
							depth++;
							if (depth == 1) {
								instr = new StringBuilder();
								beginRun = run;
								beginChar = (FldChar) u;
							}
						} else if (type == STFldCharType.SEPARATE) {
							if (depth == 1 && instr != null) {
								record(beginRun, instr.toString(), paragraphLevel, beginChar);
								instr = null;
							}
						} else if (type == STFldCharType.END) {
							if (depth == 1 && instr != null) {
								record(beginRun, instr.toString(), paragraphLevel, beginChar);
								instr = null;
							}
							if (depth > 0) depth--;
						}
					} else if (depth == 1 && instr != null) {
						if (u instanceof Text && c instanceof jakarta.xml.bind.JAXBElement
								&& "instrText".equals(((jakarta.xml.bind.JAXBElement<?>) c).getName().getLocalPart())) {
							instr.append(((Text) u).getValue());
						} else if (u instanceof R.Cr || u instanceof Br) {
							instr.append('\n');
						}
					}
				}
			}

			@Override
			public boolean shouldTraverse(Object o) {
				Object u = XmlUtils.unwrap(o);
				return !(u instanceof R) && !(u instanceof CTSimpleField);
			}
		});
	}

	private void record(Object key, String instr, int paragraphLevel, Object secondKey) {
		if (instr == null) return;
		FieldInstruction instruction = FieldInstruction.parse(instr);
		String name = instruction.getName();
		if (name == null || !NAMES.contains(name)) return;
		String value = "LISTNUM".equals(name) ? listNum(instruction, paragraphLevel) : autoNum(instruction, paragraphLevel);
		ordinals.put(key, results.size());
		if (secondKey != null) ordinals.put(secondKey, results.size());
		results.add(value);
	}

	private String listNum(FieldInstruction instr, int paragraphLevel) {
		String name = instr.getArgument() == null ? "NumberDefault" : instr.getArgument();
		int level = intSwitch(instr, "\\l", 0);
		if (level < 1) level = paragraphLevel > 0 ? paragraphLevel : lastHeading + 1;
		level = Math.max(1, Math.min(9, level));
		for (int k = 1; k < level; k++) if (list[k] == 0) list[k] = 1; // the implicit parents
		int start = intSwitch(instr, "\\s", -1);
		list[level] = start >= 0 ? start : list[level] + 1;
		for (int k = level + 1; k <= 9; k++) list[k] = 0;
		if ("LegalDefault".equalsIgnoreCase(name)) {
			StringBuilder sb = new StringBuilder();
			for (int k = 1; k <= level; k++) sb.append(list[k]).append('.');
			return sb.toString();
		}
		return format("OutlineDefault".equalsIgnoreCase(name) ? OUTLINE_DEFAULT[level] : NUMBER_DEFAULT[level], list[level]);
	}

	private String autoNum(FieldInstruction instr, int paragraphLevel) {
		int level = paragraphLevel > 0 ? paragraphLevel : 10;
		auto[level]++;
		for (int k = level + 1; k <= 10; k++) auto[k] = 0;
		String name = instr.getName();
		if ("AUTONUMLGL".equals(name)) {
			StringBuilder sb = new StringBuilder();
			for (int k = 1; k <= level; k++) {
				if (auto[k] == 0) continue;
				if (sb.length() > 0) sb.append('.');
				sb.append(auto[k]);
			}
			if (!instr.hasSwitch("\\e")) sb.append('.');
			return sb.toString();
		}
		if ("AUTONUMOUT".equals(name) && level <= 9) return format(OUTLINE_DEFAULT[level], auto[level]);
		String separator = instr.getSwitchValue("\\s");
		return auto[level] + (separator == null ? "." : separator);
	}

	private static int intSwitch(FieldInstruction instr, String name, int dflt) {
		String v = instr.getSwitchValue(name);
		if (v == null) return dflt;
		try {
			return Integer.parseInt(v.trim());
		} catch (NumberFormatException e) {
			return dflt;
		}
	}

	/** The pattern's one placeholder ({@code %d %a %A %i %I}) replaced by the count in that form. */
	static String format(String pattern, int n) {
		int at = pattern.indexOf('%');
		char form = pattern.charAt(at + 1);
		String count;
		switch (form) {
		case 'a': count = NumberFormatter.get(NumberFormat.LOWER_LETTER).format(n); break;
		case 'A': count = NumberFormatter.get(NumberFormat.UPPER_LETTER).format(n); break;
		case 'i': count = NumberFormatter.get(NumberFormat.LOWER_ROMAN).format(n); break;
		case 'I': count = NumberFormatter.get(NumberFormat.UPPER_ROMAN).format(n); break;
		default: count = String.valueOf(n);
		}
		return pattern.substring(0, at) + count + pattern.substring(at + 2);
	}

	/**
	 * The paragraph's heading level (1 to 9), or 0 for body text: its effective outline level where its properties or
	 * style set one (9, Word's "Body Text", is 0), else the number in its style's name or id.
	 */
	public static int levelOf(P p, StyleDefinitionsPart styles, PropertyResolver resolver) {
		PPr pPr = p.getPPr();
		if (pPr == null) return 0;
		if (resolver != null) {
			try {
				PPr effective = resolver.getEffectivePPr(pPr);
				if (effective != null && effective.getOutlineLvl() != null && effective.getOutlineLvl().getVal() != null) {
					int v = effective.getOutlineLvl().getVal().intValue();
					return v >= 0 && v < 9 ? v + 1 : 0;
				}
			} catch (Exception e) {
				log.debug("Outline level not resolved: {}", e.getMessage());
			}
		}
		String styleId = pPr.getPStyle() == null ? null : pPr.getPStyle().getVal();
		if (styleId == null) return 0;
		Style style = styles == null ? null : styles.getStyleById(styleId);
		String name = style == null || style.getName() == null || style.getName().getVal() == null ? styleId : style.getName().getVal();
		Matcher m = HEADING_NAME.matcher(name.toLowerCase(Locale.ROOT));
		if (m.matches()) return Integer.parseInt(m.group(1));
		m = HEADING_NAME.matcher(styleId.toLowerCase(Locale.ROOT));
		return m.matches() ? Integer.parseInt(m.group(1)) : 0;
	}
}
