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
package org.docx4j.openpackaging.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A part class that lives outside docx4j-core.
 *
 * <p>{@code ContentTypeManager} creates a part object for each content type it
 * knows.  A few part types belong to another module, because their behaviour
 * needs dependencies docx4j-core does not have: the digital signature parts
 * ({@code XmlSignaturePart}, {@code SignatureOriginPart}) need Apache Santuario
 * and are in docx4j-digsig.  Such a module supplies a provider through
 * {@link ServiceLoader} ({@code provides} in its module-info, and
 * {@code META-INF/services/org.docx4j.openpackaging.parts.PartProvider}); core
 * asks the providers before falling back to its generic part.  With no provider
 * present the package still loads and saves: the part is then a plain XML or
 * binary part.</p>
 *
 * @since 17.3.2 (CR-033 phase 2)
 */
public interface PartProvider {

	/**
	 * @param contentType the part's content type, as in [Content_Types].xml
	 * @param partName the part name
	 * @return a part for this content type, or null if this provider does not
	 *         handle it
	 * @throws Exception if the part cannot be constructed; the caller falls back
	 *         to its generic part
	 */
	Part createPart(String contentType, PartName partName) throws Exception;

	/**
	 * The providers found on the classpath or module path, resolved once.
	 */
	static List<PartProvider> providers() {
		return Holder.PROVIDERS;
	}

	final class Holder {
		private static final Logger log = LoggerFactory.getLogger(PartProvider.class);
		static final List<PartProvider> PROVIDERS = load();

		private static List<PartProvider> load() {
			List<PartProvider> found = new ArrayList<PartProvider>();
			try {
				for (PartProvider p : ServiceLoader.load(PartProvider.class)) {
					found.add(p);
				}
				if (found.isEmpty()) {
					// TCCL didn't have it (eg a container which isolates the app);
					// try the classloader docx4j-core itself came from
					for (PartProvider p : ServiceLoader.load(PartProvider.class,
							PartProvider.class.getClassLoader())) {
						found.add(p);
					}
				}
			} catch (Throwable t) {
				log.warn("Part providers not loaded: " + t);
			}
			for (PartProvider p : found) {
				log.debug("Part provider " + p.getClass().getName());
			}
			return found;
		}
	}
}
