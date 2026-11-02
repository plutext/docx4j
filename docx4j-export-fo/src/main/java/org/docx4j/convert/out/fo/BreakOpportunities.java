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
package org.docx4j.convert.out.fo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.apache.fop.text.linebreak.LineBreakStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For each character of a text node, whether a line may break immediately before it, as the renderer's line
 * breaking will decide: what the column sizer ({@link TableWriter}) measures a cell's words by (CR-071 D6).
 *
 * <p>With the docx4j FO renderer's Word layout ({@link FopCapabilities.Capability#WORD_LAYOUT}), its
 * {@code org.apache.fop.layoutmgr.word.WordBreakOpportunities.breakBefore}, the one its line manager breaks by:
 * UAX #14 as FOP applies it, with Word's corrections.  Otherwise UAX #14 as FOP applies it, which is what plain
 * FOP's line breaking uses.  The fork's is found by reflection, since docx4j-export-fo also compiles against
 * Apache FOP, and is a static method because the FO is written before any FopFactory exists.</p>
 *
 * @since 17.3.6
 */
final class BreakOpportunities {

	private static final Logger log = LoggerFactory.getLogger(BreakOpportunities.class);

	static final String FORK_CLASS = "org.apache.fop.layoutmgr.word.WordBreakOpportunities";

	private static volatile MethodHandle fork;
	private static volatile boolean sought;

	private BreakOpportunities() {
	}

	static boolean[] breakBefore(CharSequence text) {
		MethodHandle h = fork();
		if (h != null) {
			try {
				return (boolean[]) h.invokeExact(text);
			} catch (RuntimeException | Error e) {
				throw e;
			} catch (Throwable t) {
				throw new IllegalStateException(t);
			}
		}
		return uax14(text);
	}

	/** UAX #14 as FOP applies it (a mandatory break counts); index 0 is never a break (LB2). */
	static boolean[] uax14(CharSequence text) {
		int n = text.length();
		boolean[] out = new boolean[n];
		LineBreakStatus status = new LineBreakStatus();
		for (int i = 0; i < n; i++) {
			switch (status.nextChar(text.charAt(i))) {
				case LineBreakStatus.DIRECT_BREAK:
				case LineBreakStatus.INDIRECT_BREAK:
				case LineBreakStatus.COMBINING_INDIRECT_BREAK:
				case LineBreakStatus.EXPLICIT_BREAK:
					out[i] = true;
					break;
				default:
					out[i] = false;
			}
		}
		return out;
	}

	/** The fork's method where the renderer carries Word layout; null otherwise. */
	private static MethodHandle fork() {
		if (!sought) {
			synchronized (BreakOpportunities.class) {
				if (!sought) {
					if (FopCapabilities.has(FopCapabilities.Capability.WORD_LAYOUT)) {
						try {
							fork = MethodHandles.publicLookup().findStatic(Class.forName(FORK_CLASS), "breakBefore",
									MethodType.methodType(boolean[].class, CharSequence.class));
						} catch (ReflectiveOperationException | RuntimeException e) {
							log.warn("The FO renderer advertises word-layout but " + FORK_CLASS
									+ ".breakBefore could not be reached (" + e + "); UAX #14 as FOP applies it");
						}
					}
					sought = true;
				}
			}
		}
		return fork;
	}
}
