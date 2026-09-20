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
package org.docx4j.anon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Scrubs a field instruction (w:instrText, w:delInstrText, w:fldSimple/@w:instr):
 * the field keyword and its switches survive, every argument loses its text.
 * <p>
 * Rules (CR-019 phase 1, item 2, "the MERGEFIELD pattern, generalised"):
 * <ul>
 * <li>the keyword is kept;</li>
 * <li>a switch ({@code \x}) is kept; the argument of a format switch
 * ({@code \*}, {@code \#}, {@code \@}) is a picture, not content, and is kept;</li>
 * <li>an argument which names a bookmark (REF, PAGEREF, NOTEREF, ASK, SET and
 * SEQ's identifier; HYPERLINK {@code \l}; TOC {@code \b} and {@code \c})
 * is renamed through the identifier map, so it still matches the renamed
 * bookmark;</li>
 * <li>STYLEREF's argument is kept when it is a built-in style name (the
 * predicate decides), else scrambled like any other;</li>
 * <li>a formula field ({@code = ...}) keeps its operators and functions;
 * only tokens the identifier map knows (bookmark names) are renamed;</li>
 * <li>form-field instructions (FORMTEXT, FORMCHECKBOX, FORMDROPDOWN) have no
 * argument and are untouched;</li>
 * <li>every other argument, quoted or bare, has its letters scrambled (digits
 * and punctuation stay, so a path keeps its shape and a switch value like
 * {@code "1-3"} its meaning).</li>
 * </ul>
 *
 * @since 17.2.0
 */
public class FieldInstructions {

	/** Word's field keywords: kept by the scrub, allowed by {@link Verify}. */
	public static final Set<String> KEYWORDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"ADDRESSBLOCK", "ADVANCE", "ASK", "AUTHOR", "AUTONUM", "AUTONUMLGL", "AUTONUMOUT", "AUTOTEXT",
			"AUTOTEXTLIST", "BARCODE", "BIBLIOGRAPHY", "BIDIOUTLINE", "CITATION", "COMMENTS", "COMPARE",
			"CREATEDATE", "DATABASE", "DATE", "DDE", "DDEAUTO", "DISPLAYBARCODE", "DOCPROPERTY", "DOCVARIABLE",
			"EDITTIME", "EMBED", "EQ", "FILENAME", "FILESIZE", "FILLIN", "FORMCHECKBOX", "FORMDROPDOWN",
			"FORMTEXT", "GLOSSARY", "GOTOBUTTON", "GREETINGLINE", "HYPERLINK", "IF", "IMPORT", "INCLUDE",
			"INCLUDEPICTURE", "INCLUDETEXT", "INDEX", "INFO", "KEYWORDS", "LASTSAVEDBY", "LINK", "LISTNUM",
			"MACROBUTTON", "MERGEBARCODE", "MERGEFIELD", "MERGEREC", "MERGESEQ", "NEXT", "NEXTIF", "NOTEREF",
			"NUMCHARS", "NUMPAGES", "NUMWORDS", "PAGE", "PAGEREF", "PRINT", "PRINTDATE", "PRIVATE", "QUOTE",
			"RD", "REF", "REVNUM", "SAVEDATE", "SECTION", "SECTIONPAGES", "SEQ", "SET", "SHAPE", "SKIPIF",
			"STYLEREF", "SUBJECT", "SYMBOL", "TA", "TC", "TEMPLATE", "TIME", "TITLE", "TOA", "TOC",
			"USERADDRESS", "USERINITIALS", "USERNAME", "XE",
			// format-switch words and formula functions, kept with the switch / the formula
			"MERGEFORMAT", "CHARFORMAT", "UPPER", "LOWER", "FIRSTCAP", "CAPS", "ALPHABETIC", "ARABIC",
			"ARABICDASH", "CARDTEXT", "DOLLARTEXT", "HEX", "ORDINAL", "ORDTEXT", "ROMAN", "SUM", "AVERAGE",
			"ABOVE", "BELOW", "LEFT", "RIGHT", "COUNT", "MAX", "MIN", "PRODUCT", "ROUND", "ABS", "AND",
			"OR", "NOT", "DEFINED", "FALSE", "TRUE", "INT", "MOD", "SIGN")));

	private static final Set<String> FORMAT_SWITCHES = new HashSet<String>(Arrays.asList("\\*", "\\#", "\\@"));

	/** keywords whose first argument is a bookmark or sequence identifier */
	private static final Set<String> FIRST_ARG_IS_IDENTIFIER = new HashSet<String>(Arrays.asList(
			"REF", "PAGEREF", "NOTEREF", "ASK", "SET", "SEQ", "GOTOBUTTON"));

	/** switches whose argument is a bookmark or sequence identifier */
	private static final Set<String> SWITCH_ARG_IS_IDENTIFIER = new HashSet<String>(Arrays.asList("\\l", "\\b", "\\c", "\\s"));

	private static final Set<String> NO_ARGUMENT = new HashSet<String>(Arrays.asList(
			"FORMTEXT", "FORMCHECKBOX", "FORMDROPDOWN"));

	private final Function<String, String> scrambler;
	private final Function<String, String> identifiers;
	private final Predicate<String> safeName;

	/**
	 * @param scrambler   replaces the letters of an argument (digits and punctuation kept)
	 * @param identifiers maps a bookmark or sequence name to its anonymised name
	 *                    (returns null for a name it does not know)
	 * @param safeName    true for a name which may survive (a built-in style name for STYLEREF)
	 */
	public FieldInstructions(Function<String, String> scrambler, Function<String, String> identifiers,
			Predicate<String> safeName) {
		this.scrambler = scrambler;
		this.identifiers = identifiers;
		this.safeName = safeName;
	}

	/** The keyword of an instruction ("=" for a formula), upper-cased, or null. */
	public static String keyword(String instr) {
		if (instr == null) return null;
		List<Token> tokens = tokenize(instr);
		return tokens.isEmpty() ? null : tokens.get(0).text.toUpperCase(Locale.ROOT);
	}

	public String scrub(String instr) {
		if (instr == null || instr.trim().isEmpty()) return instr;

		List<Token> tokens = tokenize(instr);
		if (tokens.isEmpty()) return instr;

		String keyword = tokens.get(0).text.toUpperCase(Locale.ROOT);
		if (NO_ARGUMENT.contains(keyword)) return instr;

		StringBuilder out = new StringBuilder();
		int pos = 0;
		String pendingSwitch = null; // the switch whose argument the next token is
		boolean firstArgSeen = false;

		for (int i = 0; i < tokens.size(); i++) {
			Token t = tokens.get(i);
			out.append(instr, pos, t.start);
			pos = t.end;

			String replacement;
			if (i == 0) {
				replacement = t.text; // the keyword
			} else if (t.kind == Kind.SWITCH) {
				out.append(t.text);
				pendingSwitch = t.text;
				continue;
			} else if (pendingSwitch != null && FORMAT_SWITCHES.contains(pendingSwitch)) {
				replacement = t.text; // a picture
			} else if (pendingSwitch != null && SWITCH_ARG_IS_IDENTIFIER.contains(pendingSwitch)
					&& (keyword.equals("HYPERLINK") || keyword.equals("TOC") || keyword.equals("SEQ"))) {
				replacement = identifier(t);
			} else if (keyword.equals("=")) {
				replacement = formulaToken(t);
			} else if (!firstArgSeen && pendingSwitch == null && FIRST_ARG_IS_IDENTIFIER.contains(keyword)) {
				replacement = identifier(t);
			} else if (!firstArgSeen && pendingSwitch == null && keyword.equals("STYLEREF")
					&& safeName.test(unquote(t.text))) {
				replacement = t.text;
			} else {
				replacement = scramble(t);
			}
			if (i > 0 && pendingSwitch == null) firstArgSeen = true;
			pendingSwitch = null;
			out.append(replacement);
		}
		out.append(instr, pos, instr.length());
		return out.toString();
	}

	private String identifier(Token t) {
		String name = unquote(t.text);
		String mapped = identifiers.apply(name);
		if (mapped == null) return scramble(t);
		return requote(t.text, mapped);
	}

	private static final java.util.regex.Pattern PLAIN_IDENTIFIER = java.util.regex.Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_.]*");

	/**
	 * In a formula only bookmark names change: a plain identifier which is not a
	 * function or keyword; operators, numbers, cell references and functions stay.
	 */
	private String formulaToken(Token t) {
		if (t.kind == Kind.QUOTED) return scramble(t);
		if (!PLAIN_IDENTIFIER.matcher(t.text).matches()) return t.text;
		if (KEYWORDS.contains(t.text.toUpperCase(Locale.ROOT))) return t.text;
		if (t.text.matches("[A-Za-z]{1,3}[0-9]+")) return t.text; // a cell reference
		String mapped = identifiers.apply(t.text);
		return mapped == null ? t.text : mapped;
	}

	private String scramble(Token t) {
		String inner = unquote(t.text);
		return requote(t.text, scrambler.apply(inner));
	}

	private static String unquote(String s) {
		if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	private static String requote(String original, String inner) {
		if (original.length() >= 2 && original.charAt(0) == '"' && original.charAt(original.length() - 1) == '"') {
			return "\"" + inner + "\"";
		}
		return inner;
	}

	enum Kind { WORD, QUOTED, SWITCH }

	static class Token {
		final int start, end;
		final String text;
		final Kind kind;
		Token(int start, int end, String text, Kind kind) {
			this.start = start; this.end = end; this.text = text; this.kind = kind;
		}
	}

	/** Splits on whitespace, keeping quoted strings (with \" escapes) whole; a switch is a backslash plus one character. */
	static List<Token> tokenize(String s) {
		List<Token> tokens = new ArrayList<Token>();
		int i = 0, n = s.length();
		while (i < n) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) { i++; continue; }
			int start = i;
			if (c == '"') {
				i++;
				while (i < n) {
					char d = s.charAt(i);
					if (d == '\\' && i + 1 < n) { i += 2; continue; }
					i++;
					if (d == '"') break;
				}
				tokens.add(new Token(start, i, s.substring(start, i), Kind.QUOTED));
			} else if (c == '\\' && i + 1 < n && !Character.isWhitespace(s.charAt(i + 1))) {
				i += 2;
				tokens.add(new Token(start, i, s.substring(start, i), Kind.SWITCH));
			} else {
				while (i < n && !Character.isWhitespace(s.charAt(i)) && s.charAt(i) != '"') i++;
				if (i == start) i++;
				tokens.add(new Token(start, i, s.substring(start, i), Kind.WORD));
			}
		}
		return tokens;
	}

}
