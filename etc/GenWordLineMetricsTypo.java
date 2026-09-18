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
 * Adds the typo metrics - the optional fields 8 to 10, {@code sTypoAscender;sTypoDescender;
 * sTypoLineGap} - to <code>word-line-metrics.properties</code> for the families whose
 * Regular face sets OS/2 {@code fsSelection} USE_TYPO_METRICS (bit 7) and whose typo box
 * differs from their usWin box, reading them off the font files of a Windows font folder.
 *
 * <p>Word lays such a font out on its typo box (measured on Aptos: 13.45pt at 11pt, the
 * typo box 2500/2048, where the usWin box would give 14.13).  As
 * {@code GenWordLineMetricsEastAsian} does for the seventh field, this tool leaves every
 * other byte of every row alone; a row that already has fields 8-10 is rewritten from the
 * font file.  Where a row has six fields and the family needs fields 8-10, a seventh field
 * of {@code 0} is written first.</p>
 *
 * <pre>
 *   javac -d /tmp etc/GenWordLineMetricsTypo.java
 *   java -cp /tmp GenWordLineMetricsTypo \
 *        docx4j-core/src/main/resources/org/docx4j/fonts/word-line-metrics.properties \
 *        "/path/to/Fonts" "/path/to/Fonts/CloudFonts"
 * </pre>
 *
 * @since 17.1.1
 */
public class GenWordLineMetricsTypo {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("usage: GenWordLineMetricsTypo <properties> <fontDir>...");
			System.exit(2);
		}
		Path props = new File(args[0]).toPath();
		Map<String, int[]> typo = new TreeMap<String, int[]>();      // family -> {typoA, typoD, typoG}
		Map<String, Boolean> regular = new TreeMap<String, Boolean>(); // whether the value came from a Regular face
		int files = 0, flagged = 0;
		for (int i = 1; i < args.length; i++) {
			List<File> fonts = new ArrayList<File>();
			collect(new File(args[i]), fonts);
			for (File f : fonts) {
				files++;
				try {
					byte[] data = Files.readAllBytes(f.toPath());
					int[] m = typoIfFlagged(data);
					if (m == null) continue;
					flagged++;
					boolean isRegular = subfamily(data).equalsIgnoreCase("Regular");
					for (String family : families(data)) {
						if (!typo.containsKey(family) || (isRegular && !regular.get(family))) {
							typo.put(family, m);
							regular.put(family, isRegular);
						}
					}
				} catch (Exception e) {
					// a .fon, a bitmap font, a file we cannot parse: not our business
				}
			}
		}
		System.out.println("font files seen: " + files + "; USE_TYPO_METRICS with a typo box unlike the usWin box: " + flagged);
		List<String> out = new ArrayList<String>();
		int written = 0;
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
			String[] v = trimmed.substring(eq + 1).split(";");
			int[] m = typo.get(key);
			if (m == null || v.length < 6) {
				out.add(line);
				continue;
			}
			StringBuilder sb = new StringBuilder(trimmed.substring(0, eq + 1));
			for (int i = 0; i < 6; i++) sb.append(i == 0 ? "" : ";").append(v[i].trim());
			sb.append(';').append(v.length > 6 ? v[6].trim() : "0");
			sb.append(';').append(m[0]).append(';').append(m[1]).append(';').append(m[2]);
			out.add(sb.toString());
			written++;
		}
		for (String family : typo.keySet()) {
			if (!inFile.contains(family)) System.out.println("  not in the table: " + family);
		}
		Files.write(props, out, StandardCharsets.ISO_8859_1);
		System.out.println("rows given fields 8-10: " + written);
	}

	/** {typoAsc, typoDesc, typoGap} where the OS/2 table (version 4 or later) sets
	 *  USE_TYPO_METRICS and the typo box differs from the usWin box; else null. */
	static int[] typoIfFlagged(byte[] data) {
		int offset = 0;
		if (data.length >= 12 && tag(data, 0).equals("ttcf")) offset = u32(data, 12);
		int numTables = u16(data, offset + 4);
		for (int i = 0; i < numTables; i++) {
			int rec = offset + 12 + 16 * i;
			if (!tag(data, rec).equals("OS/2")) continue;
			int off = u32(data, rec + 8), len = u32(data, rec + 12);
			if (off + len > data.length || len < 78) return null;
			if (u16(data, off) < 4 || (u16(data, off + 62) & 0x80) == 0) return null;
			int typoA = s16(data, off + 68), typoD = s16(data, off + 70), typoG = s16(data, off + 72);
			int winA = u16(data, off + 74), winD = u16(data, off + 76);
			if (typoA - typoD + typoG == winA + winD) return null;
			return new int[] { typoA, typoD, typoG };
		}
		return null;
	}

	static String subfamily(byte[] data) {
		for (String[] n : names(data)) if (n[0].equals("2")) return n[1];
		return "";
	}

	/** name ids 1 and 16, lower-cased. */
	static Set<String> families(byte[] data) {
		Set<String> out = new TreeSet<String>();
		for (String[] n : names(data)) {
			if (n[0].equals("1") || n[0].equals("16")) out.add(n[1].toLowerCase(Locale.ROOT));
		}
		return out;
	}

	private static List<String[]> names(byte[] data) {
		List<String[]> out = new ArrayList<String[]>();
		int offset = 0;
		if (data.length >= 12 && tag(data, 0).equals("ttcf")) offset = u32(data, 12);
		int numTables = u16(data, offset + 4);
		for (int i = 0; i < numTables; i++) {
			int rec = offset + 12 + 16 * i;
			if (!tag(data, rec).equals("name")) continue;
			int off = u32(data, rec + 8);
			int count = u16(data, off + 2), strings = off + u16(data, off + 4);
			for (int j = 0; j < count; j++) {
				int r = off + 6 + 12 * j;
				int platform = u16(data, r), lang = u16(data, r + 4), id = u16(data, r + 6);
				int len = u16(data, r + 8), so = u16(data, r + 10);
				if (platform != 3 || lang != 0x409) continue;
				if (strings + so + len > data.length) continue;
				out.add(new String[] { String.valueOf(id), new String(data, strings + so, len, StandardCharsets.UTF_16BE) });
			}
		}
		return out;
	}

	private static void collect(File dir, List<File> out) {
		File[] entries = dir.listFiles();
		if (entries == null) return;
		for (File f : entries) {
			if (f.isDirectory()) collect(f, out);
			else if (f.getName().toLowerCase(Locale.ROOT).matches(".*\\.(ttf|otf|ttc)")) out.add(f);
		}
	}

	private static String tag(byte[] d, int p) { return new String(d, p, 4, StandardCharsets.ISO_8859_1); }
	private static int u16(byte[] d, int p) { return ((d[p] & 0xff) << 8) | (d[p + 1] & 0xff); }
	private static int s16(byte[] d, int p) { return (short) u16(d, p); }
	private static int u32(byte[] d, int p) { return ((d[p] & 0xff) << 24) | ((d[p + 1] & 0xff) << 16) | ((d[p + 2] & 0xff) << 8) | (d[p + 3] & 0xff); }
}
