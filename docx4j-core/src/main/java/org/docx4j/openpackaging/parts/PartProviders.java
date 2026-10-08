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
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the {@link PartProvider}s once.  Package-private: the interface and
 * its {@code providers()} are the API.
 */
final class PartProviders {

	private static final Logger log = LoggerFactory.getLogger(PartProvider.class);

	static final List<PartProvider> PROVIDERS = load();

	private PartProviders() {}

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
		return Collections.unmodifiableList(found);
	}
}
