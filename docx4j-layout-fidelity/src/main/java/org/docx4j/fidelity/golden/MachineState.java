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
 * <p><b>The printer's driver and its page.</b> The resolution is not all the printer decides.
 * Two 600-dpi printers cut 11875 differently: on an HP OfficeJet Pro 9010 every page's header and
 * body stood 31.2pt lower than on a Fujifilm Apeos, and the document took 152 pages to 144
 * (ledger9 §8). Not because of its printable area: the HP's hard margins are 8.4pt all round,
 * inside the document's 18pt top margin, as this class read them; why is not known (its driver
 * is a v4 driver, the C2255's a v3). So the driver (its name, version, date and model) and the
 * default page it offers (the paper and its four hard margins) are recorded, for the next such
 * difference to be read against.  @since 17.3.1</p>
 *
 * <p><b>Word's build.</b> Word's layout changes between builds; "Microsoft 365" says nothing
 * about which. The Click-to-Run version and channel, and winword.exe's own file version, are
 * recorded.  @since 17.3.1</p>
 *
 * <p>The first two are read from the Windows registry; the printer, its driver and page, and
 * Word's build in one PowerShell call (WMI {@code Win32_Printer}, {@code Get-PrinterDriver},
 * {@code System.Drawing.Printing}, the registry and winword.exe), made once and kept for the
 * run, where this runs on Windows, which is where the golden runner runs. No reading is a
 * failure: a value that cannot be read is {@code unknown}, and the operator can state each with
 * {@code -Dfidelity.connectedExperiences=on|off|unknown}, {@code -Dfidelity.proofingLanguages=<list>},
 * {@code -Dfidelity.printer}, {@code -Dfidelity.printerDriver}, {@code -Dfidelity.printerPage} and
 * {@code -Dfidelity.word}. An explicit property always wins over the reading, so an operator who
 * knows what the machine is set to can say so.</p>
 */
public final class MachineState {

	/** {@code -Dfidelity.connectedExperiences=on|off|unknown}: what the operator states. */
	public static final String CONNECTED_PROPERTY = "fidelity.connectedExperiences";

	/** {@code -Dfidelity.proofingLanguages=en-US,de-DE}: what the operator states. */
	public static final String PROOFING_PROPERTY = "fidelity.proofingLanguages";

	/** {@code -Dfidelity.printer=<name, resolution>}: what the operator states. */
	public static final String PRINTER_PROPERTY = "fidelity.printer";

	/** {@code -Dfidelity.printerDriver=<name, version>}: what the operator states.  @since 17.3.1 */
	public static final String DRIVER_PROPERTY = "fidelity.printerDriver";

	/** {@code -Dfidelity.printerPage=<paper, hard margins>}: what the operator states.  @since 17.3.1 */
	public static final String PAGE_PROPERTY = "fidelity.printerPage";

	/** {@code -Dfidelity.word=<version>}: what the operator states.  @since 17.3.1 */
	public static final String WORD_PROPERTY = "fidelity.word";

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
		String line = reading("PRINTER|");
		if (line != null) {
			String[] f = line.split("\\|", -1);
			if (f.length >= 4 && !f[1].trim().isEmpty()) {
				return f[1].trim() + ", " + f[2].trim() + "x" + f[3].trim() + " dpi (Win32_Printer)";
			}
		}
		return UNKNOWN + " (Win32_Printer named no default printer)";
	}

	/** The default printer's driver: its name, version, date, maker and model (v3 or v4), or
	 *  {@code unknown}.  @since 17.3.1 */
	public static String printerDriver() {
		String stated = stated(DRIVER_PROPERTY);
		if (stated != null) return stated + " (stated by the operator)";
		if (!isWindows()) return UNKNOWN + " (not Windows, and -D" + DRIVER_PROPERTY + " was not set)";
		String parsed = parseDriver(reading("DRIVER|"));
		return parsed != null ? parsed : UNKNOWN + " (Get-PrinterDriver named no driver for the default printer)";
	}

	/** The default page the default printer offers: the paper and its four hard margins, in
	 *  points, or {@code unknown}.  @since 17.3.1 */
	public static String printerPage() {
		String stated = stated(PAGE_PROPERTY);
		if (stated != null) return stated + " (stated by the operator)";
		if (!isWindows()) return UNKNOWN + " (not Windows, and -D" + PAGE_PROPERTY + " was not set)";
		String parsed = parsePage(reading("PAGE|"));
		return parsed != null ? parsed : UNKNOWN + " (System.Drawing.Printing gave no default page)";
	}

	/** Word's build: the Click-to-Run version and channel and winword.exe's file version, or
	 *  {@code unknown}.  @since 17.3.1 */
	public static String word() {
		String stated = stated(WORD_PROPERTY);
		if (stated != null) return stated + " (stated by the operator)";
		if (!isWindows()) return UNKNOWN + " (not Windows, and -D" + WORD_PROPERTY + " was not set)";
		String parsed = parseWord(reading("WORD|"));
		return parsed != null ? parsed : UNKNOWN + " (neither Click-to-Run nor winword.exe answered)";
	}

	// ------------------------------------------------------------------ the PowerShell reading

	/**
	 * One PowerShell call answers for the printer, its driver and page, and Word: a line each,
	 * {@code TAG|field|field...}, with only single quotes inside so that the command survives
	 * Windows' argument quoting, and the decimals written invariantly whatever the machine's
	 * locale.  Every statement may fail on its own and the rest still answer.
	 */
	static final String READING_SCRIPT = "$ErrorActionPreference = 'SilentlyContinue'; "
			+ "$inv = [cultureinfo]::InvariantCulture; "
			+ "$pr = Get-CimInstance Win32_Printer -Filter 'Default=TRUE' | Select-Object -First 1; "
			+ "if ($pr) { 'PRINTER|' + $pr.Name + '|' + $pr.HorizontalResolution + '|' + $pr.VerticalResolution + '|' + $pr.DriverName; "
			+ "  $d = Get-PrinterDriver -Name $pr.DriverName | Select-Object -First 1; "
			+ "  if ($d) { $v = [uint64]$d.DriverVersion; "
			+ "    $ver = '{0}.{1}.{2}.{3}' -f (($v -shr 48) -band 65535), (($v -shr 32) -band 65535), (($v -shr 16) -band 65535), ($v -band 65535); "
			+ "    $date = ''; if ($d.DriverDate) { $date = $d.DriverDate.ToString('yyyy-MM-dd', $inv) }; "
			+ "    'DRIVER|' + $d.Name + '|' + $ver + '|' + $date + '|' + $d.Manufacturer + '|' + $d.MajorVersion } }; "
			+ "Add-Type -AssemblyName System.Drawing; "
			+ "$ps = New-Object System.Drawing.Printing.PrinterSettings; "
			+ "$pg = $ps.DefaultPageSettings; "
			+ "if ($pg) { $a = $pg.PrintableArea; "
			+ "  'PAGE|' + $ps.PrinterName + '|' + $pg.PaperSize.PaperName + '|' + $pg.PaperSize.Width + '|' + $pg.PaperSize.Height"
			+ " + '|' + $a.X.ToString($inv) + '|' + $a.Y.ToString($inv) + '|' + $a.Width.ToString($inv) + '|' + $a.Height.ToString($inv)"
			+ " + '|' + $pg.Landscape }; "
			+ "$w = (Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\Winword.exe').'(default)'; "
			+ "$fv = ''; if ($w -and (Test-Path $w)) { $fv = (Get-Item $w).VersionInfo.FileVersion }; "
			+ "$c = Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Office\\ClickToRun\\Configuration'; "
			+ "'WORD|' + $fv + '|' + $c.VersionToReport + '|' + $c.UpdateChannel + '|' + $c.CDNBaseUrl + '|' + $c.Platform + '|' + $w";

	private static List<String> readings;

	/** The reading's line with this tag, or null; the call is made once per run. */
	private static synchronized String reading(String tag) {
		if (readings == null) {
			readings = run(new String[] { "powershell", "-NoProfile", "-NonInteractive", "-Command", READING_SCRIPT });
		}
		for (String line : readings) {
			if (line.startsWith(tag)) return line;
		}
		return null;
	}

	/** {@code DRIVER|name|version|date|maker|major} as a manifest value, or null. */
	static String parseDriver(String line) {
		if (line == null) return null;
		String[] f = line.split("\\|", -1);
		if (f.length < 6 || f[1].trim().isEmpty()) return null;
		StringBuilder sb = new StringBuilder(f[1].trim());
		if (!f[2].trim().isEmpty() && !f[2].trim().equals("0.0.0.0")) sb.append(", version ").append(f[2].trim());
		if (!f[3].trim().isEmpty()) sb.append(", dated ").append(f[3].trim());
		if (!f[4].trim().isEmpty()) sb.append(", by ").append(f[4].trim());
		if (!f[5].trim().isEmpty()) sb.append(", v").append(f[5].trim()).append(" driver");
		return sb.append(" (Get-PrinterDriver)").toString();
	}

	/**
	 * {@code PAGE|printer|paper|width|height|x|y|printableWidth|printableHeight|landscape} as a
	 * manifest value, or null.  System.Drawing gives the paper and the printable area in
	 * hundredths of an inch, the area already turned for landscape; the four hard margins are
	 * what lies outside the area, in points.
	 */
	static String parsePage(String line) {
		if (line == null) return null;
		String[] f = line.split("\\|", -1);
		if (f.length < 10) return null;
		try {
			double w = Double.parseDouble(f[3].trim()), h = Double.parseDouble(f[4].trim());
			if (Boolean.parseBoolean(f[9].trim())) { double t = w; w = h; h = t; }
			double x = Double.parseDouble(f[5].trim()), y = Double.parseDouble(f[6].trim());
			double pw = Double.parseDouble(f[7].trim()), ph = Double.parseDouble(f[8].trim());
			return String.format(Locale.ROOT,
					"%s %.1f x %.1fpt%s, hard margins left %.1f top %.1f right %.1f bottom %.1f pt (System.Drawing.Printing)",
					f[2].trim(), w * 0.72, h * 0.72, Boolean.parseBoolean(f[9].trim()) ? " landscape" : "",
					x * 0.72, y * 0.72, (w - x - pw) * 0.72, (h - y - ph) * 0.72);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** The Click-to-Run channels Microsoft documents, by the GUID that ends their CDN URL. */
	private static final String[][] CHANNELS = {
			{ "492350f6-3a01-4f97-b9c0-c7c6ddf67d60", "Current Channel" },
			{ "64256afe-f5d9-4f86-8936-8840a6a4f5be", "Current Channel (Preview)" },
			{ "5440fd1f-7ecb-4221-8110-145efaa6372f", "Beta Channel" },
			{ "55336b82-a18d-4dd6-b5f6-9e5095c314a6", "Monthly Enterprise Channel" },
			{ "7ffbc6bf-bc32-4f92-8982-f9dd17fd3114", "Semi-Annual Enterprise Channel" },
			{ "b8f9b850-328d-4355-9145-c59439a0c4cf", "Semi-Annual Enterprise Channel (Preview)" } };

	/** {@code WORD|fileVersion|versionToReport|updateChannel|cdnBaseUrl|platform|path} as a
	 *  manifest value, or null.  The channel is the GUID ending the update URL, named where it
	 *  is one Microsoft documents; the GUID is kept either way. */
	static String parseWord(String line) {
		if (line == null) return null;
		String[] f = line.split("\\|", -1);
		if (f.length < 7) return null;
		String file = f[1].trim(), c2r = f[2].trim(), path = f[6].trim();
		String url = !f[3].trim().isEmpty() ? f[3].trim() : f[4].trim();
		if (file.isEmpty() && c2r.isEmpty()) return null;
		StringBuilder sb = new StringBuilder();
		if (!c2r.isEmpty()) {
			sb.append(c2r).append(" (Click-to-Run");
			if (!url.isEmpty()) {
				String guid = url.substring(url.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
				String name = null;
				for (String[] ch : CHANNELS) if (ch[0].equals(guid)) name = ch[1];
				sb.append(", ").append(name != null ? name + " " + guid : "channel " + guid);
			}
			if (!f[5].trim().isEmpty()) sb.append(", ").append(f[5].trim());
			sb.append(')');
			if (!file.isEmpty()) sb.append("; ");
		}
		if (!file.isEmpty()) sb.append("winword.exe ").append(file);
		if (c2r.isEmpty() && !path.isEmpty()) sb.append(" (").append(path).append(')');
		return sb.toString();
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

	/** As {@link #manifestLines(String, String, String)}, with the printer's driver and page and
	 *  Word's build after them.  @since 17.3.1 */
	public static List<String> manifestLines(String connected, String proofing, String printer,
			String driver, String page, String word) {
		List<String> out = new ArrayList<String>(manifestLines(connected, proofing, printer));
		out.add("printerDriver=" + blankToUnknown(driver));
		out.add("printerPage=" + blankToUnknown(page));
		out.add("word=" + blankToUnknown(word));
		return Collections.unmodifiableList(out);
	}

	/** {@link #manifestLines(String, String, String, String, String, String)} of this machine. */
	public static List<String> manifestLines() {
		return manifestLines(connectedExperiences(), proofingLanguages(), defaultPrinter(),
				printerDriver(), printerPage(), word());
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
