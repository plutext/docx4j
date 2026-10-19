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
package org.docx4j.model.text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;

/**
 * One inspection's state (CR-050 section 2.5): its findings so far, its skipped and undecided
 * entries, its budgets, and the packages it loaded one level down (which the text model reads too,
 * so that the addresses within them are its).
 */
final class InspectRun {

	/** A global limit reached: the inspection stops. */
	static final class Stop extends RuntimeException {
		private static final long serialVersionUID = 1L;
		final String reason;
		Stop(String reason) {
			super(reason, null, false, false);
			this.reason = reason;
		}
	}

	/** A payload's own limit reached: that payload stops, the inspection goes on. */
	static final class PayloadLimit extends Exception {
		private static final long serialVersionUID = 1L;
		PayloadLimit() {
			super(Inspection.LIMIT_PAYLOAD_BYTES, null, false, false);
		}
	}

	final int maxFindings;
	final long maxPayloadBytes, maxTotalBytes, maxElements;
	long totalBytes, elements;

	final List<InspectWalk.Found> found = new ArrayList<InspectWalk.Found>();
	final List<Inspection.Entry> skipped = new ArrayList<Inspection.Entry>();
	int skippedBeyond;
	final List<Inspection.Entry> undecided = new ArrayList<Inspection.Entry>();
	/** A package read one level down, by the part that holds it: the text model reads the same instance. */
	final Map<Part, OpcPackage> loaded = new IdentityHashMap<Part, OpcPackage>();
	/** The packages being walked, outermost first: on a global stop, what each has not inspected is listed. */
	final Deque<InspectPackage> walking = new ArrayDeque<InspectPackage>();

	InspectRun(int maxFindings, long maxPayloadBytes, long maxTotalBytes, long maxElements) {
		this.maxFindings = maxFindings;
		this.maxPayloadBytes = maxPayloadBytes;
		this.maxTotalBytes = maxTotalBytes;
		this.maxElements = maxElements;
	}

	void add(InspectWalk.Found f) {
		if (found.size() >= maxFindings) throw new Stop(Inspection.LIMIT_FINDINGS);
		found.add(f);
	}

	void skip(String reason, String location) {
		Inspection.Entry e = new Inspection.Entry(reason, location);
		if (skipped.contains(e)) return;
		if (skipped.size() < Inspect.MAX_SKIPPED) skipped.add(e);
		else skippedBeyond++;
	}

	void undecided(String reason, String location) {
		undecided.add(new Inspection.Entry(reason, location));
	}

	/** Elements walked: past the budget, the inspection stops. */
	void elements(long n) {
		elements += n;
		if (elements > maxElements) throw new Stop(Inspection.LIMIT_ELEMENTS);
	}

	/** A payload being decoded: its bytes are counted as they come. */
	Payload payload() {
		return new Payload();
	}

	final class Payload {
		long bytes;

		/** More bytes decoded: past the payload's budget it stops, past the total the inspection does. */
		void add(long n) throws PayloadLimit {
			bytes += n;
			if (bytes > maxPayloadBytes) throw new PayloadLimit();
			totalBytes += n;
			if (totalBytes > maxTotalBytes) throw new Stop(Inspection.LIMIT_TOTAL_BYTES);
		}
	}

	/** A global limit stopped the inspection: what every package being walked has not inspected is skipped. */
	void stopped(String reason) {
		for (InspectPackage p : walking) {
			for (String source : p.sources()) if (!p.done.contains(source)) skip(reason, p.location(source, ""));
		}
	}
}
