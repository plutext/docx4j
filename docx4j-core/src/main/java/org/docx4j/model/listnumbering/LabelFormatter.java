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
package org.docx4j.model.listnumbering;

/**
 * Turns a list counter into its label text for one {@code w:numFmt}: 3 into "iii",
 * "C", "3rd", "Three", "Third", "03", "③" and so on.  Stateless; one instance
 * per format is registered in {@link NumberFormatter}, which is the entry point
 * the numbering emulator uses.
 *
 * <p>A formatter that cannot express a value throws {@link NumberFormatException}
 * (Roman above 3999, a circled digit above 20, any format below 1 that has no
 * symbol for it) and {@link NumberFormatter} substitutes the decimal label, as
 * Word does, logging once per format.  Formatters themselves are not fail-soft,
 * so a caller which wants to know that a value is out of range still can.
 *
 * <p>Before 17.1.1 this class was {@code NumberFormat}, which clashed with
 * {@link org.docx4j.wml.NumberFormat}, the enumeration of {@code w:numFmt}
 * values; that name remains as a deprecated empty subclass for one release.
 *
 * @since 17.1.1 (CR-014 phase 1)
 */
public abstract class LabelFormatter {

	/**
	 * The label for the given counter value.
	 *
	 * @throws NumberFormatException when the format has no label for the value
	 */
	public abstract String format(int in);

}
