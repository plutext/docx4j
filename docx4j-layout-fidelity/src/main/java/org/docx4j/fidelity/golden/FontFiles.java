/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.fontbox.ttf.FontHeaders;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

/**
 * The font files behind the faces a golden set's PDFs embed, by SHA-256: what a face name
 * cannot say.
 *
 * <p>Two machines can both draw "Aptos" from different releases of it, and a release can move
 * advance widths, kerning and vertical metrics, so line breaks with them. {@link PdfFonts}
 * records which faces each PDF embeds; this records which files those faces are, so that two
 * sets can be compared file for file. A face is matched by its PostScript name (the name a
 * PDF's BaseFont carries once the subset tag is gone) against every font in the machine's font
 * folders: Windows' own, the per-user one, Office's cloud-font cache and Office's private fonts.
 * Where several files carry the name - a per-user copy beside the system's, or a cloud font
 * cached twice - all of them are listed, since which one Word took is not knowable from here.
 * A face with Word's simulated style ({@code Calibri,Bold}) is matched by its family's name.</p>
 *
 * <p>The folders can be named with {@code -Dfidelity.fontDirs=<dir>;<dir>} (the platform's path
 * separator). Off Windows, with none named, the record says so. As everywhere in the manifest,
 * a font that cannot be read is skipped and a reading never fails a run.</p>
 *
 * @since 17.3.1
 */
public final class FontFiles {

	/** {@code -Dfidelity.fontDirs=<dir>;<dir>}: the folders to search, instead of Windows'. */
	public static final String DIRS_PROPERTY = "fidelity.fontDirs";

	private FontFiles() {}

	/** The folders searched: those named by {@link #DIRS_PROPERTY}, or Windows' font folders. */
	static List<File> dirs() {
		List<File> out = new ArrayList<File>();
		String stated = MachineState.stated(DIRS_PROPERTY);
		if (stated != null) {
			for (String d : stated.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
				if (!d.trim().isEmpty()) out.add(new File(d.trim()));
			}
			return out;
		}
		if (!MachineState.isWindows()) return out;
		String windir = System.getenv("WINDIR");
		String local = System.getenv("LOCALAPPDATA");
		out.add(new File(windir != null ? windir : "C:\\Windows", "Fonts"));
		if (local != null) {
			out.add(new File(local, "Microsoft\\Windows\\Fonts"));
			out.add(new File(local, "Microsoft\\FontCache\\4\\CloudFonts"));
		}
		for (String pf : new String[] { System.getenv("ProgramFiles"), System.getenv("ProgramFiles(x86)") }) {
			if (pf != null) out.add(new File(pf, "Microsoft Office\\root\\vfs\\Fonts"));
		}
		return out;
	}

	/**
	 * One manifest line per face, {@code fontFile.<face>=sha256:<hex>, <bytes> bytes, <path>}
	 * ({@code ; } between several files), sorted by face; or one {@code fontFiles=unknown}
	 * line saying why there was nothing to search.
	 */
	public static List<String> manifestLines(Collection<String> faces) {
		return manifestLines(faces, dirs());
	}

	static List<String> manifestLines(Collection<String> faces, List<File> dirs) {
		List<String> out = new ArrayList<String>();
		if (faces.isEmpty()) return out;
		List<File> existing = new ArrayList<File>();
		for (File d : dirs) if (d.isDirectory()) existing.add(d);
		if (existing.isEmpty()) {
			out.add("fontFiles=" + MachineState.UNKNOWN + (dirs.isEmpty()
					? " (not Windows, and -D" + DIRS_PROPERTY + " was not set)"
					: " (none of " + dirs + " is a directory)"));
			return out;
		}
		Map<String, List<File>> byName = index(existing);
		Map<String, String> lines = new TreeMap<String, String>();
		for (String face : faces) {
			if (face == null || face.isEmpty()) continue;
			String name = face;
			String simulated = null;
			int comma = face.indexOf(',');
			if (comma > 0) {
				name = face.substring(0, comma);
				simulated = face.substring(comma + 1);
			}
			List<File> files = byName.get(name);
			StringBuilder sb = new StringBuilder();
			if (files == null || files.isEmpty()) {
				sb.append("not found in ").append(existing);
			} else {
				for (File f : files) {
					if (sb.length() > 0) sb.append("; ");
					sb.append("sha256:").append(sha256(f)).append(", ").append(f.length()).append(" bytes, ").append(f.getPath());
				}
			}
			if (simulated != null) sb.append(" (Word's simulated ").append(simulated).append(" of ").append(name).append(')');
			lines.put(face, MachineState.oneLine(sb.toString()));
		}
		for (Map.Entry<String, String> e : lines.entrySet()) out.add("fontFile." + e.getKey() + "=" + e.getValue());
		return out;
	}

	/** PostScript name to the files carrying it, over every font under these folders. */
	static Map<String, List<File>> index(List<File> dirs) {
		Map<String, List<File>> byName = new LinkedHashMap<String, List<File>>();
		for (File dir : dirs) {
			List<Path> paths = new ArrayList<Path>();
			try (Stream<Path> walk = Files.walk(dir.toPath(), 4)) {
				walk.filter(Files::isRegularFile).forEach(paths::add);
			} catch (IOException | RuntimeException e) {
				continue; // a folder that cannot be walked is not a failure
			}
			java.util.Collections.sort(paths);
			for (Path p : paths) {
				File f = p.toFile();
				for (String name : postScriptNames(f)) {
					List<File> files = byName.get(name);
					if (files == null) byName.put(name, files = new ArrayList<File>());
					if (!files.contains(f)) files.add(f);
				}
			}
		}
		return byName;
	}

	/** The PostScript names a font file carries (several for a collection), or none. */
	static List<String> postScriptNames(File f) {
		List<String> names = new ArrayList<String>();
		String n = f.getName().toLowerCase(Locale.ROOT);
		try {
			if (n.endsWith(".ttc") || n.endsWith(".otc")) {
				TrueTypeCollection.processAllFontHeaders(f, h -> add(names, h));
			} else if (n.endsWith(".ttf") || n.endsWith(".otf")) {
				TTFParser parser = isOpenTypeCff(f) ? new OTFParser(false) : new TTFParser(false);
				try (RandomAccessReadBufferedFile in = new RandomAccessReadBufferedFile(f)) {
					add(names, parser.parseTableHeaders(in));
				}
			}
		} catch (IOException | RuntimeException e) {
			// not a font this can read
		}
		return names;
	}

	private static void add(List<String> names, FontHeaders h) {
		if (h != null && h.getError() == null && h.getName() != null && !h.getName().isEmpty()) names.add(h.getName());
	}

	/** Whether the file starts with 'OTTO', a CFF-flavoured OpenType font. */
	private static boolean isOpenTypeCff(File f) {
		try (InputStream in = Files.newInputStream(f.toPath())) {
			byte[] tag = new byte[4];
			return in.read(tag) == 4 && tag[0] == 'O' && tag[1] == 'T' && tag[2] == 'T' && tag[3] == 'O';
		} catch (IOException e) {
			return false;
		}
	}

	static String sha256(File f) {
		try (InputStream in = Files.newInputStream(f.toPath())) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buf = new byte[65536];
			int r;
			while ((r = in.read(buf)) > 0) md.update(buf, 0, r);
			StringBuilder sb = new StringBuilder();
			for (byte b : md.digest()) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) {
			return "unreadable (" + e.getClass().getSimpleName() + ")";
		}
	}
}
