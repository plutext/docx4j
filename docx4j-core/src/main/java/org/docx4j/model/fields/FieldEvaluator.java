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

/**
 * Computes one kind of field's result (CR-067 D1). Registered by field name in
 * {@link FieldEvaluators}, and consumed there by everything that evaluates a field: FieldUpdater
 * (which writes the result back), the text extraction's {@code fields: update}, and the PDF and
 * HTML output for a field the renderer does not lay out.
 *
 * @since 17.3.5
 */
public interface FieldEvaluator {

	/**
	 * The field's result, before the formatting switches ({@code \*}, {@code \#}, {@code \@}) are
	 * applied, or null where it cannot be computed (the input is absent from the context, the
	 * property does not exist, a switch is not supported): the field then keeps its cached result
	 * (D5).
	 */
	String evaluate(FieldInstruction instruction, FieldContext context) throws Exception;

	/**
	 * Whether {@link #evaluate} returns the text as it is to be shown, formatting switches included
	 * (a date field reads its own {@code \@}); false, the default, has {@link FieldEvaluators} apply
	 * {@link FormattingSwitchHelper} to the result.
	 */
	default boolean formatsItself() {
		return false;
	}
}
