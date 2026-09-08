package org.docx4j.fidelity.golden;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTCompatSetting;

/**
 * Compares the {@code w:compat} of each document with the {@code w:compat} Word wrote back
 * when it opened and saved it ({@link WordGoldenRunner}'s third directory).
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.CompatDiff &lt;originalDir&gt; &lt;resavedDir&gt;</pre>
 *
 * <p>Two things come out of it, and they matter in opposite directions.</p>
 *
 * <p><b>A check on every measurement made from a golden.</b>  The whole settings-sensitivity
 * table assumes the document Word laid out is the document we read: if Word changes
 * {@code compatibilityMode} on open, or drops a flag, then the golden PDF was produced under
 * settings which are not the ones in the docx, and a rule keyed on the declared flag was
 * measured against the wrong thing.  Anything reported here as changed is a document whose
 * golden has to be read with that in mind.</p>
 *
 * <p><b>And Word's own mode defaults, measured rather than inferred.</b>  Where Word
 * <em>adds</em> a flag the document did not state, it has written down the value it was
 * already using - which is exactly what the rules doc records as MODE-DEFAULTED and has so
 * far had to take from the specification.  Grouped by the document's compatibility mode,
 * the additions below are that table, derived from Word.</p>
 *
 * <p>Reflection is used over {@code CTCompat}'s getters so that a flag nobody has thought
 * about yet is still reported.</p>
 */
public final class CompatDiff {

	public static void main(String[] args) throws Exception {
		File origDir = new File(args[0]), resavedDir = new File(args[1]);
		File[] files = origDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
		if (files == null || files.length == 0) throw new IllegalArgumentException("no docx in " + origDir);
		Arrays.sort(files);

		// mode -> flag -> "added=v" / "removed" / "a->b" -> count
		Map<String, Map<String, Map<String, Integer>>> byMode = new TreeMap<String, Map<String, Map<String, Integer>>>();
		Map<String, Integer> docsPerMode = new TreeMap<String, Integer>();
		int seen = 0, changed = 0, modeChanged = 0;

		for (File orig : files) {
			String id = orig.getName().replaceAll("\\.docx$", "");
			File resaved = new File(resavedDir, id + ".docx");
			if (resaved.length() == 0) continue;
			Map<String, String> a, b;
			try {
				a = compat(orig);
				b = compat(resaved);
			} catch (Exception e) {
				System.out.println(id + ": cannot read - " + e);
				continue;
			}
			seen++;
			String mode = a.get("compatSetting:compatibilityMode");
			if (mode == null) mode = "absent";
			docsPerMode.merge(mode, 1, Integer::sum);

			List<String> lines = new ArrayList<String>();
			for (String k : new TreeSet<String>(union(a, b))) {
				String x = a.get(k), y = b.get(k);
				if (x == null ? y == null : x.equals(y)) continue;
				String how = x == null ? "added=" + y : y == null ? "removed" : x + " -> " + y;
				lines.add("    " + k + ": " + how);
				count(byMode, mode, k, how);
			}
			if (!lines.isEmpty()) {
				changed++;
				System.out.println(id + " (compatibilityMode " + mode + ")");
				for (String l : lines) System.out.println(l);
			}
			String modeAfter = b.get("compatSetting:compatibilityMode");
			if (mode.equals(modeAfter) == false) {
				modeChanged++;
				System.out.println("  *** " + id + ": compatibilityMode " + mode + " -> " + modeAfter
						+ " - every measurement from this golden is under the second, not the first");
			}
		}

		System.out.println();
		System.out.printf("%d of %d documents had their w:compat rewritten; %d had compatibilityMode changed%n",
				changed, seen, modeChanged);
		System.out.println();
		System.out.println("What Word writes down, by the mode the document declared");
		System.out.println("(an \"added\" is a value Word was already using and the document did not state):");
		for (Map.Entry<String, Map<String, Map<String, Integer>>> e : byMode.entrySet()) {
			System.out.println("  compatibilityMode " + e.getKey()
					+ "  (" + docsPerMode.get(e.getKey()) + " documents)");
			for (Map.Entry<String, Map<String, Integer>> f : e.getValue().entrySet()) {
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, Integer> g : f.getValue().entrySet()) {
					if (sb.length() > 0) sb.append(", ");
					sb.append(g.getKey()).append(" x").append(g.getValue());
				}
				System.out.println("    " + f.getKey() + ": " + sb);
			}
		}
	}

	private static void count(Map<String, Map<String, Map<String, Integer>>> byMode,
			String mode, String flag, String how) {
		byMode.computeIfAbsent(mode, k -> new TreeMap<String, Map<String, Integer>>())
				.computeIfAbsent(flag, k -> new LinkedHashMap<String, Integer>())
				.merge(how, 1, Integer::sum);
	}

	private static TreeSet<String> union(Map<String, String> a, Map<String, String> b) {
		TreeSet<String> all = new TreeSet<String>(a.keySet());
		all.addAll(b.keySet());
		return all;
	}

	/** Every w:compat child of this document, as name -> value: the boolean flags by
	 *  reflection over CTCompat, and each w:compatSetting as "compatSetting:name". */
	static Map<String, String> compat(File docx) throws Exception {
		WordprocessingMLPackage pkg = Docx4J.load(docx);
		Map<String, String> out = new TreeMap<String, String>();
		DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
		if (dsp == null || dsp.getContents() == null || dsp.getContents().getCompat() == null) return out;
		Object compat = dsp.getContents().getCompat();
		for (Method m : compat.getClass().getMethods()) {
			if (m.getParameterCount() != 0) continue;
			String n = m.getName();
			if (!n.startsWith("get") || "getClass".equals(n) || "getCompatSetting".equals(n)) continue;
			Object v = m.invoke(compat);
			if (!(v instanceof BooleanDefaultTrue)) continue;
			Boolean val = ((BooleanDefaultTrue) v).isVal();
			out.put(Character.toLowerCase(n.charAt(3)) + n.substring(4),
					val == null ? "true" : String.valueOf(val));
		}
		for (CTCompatSetting cs : dsp.getContents().getCompat().getCompatSetting()) {
			out.put("compatSetting:" + cs.getName(), cs.getVal());
		}
		return out;
	}
}
