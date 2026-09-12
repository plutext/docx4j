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
/**
 * List numbering: what number a paragraph gets, and what its label looks like.
 *
 * <h2>Definitions and state</h2>
 *
 * The <b>definitions</b> are read once from the numbering part and hold no counters:
 * {@link org.docx4j.model.listnumbering.AbstractListNumberingDefinition} is a
 * {@code w:abstractNum} with its levels, {@link org.docx4j.model.listnumbering.ListNumberingDefinition}
 * a {@code w:num} (an instance: the abstract definition plus its {@code w:lvlOverride}s),
 * and {@link org.docx4j.model.listnumbering.ListLevel} one level, merged from the abstract
 * level and the instance's override of it: start value, number format, label text,
 * {@code w:lvlRestart}, indent, rPr.
 *
 * <p>The <b>state</b> is a {@link org.docx4j.model.listnumbering.NumberingState}: the
 * counters, keyed by the referencing abstract list and the level, plus which
 * {@code w:num} start overrides have been applied.  A traversal (an export, a TOC
 * generation) owns its states, so two conversions of one package at once do not
 * interleave, and {@link org.docx4j.model.listnumbering.Emulator#peek} can ask what a
 * paragraph's number would be without taking it.  The numbering part keeps one default
 * state for the no-state overloads, which behave as they always did.
 *
 * <h2>The rules, as Word applies them (measured, CR-014, 2026)</h2>
 *
 * <ul>
 * <li><b>Shared counter.</b>  Every {@code w:num} over one {@code w:abstractNum}
 *     continues one sequence; a {@code w:num} with a {@code w:startOverride} resets the
 *     shared counter for that level the first time it is met in a story.  A
 *     {@code w:numStyleLink} abstract definition is a list of its own.</li>
 * <li><b>Stories.</b>  The body, a section's header and footer together, the
 *     footnotes part, the endnotes part, each text box and each comment count from
 *     their own start ({@link org.docx4j.model.listnumbering.NumberingStates}).</li>
 * <li><b>{@code w:lvlRestart}</b> (ECMA-376 17.9.11): {@code w:val="0"} means no
 *     shallower level restarts the count, {@code w:val="n"} that only levels 1..n
 *     (1-based) do; a level reset but not yet used shows its start value in deeper
 *     labels (2.1.1, not 2.0.1).</li>
 * <li><b>Where the numbering comes from</b> ({@link org.docx4j.model.listnumbering.Emulator#resolve}):
 *     the paragraph's own {@code w:numPr}, else its style's effective one - the default
 *     paragraph style included - and a level linked by {@code w:lvl/w:pStyle} to a
 *     different style than the one that brought the numbering numbers nothing.</li>
 * <li><b>The label's rPr</b> is the override level's where the {@code w:num} overrides
 *     the level (a replacement, not a merge), else the abstract level's, applied over
 *     the paragraph mark's; it formats the number alone.</li>
 * <li><b>Formatting is fail-soft</b> ({@link org.docx4j.model.listnumbering.NumberFormatter}):
 *     a value a format cannot express, or a format with no formatter, gives the decimal
 *     label with one warning per format.</li>
 * </ul>
 *
 * <p>The C#-style method names of the original translation ({@code IncrementCounter},
 * {@code GetCurrentNumberString}, ...) and {@code Emulator.ResultTriple} are deprecated
 * since 17.1.1 in favour of Java-style names and {@code NumberingResult}; removal no
 * earlier than 17.2.
 *
 * @since 17.1.1 (this description; the package itself dates from 2.x)
 */
package org.docx4j.model.listnumbering;
