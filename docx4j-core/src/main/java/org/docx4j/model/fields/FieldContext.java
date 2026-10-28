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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.model.fields.docproperty.DocPropertyResolver;
import org.docx4j.model.fields.docvariable.DocVariableResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;

/**
 * What a field evaluator may ask for (CR-067 D10): the package and the part; a clock; the language
 * the result is formatted in; the user; a file name; a resolver for external content, absent unless
 * the caller supplies one (D8); and the state one pass over a document accumulates (sequence
 * numbers, the text of bookmarks seen so far). The defaults are what the package gives and the
 * present moment; what is not supplied is absent, and a field that needs it keeps its cached result.
 *
 * <p>One context serves one pass over one document: its sequence counters and bookmarks are that
 * pass's.</p>
 *
 * @since 17.3.5
 */
public final class FieldContext {

	/** External content a field names (INCLUDETEXT, INCLUDEPICTURE, LINK): supplied by the caller, or absent. */
	public interface External {
		/** The content the reference names, or null where the resolver declines it. */
		InputStream open(String reference) throws IOException;
	}

	private final WordprocessingMLPackage pkg;
	private Part part;
	private Supplier<Date> clock = Date::new;
	private String language;
	private String userName;
	private String userInitials;
	private String userAddress;
	private String fileName;
	private External external;
	private Integer section;
	private java.time.ZoneId zone;
	private Map<String, String> noteNumbers;
	private Map<String, java.util.List<org.docx4j.wml.R>> bookmarkRuns;
	private DocPropertyResolver properties;
	private DocVariableResolver variables;
	private final Map<String, Integer> sequences = new HashMap<String, Integer>();
	private final Map<String, String> bookmarks = new HashMap<String, String>();

	private FieldContext(WordprocessingMLPackage pkg) {
		this.pkg = pkg;
	}

	/** A context for the package, with the defaults. */
	public static FieldContext of(WordprocessingMLPackage pkg) {
		return new FieldContext(pkg);
	}

	public WordprocessingMLPackage getPackage() {
		return pkg;
	}

	/** The part the field is in, or null. */
	public Part getPart() {
		return part;
	}

	public FieldContext part(Part part) {
		this.part = part;
		return this;
	}

	/** The moment DATE and TIME show (and the output path's PRINTDATE, the conversion being the printing): the clock's, now by default. */
	public Date now() {
		return clock.get();
	}

	public FieldContext clock(Supplier<Date> clock) {
		this.clock = clock == null ? Date::new : clock;
		return this;
	}

	/**
	 * The zone a date field's instant is shown in (CR-067 D12): this context's where set, else the package's
	 * {@link WordprocessingMLPackage#effectiveDateTimeZone()} (the package's own, the JVM-wide property, the
	 * JVM default), as Word shows an instant in the machine's zone.
	 */
	public java.time.ZoneId getZone() {
		if (zone != null) return zone;
		if (pkg != null) return pkg.effectiveDateTimeZone();
		java.time.ZoneId property = org.docx4j.openpackaging.packages.OpcPackage.propertyDateTimeZone();
		return property != null ? property : java.time.ZoneId.systemDefault();
	}

	/** The per-call override of {@link #getZone()}; null returns to the package's. */
	public FieldContext zone(java.time.ZoneId zone) {
		this.zone = zone;
		return this;
	}

	/** {@link #getZone()} as a {@link java.util.TimeZone}, for the formatters. */
	public java.util.TimeZone getTimeZone() {
		return java.util.TimeZone.getTimeZone(getZone());
	}

	/** The language a result is formatted in ({@code fr-CA}), or null for the platform's default, as before. */
	public String getLanguage() {
		return language;
	}

	public FieldContext language(String language) {
		this.language = (language == null || language.trim().isEmpty()) ? null : language.trim();
		return this;
	}

	/**
	 * The language the document's defaults state ({@code w:docDefaults/w:rPrDefault/w:rPr/w:lang}), in
	 * which Word writes the month and day names a date field prints; null where it states none.
	 */
	public static String documentLanguage(WordprocessingMLPackage pkg) {
		try {
			org.docx4j.wml.CTLanguage lang = pkg.getMainDocumentPart().getStyleDefinitionsPart(false)
					.getJaxbElement().getDocDefaults().getRPrDefault().getRPr().getLang();
			String val = lang == null ? null : lang.getVal();
			return (val == null || val.trim().length() == 0) ? null : val.trim();
		} catch (Exception e) {
			return null;
		}
	}

	public String getUserName() {
		return userName;
	}

	public String getUserInitials() {
		return userInitials;
	}

	public String getUserAddress() {
		return userAddress;
	}

	/** The user USERNAME, USERINITIALS and USERADDRESS show; absent by default. */
	public FieldContext user(String name, String initials, String address) {
		this.userName = name;
		this.userInitials = initials;
		this.userAddress = address;
		return this;
	}

	/** The name FILENAME shows; absent by default (a package has no file name). */
	public String getFileName() {
		return fileName;
	}

	public FieldContext fileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	/** The resolver for external content, or null: no field reads a file unless one is supplied (D8). */
	public External getExternal() {
		return external;
	}

	public FieldContext external(External external) {
		this.external = external;
		return this;
	}

	/** The section (from 1) the field being evaluated is in, or null where the consumer has not said (SECTION keeps its cached result). */
	public Integer getSection() {
		return section;
	}

	/** Sets the section the next fields are in; the consumer calls it as its walk passes section ends. @since 17.3.5 (phase 1) */
	public FieldContext section(Integer section) {
		this.section = section;
		return this;
	}

	/**
	 * The section the node is in, read off the main document part (the paragraphs with a {@code w:sectPr} before
	 * its top-level ancestor), or null where the part is not the main one.
	 */
	public static Integer sectionOf(Part part, Object node) {
		return FieldSources.sectionOf(part, node);
	}

	/**
	 * The number of the footnote or endnote whose reference each bookmark holds, by name in lower case (NOTEREF):
	 * read from the main document part on first use.
	 */
	public Map<String, String> noteNumbers() {
		if (noteNumbers == null) noteNumbers = FieldSources.noteNumbers(pkg);
		return noteNumbers;
	}

	/**
	 * Fills {@link #bookmarks()} from the main document part (each bookmark's {@code w:t} text in document order),
	 * for a consumer with no reading of its own; a REF then resolves. Returns this.
	 */
	public FieldContext readBookmarks() {
		if (bookmarks.isEmpty()) bookmarks.putAll(FieldSources.bookmarkTexts(pkg));
		return this;
	}

	/**
	 * A bookmark's runs (its {@code w:t} with their run properties), by name in lower case, read from the main
	 * document part on first use: what a REF's result is written as, the bookmark's formatting kept.
	 */
	public Map<String, java.util.List<org.docx4j.wml.R>> bookmarkRuns() {
		if (bookmarkRuns == null) bookmarkRuns = FieldSources.bookmarkRuns(pkg);
		return bookmarkRuns;
	}

	/** The document's properties (core, application, custom), read once. */
	public DocPropertyResolver properties() {
		if (properties == null) properties = new DocPropertyResolver(pkg);
		return properties;
	}

	/** The document's variables, read once. */
	public DocVariableResolver variables() {
		if (variables == null) variables = new DocVariableResolver(pkg);
		return variables;
	}

	/** SEQ counters by identifier (upper case), this pass's. */
	public Map<String, Integer> sequences() {
		return sequences;
	}

	/** The text of the bookmarks this pass has seen, by name (lower case); a REF reads it. */
	public Map<String, String> bookmarks() {
		return bookmarks;
	}
}
