/*
 * Copyright 2026, Plutext Pty Ltd.
 * This file is part of docx4j.
 */
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.model.text.*;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/** Each file through each reader, a fresh load each, and through a save. One tab-separated line a step. */
public class Repro {
	interface Step { String run(WordprocessingMLPackage p) throws Exception; }

	static String one(String s) { return s == null ? "" : s.replaceAll("\\s+", " ").trim(); }

	static String fail(Throwable t) {
		Throwable c = t;
		while (c.getCause() != null && c.getCause() != c) c = c.getCause();
		String at = "";
		for (StackTraceElement e : c.getStackTrace()) {
			if (e.getClassName().startsWith("org.docx4j")) { at = e.getClassName().replace("org.docx4j.", "") + ":" + e.getLineNumber(); break; }
		}
		return "FAILED " + c.getClass().getSimpleName() + " at " + at + " " + one(String.valueOf(c.getMessage())).replaceAll("(.{90}).*", "$1");
	}

	static void step(String file, String name, Step s) {
		System.err.println("@@@ " + file + " " + name);
		String r;
		try {
			WordprocessingMLPackage p = WordprocessingMLPackage.load(new File(file));
			r = s.run(p);
		} catch (Throwable t) {
			r = fail(t);
		}
		System.out.println(new File(file).getName().replace(".docx", "") + "\t" + name + "\t" + one(r));
	}

	static String saved(WordprocessingMLPackage p) throws Exception {
		p.getMainDocumentPart().getJaxbElement();
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		Docx4J.save(p, b);
		try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(b.toByteArray()))) {
			for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
				if (e.getName().equals("word/document.xml")) {
					String x = new String(z.readAllBytes(), StandardCharsets.UTF_8);
					java.util.regex.Matcher m = java.util.regex.Pattern.compile("<(?!\\?)([A-Za-z0-9:]+)").matcher(x);
					String root = m.find() ? m.group(1) : "?";
					StringBuilder t = new StringBuilder();
					java.util.regex.Matcher w = java.util.regex.Pattern.compile("<w:t[ >][^<]*").matcher(x);
					while (w.find()) t.append(w.group().replaceAll("^<w:t[^>]*>", "")).append(" | ");
					int bodies = x.split("<w:body[ >]", -1).length - 1;
					int docs = x.split("<w:document[ >]", -1).length - 1;
					return "root=" + root + " documents=" + docs + " bodies=" + bodies + " text=" + t;
				}
			}
		}
		return "no document.xml";
	}

	public static void main(String[] a) throws Exception {
		File[] files = new File(a[0]).listFiles((d, n) -> n.endsWith(".docx"));
		Arrays.sort(files);
		for (File f : files) {
			String n = f.getPath();
			step(n, "type", p -> {
				Object o = p.getMainDocumentPart().getJaxbElement();
				return (o == null ? "null" : o.getClass().getName()) + " resolved=" + p.getMainDocumentPart().getResolvedAlternateContent().size();
			});
			step(n, "texts", p -> Texts.plainText(p, new TextOptions()));
			step(n, "textutils", p -> { StringWriter w = new StringWriter(); TextUtils.extractText(p.getMainDocumentPart().getJaxbElement(), w); return w.toString(); });
			step(n, "getContent", p -> { List<Object> c = p.getMainDocumentPart().getContent(); return c == null ? "null" : c.size() + " items"; });
			step(n, "html", p -> { ByteArrayOutputStream b = new ByteArrayOutputStream(); Docx4J.toHTML(p, null, null, b); String h = b.toString("UTF-8"); return h.replaceAll("(?s)<style.*?</style>", "").replaceAll("<[^>]+>", " "); });
			step(n, "save", Repro::saved);
		}
	}
}
