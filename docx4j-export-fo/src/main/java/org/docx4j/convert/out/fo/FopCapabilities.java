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
package org.docx4j.convert.out.fo;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Which FO renderer is on the classpath, probed once per JVM: Apache FOP 2.11, or the
 * docx4j FO renderer ({@code org.docx4j:docx4j-fo-renderer}, an upstream-tracking fork of
 * it with hooks docx4j's Word-layout rules can use), and which of those hooks it carries.
 *
 * <p>Both are supported. Every rule that needs a hook asks {@link #has(Capability)} and
 * falls back (to today's reflective path where one exists, or to not applying the rule)
 * when the hook is absent, so on Apache FOP the exporter behaves as it did before the
 * fork. One line is logged at INFO the first time the probe runs, naming the renderer,
 * its version and its hooks, so a support question can start from it.</p>
 *
 * <p>Two hazards are checked at the same time, neither of which was detected before:
 * two copies of FOP on the classpath (the fork keeps Apache's packages, so the copies
 * shadow each other and classpath order decides which wins - a warning names both), and
 * a FOP of another line than the one docx4j-export-fo was built for (its layout managers
 * subclass 2.11 internals - a warning gives both versions).</p>
 *
 * <p>Design: docx4j CR-020. The fork's marker is {@code org.apache.fop.docx4j.Docx4jFop},
 * read reflectively so that this module compiles against Apache FOP.</p>
 *
 * @since 17.1.1
 */
public final class FopCapabilities {

	private static final Logger log = LoggerFactory.getLogger(FopCapabilities.class);

	/** The Apache FOP line this module's layout managers were written against. */
	public static final String BUILT_FOR_LINE = "2.11";

	/** The fork's marker class; its {@code version()} and {@code capabilities()} statics are the contract. */
	static final String MARKER_CLASS = "org.apache.fop.docx4j.Docx4jFop";

	/**
	 * The hooks a rule can ask for. Each constant names one hook the docx4j FO renderer
	 * carries, by the string the fork publishes it under; the set grows with CR-020
	 * phase 1, one constant per hook, and a constant is never removed while the hook
	 * exists. Empty until the first hook lands.
	 */
	public enum Capability {
		;

		private final String key;

		Capability(String key) {
			this.key = key;
		}

		/** The name the fork publishes this hook under. */
		public String key() {
			return key;
		}
	}

	private static volatile FopCapabilities instance;

	private final boolean docx4jRenderer;
	private final String version;
	private final String line;
	private final Set<String> capabilities;
	private final List<String> warnings;
	private final String description;

	private FopCapabilities(boolean docx4jRenderer, String version, String line, Set<String> capabilities,
			List<String> warnings) {
		this.docx4jRenderer = docx4jRenderer;
		this.version = version;
		this.line = line;
		this.capabilities = Collections.unmodifiableSet(new LinkedHashSet<String>(capabilities));
		this.warnings = Collections.unmodifiableList(new ArrayList<String>(warnings));
		this.description = describe(docx4jRenderer, version, capabilities);
	}

	/** The probe's result, computed and logged on the first call. */
	public static FopCapabilities get() {
		FopCapabilities c = instance;
		if (c == null) {
			synchronized (FopCapabilities.class) {
				c = instance;
				if (c == null) {
					c = probe(FopCapabilities.class.getClassLoader());
					log.info(c.description);
					for (String w : c.warnings) {
						log.warn(w);
					}
					instance = c;
				}
			}
		}
		return c;
	}

	/** Whether the renderer carries the hook; the question every hook-dependent rule asks. */
	public static boolean has(Capability capability) {
		return get().capabilities.contains(capability.key());
	}

	/** Whether the renderer is the docx4j FO renderer rather than Apache FOP. */
	public boolean isDocx4jRenderer() {
		return docx4jRenderer;
	}

	/** The renderer's version as it reports it: {@code 2.11}, {@code 2.11-docx4j.1}, or {@code unknown}. */
	public String getVersion() {
		return version;
	}

	/** The Apache FOP line ({@code major.minor}) of the renderer, or {@code unknown}. */
	public String getLine() {
		return line;
	}

	/** The hook names the renderer published (empty on Apache FOP). */
	public Set<String> getCapabilities() {
		return capabilities;
	}

	/** The warnings the probe raised (two FOPs, another line); empty when all is well. */
	public List<String> getWarnings() {
		return warnings;
	}

	/** The one line logged at start-up, for a report to quote. */
	public String describe() {
		return description;
	}

	@Override
	public String toString() {
		return description;
	}

	// -----------------------------------------------------------------------------------

	/** The probe itself, package-private so a test can run it against a class loader of its own. */
	static FopCapabilities probe(ClassLoader loader) {
		List<String> warnings = new ArrayList<String>();

		String version = fopVersion();
		String line = lineOf(version);

		boolean fork = false;
		Set<String> caps = new LinkedHashSet<String>();
		try {
			Class<?> marker = Class.forName(MARKER_CLASS, true, loader);
			fork = true;
			Object v = marker.getMethod("version").invoke(null);
			if (v != null) {
				version = v.toString();
				line = lineOf(version);
			}
			Object c = marker.getMethod("capabilities").invoke(null);
			if (c instanceof Iterable) {
				for (Object o : (Iterable<?>) c) {
					if (o != null) caps.add(o.toString());
				}
			}
		} catch (ClassNotFoundException e) {
			// Apache FOP: no marker
		} catch (Throwable t) {
			// a marker that does not honour the contract is reported, not fatal
			warnings.add("FO renderer: the docx4j FO renderer's marker " + MARKER_CLASS
					+ " could not be read (" + t + "); its hooks are treated as absent");
		}

		List<String> copies = fopCopies(loader);
		if (copies.size() > 1) {
			warnings.add("FO renderer: " + copies.size() + " copies of FOP are on the classpath; classpath order"
					+ " decides which one is used, and a class from the other can still be loaded. Remove one of: "
					+ copies + " (the docx4j FO renderer, org.docx4j:docx4j-fo-renderer, replaces"
					+ " org.apache.xmlgraphics:fop, fop-core, fop-events and fop-util).");
		}

		if (!"unknown".equals(line) && !BUILT_FOR_LINE.equals(line)) {
			warnings.add("FO renderer: docx4j-export-fo was built for the Apache FOP " + BUILT_FOR_LINE
					+ " line, but the renderer found is " + version + " (line " + line + "); its layout managers"
					+ " subclass " + BUILT_FOR_LINE + " internals, so PDF output may differ or fail.");
		}

		return new FopCapabilities(fork, version, line, caps, warnings);
	}

	private static String describe(boolean fork, String version, Set<String> caps) {
		StringBuilder sb = new StringBuilder("FO renderer: ");
		if (fork) {
			sb.append("docx4j-fo-renderer ").append(version);
		} else {
			sb.append("Apache FOP ").append(version);
		}
		sb.append(", hooks: ");
		if (caps.isEmpty()) {
			sb.append("none");
			if (!fork) sb.append(" (Apache FOP; rules that need a hook of the docx4j FO renderer are off)");
		} else {
			boolean first = true;
			for (String c : caps) {
				if (!first) sb.append(", ");
				sb.append(c);
				first = false;
			}
		}
		return sb.toString();
	}

	/** FOP's own version string, from its jar manifest; {@code unknown} outside a jar or without FOP. */
	private static String fopVersion() {
		try {
			String v = org.apache.fop.Version.getVersion();
			// outside a jar FOP answers "SVN" or "SVN <path>", which is no version
			if (v == null || v.trim().isEmpty() || v.startsWith("SVN")) return "unknown";
			return v.trim();
		} catch (Throwable t) {
			return "unknown";
		}
	}

	private static final Pattern LINE = Pattern.compile("^(\\d+\\.\\d+)");

	/** {@code major.minor} of a version string, or {@code unknown}. */
	static String lineOf(String version) {
		if (version == null) return "unknown";
		Matcher m = LINE.matcher(version);
		return m.find() ? m.group(1) : "unknown";
	}

	/** Every location the class loader can find FOP's entry class at: one per copy of FOP. */
	private static List<String> fopCopies(ClassLoader loader) {
		List<String> out = new ArrayList<String>();
		try {
			Enumeration<URL> urls = loader.getResources("org/apache/fop/apps/Fop.class");
			while (urls.hasMoreElements()) {
				out.add(jarOf(urls.nextElement()));
			}
		} catch (IOException e) {
			// nothing to report
		}
		if (out.isEmpty()) {
			// the module path: getResources finds nothing, but the class has a code source
			try {
				CodeSource cs = org.apache.fop.apps.Fop.class.getProtectionDomain().getCodeSource();
				if (cs != null && cs.getLocation() != null) out.add(cs.getLocation().toString());
			} catch (Throwable t) {
				// nothing to report
			}
		}
		return out;
	}

	private static String jarOf(URL url) {
		String s = url.toString();
		int bang = s.indexOf("!/");
		if (bang > 0) s = s.substring(0, bang);
		if (s.startsWith("jar:")) s = s.substring(4);
		return s;
	}
}
