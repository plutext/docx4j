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
 * A property field with no value to show, the properties part gone or holding nothing for it alike. It is a
 * {@link FieldValueException}, so that {@link FieldEvaluators#evaluate(FieldInstruction, FieldContext)} keeps the cached
 * result, as an updater and the output path do (CR-067 D5). It carries what Word gives there, for the text model under
 * {@code fields: update} to read (CR-067 section 16).
 *
 * @since 17.3.6
 */
@SuppressWarnings("serial")
public class PropertyAbsentException extends FieldValueException {

	private final String result;

	public PropertyAbsentException(String message, String result) {
		super(message);
		this.result = result;
	}

	/** What Word gives for the field. */
	public String getResult() {
		return result;
	}
}
