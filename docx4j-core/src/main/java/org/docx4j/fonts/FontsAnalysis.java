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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What this document asks of its fonts, what docx4j will draw them with, and what a user
 * can do about it.
 *
 * <p>Three things the package knew separately and said nowhere: the {@link Mapper}'s
 * decisions (CR-017 phase 1), what each font is actually used for - which scripts, which
 * faces, how much text - and the knowledge table's answer to "where do I get one"
 * ({@link FontSubstitutionTable}).  Put together they are a report a user (or an agent,
 * before converting) can act on, and the one the conversion logs.</p>
 *
 * @since 17.1.1
 */
public final class FontsAnalysis {

	private static final Logger log = LoggerFactory.getLogger(FontsAnalysis.class);

	private FontsAnalysis() {}

	// ------------------------------------------------------------------ the use walk

	/**
	 * What the document uses each font for: characters and runs by script and by face,
	 * over the body, the headers and footers, the notes and the comments.
	 *
	 * <p>The font of each <em>character</em> is
	 * {@link RunFontSelector#documentFontFor}'s answer, so this is the same dispatch the
	 * conversion makes (the [MS-OI29500] 17.3.2.26 table, the theme, the complex-script
	 * and symbol rules), not a reading of {@code w:rFonts}.  One selector per part, the
	 * run's effective properties resolved once per run rather than once per character,
	 * and each run's answers memoised by code point.</p>
	 *
	 * @since 17.1.1
	 */
	public static FontUsage usage(WordprocessingMLPackage pkg) {

		FontUsage usage = new FontUsage();
		if (pkg==null || pkg.getMainDocumentPart()==null) return usage;
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		org.docx4j.wml.Document document = mdp.getJaxbElement();
		if (document!=null && document.getBody()!=null) {
			walk(pkg, usage, document.getBody().getContent());
		}
		RelationshipsPart rp = mdp.getRelationshipsPart();
		if (rp!=null) {
			for (Relationship r : rp.getRelationships().getRelationship()) {
				Part part = rp.getPart(r);
				if (part instanceof HeaderPart) {
					walk(pkg, usage, ((HeaderPart)part).getJaxbElement());
				} else if (part instanceof FooterPart) {
					walk(pkg, usage, ((FooterPart)part).getJaxbElement());
				}
			}
		}
		if (mdp.getFootnotesPart()!=null) walk(pkg, usage, mdp.getFootnotesPart().getJaxbElement());
		if (mdp.getEndNotesPart()!=null) walk(pkg, usage, mdp.getEndNotesPart().getJaxbElement());
		if (mdp.getCommentsPart()!=null) walk(pkg, usage, mdp.getCommentsPart().getJaxbElement());
		return usage;
	}

	private static void walk(WordprocessingMLPackage pkg, FontUsage usage, Object content) {
		UsageWalk walk = new UsageWalk(pkg, usage);
		if (content instanceof List) {
			new TraversalUtil((List<?>)content, walk);
		} else if (content!=null) {
			walk.walkJAXBElements(content);
		}
	}

	/** One selector per part, as CR-016 phase 4 arranged (the scratch DOM is the
	 *  selector's). */
	private static final class UsageWalk extends TraversalUtil.CallbackImpl {

		private final FontUsage usage;
		private final RunFontSelector selector;
		private final PropertyResolver propertyResolver;
		private PPr pPr;

		UsageWalk(WordprocessingMLPackage pkg, FontUsage usage) {
			this.usage = usage;
			this.selector = new RunFontSelector(pkg, NO_OP_VISITOR, RunFontSelector.RunFontActionType.DISCOVERY);
			PropertyResolver resolver = null;
			try {
				resolver = pkg.getMainDocumentPart().getPropertyResolver();
			} catch (Exception e) {
				log.warn("No property resolver: " + e.getMessage());
			}
			this.propertyResolver = resolver;
		}

		@Override
		public List<Object> apply(Object o) {
			if (o instanceof P) {
				pPr = ((P)o).getPPr();
			} else if (o instanceof R) {
				run((R)o);
			}
			return null;
		}

		private void run(R r) {

			RPr effective = r.getRPr();
			if (propertyResolver!=null) {
				try {
					effective = propertyResolver.getEffectiveRPr(r.getRPr(), pPr);
				} catch (Exception e) {
					log.debug("effective rPr: " + e.getMessage());
				}
			}
			FontUsage.Face face = faceOf(effective);

			Map<Integer, String> fontOfCodePoint = new HashMap<Integer, String>();
			Set<String> fontsSeen = new LinkedHashSet<String>();
			Map<String, Set<String>> scriptsPerFont = new HashMap<String, Set<String>>();
			int characters = 0;

			for (Object o : r.getContent()) {
				Object unwrapped = XmlUtils.unwrap(o);
				if (unwrapped instanceof Text) {
					String text = ((Text)unwrapped).getValue();
					if (text==null) continue;
					for (int i=0; i<text.length(); i = text.offsetByCodePoints(i, 1)) {
						int cp = text.codePointAt(i);
						String font = fontOfCodePoint.get(cp);
						if (font==null) {
							font = selector.documentFontFor(pPr, effective, cp, true);
							if (font==null) font = "";
							fontOfCodePoint.put(cp, font);
						}
						if (font.length()==0) continue;
						String script = FontFallback.coverageGroupOf(cp);
						usage.use(font).add(script, face, 1, cp);
						fontsSeen.add(font);
						scripts(scriptsPerFont, font).add(script);
						characters++;
					}
				} else if (unwrapped instanceof R.Sym) {
					// a symbol character is one character of the font the w:sym names
					R.Sym sym = (R.Sym)unwrapped;
					if (sym.getFont()==null || sym.getFont().trim().length()==0) continue;
					String font = sym.getFont();
					usage.use(font).add(FontFallback.SYMBOL_GROUP, face, 1, symbolCodePoint(sym));
					fontsSeen.add(font);
					scripts(scriptsPerFont, font).add(FontFallback.SYMBOL_GROUP);
					characters++;
				}
			}

			if (characters==0) return;
			usage.countRun();
			usage.countCharacters(characters);
			Set<FontUsage.Face> faces = new HashSet<FontUsage.Face>();
			faces.add(face);
			for (String font : fontsSeen) {
				usage.use(font).endRun(scripts(scriptsPerFont, font), faces);
			}
		}

		/** The code point a {@code w:sym} asks for ({@code w:char} is hex), or a space. */
		private static int symbolCodePoint(R.Sym sym) {
			try {
				return sym.getChar()==null ? 0x20 : Integer.parseInt(sym.getChar().trim(), 16);
			} catch (NumberFormatException e) {
				return 0x20;
			}
		}

		private static Set<String> scripts(Map<String, Set<String>> map, String font) {
			Set<String> scripts = map.get(font);
			if (scripts==null) {
				scripts = new LinkedHashSet<String>();
				map.put(font, scripts);
			}
			return scripts;
		}

		private static FontUsage.Face faceOf(RPr rPr) {
			boolean bold = rPr!=null && rPr.getB()!=null && rPr.getB().isVal();
			boolean italic = rPr!=null && rPr.getI()!=null && rPr.getI().isVal();
			if (bold && italic) return FontUsage.Face.BOLD_ITALIC;
			if (bold) return FontUsage.Face.BOLD;
			if (italic) return FontUsage.Face.ITALIC;
			return FontUsage.Face.REGULAR;
		}
	}


	// ------------------------------------------------------------------ the report

	/** What this machine's fonts make of the document. */
	public static FontReport analyse(WordprocessingMLPackage pkg) {
		return analyse(pkg, FontEnvironment.machine());
	}

	/**
	 * The report: per document font, in order of the text it carries, what the document
	 * uses it for, what docx4j decided to draw it with and why, how close that is, what
	 * the fontTable says about the machine the document was saved on, and what to do.
	 *
	 * <p>Built from the <em>use</em> walk, not from the mapper's whole map: the metric
	 * table maps its 26 fonts whether the document names them or not, and a report about
	 * fonts the document does not use is noise.  A font the conversion met and could not
	 * map ({@code UNMAPPED}), or drew as a symbol font, is added even where the walk did
	 * not see it.</p>
	 *
	 * @param environment the fonts of the deployment being asked about -
	 *        {@link FontEnvironment#machine()} or {@link FontEnvironment#jarsOnly()}
	 * @since 17.1.1
	 */
	public static FontReport analyse(WordprocessingMLPackage pkg, FontEnvironment environment) {

		if (environment==null) environment = FontEnvironment.machine();
		FontUsage usage = usage(pkg);
		Mapper mapper = pkg.getFontMapper();
		Map<String, org.docx4j.wml.Fonts.Font> fontTable = fontTable(pkg);

		List<FontReport.Entry> entries = new ArrayList<FontReport.Entry>();
		Set<String> done = new HashSet<String>();
		for (FontUsage.Use use : usage.byTextCarried()) {
			entries.add(entry(use.getDocumentFont(), use, mapper, fontTable, environment));
			done.add(key(use.getDocumentFont()));
		}
		/* A font the conversion met but no text is set in through the selector: one it
		 * could not map at all, or a symbol font whose characters are w:sym. */
		for (FontDecision decision : mapper.getDecisions()) {
			if (done.contains(key(decision.getDocumentFont()))) continue;
			if (decision.getSource()!=FontDecision.Source.UNMAPPED
					&& decision.getSource()!=FontDecision.Source.SYMBOL) continue;
			entries.add(entry(decision.getDocumentFont(), null, mapper, fontTable, environment));
		}
		return new FontReport(documentName(pkg), environment.getName(), entries, usage);
	}

	private static FontReport.Entry entry(String documentFont, FontUsage.Use use, Mapper mapper,
			Map<String, org.docx4j.wml.Fonts.Font> fontTable, FontEnvironment environment) {

		recordSymbolFontFace(documentFont, mapper);
		FontDecision decision = mapper.decisionFor(documentFont);
		List<String> notes = new ArrayList<String>();
		List<String> uncovered = new ArrayList<String>();
		FontReport.Grade grade = grade(decision, use, notes, uncovered);

		org.docx4j.wml.Fonts.Font entry = fontTable.get(documentFont.trim().toLowerCase(java.util.Locale.ROOT));
		StringBuilder evidence = new StringBuilder();
		FontReport.AuthorHad authorHad = authorHad(documentFont, entry, mapper, evidence);

		// what this environment would draw it with, where it is not what this JVM chose
		String elsewhere = inEnvironment(decision, environment, notes);

		String action = action(documentFont, decision, grade, authorHad, use, uncovered, elsewhere, environment);
		return new FontReport.Entry(use, decision, grade, authorHad, evidence.toString(), action, notes);
	}

	/**
	 * A report asked for before any conversion has to say what a symbol font will be
	 * drawn in: {@link RunFontSelector} takes the face
	 * {@link PhysicalFonts#getWDingsFont} or {@link PhysicalFonts#getSymbolFont} picks,
	 * whatever the mapper made of the name, and only records it as it converts.  Asking
	 * the same two methods here records the same decision.
	 */
	private static void recordSymbolFontFace(String documentFont, Mapper mapper) {

		String symbolFont = RunFontSelector.symbolFontName(documentFont);
		if (symbolFont==null) return;
		FontDecision existing = mapper.getDecision(documentFont);
		if (existing!=null && existing.getSource()==FontDecision.Source.SYMBOL) return;
		PhysicalFont face = "Symbol".equals(symbolFont)
				? PhysicalFonts.getSymbolFont() : PhysicalFonts.getWDingsFont();
		if (face!=null) mapper.recordSymbolFace(documentFont, face);
	}

	// ---- the grade

	private static FontReport.Grade grade(FontDecision decision, FontUsage.Use use, List<String> notes,
			List<String> uncovered) {

		if (decision==null || decision.getSource()==FontDecision.Source.UNMAPPED) return FontReport.Grade.NONE;
		boolean covers = coversWhatTheDocumentUses(decision, use, notes, uncovered);
		switch (decision.getSource()) {
			case INSTALLED:
			case EMBEDDED:
				return covers ? FontReport.Grade.EXACT : FontReport.Grade.NEAR;
			case METRIC_CLONE:
				return covers ? FontReport.Grade.EXACT : FontReport.Grade.NEAR;
			case ALT_NAME:
				/* The document's own alternate is what Word itself falls back to (ECMA-376
				 * 17.8.3.1), so where the machine has the alternate under its own name the
				 * widths are the ones Word used - measured, batch 42 28.1: three such
				 * documents at 0.15 to 1.5 per cent and line parity 1.0000. */
				return alternateIsInstalled(decision) && covers
						? FontReport.Grade.EXACT : FontReport.Grade.NEAR;
			case MAPPER_OWN:
				// a variant of the name is the font itself; a panose guess is not
				return decision.getVia()!=null && decision.getVia().startsWith("a variant of the name")
						? (covers ? FontReport.Grade.EXACT : FontReport.Grade.NEAR)
						: FontReport.Grade.CLASS;
			case MEASURED_STAND_IN:
				return FontReport.Grade.NEAR;
			case CLASS:
			case WORD_DEFAULT:
			case SYMBOL:
			default:
				return FontReport.Grade.CLASS;
		}
	}

	/** Whether the face this font is drawn in can draw every script the document sets in
	 *  it, and has the weights it asks for. */
	private static boolean coversWhatTheDocumentUses(FontDecision decision, FontUsage.Use use,
			List<String> notes, List<String> uncovered) {

		PhysicalFont pf = decision.getPhysicalFont();
		if (pf==null) return false;
		boolean covers = true;

		// what the conversion already found: a script that left this face
		for (FontDecision.ScriptChoice choice : decision.getPerScript()) {
			if (use!=null && !use.getByScript().containsKey(choice.getScript())) continue;
			covers = false;
			uncovered.add(choice.getScript());
			notes.add(choice.getScript() + " is drawn in "
					+ (choice.getFace()==null ? "nothing installed covers it, so it will render as notdef"
							: choice.getFace())
					+ (choice.getError()==null ? "" : " (" + choice.getError() + ")"));
		}
		if (use==null) return covers;

		// and, for a report asked for before any conversion, ask the font itself
		for (String script : use.getScripts()) {
			Integer codePoint = use.sampleCodePoint(script);
			if (codePoint==null || hasScriptNote(notes, script)) continue;
			if (!worthSampling(script, decision)) continue;
			if (!FontFallback.covers(pf, new int[] { codePoint.intValue() })) {
				covers = false;
				uncovered.add(script);
				notes.add(script + " is not in " + pf.getName()
						+ "; the conversion will look for a face which has it");
			}
		}

		// the weights: a synthesised bold is right for a family which has none of its own
		// (Word does the same), and wrong where the document font has a real bold face
		boolean usesBold = use.getFaces().contains(FontUsage.Face.BOLD)
				|| use.getFaces().contains(FontUsage.Face.BOLD_ITALIC);
		if (usesBold && Mapper.SYNTHETIC.equals(decision.getBoldFace())
				&& Mapper.hasBoldFace(use.getDocumentFont())) {
			covers = false;
			notes.add("its bold is synthesised from the regular face, where " + use.getDocumentFont()
					+ " has a bold face of its own");
		}
		return covers;
	}

	/**
	 * Whether to ask the face for a glyph of that group.  Not for a symbol font, whose
	 * characters the selector has already mapped to a face that has them (the sample is
	 * the unmapped private-use code point the document writes, which nothing has); not
	 * for the symbol and emoji blocks, which the selector groups and substitutes on their
	 * own; and not for COMMON or INHERITED, which are the shared characters that keep
	 * their neighbours' font (CR-016 phase 2), so they are never the reason a font is
	 * wrong.  Only real scripts.
	 */
	private static boolean worthSampling(String group, FontDecision decision) {
		if (decision.getSource()==FontDecision.Source.SYMBOL) return false;
		if (FontFallback.SYMBOL_GROUP.equals(group) || FontFallback.EMOJI_GROUP.equals(group)) return false;
		return !"COMMON".equals(group) && !"INHERITED".equals(group) && !"UNKNOWN".equals(group);
	}

	private static boolean hasScriptNote(List<String> notes, String script) {
		for (String note : notes) {
			if (note.startsWith(script + " ")) return true;
		}
		return false;
	}

	private static boolean alternateIsInstalled(FontDecision decision) {
		String alternate = alternateOf(decision);
		return alternate!=null && PhysicalFonts.get(alternate)!=null;
	}

	/** The last hop of an ALT_NAME decision's chain: the font the document says to use
	 *  instead, and so the one to install. */
	private static String alternateOf(FontDecision decision) {
		String via = decision.getVia();
		if (via==null || !via.startsWith("w:altName ")) return null;
		int arrow = via.lastIndexOf("-> ");
		return (arrow>=0 ? via.substring(arrow+3) : via.substring("w:altName ".length())).trim();
	}

	// ---- did the author have it?

	/**
	 * What the fontTable says about the machine the document was saved on.  Word writes
	 * {@code w:panose1} and {@code w:sig} off the font file as it saves, so an entry
	 * carrying them says that machine had the font and a name-only entry says it did not
	 * (CR-017, "This matters because the target is what Word drew for the author").  The
	 * worked cases are corpus documents 9919 - a name-only entry whose {@code w:altName}
	 * Word resolved - and the EnBW document of the 17.1.1 CHANGELOG.
	 */
	static FontReport.AuthorHad authorHad(String documentFont, org.docx4j.wml.Fonts.Font entry,
			Mapper mapper, StringBuilder evidence) {

		if (mapper!=null && mapper.isEmbedded(documentFont)) {
			evidence.append("the document embeds it");
			return FontReport.AuthorHad.EMBEDDED;
		}
		if (entry!=null && (entry.getEmbedRegular()!=null || entry.getEmbedBold()!=null
				|| entry.getEmbedItalic()!=null || entry.getEmbedBoldItalic()!=null)) {
			evidence.append("its w:font entry embeds the font");
			return FontReport.AuthorHad.EMBEDDED;
		}
		if (entry==null) {
			evidence.append("no w:font entry in the font table");
			return FontReport.AuthorHad.UNKNOWN;
		}
		if (entry.getNotTrueType()!=null && entry.getNotTrueType().isVal()) {
			/* Word writes w:notTrueType for a name it has no font file for; read on two
			 * corpus documents (9919's "Nokia Pure Text DFLT" and "Wingdings-Regular",
			 * both with an all-zero w:panose1 beside it). */
			evidence.append("its w:font entry is marked w:notTrueType");
			return FontReport.AuthorHad.UNLIKELY;
		}
		boolean panose = entry.getPanose1()!=null && meaningful(entry.getPanose1().getVal());
		boolean sig = entry.getSig()!=null && (meaningful(entry.getSig().getUsb0())
				|| meaningful(entry.getSig().getCsb0()));

		if (panose || sig) {
			evidence.append(panose && sig ? "w:panose1 and w:sig" : (panose ? "w:panose1" : "w:sig"))
					.append(" in its w:font entry, read off the font file by the Word that saved it"
							+ " (possibly an earlier save)");
			return FontReport.AuthorHad.LIKELY;
		}
		evidence.append("its w:font entry names it and carries no usable w:panose1 or w:sig,"
				+ " which Word writes only for a font it has");
		return FontReport.AuthorHad.UNLIKELY;
	}

	/** A panose or signature of all zeros is Word saying it knows nothing, not evidence
	 *  (read on corpus 9919, whose "Nokia Pure Text DFLT" entry carries
	 *  {@code w:panose1 w:val="00000000000000000000"} beside {@code w:notTrueType}). */
	private static boolean meaningful(String hex) {
		if (hex==null) return false;
		for (int i=0; i<hex.length(); i++) {
			char c = hex.charAt(i);
			if (c!='0' && !Character.isWhitespace(c)) return true;
		}
		return false;
	}

	/** The same, for a w:panose1, which JAXB binds as bytes. */
	private static boolean meaningful(byte[] panose) {
		if (panose==null) return false;
		for (byte b : panose) {
			if (b!=0) return true;
		}
		return false;
	}

	// ---- what this environment would do

	/** Where the environment asked about does not have the face this JVM chose, the face
	 *  it would use instead, as a note; null where it has it. */
	private static String inEnvironment(FontDecision decision, FontEnvironment environment, List<String> notes) {

		PhysicalFont pf = decision.getPhysicalFont();
		if (pf==null || environment.has(pf.getName())) return null;
		FontSubstitutionTable.Row row = FontSubstitutionTable.rowFor(decision.getDocumentFont());
		String instead = null;
		if (row!=null) {
			for (String candidate : row.substituteNames()) {
				if (environment.has(candidate)) { instead = candidate; break; }
			}
		}
		notes.add(environment.getName() + " does not have " + pf.getName() + "; there it would be drawn in "
				+ (instead==null ? "the document's default font" : instead));
		return instead==null ? "" : instead;
	}

	// ---- what to do about it

	private static String action(String documentFont, FontDecision decision, FontReport.Grade grade,
			FontReport.AuthorHad authorHad, FontUsage.Use use, List<String> uncovered, String elsewhere,
			FontEnvironment environment) {

		if (decision==null) return "";
		if (authorHad==FontReport.AuthorHad.UNLIKELY && decision.getSource()==FontDecision.Source.WORD_DEFAULT) {
			/* Word itself could not find it either, and substituted; imitating a
			 * rendering that never existed would move the output away from the author's
			 * page (CR-017, "This matters because"). */
			String family = decision.getVia()==null ? "its own default"
					: decision.getVia().substring(decision.getVia().lastIndexOf(": ")+2);
			return "nothing to do: the Word that saved this document had no " + documentFont
					+ " either, and drew it in " + family + ", which is what docx4j does; installing "
					+ documentFont + " would move the output away from the page its author saw";
		}
		if (grade==FontReport.Grade.EXACT) {
			return elsewhere==null ? "" : getIt(nameOf(decision), environment);
		}

		StringBuilder sb = new StringBuilder();
		if (!uncovered.isEmpty()) {
			/* The script is the reason, and the script is what to say: "Greek in a Cambria
			 * document: install Cambria; Caladea has no Greek and P052 is 2.6% out". */
			String script = uncovered.get(0);
			sb.append("install ").append(documentFont).append(" for its ")
					.append(script.toLowerCase(java.util.Locale.ROOT)).append(": ")
					.append(nameOf(decision)).append(" has none of it");
			String standIn = standInFor(documentFont, script, decision, environment);
			if (standIn!=null) sb.append(", and ").append(standIn);
			return sb.toString();
		}
		if (decision.getSource()==FontDecision.Source.ALT_NAME) {
			/* The document itself names the alternate, and Word uses it (ECMA-376
			 * 17.8.3.1), so the font to install is the alternate, not the name the document
			 * asks for. */
			String alternate = alternateOf(decision);
			sb.append("install ").append(alternate==null ? documentFont : alternate);
			if (alternate!=null) {
				sb.append(", which this document names as its alternate for ").append(documentFont);
			}
			if (elsewhere!=null && FontSubstitutionTable.cloneNamed(nameOf(decision))!=null) {
				sb.append("; ").append(getIt(nameOf(decision), environment));
			}
			return sb.toString();
		}
		sb.append("install ").append(documentFont);
		FontSubstitutionTable.Clone clone = FontSubstitutionTable.cloneNamed(nameOf(decision));
		FontSubstitutionTable.Row row = FontSubstitutionTable.rowFor(documentFont);
		FontSubstitutionTable.Substitute metric = null;
		if (row!=null) {
			for (FontSubstitutionTable.Substitute s : row.getSubstitutes()) {
				if ("metric".equals(s.getQuality())) { metric = s; break; }
			}
		}
		if (metric!=null && !metric.getFont().equalsIgnoreCase(nameOf(decision))) {
			FontSubstitutionTable.Clone metricClone = FontSubstitutionTable.cloneNamed(metric.getFont());
			sb.append(", or ").append(metric.getFont()).append(" its metric clone");
			if (metricClone!=null) sb.append(" (").append(where(metricClone)).append(")");
		} else if (grade==FontReport.Grade.NEAR) {
			// the measurement is in the line already (Entry.describe names it)
			sb.append(" for Word's line breaks: no open clone of it exists, and ")
					.append(nameOf(decision)).append(" is the closest measured");
		} else if (decision.getSource()==FontDecision.Source.SYMBOL) {
			sb.append(": its characters are drawn in ").append(nameOf(decision))
					.append(", whose symbols are not its own");
		} else if (decision.getSource()==FontDecision.Source.WORD_DEFAULT) {
			sb.append(": no metrics or clone for it, so it is drawn in Word's own default for a font"
					+ " it cannot find; lines and line heights will differ");
		} else if (grade==FontReport.Grade.CLASS) {
			sb.append(": no open clone; ").append(nameOf(decision)).append(" stands in, a face of the"
					+ " same class");
			String known = decision.getWidthError();
			if (known!=null && !Mapper.UNKNOWN_ERROR.equals(known)) sb.append(" (").append(known).append(")");
			sb.append("; lines will break where Word's did not");
			if ("documentFont".equals(decision.getLineBox())) {
				sb.append(", though the line heights are Word's");
			} else {
				sb.append(", and the line heights may too");
			}
		} else if (grade==FontReport.Grade.NONE) {
			sb.append(", or map it yourself (Mapper.put): nothing installed was close enough to stand in,"
					+ " and FOP will draw it in its base-14 fallback");
		}
		if (clone!=null && elsewhere!=null) {
			sb.append("; ").append(getIt(nameOf(decision), environment));
		}
		if (use!=null && !use.getScripts().isEmpty() && decision.getPerScript().size()>0) {
			for (FontDecision.ScriptChoice choice : decision.getPerScript()) {
				if (!use.getByScript().containsKey(choice.getScript())) continue;
				sb.append("; its ").append(choice.getScript().toLowerCase(java.util.Locale.ROOT))
						.append(" needs it too (")
						.append(choice.getFace()==null ? "nothing installed covers that script"
								: choice.getFace() + " stands in")
						.append(")");
				break;
			}
		}
		return sb.toString();
	}

	/** What stands in for one script: the choice the conversion made where it has run,
	 *  else the table's measured row for that font and script. */
	private static String standInFor(String documentFont, String script, FontDecision decision,
			FontEnvironment environment) {

		for (FontDecision.ScriptChoice choice : decision.getPerScript()) {
			if (!script.equals(choice.getScript())) continue;
			if (choice.getFace()==null) return "nothing installed covers that script, so it will render as notdef";
			return choice.getFace() + " stands in"
					+ (choice.getError()==null ? "" : " (" + choice.getError() + ")");
		}
		for (FontSubstitutionTable.Row row : FontSubstitutionTable.scriptSubstitutes()) {
			if (!script.equals(row.getScript()) || !row.matches(documentFont)) continue;
			for (FontSubstitutionTable.Substitute s : row.getSubstitutes()) {
				if (!environment.has(s.getFont())) continue;
				return s.getFont() + " stands in" + (s.getError()==null ? "" : " (" + s.getError() + ")");
			}
		}
		return null;
	}

	/** "add the docx4j-export-fo-fonts-crosextra jar, or for example the package ..." */
	private static String getIt(String physicalFontName, FontEnvironment environment) {
		FontSubstitutionTable.Clone clone = FontSubstitutionTable.cloneNamed(physicalFontName);
		if (clone==null) return "";
		return environment.getName() + " lacks " + clone.getName() + ": " + where(clone);
	}

	private static String where(FontSubstitutionTable.Clone clone) {
		StringBuilder sb = new StringBuilder();
		if (clone.getLicence()!=null) sb.append(clone.getLicence()).append("; ");
		if (clone.getJar()!=null) {
			sb.append("the ").append(clone.getJar()).append(" jar");
			if (clone.getPackages()!=null) sb.append(", or for example ").append(clone.getPackages());
		} else if (clone.getPackages()!=null) {
			sb.append("for example ").append(clone.getPackages());
		}
		return sb.toString();
	}

	/** The face's name as a user would look for it; see FontReport.Entry.name. */
	private static String nameOf(FontDecision decision) {
		return decision.getPhysicalFont()==null ? ""
				: PhysicalFonts.stripSuffixes(decision.getPhysicalFont().getName());
	}

	/** What to call the document in the report: its title where the properties carry one
	 *  (a package has no file name of its own). */
	private static String documentName(WordprocessingMLPackage pkg) {
		try {
			if (pkg.getDocPropsCorePart()==null) return null;
			org.docx4j.docProps.core.CoreProperties props = pkg.getDocPropsCorePart().getJaxbElement();
			if (props==null || props.getTitle()==null) return null;
			StringBuilder title = new StringBuilder();
			for (Object content : props.getTitle().getValue().getContent()) {
				if (content instanceof String) title.append((String)content);
			}
			return title.toString().trim().length()==0 ? null : title.toString().trim();
		} catch (Exception e) {
			return null;
		}
	}

	private static Map<String, org.docx4j.wml.Fonts.Font> fontTable(WordprocessingMLPackage pkg) {
		try {
			org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart part
					= pkg.getMainDocumentPart().getFontTablePart();
			return Mapper.fontTable(part==null ? null : (org.docx4j.wml.Fonts)part.getJaxbElement());
		} catch (Exception e) {
			log.warn("font table: " + e.getMessage());
			return Mapper.fontTable(null);
		}
	}

	private static String key(String name) {
		return name==null ? "" : name.trim().toLowerCase(java.util.Locale.ROOT);
	}

	// ------------------------------------------------------------------ the conversion log

	/** {@code docx4j.fonts.report.log} = {@code summary} (the default: one line per
	 *  document font), {@code full} (the whole report) or {@code off}.  @since 17.1.1 */
	public static final String LOG_PROPERTY = "docx4j.fonts.report.log";

	/**
	 * Log what this conversion made of the document's fonts, once: one line per document
	 * font, at INFO where the font itself or a metric clone draws it and at WARN where a
	 * user could do something about it, with what to do in the line.
	 *
	 * <p>Called by {@code Docx4J.toFO} (and so by {@code toPDF} through it) after the
	 * export, so the per-script choices the selector made during it are in the report.
	 * Until 17.1.1 a conversion said nothing about its fonts except a DEBUG line per
	 * mapping and FOP's own "font not found" warning, which names no action (CR-017
	 * gaps 2 and 3).</p>
	 *
	 * @since 17.1.1
	 */
	public static void logReport(Object pkg) {

		String mode = org.docx4j.Docx4jProperties.getProperty(LOG_PROPERTY, "summary");
		mode = mode==null ? "summary" : mode.trim().toLowerCase(java.util.Locale.ROOT);
		if ("off".equals(mode)) return;
		if (!(pkg instanceof WordprocessingMLPackage)) return;
		if (!log.isInfoEnabled() && !log.isWarnEnabled()) return;

		try {
			FontReport report = analyse((WordprocessingMLPackage)pkg);
			if ("full".equals(mode)) {
				log.info(report.toText());
				return;
			}
			for (FontReport.Entry entry : report.getEntries()) {
				if (entry.getGrade()==FontReport.Grade.EXACT) {
					log.info(entry.getSummaryLine());
				} else {
					log.warn(entry.getSummaryLine());
				}
			}
		} catch (Exception e) {
			log.warn("Couldn't report on this document's fonts: " + e.getMessage());
		}
	}

	// ------------------------------------------------------------------ the command line

	/**
	 * {@code java -cp ... org.docx4j.fonts.FontsAnalysis in.docx [--jars-only] [--json]}
	 *
	 * <p>Put docx4j-export-fo's font jars on the classpath for {@code --jars-only} to
	 * have anything to report.</p>
	 */
	public static void main(String[] args) throws Exception {

		String in = null;
		boolean jarsOnly = false, json = false;
		for (String arg : args) {
			if ("--jars-only".equals(arg)) jarsOnly = true;
			else if ("--json".equals(arg)) json = true;
			else if (in==null) in = arg;
		}
		if (in==null) {
			System.out.println("usage: FontsAnalysis in.docx [--jars-only] [--json]");
			return;
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new java.io.File(in));
		FontReport report = analyse(pkg, jarsOnly ? FontEnvironment.jarsOnly() : FontEnvironment.machine());
		System.out.println(json ? report.toJson() : report.toText());
	}

	/** {@link RunFontSelector} needs a visitor; {@code documentFontFor} never calls it. */
	private static final RunFontSelector.RunFontCharacterVisitor NO_OP_VISITOR
			= new RunFontSelector.RunFontCharacterVisitor() {
		public void setDocument(org.w3c.dom.Document document) {}
		public boolean isReusable() { return true; }
		public void addCharacterToCurrent(char c) {}
		public void addCodePointToCurrent(int cp) {}
		public void finishPrevious() {}
		public void createNew() {}
		public void setMustCreateNewFlag(boolean val) {}
		public void fontAction(String fontname) {}
		public Object getResult() { return null; }
		public void setRunFontSelector(RunFontSelector rfs) {}
		public void setFallbackFont(String fontname) {}
	};
}
