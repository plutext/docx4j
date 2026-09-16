/*
   Copyright 2026, Plutext Pty Ltd.

   This file is part of docx4j.

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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Adds the East Asian flag - the optional seventh field - to
 * <code>word-line-metrics.properties</code>, reading it off the font files of a Windows
 * font folder.
 *
 * <p>The tool which first produced that file is not in the tree (see its header and
 * {@code git log} for the resource); this one does not regenerate it. It reads the font
 * files, works out which families the OS/2 code-page bits call East Asian, and rewrites the
 * properties file appending <code>;1</code> to the rows of exactly those families. Every
 * other byte of every row is left alone, which is the precedent the file's own history sets
 * ("Existing rows unchanged").</p>
 *
 * <p>East Asian here is <code>OS/2.ulCodePageRange1</code> bits 17 to 21 - JIS/Japan (932),
 * Chinese Simplified (936), Korean Wansung (949), Chinese Traditional (950) and Korean
 * Johab (1361) - which is the set Word's own line-height behaviour follows: such a font's
 * single line is 1.3 x (usWinAscent + usWinDescent) / unitsPerEm with no external leading
 * (CR-001 batch 43, measured on the fonts-cjk-linebox golden).</p>
 *
 * <p>The parsing is {@code WordLineMetrics.readVerticalMetrics}'s, extended by the one
 * field: a font file is a table directory, OS/2 holds usWinAscent at offset 74 and
 * ulCodePageRange1 at 78 (version 1 and later, so the table must be at least 82 bytes),
 * and a .ttc names its first font at offset 12. The family is the name table's ids 1 and
 * 16, lower-cased, as the properties file's keys are.</p>
 *
 * <pre>
 *   javac -d /tmp etc/GenWordLineMetricsEastAsian.java
 *   java -cp /tmp GenWordLineMetricsEastAsian \
 *        docx4j-core/src/main/resources/org/docx4j/fonts/word-line-metrics.properties \
 *        "/path/to/Fonts" "/path/to/Fonts/CloudFonts"
 * </pre>
 *
 * @since 17.1.1
 */
public class GenWordLineMetricsEastAsian {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("usage: GenWordLineMetricsEastAsian <properties> <fontDir>...");
			System.exit(2);
		}
		Path props = new File(args[0]).toPath();
		Set<String> eastAsian = new TreeSet<String>();
		Map<String, String> whichFile = new TreeMap<String, String>();
		int files = 0, read = 0;
		for (int i = 1; i < args.length; i++) {
			List<File> fonts = new ArrayList<File>();
			collect(new File(args[i]), fonts);
			for (File f : fonts) {
				files++;
				try {
					byte[] data = Files.readAllBytes(f.toPath());
					if (!isEastAsian(data)) continue;
					read++;
					for (String family : families(data)) {
						eastAsian.add(family);
						if (!whichFile.containsKey(family)) whichFile.put(family, f.getName());
					}
				} catch (Exception e) {
					// a .fon, a bitmap font, a file we cannot parse: not our business
				}
			}
		}
		System.out.println("font files seen: " + files + "; East Asian by OS/2 bits 17-21: " + read);
		System.out.println("families flagged: " + eastAsian.size());

		List<String> out = new ArrayList<String>();
		int flagged = 0;
		Set<String> inFile = new TreeSet<String>();
		for (String line : Files.readAllLines(props, StandardCharsets.ISO_8859_1)) {
			String trimmed = line.trim();
			int eq = trimmed.indexOf('=');
			if (trimmed.startsWith("#") || eq < 0) {
				out.add(line);
				continue;
			}
			String key = trimmed.substring(0, eq).replace("\\ ", " ").toLowerCase(Locale.ROOT);
			inFile.add(key);
			String value = trimmed.substring(eq + 1);
			if (eastAsian.contains(key) && value.split(";").length == 6) {
				out.add(trimmed.substring(0, eq) + "=" + value + ";1");
				flagged++;
			} else {
				out.add(line);
			}
		}
		Files.write(props, out, StandardCharsets.ISO_8859_1);
		System.out.println("rows given the flag: " + flagged);

		List<String> missing = new ArrayList<String>();
		for (String f : eastAsian) if (!inFile.contains(f)) missing.add(f + " (" + whichFile.get(f) + ")");
		System.out.println("East Asian families with no row in the table: " + missing.size());
		for (String m : missing) System.out.println("    " + m);
	}

	private static void collect(File dir, List<File> out) {
		File[] kids = dir.listFiles();
		if (kids == null) return;
		for (File f : kids) {
			if (f.isDirectory()) collect(f, out);
			else {
				String n = f.getName().toLowerCase(Locale.ROOT);
				if (n.endsWith(".ttf") || n.endsWith(".otf") || n.endsWith(".ttc")) out.add(f);
			}
		}
	}

	/** OS/2 ulCodePageRange1 bits 17-21: JIS, GB2312, Korean Wansung, Big5, Johab. */
	private static boolean isEastAsian(byte[] data) throws IOException {
		int offset = base(data);
		int numTables = u16(data, offset + 4);
		for (int i = 0; i < numTables; i++) {
			int rec = offset + 12 + 16 * i;
			if (!tag(data, rec).equals("OS/2")) continue;
			int off = u32(data, rec + 8);
			int len = u32(data, rec + 12);
			if (off + len > data.length || len < 82) return false;
			long cp1 = u32(data, off + 78) & 0xffffffffL;
			return (cp1 & (0x1fL << 17)) != 0;
		}
		return false;
	}

	/** The name table's family names, ids 1 and 16, lower-cased. */
	private static List<String> families(byte[] data) throws IOException {
		List<String> out = new ArrayList<String>();
		int offset = base(data);
		int numTables = u16(data, offset + 4);
		for (int i = 0; i < numTables; i++) {
			int rec = offset + 12 + 16 * i;
			if (!tag(data, rec).equals("name")) continue;
			int off = u32(data, rec + 8);
			int count = u16(data, off + 2);
			int strings = off + u16(data, off + 4);
			for (int j = 0; j < count; j++) {
				int r = off + 6 + 12 * j;
				int platform = u16(data, r);
				int nameId = u16(data, r + 6);
				if (nameId != 1 && nameId != 16) continue;
				int len = u16(data, r + 8);
				int so = strings + u16(data, r + 10);
				if (so + len > data.length) continue;
				String s = platform == 3 || platform == 0
						? new String(data, so, len, StandardCharsets.UTF_16BE)
						: new String(data, so, len, StandardCharsets.ISO_8859_1);
				s = s.trim().toLowerCase(Locale.ROOT);
				if (s.length() > 0 && !out.contains(s)) out.add(s);
			}
		}
		return out;
	}

	private static int base(byte[] data) {
		return (data.length >= 16 && tag(data, 0).equals("ttcf")) ? u32(data, 12) : 0;
	}

	private static String tag(byte[] d, int p) {
		return new String(d, p, 4, StandardCharsets.ISO_8859_1);
	}

	private static int u16(byte[] d, int p) {
		return ((d[p] & 0xff) << 8) | (d[p + 1] & 0xff);
	}

	private static int u32(byte[] d, int p) {
		return ((d[p] & 0xff) << 24) | ((d[p + 1] & 0xff) << 16)
				| ((d[p + 2] & 0xff) << 8) | (d[p + 3] & 0xff);
	}
}
