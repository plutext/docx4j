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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * How to obtain an inspection finding's content (WD REQ-901, CR-050 D12): in the default extraction
 * already ({@link Form#INCLUDED}), in none ({@link Form#UNEXTRACTABLE}: an unselected branch, an RTF
 * chunk, a picture's title), or under the options every container on its chain needs, in the WD's
 * section 10 order ({@code options: view: original; include: headers; hidden: true}). It says how to
 * obtain the content in plain text, not whether a given output holds it: that is the finding's group
 * ({@link InspectFinding#group}).
 *
 * @since 17.3.4 (CR-050 phase 1)
 */
public final class Reveal {

	/** The three forms a reveal takes. */
	public enum Form { INCLUDED, UNEXTRACTABLE, OPTIONS }

	static final Reveal INCLUDED = new Reveal(Form.INCLUDED, Collections.<String>emptyList());
	static final Reveal UNEXTRACTABLE = new Reveal(Form.UNEXTRACTABLE, Collections.<String>emptyList());

	private final Form form;
	private final List<String> options;

	private Reveal(Form form, List<String> options) {
		this.form = form;
		this.options = options;
	}

	static Reveal options(List<String> options) {
		return options.isEmpty() ? INCLUDED : new Reveal(Form.OPTIONS, Collections.unmodifiableList(new ArrayList<String>(options)));
	}

	public Form getForm() {
		return form;
	}

	/** The options, each {@code name: value} as the WD writes it; empty unless {@link Form#OPTIONS}. */
	public List<String> getOptions() {
		return options;
	}

	/** {@code included}, {@code unextractable}, or {@code options: } and the options, {@code "; "} apart. */
	@Override
	public String toString() {
		switch (form) {
		case INCLUDED: return "included";
		case UNEXTRACTABLE: return "unextractable";
		default: return "options: " + String.join("; ", options);
		}
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Reveal && ((Reveal) o).form == form && ((Reveal) o).options.equals(options);
	}

	@Override
	public int hashCode() {
		return Objects.hash(form, options);
	}
}
