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
package org.docx4j.survey;

import java.util.Locale;

/**
 * The columns of the catalogue's {@code docx4j_load} table, one row per document, in the order the
 * TSV holds them. The first nine (less {@code misplaced_children}, which a load cannot meet: CR-064
 * refuses on add, and JAXB never produces one) are catalogue-schema.sql's; the rest are this
 * survey's additions.
 */
enum Column {

	SHA256("TEXT PRIMARY KEY", "hex SHA-256 of the file, its name in the corpus"),
	DOCX4J_VERSION("TEXT", "the docx4j release that loaded it"),
	OUTCOME("TEXT", "ok | main_unreadable | load_failed | timeout | out_of_memory | crashed"),
	LOADED("INTEGER", "1 when OpcPackage.load returned a package"),
	LOAD_MS("INTEGER", "OpcPackage.load"),
	READ_MS("INTEGER", "reading every JAXB part after load, which leaves them to unmarshal on first use"),
	EXCEPTION("TEXT", "what load threw (class: message), or the timeout, out of memory or crash"),
	ROOT_CAUSE("TEXT", "the exception's innermost cause"),
	WARNINGS("INTEGER", "WARN log events while loading and reading"),
	ERRORS("INTEGER", "ERROR log events while loading and reading"),
	MC_CHOICES("TEXT", "the branch read of each mc:AlternateContent in the text stories (CR-051's rule, as Word reads): Requires=count, Fallback=count, none=count"),
	MC_ELEMENTS("INTEGER", "mc:AlternateContent elements read in the text stories"),
	MC_RESOLVED("INTEGER", "of them, resolved at load (block, row and cell level; CR-051, mc.onLoad=word)"),
	NOTES("TEXT", "anything else worth knowing about this row"),
	BYTES("INTEGER", "file size"),
	PACKAGE("TEXT", "the package class load made: WordprocessingMLPackage, ..."),
	MAIN_OK("INTEGER", "1 when the main document part read, 0 when not or absent; NULL when not loaded or not a WordprocessingML package"),
	PARTS("INTEGER", "parts loaded"),
	PARTS_XML("INTEGER", "of them, JAXB parts"),
	PARTS_UNREADABLE("INTEGER", "JAXB parts that could not be read"),
	UNREADABLE("TEXT", "the first unreadable part: name: root cause class: message"),
	PARTS_PREPROCESSED("INTEGER", "parts JAXB refused that read once mc-preprocessor.xslt had transformed them"),
	STRICT("INTEGER", "1 when the package was Strict OOXML (read through the Strict transform)"),
	LENIENT_TRUNCATED("INTEGER", "decimal integer attribute values truncated as Word reads them (CR-065)"),
	LENIENT_EMPTY("INTEGER", "empty integer attribute values read as 0 (CR-065)"),
	LENIENT_DROPPED("INTEGER", "integer attribute values of a form Word refuses, dropped (CR-065)"),
	OUT_OF_RANGE("INTEGER", "integer attribute values outside Word's range, held as written (CR-066)"),
	SAVED("INTEGER", "with --save: 1 when the loaded package saved; NULL without --save"),
	SAVE_MS("INTEGER", "with --save: the save, to a stream that discards it"),
	SAVE_EXCEPTION("TEXT", "with --save: what the save threw");

	final String sqlType;
	final String description;

	Column(String sqlType, String description) {
		this.sqlType = sqlType;
		this.description = description;
	}

	String sqlName() {
		return name().toLowerCase(Locale.ROOT);
	}

	static String[] names() {
		Column[] cs = values();
		String[] n = new String[cs.length];
		for (int i = 0; i < cs.length; i++) n[i] = cs[i].sqlName();
		return n;
	}

	/** The columns of {@code docx4j_load_messages}: one row per distinct log message a document raised. */
	enum Message {
		SHA256("TEXT", "the document"),
		PHASE("TEXT", "load | read | walk (the survey's own mc count) | save"),
		LEVEL("TEXT", "WARN | ERROR"),
		LOGGER("TEXT", "the logger, usually the docx4j class"),
		MESSAGE("TEXT", "the message, one line, at most 500 characters"),
		THROWABLE("TEXT", "the exception logged with it, if any: class: message"),
		COUNT("INTEGER", "how many times this document raised it");

		final String sqlType;
		final String description;

		Message(String sqlType, String description) {
			this.sqlType = sqlType;
			this.description = description;
		}

		String sqlName() {
			return name().toLowerCase(Locale.ROOT);
		}

		static String[] names() {
			Message[] cs = values();
			String[] n = new String[cs.length];
			for (int i = 0; i < cs.length; i++) n[i] = cs[i].sqlName();
			return n;
		}
	}
}
