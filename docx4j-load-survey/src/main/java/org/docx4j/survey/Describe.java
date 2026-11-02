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
package org.docx4j.survey;

/** An exception or a message as one line of a row. */
final class Describe {

	static final int MAX = 500;

	private Describe() {
	}

	/** class: message, on one line. */
	static String of(Throwable t) {
		if (t == null) return null;
		String m = t.getMessage();
		return line(t.getClass().getName() + (m == null || m.isEmpty() ? "" : ": " + m));
	}

	/** The innermost cause (JAXB's linked exception is its cause). */
	static Throwable root(Throwable t) {
		Throwable r = t;
		for (int i = 0; i < 32 && r != null && r.getCause() != null && r.getCause() != r; i++) {
			r = r.getCause();
		}
		return r;
	}

	/** {@link #of} the throwable, and its root cause after it where that is another. */
	static String withRoot(Throwable t) {
		Throwable r = root(t);
		return r == t ? of(t) : line(of(t) + " [root: " + of(r) + "]");
	}

	/** Lines joined with " / ", cut to {@link #MAX} characters. */
	static String line(String s) {
		if (s == null) return null;
		String one = s.trim().replace("\r\n", " / ").replace('\r', '\n').replace("\n", " / ");
		return one.length() <= MAX ? one : one.substring(0, MAX - 1) + "…";
	}
}
