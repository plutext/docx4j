/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.fidelity.corpus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Copies of corpus documents whose fonts neither machine can draw alike are rewritten to faces
 * both have, so that their goldens, cut from the copies, are class 2 (RULE-CLASSES.md; the
 * layout fidelity plan, docx4j-portfolio docs/layout_fidelity_plan.md section 4).
 *
 * <p><b>Which documents.</b> The left-to-right European documents of classes 2n, 3a and 3b in a
 * baseline's classes.txt (per corpus, the harness's own class evidence), with a font to rewrite:
 * every font docx4j substitutes that is not among the Windows VM's common fonts (which are copied
 * to the scoring machine instead: Cambria, Tahoma, Calibri Light, Verdana and the rest of
 * {@link #VM_COMMON}), and in a 3b document every font Word lacked as well; and then, in such a
 * document, whatever else docx4j's FontsAnalysis does not grade EXACT (East Asian faces in
 * w:eastAsia slots, names the evidence did not list), so the copy is complete.  A 2n or 3a document
 * whose only substitutions are VM fonts is left alone: the font copy makes it class 2.</p>
 *
 * <p><b>Which fonts become what.</b> Only the fonts named above, in each document; everything else
 * is left exactly as it is.  By metric twin where the name says which face it is (Calibri ->
 * Carlito, Arial and Helvetica -> Liberation Sans, Times -> Liberation Serif,
 * Courier -> Liberation Mono, PostScript and localised names included; a PostScript or styled
 * spelling of a VM font, Cambria's included, to that font), otherwise by the font
 * table's w:family (swiss, roman, modern), otherwise by name; symbol faces go to DejaVu Sans.</p>
 *
 * <p><b>Where.</b> Every XML part under word/ - document, styles (docDefaults included),
 * numbering, headers, footers, notes, comments, the theme's major and minor fonts, the font
 * table, charts and diagrams - at the attribute level: w:rFonts' w:ascii, w:hAnsi, w:cs and
 * w:eastAsia; DrawingML's typeface; the font table's w:name, where a renamed entry loses its
 * w:altName and a second entry of one target name is dropped; VML's font-family.  The parts are
 * rewritten as text, not round-tripped through JAXB, so nothing else in the package changes;
 * after the rewrite the copy is searched for the old names, and any left over is reported.</p>
 *
 * <p>Usage: {@code FontRewrite <baselineScoreDirName> <outCorpusDir> <corpusRoot>...}, where each
 * corpus root is a ~/fidelity-&lt;c&gt; directory holding score/&lt;baseline&gt;/classes.txt and
 * resaved-nofields/; the copies are made from Word's re-save.  A manifest,
 * font-rewrite-manifest.tsv, records each document's mapping.</p>
 *
 * @since 17.3.1
 */
public final class FontRewrite {

	/** The Windows VM's fonts which are copied to the scoring machine rather than rewritten
	 *  (the plan's section 4 table), lower case.  Symbol, Wingdings and Webdings are mapped by
	 *  docx4j's SymbolMapper and drawn from its symbol jar. */
	static final Set<String> VM_COMMON = lower("Cambria", "Tahoma", "Calibri Light", "Verdana",
			"Trebuchet MS", "Sylfaen", "Arial Black", "Consolas", "Segoe UI", "Cambria Math",
			"Georgia", "Garamond", "Comic Sans MS", "Book Antiqua", "Bookman Old Style",
			"Wingdings", "Wingdings 2", "Wingdings 3", "Symbol", "Webdings");

	static final String SANS = "Liberation Sans", SERIF = "Liberation Serif", MONO = "Liberation Mono";

	/** European languages, by the locale's language subtag in a corpus file name. */
	static final Set<String> EUROPEAN = new HashSet<>(Arrays.asList(("bg bs ca cs cy da de el en es et eu fi fr ga gl "
			+ "hr hu is it lb lt lv mk mt nb nl nn no pl pt ro ru sk sl sq sr sv tr uk be").split(" ")));

	/** Right-to-left, Indic, Thai/Lao, Hangul and CJK text. */
	static final Pattern NON_LTR = Pattern.compile("[֐-ࣿיִ-﷿ﹰ-﻿ऀ-෿"
			+ "฀-໿ᄀ-ᇿ⺀-⿟぀-ヿ㄀-ㇿ㐀-䶿一-鿿"
			+ "ꥠ-꥿가-힯豈-﫿]");

	private FontRewrite() {}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("usage: FontRewrite <baselineScoreDirName> <outCorpusDir> <corpusRoot>...");
			System.exit(2);
		}
		String baseline = args[0];
		File out = new File(args[1]);
		out.mkdirs();
		int written = 0, leftovers = 0;
		try (PrintWriter manifest = new PrintWriter(new File(out, "font-rewrite-manifest.tsv"), "UTF-8")) {
			manifest.println("id\tcorpus\tclass\tmapping\tleft over");
			for (int i = 2; i < args.length; i++) {
				File root = new File(args[i]);
				File classes = new File(root, "score/" + baseline + "/classes.txt");
				for (String line : Files.readAllLines(classes.toPath(), StandardCharsets.UTF_8)) {
					String[] f = line.trim().split("\\s+");
					if (f.length < 2) continue;
					String id = f[0], cls = f[1];
					if (!cls.equals("2n") && !cls.equals("3a") && !cls.equals("3b")) continue;
					File src = new File(root, "resaved-nofields/" + id + ".docx");
					if (!src.exists() || !ltrEuropean(id, src)) continue;
					Set<String> fonts = fontsToRewrite(line, cls);
					if (fonts.isEmpty()) continue;
					// complete the set: whatever else docx4j cannot draw as it is (an East Asian face
					// in a w:eastAsia slot, a name the harness's evidence did not list)
					fonts.addAll(notExact(src));
					Map<String, String> mapping = mapping(src, fonts);
					File dst = new File(out, id + ".docx");
					Set<String> left = rewrite(src, dst, mapping);
					written++;
					if (!left.isEmpty()) leftovers++;
					manifest.println(id + "\t" + root.getName() + "\t" + cls + "\t" + mapping + "\t" + left);
					System.out.println(id + "  " + mapping + (left.isEmpty() ? "" : "  LEFT OVER " + left));
				}
			}
		}
		System.out.println("wrote " + written + " copies to " + out + " (" + leftovers + " with a name left over)");
	}

	/** A document of a European locale whose main part holds no right-to-left, CJK or Indic
	 *  text and no w:bidi / w:rtl: the plan's section 1. */
	static boolean ltrEuropean(String id, File docx) throws IOException {
		Matcher m = Pattern.compile("^\\d+_([a-z]{2,3})(?:-[A-Za-z]+)*_").matcher(id);
		if (!m.find() || !EUROPEAN.contains(m.group(1))) return false;
		String xml = new String(entry(docx, "word/document.xml"), StandardCharsets.UTF_8);
		Matcher on = Pattern.compile("<w:(bidi|rtl)(\\s[^>]*)?/?>").matcher(xml);
		while (on.find()) {
			String attrs = on.group(2) == null ? "" : on.group(2);
			if (!attrs.matches(".*w:val=\"(0|false)\".*")) return false;
		}
		StringBuilder text = new StringBuilder();
		Matcher t = Pattern.compile("<w:t(?:\\s[^>]*)?>([^<]*)</w:t>").matcher(xml);
		while (t.find()) text.append(t.group(1));
		return !NON_LTR.matcher(text).find();
	}

	/**
	 * The fonts of one classes.txt line to rewrite: docx4j's substitutions
	 * ({@code docx4j=Name:KIND,...}) that are not VM fonts, and in a 3b document Word's missing
	 * fonts ({@code wordMissing=Name(n),...}) too.  Names may hold spaces and commas never.
	 */
	static Set<String> fontsToRewrite(String line, String cls) {
		Set<String> fonts = new TreeSet<>();
		// a name may hold commas (a PostScript "TrebuchetMS,Bold"), so the lists are read by
		// their markers: docx4j=Name:KIND,Name:KIND and wordMissing=Name(n),Name(n)
		Matcher d = Pattern.compile("docx4j=(.*)$").matcher(line.trim());
		if (d.find()) {
			Matcher p = Pattern.compile("\\G(.+?):(?:NEAR|CLASS|NONE|EXACT)(?:,|$)").matcher(d.group(1));
			while (p.find()) {
				String name = p.group(1).trim();
				if (!name.isEmpty() && !VM_COMMON.contains(name.toLowerCase(Locale.ROOT))) fonts.add(name);
			}
		}
		if ("3b".equals(cls)) {
			Matcher w = Pattern.compile("wordMissing=(.*?)(?:\\s{2,}docx4j=|$)").matcher(line.trim());
			if (w.find()) {
				Matcher p = Pattern.compile("\\G(.+?)\\(\\d+\\)(?:,|$)").matcher(w.group(1));
				while (p.find()) {
					String name = p.group(1).trim();
					if (!name.isEmpty()) fonts.add(name);
				}
			}
		}
		return fonts;
	}

	/** The document fonts docx4j's FontsAnalysis does not grade EXACT on this machine, bar the
	 *  VM fonts, the symbol faces and the faces this rewrite maps to. */
	static Set<String> notExact(File docx) throws Exception {
		Set<String> s = new TreeSet<>();
		org.docx4j.openpackaging.packages.WordprocessingMLPackage pkg =
				org.docx4j.openpackaging.packages.WordprocessingMLPackage.load(docx);
		org.docx4j.fonts.FontReport report = org.docx4j.fonts.FontsAnalysis.analyse(pkg);
		for (org.docx4j.fonts.FontReport.Entry e : report.getEntries()) {
			String name = e.getDocumentFont();
			if (name == null || e.getGrade() == org.docx4j.fonts.FontReport.Grade.EXACT) continue;
			String lower = name.toLowerCase(Locale.ROOT);
			if (VM_COMMON.contains(lower) || lower.startsWith("liberation") || lower.startsWith("dejavu")
					|| lower.equals("carlito") || lower.equals("caladea")) {
				continue;
			}
			s.add(name);
		}
		return s;
	}

	/** Each font's target face. */
	static Map<String, String> mapping(File docx, Set<String> fonts) throws IOException {
		Map<String, String> families = fontTableFamilies(docx);
		Map<String, String> m = new LinkedHashMap<>();
		for (String font : fonts) {
			String t = target(font, families.get(font));
			// a VM font Word itself lacked in this document (a 3b "Arial Black") goes by its class
			if (t.equalsIgnoreCase(font)) t = byClass(font.toLowerCase(Locale.ROOT).replaceAll("[\\s_,-]", ""),
					families.get(font));
			m.put(font, t);
		}
		return m;
	}

	/**
	 * The face both machines have for a font neither draws alike: its metric twin where the name
	 * says which face it is, else by the font table's w:family, else by name.
	 */
	static String target(String font, String family) {
		String n = font.toLowerCase(Locale.ROOT).replaceAll("[\\s_,-]", "");
		// a PostScript or style-suffixed spelling of a VM font is that font, which both
		// machines will have ("TrebuchetMS,Bold", "Wingdings-Regular")
		String bare = n.replaceAll("(psmt|mt|regular|bold|italic|oblique)+$", "");
		for (String vm : VM_COMMON) {
			if (vm.replaceAll("\\s", "").equals(bare)) return properName(vm);
		}
		if (n.startsWith("calibri")) return "Carlito";
		// Caladea is not Cambria's metric twin (docx4j measures it 4.9% narrower and corrects
		// it), and Cambria is one of the VM fonts the scoring machine gets: keep the family
		if (n.startsWith("cambria")) return "Cambria";
		if (n.startsWith("arial") || n.startsWith("helv") || n.contains("helvetica")) return SANS;
		if (n.startsWith("times") || n.startsWith("mtimes") || n.contains("timesnewroman")
				|| n.startsWith("tmsrmn")) {
			return SERIF;
		}
		if (n.startsWith("courier")) return MONO;
		if (n.contains("symbol") || n.contains("wingding") || n.contains("dings") || n.contains("imb")) {
			return "DejaVu Sans";
		}
		// sans families whose names say so, before "Roman" in "Univers55Roman" can mislead
		for (String sans : new String[] { "univers", "frutiger", "sans", "roboto", "lato", "myriad",
				"proxima", "segoe", "tahoma", "verdana", "futura", "gillsans", "din" }) {
			if (n.startsWith(sans)) return SANS;
		}
		if (n.contains("sans")) return SANS;
		// East Asian faces, by name: their font table entries are often "modern" (fixed pitch)
		if (n.contains("mincho") || n.contains("ming") || n.contains("song")) return SERIF;
		if (n.contains("gothic") || n.contains("hei") || n.contains("gulim") || n.contains("meiryo")) return SANS;
		return byClass(n, family);
	}

	/** A font's class, from the font table (w:pitch fixed is mono; w:family swiss sans, roman
	 *  serif, modern - constant stroke, not fixed pitch - sans), else from its name. */
	static String byClass(String n, String family) {
		if (family != null && family.endsWith("/fixed")) return MONO;
		String fam = family == null ? null : family.replaceFirst("/.*$", "");
		if ("swiss".equals(fam) || "modern".equals(fam)) return SANS;
		if ("roman".equals(fam)) return SERIF;
		if ((n.contains("mono") && !n.startsWith("monotype")) || n.contains("console") || n.contains("consolas")) {
			return MONO;
		}
		if (n.contains("mincho") || n.contains("serif") || n.contains("garamond") || n.contains("georgia")
				|| n.contains("book") || n.contains("palatino") || n.contains("minion") || n.contains("roman")
				|| n.contains("antiqua") || n.contains("century") || n.contains("corsiva")) {
			return SERIF;
		}
		return SANS;
	}

	/** A VM font's name as Windows spells it. */
	private static String properName(String lower) {
		for (String n : new String[] { "Cambria", "Tahoma", "Calibri Light", "Verdana", "Trebuchet MS",
				"Sylfaen", "Arial Black", "Consolas", "Segoe UI", "Cambria Math", "Georgia", "Garamond",
				"Comic Sans MS", "Book Antiqua", "Bookman Old Style", "Wingdings", "Wingdings 2",
				"Wingdings 3", "Symbol", "Webdings" }) {
			if (n.toLowerCase(Locale.ROOT).equals(lower)) return n;
		}
		return lower;
	}

	/** The font table's w:family and w:pitch per w:name, as "family/pitch". */
	static Map<String, String> fontTableFamilies(File docx) throws IOException {
		Map<String, String> families = new LinkedHashMap<>();
		byte[] bytes = entry(docx, "word/fontTable.xml");
		if (bytes == null) return families;
		String xml = new String(bytes, StandardCharsets.UTF_8);
		Matcher f = Pattern.compile("<w:font w:name=\"([^\"]*)\"[^>]*>(.*?)</w:font>", Pattern.DOTALL).matcher(xml);
		while (f.find()) {
			Matcher fam = Pattern.compile("<w:family w:val=\"([^\"]*)\"").matcher(f.group(2));
			Matcher pitch = Pattern.compile("<w:pitch w:val=\"([^\"]*)\"").matcher(f.group(2));
			String v = fam.find() ? fam.group(1) : "";
			if (pitch.find()) v += "/" + pitch.group(1);
			families.put(unescape(f.group(1)), v);
		}
		return families;
	}

	/**
	 * Writes the copy: every XML part under word/ rewritten as text, everything else copied as
	 * it is, in the same order.  Returns the old names still found in the copy's XML.
	 */
	static Set<String> rewrite(File src, File dst, Map<String, String> mapping) throws IOException {
		Set<String> left = new LinkedHashSet<>();
		try (ZipInputStream in = new ZipInputStream(new FileInputStream(src));
				ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(dst))) {
			ZipEntry e;
			while ((e = in.getNextEntry()) != null) {
				byte[] bytes = readAll(in);
				String name = e.getName();
				if (name.startsWith("word/") && name.endsWith(".xml")) {
					String xml = new String(bytes, StandardCharsets.UTF_8);
					// the glossary document has a font table of its own
					String rewritten = rewriteXml(xml, mapping, name.endsWith("/fontTable.xml"));
					Matcher v = Pattern.compile("(?:w:ascii|w:hAnsi|w:cs|w:eastAsia|typeface|w:name)=\"([^\"]*)\"")
							.matcher(rewritten);
					while (v.find()) {
						String value = unescape(v.group(1));
						if (targetOf(value, mapping) != null && !mapping.containsValue(value)) {
							left.add(value + " in " + name);
						}
					}
					bytes = rewritten.getBytes(StandardCharsets.UTF_8);
				}
				ZipEntry o = new ZipEntry(name);
				zout.putNextEntry(o);
				zout.write(bytes);
				zout.closeEntry();
			}
		}
		return left;
	}

	/** One part's text with the fonts renamed; in the font table, renamed entries lose their
	 *  w:altName and a second entry of the same name is dropped. */
	static String rewriteXml(String xml, Map<String, String> mapping, boolean fontTable) {
		// one pass over every font-naming attribute; a value matches as written or through its
		// English name (a theme's <a:font script="Jpan" typeface="ＭＳ 明朝"/> is MS Mincho)
		StringBuffer sb0 = new StringBuffer();
		Matcher a = Pattern.compile("((?:w:ascii|w:hAnsi|w:cs|w:eastAsia|typeface"
				+ (fontTable ? "|<w:font w:name" : "") + ")=\")([^\"]*)(\")").matcher(xml);
		while (a.find()) {
			String target = targetOf(unescape(a.group(2)), mapping);
			a.appendReplacement(sb0, Matcher.quoteReplacement(target == null ? a.group(0)
					: a.group(1) + escape(target) + a.group(3)));
		}
		a.appendTail(sb0);
		String s = sb0.toString();
		for (Map.Entry<String, String> m : mapping.entrySet()) {
			String from = Pattern.quote(escape(m.getKey()));
			String to = Matcher.quoteReplacement(escape(m.getValue()));
			s = s.replaceAll("(font-family:\\s*(?:&quot;|'|\"?))" + from + "((?:&quot;|'|\")?)", "$1" + to + "$2");
		}
		if (fontTable) {
			Set<String> renamed = new HashSet<>(mapping.values());
			Set<String> seen = new HashSet<>();
			StringBuffer sb = new StringBuffer();
			Matcher f = Pattern.compile("<w:font w:name=\"([^\"]*)\"[^>]*>.*?</w:font>|<w:font w:name=\"([^\"]*)\"[^>]*/>",
					Pattern.DOTALL).matcher(s);
			while (f.find()) {
				String name = unescape(f.group(1) != null ? f.group(1) : f.group(2));
				String entry = f.group(0);
				if (!seen.add(name)) {
					entry = "";  // a second entry of one name
				} else if (renamed.contains(name)) {
					entry = entry.replaceAll("<w:altName [^>]*/>", "");
				}
				f.appendReplacement(sb, Matcher.quoteReplacement(entry));
			}
			f.appendTail(sb);
			s = sb.toString();
		}
		return s;
	}

	/** The target for a font-naming attribute's value, or null where it is not rewritten. */
	static String targetOf(String value, Map<String, String> mapping) {
		if (mapping.containsKey(value)) return mapping.get(value);
		String english = org.docx4j.fonts.CJKToEnglish.toEnglish(value);
		return english == null ? null : mapping.get(english);
	}

	static byte[] entry(File docx, String name) throws IOException {
		try (ZipInputStream in = new ZipInputStream(new FileInputStream(docx))) {
			ZipEntry e;
			while ((e = in.getNextEntry()) != null) {
				if (e.getName().equals(name)) return readAll(in);
			}
		}
		return null;
	}

	private static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[65536];
		int n;
		while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
		return bos.toByteArray();
	}

	static String escape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	static String unescape(String s) {
		return s.replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
	}

	private static Set<String> lower(String... names) {
		Set<String> s = new HashSet<>();
		for (String n : names) s.add(n.toLowerCase(Locale.ROOT));
		return s;
	}
}
