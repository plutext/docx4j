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
 * Merging and comparing WordprocessingML properties, and the styles tree the HTML
 * exporter's CSS is built from.
 *
 * <ul>
 * <li>{@link org.docx4j.model.styles.PropertyCatalogue} - the members of {@code w:rPr}
 * (and the paragraph mark's) and of {@code w:pPr}, each with its accessors, its merge
 * rule and its emptiness, in one list per element type.</li>
 * <li>{@link org.docx4j.model.styles.StyleUtil} - {@code apply} (a more specific value
 * over an inherited one, per member and where Word does so per attribute: tabs,
 * indents, frames, spacing, numbering, language), {@code isEmpty}, {@code unset},
 * {@code hasDirectFormatting}, {@code areEqual}, and {@code isCyclic} for the
 * {@code w:basedOn} walk.  The measured merge rules cite their document or probe
 * golden at the method.</li>
 * <li>{@link org.docx4j.model.styles.StyleTree} (with {@code Tree}/{@code Node}) - the
 * styles in use and their {@code w:basedOn} hierarchy, for CSS class generation.</li>
 * <li>{@link org.docx4j.model.styles.BrokenStyleRemediator} - repairs to a styles part.</li>
 * </ul>
 *
 * <p>The resolution order that consumes these is {@link org.docx4j.model.PropertyResolver};
 * see {@code docs/developer/change-requests/CR-015-property-resolution.md}.</p>
 *
 * @since 17.1.1 (package-info)
 */
package org.docx4j.model.styles;
