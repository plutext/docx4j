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
package org.docx4j.model.revisions;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.openpackaging.packages.OpcPackage;

/**
 * How many accepts and rejects a package has seen, so that a {@link TrackedChange} listed before
 * the last one can tell it is stale (CR-043 section 3.1).
 */
final class Generation {

	private static final Map<OpcPackage, AtomicInteger> COUNTERS = Collections.synchronizedMap(new WeakHashMap<OpcPackage, AtomicInteger>());

	private final AtomicInteger counter;
	private final int at;

	private Generation(AtomicInteger counter) {
		this.counter = counter;
		this.at = counter.get();
	}

	static Generation of(OpcPackage pkg) {
		AtomicInteger counter;
		synchronized (COUNTERS) {
			counter = COUNTERS.get(pkg);
			if (counter == null) {
				counter = new AtomicInteger();
				COUNTERS.put(pkg, counter);
			}
		}
		return new Generation(counter);
	}

	boolean isCurrent() {
		return counter.get() == at;
	}

	void advance() {
		counter.incrementAndGet();
	}
}
