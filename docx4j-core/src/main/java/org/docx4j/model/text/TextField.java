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

/**
 * A field in a block (WD REQ-505): its instruction, and its cached result, null where it has none.
 *
 * @since 17.3.4 (CR-046 phase 1)
 */
public final class TextField {

	private final String instr;
	private final String result;

	public TextField(String instr, String result) {
		this.instr = instr;
		this.result = result;
	}

	/** The instruction ({@code w:instrText}, {@code w:fldSimple/@instr}), as written. */
	public String getInstr() { return instr; }

	/** The cached result's text in the view, or null for a field with none. */
	public String getResult() { return result; }

	@Override
	public String toString() {
		return "field{" + instr + " -> " + result + "}";
	}
}
