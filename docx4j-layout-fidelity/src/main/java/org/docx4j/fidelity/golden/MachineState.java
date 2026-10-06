/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.fidelity.golden;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * The two settings of the rendering machine which decide a golden as much as its fonts
 * do, recorded at cut time so that a set says what it is.
 *
 * <p><b>Connected experiences.</b> A cloud font in Office's {@code FontCache/CloudFonts}
 * folder counts as installed only while the "connected experiences" setting is on.
 * Measured on one document in one day (RULE-CLASSES.md section 1): with the setting off
 * Word set Aptos in Calibri and produced a 27-page PDF; with it on Word set the body in
 * Aptos and produced a different 27-page PDF whose every line broke elsewhere. Both are
 * Word's output; only the second is Word's layout of the document's fonts. The corpus
 * goldens were cut on 2026-09-05 with the state unrecorded, which is the gap this
 * closes.</p>
 *
 * <p><b>Proofing languages.</b> They decide hyphenation, which is why the corpora are
 * scored with {@code -Dfidelity.hyphenate=false}: a golden made on a machine without the
 * language's proofing tools is unhyphenated whatever the document asks for. Which
 * languages the machine had is therefore part of the set.</p>
 *
 * <p><b>The default printer.</b> Word formats text against the default printer's device: it
 * sets each font size in whole device pixels, so the size it writes into the PDF is the nominal
 * size rounded to 1/dpi inch, and line widths move with it. Measured on corpus document 11875,
 * cut on 2026-09-09 with a 300-dpi default printer and on 2026-10-06 with a 600-dpi one: its
 * 10pt text is written 10.08pt (42 pixels at 300 dpi) and 9.96pt (83 at 600), its 14pt 13.92 and
 * 14.04, and the same 10pt line is 0.33% wider in the first, the order of the knife-edge line
 * breaks the harness measures. The two runs of one script on the same day, with and without the
 * review-markup lines, write the same sizes, so the printer, not the script, decided it
 * (ledger9 §8). The printer and its resolution are therefore part of the set.</p>
 *
 * <p>The first two are read from the Windows registry, and the printer from WMI
 * ({@code Win32_Printer}), where this runs on Windows, which is where the golden runner runs. Neither reading is a failure: a value that cannot be read is
 * {@code unknown}, and the operator can state it with
 * {@code -Dfidelity.connectedExperiences=on|off|unknown} and
 * {@code -Dfidelity.proofingLanguages=<list>}. An explicit property always wins over the
 * registry, so an operator who knows what the machine is set to can say so.</p>
 */
public final class MachineState {

	/** {@code -Dfidelity.connectedExperiences=on|off|unknown}: what the operator states. */
	public static final String CONNECTED_PROPERTY = "fidelity.connectedExperiences";

	/** {@code -Dfidelity.proofingLanguages=en-US,de-DE}: what the operator states. */
	public static final String PROOFING_PROPERTY = "fidelity.proofingLanguages";

	/** {@code -Dfidelity.printer=<name, resolution>}: what the operator states. */
	public static final String PRINTER_PROPERTY = "fidelity.printer";

	/** Not read, and not stated. */
	public static final String UNKNOWN = "unknown";

	/**
	 * Office's privacy settings. {@code DisconnectedState} is the one Office writes when
	 * the user turns "all connected experiences" off; {@code ControllerConnectedServicesEnabled}
	 * is the per-machine policy. 16.0 is Office 2016 through Microsoft 365, which is what
	 * the runner has.
	 */
	static final String[] CONNECTED_KEYS = {
			"HKCU\\Software\\Microsoft\\Office\\16.0\\Common\\Privacy",
			"HKCU\\Software\\Policies\\Microsoft\\office\\16.0\\common\\privacy" };

	static final String[] CONNECTED_VALUES = { "DisconnectedState", "ControllerConnectedServicesEnabled" };

	/** Where Office records the proofing tools it has, one subkey per LCID. */
	static final String PROOFING_KEY = "HKLM\\SOFTWARE\\Microsoft\\Shared Tools\\Proofing Tools\\1.0\\Override";

	private MachineState() {}

	// ------------------------------------------------------------------ the two readings

	/** {@code on}, {@code off} or {@code unknown}, with where it came from in brackets. */
	public static String connectedExperiences() {
		String stated = stated(CONNECTED_PROPERTY);
		if (stated != null) return stated + " (stated by the operator)";
		if (!isWindows()) return UNKNOWN + " (not Windows; no registry to read, and -D"
				+ CONNECTED_PROPERTY + " was not set)";
		for (String key : CONNECTED_KEYS) {
			for (String value : CONNECTED_VALUES) {
				String dword = registryDword(key, value);
				if (dword == null) continue;
				/* DisconnectedState is non-zero when the user has turned the connected
				 * experiences off; ControllerConnectedServicesEnabled is 0 when policy
				 * has.  Either way a reading is worth more than a guess at which. */
				boolean off = "DisconnectedState".equals(value) ? !dword.equals("0") : dword.equals("0");
				return (off ? "off" : "on") + " (" + key + "\\" + value + "=" + dword + ")";
			}
		}
		return UNKNOWN + " (neither privacy key is set; Office's own default is on)";
	}

	/**
	 * The proofing languages the machine has, or {@code unknown}.
	 *
	 * <p>The registry names them by LCID, one subkey each ({@code 1033} for en-US), so
	 * that is what is recorded where the reading comes from the registry - the numbers a
	 * later reader can look up, rather than a translation this code would have to carry a
	 * table for. An operator who states the languages with {@code -D}
	 * {@value #PROOFING_PROPERTY} may use whatever form they like.</p>
	 */
	public static String proofingLanguages() {
		String stated = stated(PROOFING_PROPERTY);
		if (stated != null) return stated + " (stated by the operator)";
		if (!isWindows()) return UNKNOWN + " (not Windows; no registry to read, and -D"
				+ PROOFING_PROPERTY + " was not set)";
		List<String> lcids = registrySubkeys(PROOFING_KEY);
		if (lcids.isEmpty()) return UNKNOWN + " (" + PROOFING_KEY + " has no subkeys)";
		StringBuilder sb = new StringBuilder("lcid ");
		for (String lcid : new TreeSet<String>(lcids)) {
			if (sb.length() > "lcid ".length()) sb.append(',');
			sb.append(lcid);
		}
		return sb.toString() + " (" + PROOFING_KEY + ")";
	}

	/**
	 * The default printer's name and resolution, as Word sees it when it formats a document,
	 * or {@code unknown}.  The resolution is the driver's default print quality (the
	 * printing preferences), which is what Word's device context reports.  @since 17.3.1
	 */
	public static String defaultPrinter() {
		String stated = stated(PRINTER_PROPERTY);
		if (stated != null) return stated + " (stated by the operator)";
		if (!isWindows()) return UNKNOWN + " (not Windows; no WMI to read, and -D"
				+ PRINTER_PROPERTY + " was not set)";
		for (String line : run(new String[] { "powershell", "-NoProfile", "-NonInteractive", "-Command",
				"Get-CimInstance Win32_Printer -Filter 'Default=TRUE' | ForEach-Object { "
				+ "'PRINTER|' + $_.Name + '|' + $_.HorizontalResolution + '|' + $_.VerticalResolution }" })) {
			if (!line.startsWith("PRINTER|")) continue;
			String[] f = line.split("\\|", -1);
			if (f.length < 4 || f[1].trim().isEmpty()) continue;
			return f[1].trim() + ", " + f[2].trim() + "x" + f[3].trim() + " dpi (Win32_Printer)";
		}
		return UNKNOWN + " (Win32_Printer named no default printer)";
	}

	// ------------------------------------------------------------------ the manifest lines

	/**
	 * The lines a manifest carries for the machine's state, in order. Written by
	 * {@link WordGoldenRunner} into {@code golden-manifest.properties} and beside a
	 * resaved directory, and unit tested here rather than on a Windows VM.
	 *
	 * @param connected the {@link #connectedExperiences()} reading
	 * @param proofing  the {@link #proofingLanguages()} reading
	 */
	public static List<String> manifestLines(String connected, String proofing) {
		List<String> out = new ArrayList<String>();
		out.add("connectedExperiences=" + blankToUnknown(connected));
		out.add("proofingLanguages=" + blankToUnknown(proofing));
		return Collections.unmodifiableList(out);
	}

	/** As {@link #manifestLines(String, String)}, with the default printer's line after them.
	 *  @since 17.3.1 */
	public static List<String> manifestLines(String connected, String proofing, String printer) {
		List<String> out = new ArrayList<String>(manifestLines(connected, proofing));
		out.add("printer=" + blankToUnknown(printer));
		return Collections.unmodifiableList(out);
	}

	/** {@link #manifestLines(String, String, String)} of this machine. */
	public static List<String> manifestLines() {
		return manifestLines(connectedExperiences(), proofingLanguages(), defaultPrinter());
	}

	private static String blankToUnknown(String value) {
		return value == null || value.trim().isEmpty() ? UNKNOWN : oneLine(value.trim());
	}

	/** A manifest is a properties file read line by line: a value never wraps. */
	static String oneLine(String value) {
		return value.replace('\r', ' ').replace('\n', ' ');
	}

	// ------------------------------------------------------------------ plumbing

	static String stated(String property) {
		String value = System.getProperty(property);
		return value == null || value.trim().isEmpty() ? null : value.trim();
	}

	static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
	}

	/** {@code reg query <key> /v <value>}'s number, or null where there is none. */
	static String registryDword(String key, String value) {
		for (String line : reg(new String[] { "reg", "query", key, "/v", value })) {
			int at = line.indexOf("REG_DWORD");
			if (at < 0) continue;
			String data = line.substring(at + "REG_DWORD".length()).trim();
			if (data.toLowerCase(Locale.ROOT).startsWith("0x")) {
				try {
					return Integer.toString(Integer.parseInt(data.substring(2), 16));
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return data.isEmpty() ? null : data;
		}
		return null;
	}

	/** The last path segment of each subkey {@code reg query <key>} lists. */
	static List<String> registrySubkeys(String key) {
		List<String> out = new ArrayList<String>();
		String prefix = key.toLowerCase(Locale.ROOT);
		for (String line : reg(new String[] { "reg", "query", key })) {
			String trimmed = line.trim();
			if (!trimmed.toLowerCase(Locale.ROOT).startsWith(prefix)) continue;
			if (trimmed.length() <= key.length()) continue;
			String tail = trimmed.substring(key.length());
			while (tail.startsWith("\\")) tail = tail.substring(1);
			if (!tail.isEmpty() && tail.indexOf('\\') < 0) out.add(tail);
		}
		return out;
	}

	/** {@code reg}'s output, or nothing at all: a record must never cost a run. */
	private static List<String> reg(String[] command) {
		return run(command);
	}

	/** A command's output, or nothing at all: a record must never cost a run. */
	private static List<String> run(String[] command) {
		List<String> out = new ArrayList<String>();
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			try (BufferedReader r = new BufferedReader(
					new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = r.readLine()) != null) out.add(line);
			}
			p.waitFor();
		} catch (Exception e) {
			return out;
		}
		return out;
	}
}
