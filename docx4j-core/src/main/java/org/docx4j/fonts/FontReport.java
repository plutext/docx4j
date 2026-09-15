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
package org.docx4j.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * What a document asks of each font, what docx4j draws it with, how close that is, and
 * what the user can do about it - {@link FontsAnalysis}'s answer, rendered as the
 * conversion log, as text, or as JSON.
 *
 * <p>One entry per font the document sets text in (plus any the conversion met and could
 * not map), ordered by how much text it carries.</p>
 *
 * @since 17.1.1
 */
public final class FontReport {

	/**
	 * How close the substitution is.
	 *
	 * <p><b>EXACT</b>: the font itself, installed or embedded, or a metric clone - Word's
	 * advances - covering every script and face the document sets in it.  <b>NEAR</b>: a
	 * measured stand-in, or a clone with a script going elsewhere; the error is named.
	 * <b>CLASS</b>: a face of the same class, or Word's own default for a font it cannot
	 * find - the widths are not the document font's, so lines break differently, and the
	 * line box may differ too.  <b>NONE</b>: nothing mapped it, and FOP will draw it in
	 * its base-14 fallback.</p>
	 */
	public enum Grade { EXACT, NEAR, CLASS, NONE }

	/**
	 * What the fontTable says about the machine that <em>saved</em> the document, which is
	 * the page its author saw.
	 *
	 * <p>Word writes {@code w:panose1}, {@code w:sig} and {@code w:charset} off the font
	 * file as it saves, so an entry carrying them says the saving machine had the font,
	 * and a name-only entry says it did not - in which case Word itself substituted, and
	 * the author's page was never in that font.  See CR-017, "This matters because the
	 * target is what Word drew for the author".</p>
	 */
	public enum AuthorHad { EMBEDDED, LIKELY, UNLIKELY, UNKNOWN }

	/** One document font. */
	public static final class Entry {

		private final FontUsage.Use use;
		private final FontDecision decision;
		private final Grade grade;
		private final AuthorHad authorHad;
		private final String authorHadEvidence;
		private final String action;
		private final List<String> notes;

		Entry(FontUsage.Use use, FontDecision decision, Grade grade, AuthorHad authorHad,
				String authorHadEvidence, String action, List<String> notes) {
			this.use = use;
			this.decision = decision;
			this.grade = grade;
			this.authorHad = authorHad;
			this.authorHadEvidence = authorHadEvidence;
			this.action = action;
			this.notes = Collections.unmodifiableList(notes);
		}

		public String getDocumentFont() {
			return use!=null ? use.getDocumentFont() : decision.getDocumentFont();
		}

		/** What the document uses it for; null where the conversion met it but no text
		 *  is set in it (a font named by a style nothing uses). */
		public FontUsage.Use getUse() { return use; }

		/** Which pass answered, and with what; never null. */
		public FontDecision getDecision() { return decision; }

		public Grade getGrade() { return grade; }

		public AuthorHad getAuthorHad() { return authorHad; }

		/** The fontTable evidence the {@link #getAuthorHad} answer rests on. */
		public String getAuthorHadEvidence() { return authorHadEvidence; }

		/** What to do about it, in one sentence; empty where there is nothing to do. */
		public String getAction() { return action; }

		/** Per-script and per-face remarks: the scripts that left the font's face, a
		 *  synthesised weight, the face this environment would use instead. */
		public List<String> getNotes() { return notes; }

		/** The one line the conversion logs. */
		public String getSummaryLine() {
			StringBuilder sb = new StringBuilder();
			sb.append(getDocumentFont()).append(": ").append(grade).append(" - ");
			sb.append(describe());
			if (action!=null && action.length()>0) sb.append("; ").append(action);
			return sb.toString();
		}

		/** What is being drawn in what, in words. */
		public String describe() {
			if (decision==null) return "no decision recorded";
			PhysicalFont pf = decision.getPhysicalFont();
			switch (decision.getSource()) {
				case INSTALLED: return "installed, and used as it is";
				case EMBEDDED: return "embedded in the document, and used as it is";
				case METRIC_CLONE: return "not installed; drawn in " + name(pf) + ", its metric clone";
				case MEASURED_STAND_IN: return "not installed; drawn in " + name(pf)
						+ ", a measured stand-in" + (decision.getWidthError()==null ? ""
								: " (" + decision.getWidthError() + ")");
				case ALT_NAME: return "not installed; drawn in " + name(pf) + " through the document's own "
						+ decision.getVia();
				case CLASS: return "not installed; drawn in " + name(pf) + ", "
						+ (decision.getVia()==null ? "a face of the same class" : decision.getVia());
				case WORD_DEFAULT: return "unknown family; drawn in " + name(pf) + " by "
						+ decision.getVia();
				case MAPPER_OWN: return "drawn in " + name(pf) + " (" + decision.getVia() + ")";
				case SYMBOL: return "a symbol font; its characters drawn in " + name(pf);
				case UNMAPPED: default: return "not installed and not substituted; FOP will draw it in its"
						+ " base-14 fallback";
			}
		}

		/** The face's name as a user would look for it: the FO layer's suffixes (the
		 *  kerned twin, the no-ligature twin, the no-bold alias) are docx4j's own
		 *  bookkeeping and mean nothing to someone installing a font. */
		private static String name(PhysicalFont pf) {
			return pf==null ? "nothing" : PhysicalFonts.stripSuffixes(pf.getName());
		}

		@Override
		public String toString() { return getSummaryLine(); }
	}

	private final String documentName;
	private final String environment;
	private final List<Entry> entries;
	private final FontUsage usage;

	FontReport(String documentName, String environment, List<Entry> entries, FontUsage usage) {
		this.documentName = documentName;
		this.environment = environment;
		this.entries = Collections.unmodifiableList(entries);
		this.usage = usage;
	}

	/** One per document font, most text first. */
	public List<Entry> getEntries() { return entries; }

	/** The use walk behind it. */
	public FontUsage getUsage() { return usage; }

	/** The font environment the report was made against ("this machine", "the docx4j
	 *  font jars", a directory). */
	public String getEnvironment() { return environment; }

	public String getDocumentName() { return documentName; }

	/** The worst grade in the report, or {@code EXACT} where there is nothing to report. */
	public Grade getWorstGrade() {
		Grade worst = Grade.EXACT;
		for (Entry e : entries) {
			if (e.getGrade().ordinal() > worst.ordinal()) worst = e.getGrade();
		}
		return worst;
	}

	/** The lines the conversion logs: one per font, in report order. */
	public List<String> getSummaryLines() {
		List<String> lines = new ArrayList<String>();
		for (Entry e : entries) lines.add(e.getSummaryLine());
		return lines;
	}

	// ------------------------------------------------------------------ renderings

	/** The text form, which is what the conversion logs and the {@code main} prints. */
	public String toText() {

		StringBuilder sb = new StringBuilder();
		sb.append("Fonts in ").append(documentName==null ? "the document" : documentName);
		sb.append(" (against ").append(environment).append(")\n");
		if (usage!=null) {
			sb.append(usage.getCharacters()).append(" characters in ").append(usage.getRuns())
					.append(" runs, ").append(entries.size()).append(" fonts\n");
		}
		for (Entry e : entries) {
			sb.append('\n').append(e.getSummaryLine()).append('\n');
			if (e.getUse()!=null) {
				sb.append("    used for ").append(e.getUse().getCharacters()).append(" characters in ")
						.append(e.getUse().getRuns()).append(" runs; scripts ")
						.append(e.getUse().getScripts()).append("; faces ")
						.append(e.getUse().getFaces()).append('\n');
			}
			FontDecision d = e.getDecision();
			if (d!=null) {
				sb.append("    ").append(d.getSource());
				if (d.getPhysicalFont()!=null) sb.append(" -> ").append(d.getPhysicalFont().getName());
				if (d.getVia()!=null) sb.append(" (").append(d.getVia()).append(")");
				if (d.getWidthError()!=null) sb.append(" [").append(d.getWidthError()).append("]");
				sb.append("; line box ").append(d.getLineBox()).append('\n');
			}
			sb.append("    the machine that saved it: ").append(e.getAuthorHad());
			if (e.getAuthorHadEvidence()!=null) sb.append(" (").append(e.getAuthorHadEvidence()).append(")");
			sb.append('\n');
			for (String note : e.getNotes()) sb.append("    ").append(note).append('\n');
		}
		return sb.toString();
	}

	/** The same, as JSON: hand-written, since docx4j-core depends on no JSON library. */
	public String toJson() {

		StringBuilder sb = new StringBuilder();
		sb.append("{\n  \"document\": ").append(json(documentName));
		sb.append(",\n  \"environment\": ").append(json(environment));
		if (usage!=null) {
			sb.append(",\n  \"characters\": ").append(usage.getCharacters());
			sb.append(",\n  \"runs\": ").append(usage.getRuns());
		}
		sb.append(",\n  \"worstGrade\": ").append(json(getWorstGrade().name()));
		sb.append(",\n  \"fonts\": [");
		boolean first = true;
		for (Entry e : entries) {
			sb.append(first ? "\n" : ",\n");
			first = false;
			sb.append("    {\n");
			sb.append("      \"documentFont\": ").append(json(e.getDocumentFont())).append(",\n");
			sb.append("      \"grade\": ").append(json(e.getGrade().name())).append(",\n");
			sb.append("      \"authorHad\": ").append(json(e.getAuthorHad().name())).append(",\n");
			sb.append("      \"authorHadEvidence\": ").append(json(e.getAuthorHadEvidence())).append(",\n");
			sb.append("      \"action\": ").append(json(e.getAction())).append(",\n");
			FontDecision d = e.getDecision();
			sb.append("      \"decision\": {\n");
			sb.append("        \"source\": ").append(json(d==null ? null : d.getSource().name())).append(",\n");
			sb.append("        \"physicalFont\": ").append(json(d==null || d.getPhysicalFont()==null
					? null : d.getPhysicalFont().getName())).append(",\n");
			sb.append("        \"via\": ").append(json(d==null ? null : d.getVia())).append(",\n");
			sb.append("        \"widthError\": ").append(json(d==null ? null : d.getWidthError())).append(",\n");
			sb.append("        \"widthFactor\": ").append(d==null ? 1 : d.getWidthFactor()).append(",\n");
			sb.append("        \"lineBox\": ").append(json(d==null ? null : d.getLineBox())).append(",\n");
			sb.append("        \"faces\": {\"bold\": ").append(json(d==null ? null : d.getBoldFace()))
					.append(", \"italic\": ").append(json(d==null ? null : d.getItalicFace()))
					.append(", \"boldItalic\": ").append(json(d==null ? null : d.getBoldItalicFace()))
					.append("},\n");
			sb.append("        \"perScript\": [");
			boolean firstScript = true;
			if (d!=null) {
				for (FontDecision.ScriptChoice sc : d.getPerScript()) {
					sb.append(firstScript ? "" : ", ");
					firstScript = false;
					sb.append("{\"script\": ").append(json(sc.getScript()))
							.append(", \"face\": ").append(json(sc.getFace()))
							.append(", \"error\": ").append(json(sc.getError())).append("}");
				}
			}
			sb.append("]\n      },\n");
			sb.append("      \"use\": ");
			if (e.getUse()==null) {
				sb.append("null\n");
			} else {
				FontUsage.Use u = e.getUse();
				sb.append("{\n        \"characters\": ").append(u.getCharacters());
				sb.append(",\n        \"runs\": ").append(u.getRuns());
				sb.append(",\n        \"byScript\": {");
				boolean firstKey = true;
				for (Map.Entry<String, long[]> s : u.getByScript().entrySet()) {
					sb.append(firstKey ? "" : ", ");
					firstKey = false;
					sb.append(json(s.getKey())).append(": {\"characters\": ").append(s.getValue()[0])
							.append(", \"runs\": ").append(s.getValue()[1]).append("}");
				}
				sb.append("},\n        \"byFace\": {");
				firstKey = true;
				for (Map.Entry<FontUsage.Face, long[]> f : u.getByFace().entrySet()) {
					sb.append(firstKey ? "" : ", ");
					firstKey = false;
					sb.append(json(f.getKey().name())).append(": {\"characters\": ").append(f.getValue()[0])
							.append(", \"runs\": ").append(f.getValue()[1]).append("}");
				}
				sb.append("}\n      }\n");
			}
			sb.append("    }");
		}
		sb.append(entries.isEmpty() ? "]" : "\n  ]");
		sb.append("\n}\n");
		return sb.toString();
	}

	private static String json(String value) {
		if (value==null) return "null";
		StringBuilder sb = new StringBuilder("\"");
		for (int i=0; i<value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				default:
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int)c));
					} else {
						sb.append(c);
					}
			}
		}
		return sb.append('"').toString();
	}

	@Override
	public String toString() { return toText(); }
}
