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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.model.fields.FieldContext;
import org.docx4j.model.fields.FieldInstruction;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTMailMerge;
import org.docx4j.wml.CTOdsoFieldMapData;
import org.docx4j.wml.CTSettings;

/**
 * ADDRESSBLOCK and GREETINGLINE (CR-067 phase 3, measured against Word 365's merge 2026-10-28, section 14.2): the
 * {@code \f} format's tokens {@code <<prefix _FIELD_ suffix>>} filled from the merge record, a token printed only
 * where its field has a value, {@code \n} a line; the record's columns found by Word's address-field names (and
 * their usual spellings), or by the document's own mapping ({@code w:odso/w:fieldMapData}) where it has one.
 *
 * <p>Outside a merge (no record in the context) the cached result is kept.</p>
 *
 * @since 17.3.5
 */
public final class AddressFields {

	private AddressFields() {
	}

	/** Word's placeholder to its mapped-field name (the {@code w:mappedName} of a fieldMapData) and the usual column names. */
	static final Map<String, String[]> PLACEHOLDERS = new HashMap<String, String[]>();
	static {
		PLACEHOLDERS.put("_TITLE0_", new String[] { "Courtesy Title", "Title", "Salutation" });
		PLACEHOLDERS.put("_FIRST0_", new String[] { "First Name", "First", "Given Name", "Forename" });
		PLACEHOLDERS.put("_MIDDLE0_", new String[] { "Middle Name", "Middle" });
		PLACEHOLDERS.put("_LAST0_", new String[] { "Last Name", "Last", "Surname", "Family Name" });
		PLACEHOLDERS.put("_SUFFIX0_", new String[] { "Suffix" });
		PLACEHOLDERS.put("_NICK0_", new String[] { "Nickname" });
		PLACEHOLDERS.put("_JOBTITLE_", new String[] { "Job Title" });
		PLACEHOLDERS.put("_COMPANY_", new String[] { "Company", "Company Name", "Organization", "Organisation" });
		PLACEHOLDERS.put("_STREET1_", new String[] { "Address 1", "Address Line 1", "Address", "Street" });
		PLACEHOLDERS.put("_STREET2_", new String[] { "Address 2", "Address Line 2" });
		PLACEHOLDERS.put("_CITY_", new String[] { "City", "Town" });
		PLACEHOLDERS.put("_STATE_", new String[] { "State", "Province", "Region" });
		PLACEHOLDERS.put("_POSTAL_", new String[] { "Postal Code", "ZIP Code", "ZIP", "Postcode", "Post Code" });
		PLACEHOLDERS.put("_COUNTRY_", new String[] { "Country or Region", "Country", "Region", "Nation" });
		PLACEHOLDERS.put("_PHONE1_", new String[] { "Business Phone", "Phone", "Work Phone" });
		PLACEHOLDERS.put("_PHONE2_", new String[] { "Home Phone" });
		PLACEHOLDERS.put("_EMAIL_", new String[] { "E-mail Address", "Email", "E-mail" });
		PLACEHOLDERS.put("_SPOUSE0_", new String[] { "Spouse First Name" });
	}

	/** Word's default block (no {@code \f}; measured: the company first, no title, no suffix, the country always). */
	static final String DEFAULT_ADDRESS_FORMAT = "<<_COMPANY_\n>><<_FIRST0_>><< _LAST0_>>\n<<_STREET1_\n>><<_STREET2_\n>><<_CITY_>><< _STATE_>><< _POSTAL_>>\n<<_COUNTRY_>>"; // real line breaks, as Word's own format

	private static final Pattern TOKEN = Pattern.compile("<<(.*?)>>", Pattern.DOTALL);
	private static final Pattern PLACEHOLDER = Pattern.compile("_[A-Z0-9]+_");

	public static String addressBlock(FieldInstruction instr, FieldContext ctx) {
		if (ctx.getMergeRecord() == null) return null;
		Map<String, String> values = values(ctx);
		String format = instr.getSwitchValue("\\f");
		if (format == null) format = DEFAULT_ADDRESS_FORMAT;
		// \c: 2 omits the country where it is \e's; otherwise (1, 0 or absent) the country prints (measured for 0 and 1)
		String c = instr.getSwitchValue("\\c");
		String exclude = instr.getSwitchValue("\\e");
		String country = values.get("_COUNTRY_");
		if ("2".equals(c) && country != null && exclude != null && country.trim().equalsIgnoreCase(exclude.trim())) {
			values = new HashMap<String, String>(values);
			values.put("_COUNTRY_", "");
		}
		return fill(format, values).trim();
	}

	public static String greetingLine(FieldInstruction instr, FieldContext ctx) {
		if (ctx.getMergeRecord() == null) return null;
		String format = instr.getSwitchValue("\\f");
		if (format == null) return ""; // measured: no switches, nothing
		format = format.replace("\r", "").replace("\n", ""); // one line (Word's own format carries a break before <<_AFTER_>>; unmeasured, section 9)
		Map<String, String> values = values(ctx);
		String fallback = instr.getSwitchValue("\\e");
		StringBuilder before = new StringBuilder(), after = new StringBuilder(), name = new StringBuilder();
		boolean complete = true;
		Matcher m = TOKEN.matcher(format);
		while (m.find()) {
			String token = m.group(1);
			Matcher ph = PLACEHOLDER.matcher(token);
			if (!ph.find()) {
				name.append(token);
				continue;
			}
			String key = ph.group();
			String prefix = token.substring(0, ph.start()), suffix = token.substring(ph.end());
			// <<_BEFORE_ Dear >> is "Dear ": the space after the placeholder separates it from its text (measured)
			if ("_BEFORE_".equals(key)) before.append(prefix).append(suffix.startsWith(" ") ? suffix.substring(1) : suffix);
			else if ("_AFTER_".equals(key)) after.append(prefix).append(suffix.startsWith(" ") ? suffix.substring(1) : suffix);
			else {
				String v = values.get(key);
				if (v == null || v.isEmpty()) complete = false;
				else name.append(prefix).append(v).append(suffix);
			}
		}
		String who = name.toString().trim();
		if (!complete) {
			// Word's fallbacks (measured): the first and last names, then the first name, then \e
			String first = values.get("_FIRST0_"), last = values.get("_LAST0_");
			if (notEmpty(first) && notEmpty(last)) who = first.trim() + " " + last.trim();
			else if (notEmpty(first)) who = first.trim();
			else return fallback == null ? "" : fallback;
		}
		return before.toString() + who + after.toString();
	}

	private static boolean notEmpty(String s) {
		return s != null && !s.trim().isEmpty();
	}

	/**
	 * The format with its tokens filled: a token prints only where its field has a value. A line break in the format is a
	 * line (Word's own format breaks its lines so, as {@code w:cr} in the instruction; measured 2026-10-28, CR-067 section
	 * 14.4); a hand-written {@code \n} prints the letter n, inside a token and outside one (measured, section 14.2; Word also
	 * broke the paragraph after the name line in the cases with {@code \c}, which is unexplained, section 9).
	 */
	static String fill(String format, Map<String, String> values) {
		format = format.replace("\r\n", "\n").replace('\r', '\n');
		StringBuilder out = new StringBuilder();
		Matcher m = TOKEN.matcher(format);
		int at = 0;
		while (m.find()) {
			out.append(format, at, m.start());
			at = m.end();
			String token = m.group(1).replace("\\n", "n");
			Matcher ph = PLACEHOLDER.matcher(token);
			if (!ph.find()) {
				out.append(token);
				continue;
			}
			String v = values.get(ph.group());
			if (v == null || v.trim().isEmpty()) continue;
			out.append(token, 0, ph.start()).append(v.trim()).append(token.substring(ph.end()));
		}
		out.append(format.substring(at));
		return out.toString().replace("\\n", "n");
	}

	/** The record's values by placeholder. */
	static Map<String, String> values(FieldContext ctx) {
		Map<String, String> record = ctx.getMergeRecord();
		Map<String, String> byKey = new HashMap<String, String>();
		for (Map.Entry<String, String> e : record.entrySet()) byKey.put(key(e.getKey()), e.getValue());
		Map<String, String> mapped = documentMapping(ctx.getPackage());
		Map<String, String> out = new HashMap<String, String>();
		for (Map.Entry<String, String[]> e : PLACEHOLDERS.entrySet()) {
			String column = mapped.get(e.getKey());
			String v = column == null ? null : byKey.get(key(column));
			for (int i = 0; v == null && i < e.getValue().length; i++) v = byKey.get(key(e.getValue()[i]));
			if (v != null) out.put(e.getKey(), v);
		}
		return out;
	}

	/** A name's key: letters and digits only, in lower case (Word writes a column "First Name" as First_Name). */
	static String key(String name) {
		StringBuilder sb = new StringBuilder();
		for (char ch : name.toCharArray()) if (Character.isLetterOrDigit(ch)) sb.append(Character.toLowerCase(ch));
		return sb.toString();
	}

	/** Placeholder to column from the document's {@code w:odso/w:fieldMapData} (a mapped name's placeholder by Word's field name). */
	static Map<String, String> documentMapping(WordprocessingMLPackage pkg) {
		Map<String, String> out = new HashMap<String, String>();
		if (pkg == null || pkg.getMainDocumentPart() == null) return out;
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		if (dsp == null || dsp.getJaxbElement() == null) return out;
		CTSettings settings = dsp.getJaxbElement();
		CTMailMerge mm = settings.getMailMerge();
		if (mm == null || mm.getOdso() == null) return out;
		List<String> wordNames = new ArrayList<String>();
		for (Map.Entry<String, String[]> e : PLACEHOLDERS.entrySet()) wordNames.add(e.getKey());
		for (CTOdsoFieldMapData fmd : mm.getOdso().getFieldMapData()) {
			if (fmd.getMappedName() == null || fmd.getName() == null) continue;
			String mappedName = fmd.getMappedName().getVal(), column = fmd.getName().getVal();
			if (mappedName == null || column == null) continue;
			for (Map.Entry<String, String[]> e : PLACEHOLDERS.entrySet()) {
				if (key(e.getValue()[0]).equals(key(mappedName))) out.put(e.getKey(), column);
			}
		}
		return out;
	}

	/** For tests: a record's placeholder values. */
	static Map<String, String> valuesOf(Map<String, String> record) {
		FieldContext ctx = FieldContext.of(null).mergeRecord(record);
		return values(ctx);
	}

	static String upper(String s) {
		return s == null ? null : s.toUpperCase(Locale.ROOT);
	}
}
