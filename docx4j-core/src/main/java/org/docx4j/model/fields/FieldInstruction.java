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
package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A field instruction, parsed once (CR-067 D2): the field's name, its parameters as Word tokenises
 * them (a quoted parameter keeps its quotes, so a switch's value is told from a switch), and
 * accessors for the argument and the switches. Immutable. It is what every evaluator and every
 * writer reads; {@link FldSimpleModel}, which it replaces, delegates to it.
 *
 * <p>The grammar is ECMA-376 §17.16.2: optional white space, the field name (or {@code =} for a
 * formula, which needs no space after it), then arguments and switches ({@code \x}, some with a
 * value) separated by white space, with quotation marks grouping a parameter that holds spaces.
 * Word may split an instruction across runs; callers concatenate before parsing
 * ({@link FieldRef#getInstr()}, {@link FieldsPreprocessor#extractInstr(List)}).</p>
 *
 * @since 17.3.5
 */
public final class FieldInstruction {

	private final String text;
	private final String name;
	private final String parameterText;
	private final List<String> parameters;

	private FieldInstruction(String text, String name, String parameterText, List<String> parameters) {
		this.text = text;
		this.name = name;
		this.parameterText = parameterText;
		this.parameters = Collections.unmodifiableList(parameters);
	}

	/**
	 * Parses an instruction. Never null; an empty or blank instruction has a null {@link #getName() name}.
	 */
	public static FieldInstruction parse(String instr) {
		String name = null;
		String parameterText = null;
		if (instr != null && instr.length() > 0) {
			int nameStart = 0;
			while (nameStart < instr.length() && instr.charAt(nameStart) == ' ') nameStart++;
			if (nameStart < instr.length()) {
				int nameEnd = nameStart + 1;
				if (instr.charAt(nameStart) != '=') {
					// a formula is "=" alone, whatever follows it; any other name ends at the first space
					while (nameEnd < instr.length() && instr.charAt(nameEnd) != ' ') nameEnd++;
				}
				name = instr.substring(nameStart, nameEnd).toUpperCase(Locale.ROOT);
				if (nameEnd < instr.length()) parameterText = instr.substring(nameEnd).trim();
			}
		}
		return new FieldInstruction(instr, name, parameterText, splitParameters(parameterText));
	}

	/**
	 * Word's tokenisation of the text after the field name: parameters separated by spaces, a
	 * quoted parameter kept whole with its quotes.
	 */
	public static List<String> splitParameters(String text) {
		List<String> ret = new ArrayList<String>(4);
		if (text == null || text.length() == 0) return ret;
		int valStart = -1;
		boolean inLiteral = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == '"') {
				if (inLiteral) {
					ret.add(text.substring(valStart, i + 1));
					inLiteral = false;
					valStart = -1;
				} else {
					valStart = i;
					inLiteral = true;
				}
			} else if (ch == ' ') {
				if (valStart > -1 && !inLiteral) {
					ret.add(text.substring(valStart, i));
					valStart = -1;
				}
			} else if (valStart == -1) {
				valStart = i;
			}
		}
		if (valStart > -1) ret.add(text.substring(valStart));
		return ret;
	}

	/** The instruction as given. */
	public String getText() {
		return text;
	}

	/** The field's name, upper case ({@code DATE}, {@code MERGEFIELD}; {@code =} for a formula), or null. */
	public String getName() {
		return name;
	}

	/** The text after the name, trimmed, or null. */
	public String getParameterText() {
		return parameterText;
	}

	/** The parameters as tokenised; a quoted one keeps its quotes. Never null. */
	public List<String> getParameters() {
		return parameters;
	}

	/**
	 * The field's argument: the first parameter where it is not a switch, its enclosing quotes
	 * removed; null where the field has none ({@code DATE \@ "d MMMM"}).
	 */
	public String getArgument() {
		if (parameters.isEmpty()) return null;
		String first = parameters.get(0);
		if (first.length() > 0 && first.charAt(0) == '\\') return null;
		return unquote(first);
	}

	/** Whether the switch ({@code "\\h"}) is present. The match is exact, as Word's is for the letter switches. */
	public boolean hasSwitch(String switchDef) {
		return indexOfSwitch(switchDef, 0) > -1;
	}

	/**
	 * The value of the first occurrence of the switch, its quotes removed: null where the switch is
	 * absent, "" where it has no value (the next parameter is another switch, or there is none). A
	 * {@code \* MERGEFORMAT} is skipped in favour of a later {@code \*} with a format, as
	 * {@link FormattingSwitchHelper} reads the general switch.
	 */
	public String getSwitchValue(String switchDef) {
		int pos = indexOfSwitch(switchDef, 0);
		if (pos < 0) return null;
		String value = valueAt(pos + 1);
		if (FormattingSwitchHelper.MERGEFORMAT.equals(value)) {
			value = null;
			pos = indexOfSwitch(switchDef, pos + 1);
			if (pos > -1) value = valueAt(pos + 1);
		}
		return value == null ? "" : value;
	}

	/** Every value of the switch, in order (null entries where an occurrence has none); empty where it is absent. */
	public List<String> getSwitchValues(String switchDef) {
		List<String> ret = new ArrayList<String>();
		int pos = 0;
		while ((pos = indexOfSwitch(switchDef, pos)) > -1) {
			pos++;
			ret.add(valueAt(pos));
		}
		return ret;
	}

	/** The index of the switch's first occurrence at or after {@code from}, or -1. */
	public int indexOfSwitch(String switchDef, int from) {
		for (int i = Math.max(0, from); i < parameters.size(); i++) {
			if (switchDef.equals(parameters.get(i))) return i;
		}
		return -1;
	}

	/** The parameter at the index with its quotes removed, or null where there is none or it is a switch. */
	public String valueAt(int index) {
		if (index < 0 || index >= parameters.size()) return null;
		String ret = unquote(parameters.get(index));
		if (ret.length() > 0 && ret.charAt(0) == '\\') return null;
		return ret;
	}

	/** The parameter without its enclosing quotation marks, where it has them. */
	public static String unquote(String parameter) {
		if (parameter != null && parameter.length() > 1
				&& parameter.charAt(0) == '"' && parameter.charAt(parameter.length() - 1) == '"') {
			return parameter.substring(1, parameter.length() - 1);
		}
		return parameter;
	}

	@Override
	public String toString() {
		return "FieldInstruction[" + name + " " + parameters + "]";
	}
}
