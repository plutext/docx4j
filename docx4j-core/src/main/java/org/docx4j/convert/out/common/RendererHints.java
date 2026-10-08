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
package org.docx4j.convert.out.common;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The capabilities the FO renderer on the classpath advertises, as names, for the
 * parts of the conversion that live in docx4j-core and cannot see
 * {@code org.docx4j.convert.out.fo.FopCapabilities}: docx4j-export-fo sets them once
 * it has probed the renderer, and a wrapper that has a renderer-side alternative to a
 * transformation of its own asks here.  Empty until the probe has run, which is the
 * Apache FOP answer: no hooks.
 *
 * @since 17.3.2
 */
public final class RendererHints {

	private static volatile Set<String> capabilities = Collections.emptySet();

	private RendererHints() {}

	/** What the renderer advertises; the FO module calls this after its probe. */
	public static void set(Collection<String> names) {
		capabilities = names == null ? Collections.<String>emptySet()
				: Collections.unmodifiableSet(new LinkedHashSet<String>(names));
	}

	/** Whether the renderer advertises the named capability (the hook's key, e.g. "column-widths"). */
	public static boolean has(String name) {
		return capabilities.contains(name);
	}
}
