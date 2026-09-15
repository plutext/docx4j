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

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fonts a deployment has: this machine's, or the docx4j font jars alone, or a
 * directory's.
 *
 * <p>Two uses.  {@link FontsAnalysis#analyse(org.docx4j.openpackaging.packages.WordprocessingMLPackage,
 * FontEnvironment)} answers "what would <em>that</em> deployment draw this document
 * with" - the headless case of CR-016 phase 0c and issue #695, where the jars are the
 * whole font supply and the user has no {@code fc-list} to consult.  And
 * docx4j-layout-fidelity's {@code -Dfidelity.fonts=all|jars|&lt;dir&gt;} scores a font
 * environment; the directory walk was that harness's, and is here so both use one
 * (CR-017 phase 3).</p>
 *
 * <p>An environment is a set of physical font names, not a separate discovery:
 * {@link PhysicalFonts} is static and populated once, in the {@link Mapper}'s static
 * initialiser, so <em>restricting</em> what is discovered is a start-up matter (the
 * {@code docx4j.fonts.discoverPhysicalFonts.enabled} property, set before the first
 * Mapper is constructed).  What this offers afterwards is a view.</p>
 *
 * @since 17.1.1
 */
public final class FontEnvironment {

	private static final Logger log = LoggerFactory.getLogger(FontEnvironment.class);

	private final String name;
	/** null for the machine's own environment, which is a live view of
	 *  {@link PhysicalFonts}: discovery happens in the Mapper's static initialiser, which
	 *  may not have run when the environment is asked for. */
	private final Set<String> fonts;

	private FontEnvironment(String name, Set<String> fonts) {
		this.name = name;
		this.fonts = fonts==null ? null : Collections.unmodifiableSet(fonts);
	}

	/** Every font this JVM discovers: the jars, and the machine's own unless discovery
	 *  was turned off.  A live view, not a snapshot. */
	public static FontEnvironment machine() {
		return new FontEnvironment("this machine", null);
	}

	/**
	 * The fonts that came from a jar on the classpath - the docx4j font jars - and
	 * nothing else: what a container deployment has, the stock ubuntu, debian, fedora and
	 * alpine images shipping no font files at all (CR-016 phase 0c).
	 *
	 * <p>A font is the jars' where the URI it was discovered under is inside one
	 * ({@code jar:} scheme).  Two limits, both of them this being a view of what one JVM
	 * discovered rather than a second discovery: run from a build tree rather than from
	 * jars - the fonts under {@code target/classes/fonts} - those fonts are file URIs like
	 * any other and this cannot tell them apart; and a face a jar supplies which the
	 * machine <em>also</em> has installed under that name is the machine's file in
	 * {@link PhysicalFonts} (one entry per name, the plainer file kept), so it is missing
	 * from this view although a headless deployment would have it.  For measuring such a
	 * deployment, the harness's {@code -Dfidelity.fonts=jars} turns system discovery off
	 * before the first Mapper is built, which is the real thing.</p>
	 */
	public static FontEnvironment jarsOnly() {
		Set<String> jarFonts = new TreeSet<String>();
		for (Map.Entry<String, PhysicalFont> e : PhysicalFonts.getPhysicalFonts().entrySet()) {
			if (fromJar(e.getValue())) jarFonts.add(e.getKey().toLowerCase(Locale.ROOT));
		}
		return new FontEnvironment("the docx4j font jars", jarFonts);
	}

	private static boolean fromJar(PhysicalFont pf) {
		return pf!=null && pf.getEmbeddedURI()!=null
				&& "jar".equalsIgnoreCase(pf.getEmbeddedURI().getScheme());
	}

	/**
	 * The fonts in a directory, added to {@link PhysicalFonts} so that a conversion can
	 * use them, and returned as an environment.  Walked recursively; {@code .ttf},
	 * {@code .otf} and {@code .ttc} files.
	 *
	 * @since 17.1.1 (the walk was docx4j-layout-fidelity's, CR-016 phase 0c)
	 */
	public static FontEnvironment ofDirectory(File dir) throws Exception {

		if (dir==null || !dir.isDirectory()) {
			throw new IllegalArgumentException("not a directory: " + dir);
		}
		Set<String> before = keys(PhysicalFonts.getPhysicalFonts().keySet());
		try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(dir.toPath())) {
			for (java.nio.file.Path f : (Iterable<java.nio.file.Path>) paths::iterator) {
				String n = f.getFileName().toString().toLowerCase(Locale.ROOT);
				if (n.endsWith(".ttf") || n.endsWith(".otf") || n.endsWith(".ttc")) {
					try {
						PhysicalFonts.addPhysicalFont(f.toUri());
					} catch (Exception e) {
						log.warn("font not added: " + f + " (" + e.getMessage() + ")");
					}
				}
			}
		}
		Set<String> added = keys(PhysicalFonts.getPhysicalFonts().keySet());
		added.removeAll(before);
		return new FontEnvironment(dir.toString(), added);
	}

	/** An environment of these physical font names, for a caller which knows its own. */
	public static FontEnvironment of(String name, Set<String> physicalFontNames) {
		return new FontEnvironment(name, keys(physicalFontNames));
	}

	/** What to call this environment in a report. */
	public String getName() { return name; }

	/** Whether it has a font of that name (the FO layer's suffixes are stripped). */
	public boolean has(String physicalFontName) {
		if (physicalFontName==null) return false;
		if (fonts==null) return PhysicalFonts.get(physicalFontName)!=null;
		return fonts.contains(PhysicalFonts.stripSuffixes(physicalFontName).trim().toLowerCase(Locale.ROOT));
	}

	/** The font of that name where this environment has it, else null. */
	public PhysicalFont get(String physicalFontName) {
		return has(physicalFontName) ? PhysicalFonts.get(physicalFontName) : null;
	}

	/** The names, lower-cased as {@link PhysicalFonts} keys them. */
	public Set<String> names() {
		return fonts==null ? keys(PhysicalFonts.getPhysicalFonts().keySet()) : fonts;
	}

	public int size() { return names().size(); }

	@Override
	public String toString() {
		return name + " (" + size() + " physical fonts)";
	}

	private static Set<String> keys(Set<String> names) {
		Set<String> keys = new LinkedHashSet<String>();
		for (String name : names) {
			if (name!=null) keys.add(name.trim().toLowerCase(Locale.ROOT));
		}
		return keys;
	}
}
