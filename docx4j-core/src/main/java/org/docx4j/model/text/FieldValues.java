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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.fields.docproperty.DocPropertyResolver;
import org.docx4j.model.fields.docvariable.DocVariableResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Field results computed under {@code fields: update} (WD REQ-506), in the walk and so without a
 * copy of the package: only the fields that need neither layout nor external access, by
 * {@code FieldUpdater}'s resolvers and {@code FormattingSwitchHelper}'s switches ({@code \*},
 * {@code \#}, {@code \@}).
 *
 * <ul>
 * <li>DOCPROPERTY (core, application and custom properties) and DOCVARIABLE;</li>
 * <li>the core-property fields AUTHOR, TITLE, SUBJECT, KEYWORDS, COMMENTS, LASTSAVEDBY, REVNUM,
 * CREATEDATE and SAVEDATE (one given a text argument gives that text, as Word sets the property);</li>
 * <li>SEQ, counted in reading order ({@code \r}, {@code \c} and {@code \h} honoured; {@code \s} not);</li>
 * <li>REF to a bookmark, its text in the view (with {@code \n}, {@code \r}, {@code \w}, {@code \p}
 * or {@code \f}, which need numbering or layout, it keeps its cached result);</li>
 * <li>QUOTE, its argument.</li>
 * </ul>
 *
 * Nothing else: not MERGEFIELD (no data is supplied), FORMTEXT (its result is what was typed), DATE,
 * TIME, PRINTDATE, the layout fields or the external ones, and never a field with {@code w:fldLock}.
 * A field whose value can't be found keeps its cached result.
 */
final class FieldValues {

	private static final Logger log = LoggerFactory.getLogger(FieldValues.class);

	/** The core-property fields, by the DOCPROPERTY name of their property. */
	private static final Map<String, String> CORE = new HashMap<String, String>();
	static {
		CORE.put("AUTHOR", "Author");
		CORE.put("TITLE", "Title");
		CORE.put("SUBJECT", "Subject");
		CORE.put("COMMENTS", "Comments");
		CORE.put("LASTSAVEDBY", "LastSavedBy");
		CORE.put("REVNUM", "RevisionNumber");
		CORE.put("CREATEDATE", "CreateTime");
		CORE.put("SAVEDATE", "LastSavedTime");
		CORE.put("KEYWORDS", null);
	}

	/** REF's switches that need the numbering model or layout. */
	private static final Set<String> REF_UNSUPPORTED = new HashSet<String>(Arrays.asList("\\n", "\\r", "\\w", "\\p", "\\f"));

	private final WordprocessingMLPackage pkg;
	private DocPropertyResolver properties;
	private DocVariableResolver variables;
	private final Map<String, Integer> sequences = new HashMap<String, Integer>();
	/** Bookmarks' text by name (lower case): from a first reading, then as this one reads them. */
	final Map<String, String> bookmarks = new HashMap<String, String>();

	FieldValues(WordprocessingMLPackage pkg) {
		this.pkg = pkg;
	}

	/** The field's computed result, or null to keep its cached one. */
	String compute(String instr) {
		if (pkg == null || instr == null) return null;
		FldSimpleModel model = new FldSimpleModel();
		try {
			model.build(instr);
		} catch (Exception e) {
			return null;
		}
		String name = model.getFldName();
		if (name == null) return null;
		List<String> params = model.getFldParameters();
		String argument = params == null || params.isEmpty() || params.get(0).startsWith("\\") ? null : unquote(params.get(0));
		String value;
		try {
			switch (name) {
			case "DOCPROPERTY":
				value = argument == null ? null : properties().getValue(argument);
				break;
			case "DOCVARIABLE":
				value = argument == null ? null : variables().getValue(argument);
				break;
			case "SEQ":
				value = sequence(argument, params);
				break;
			case "REF":
				value = ref(argument, params);
				break;
			case "QUOTE":
				value = argument;
				break;
			default:
				if (!CORE.containsKey(name)) return null;
				if (argument != null) value = argument;
				else if (CORE.get(name) == null) value = keywords();
				else value = properties().getValue(CORE.get(name));
			}
		} catch (Exception e) {
			log.debug("No value for the field " + instr.trim() + ": " + e.getMessage());
			return null;
		}
		if (value == null) return null;
		try {
			return FormattingSwitchHelper.applyFormattingSwitch(pkg, model, value);
		} catch (Exception e) {
			log.debug("Couldn't apply the switches of " + instr.trim() + ": " + e.getMessage());
			return value;
		}
	}

	private DocPropertyResolver properties() {
		if (properties == null) properties = new DocPropertyResolver(pkg);
		return properties;
	}

	private DocVariableResolver variables() {
		if (variables == null) variables = new DocVariableResolver(pkg);
		return variables;
	}

	private String keywords() {
		return pkg.getDocPropsCorePart() == null || pkg.getDocPropsCorePart().getJaxbElement() == null ? null
				: pkg.getDocPropsCorePart().getJaxbElement().getKeywords();
	}

	private String sequence(String identifier, List<String> params) {
		if (identifier == null) return null;
		String key = identifier.toUpperCase(Locale.ROOT);
		Integer current = sequences.get(key);
		int n = current == null ? 0 : current;
		String reset = switchValue("\\r", params);
		if (reset != null) {
			try {
				n = Integer.parseInt(reset.trim());
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (!params.contains("\\c")) {
			n++;
		}
		sequences.put(key, n);
		if (params.contains("\\h")) return "";
		return String.valueOf(n);
	}

	private String ref(String bookmark, List<String> params) {
		if (bookmark == null) return null;
		for (String p : params) if (REF_UNSUPPORTED.contains(p.toLowerCase(Locale.ROOT))) return null;
		return bookmarks.get(bookmark.toLowerCase(Locale.ROOT));
	}

	private static String switchValue(String name, List<String> params) {
		for (int i = 0; i < params.size() - 1; i++) if (params.get(i).equalsIgnoreCase(name)) return unquote(params.get(i + 1));
		return null;
	}

	private static String unquote(String s) {
		if (s == null) return null;
		String t = s.trim();
		if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) t = t.substring(1, t.length() - 1);
		return t;
	}
}
