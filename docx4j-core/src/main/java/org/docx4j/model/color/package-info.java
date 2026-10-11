/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Colours as Office draws them: any colour reference in a docx, pptx or xlsx resolved to the
 * RGB value (and alpha) Word, PowerPoint or Excel draws for it.
 *
 * <h2>Which theme</h2>
 *
 * A reference resolves against a {@link org.docx4j.model.color.ThemeColors}: a theme's colour
 * scheme and the colour map that leads to it, made for the reference's host.
 * <ul>
 * <li>a docx: {@link org.docx4j.model.color.ThemeColors#of(org.docx4j.openpackaging.packages.WordprocessingMLPackage)},
 *     the main document part's theme and the settings' {@code w:clrSchemeMapping}, which applies
 *     to WordprocessingML theme colours and DrawingML scheme colours alike.  The same object is
 *     {@link org.docx4j.model.PropertyResolver#getThemeColors()}.</li>
 * <li>a slide, layout, master, notes slide, notes or handout master:
 *     {@link org.docx4j.model.color.ThemeColors#of(org.docx4j.openpackaging.parts.Part)}, its
 *     master's theme with the master's {@code p:clrMap}, then the layout's and the slide's
 *     {@code p:clrMapOvr}.</li>
 * <li>an xlsx: {@link org.docx4j.model.color.ThemeColors#of(org.docx4j.openpackaging.packages.SpreadsheetMLPackage)}.</li>
 * <li>a chart: {@link org.docx4j.model.color.ThemeColors#forChart(org.docx4j.openpackaging.parts.DrawingML.Chart)}
 *     on its host's: a chart is drawn in its host's theme unless it has a theme override part,
 *     then its {@code c:clrMapOvr}.</li>
 * </ul>
 * A package with no theme part gets the theme Office supplies, the bundled one
 * {@code docx4j.fonts.defaultTheme} names (Office 2023's by default).
 *
 * <h2>The references</h2>
 *
 * <ul>
 * <li>{@link org.docx4j.model.color.WordColors}: WordprocessingML's {@code w:color},
 *     {@code w:shd}, the borders and {@code w:u}.  Word draws the theme reference
 *     ({@code w:themeColor}, {@code w:themeFill}) and keeps the value beside it as a cache, which
 *     can be stale; {@code w:themeTint} and {@code w:themeShade} change the HSL luminance, the
 *     tint winning where both are given.</li>
 * <li>{@link org.docx4j.model.color.DrawingMLColors}: the six DrawingML colour kinds and their
 *     transforms, applied in document order (tint, shade, inverse, gamma and the channel
 *     transforms in linear RGB; hue, saturation and luminance in HSL; gray as Rec. 709 luma); a
 *     system colour is the machine's, not its {@code lastClr}.  A shape's style references
 *     ({@code a:fillRef}, {@code a:lnRef}, {@code a:fontRef}) resolve through the theme's format
 *     scheme; an entry that is not a solid fill comes back as a
 *     {@link org.docx4j.model.color.StyleFill} of its kind, not guessed at.</li>
 * <li>{@link org.docx4j.model.color.SpreadsheetColors}: SpreadsheetML's {@code rgb},
 *     {@code theme} (whose first four indexes Excel reads as lt1, dk1, lt2, dk2), {@code indexed}
 *     (the legacy palette or the workbook's {@code x:indexedColors}) and {@code tint}, in
 *     Windows' integer HLS as Excel computes it.</li>
 * </ul>
 * Each returns a {@link org.docx4j.model.color.ResolvedColor}, or null for "automatic", which
 * is the caller's default.
 *
 * <h2>Where docx4j uses it</h2>
 *
 * The PDF and HTML outputs draw a WordprocessingML theme colour as Word does, through
 * {@link org.docx4j.model.PropertyResolver#getThemeColors()} and the property classes of
 * {@code org.docx4j.model.properties} ({@code Property.setThemeColors}).  They do not paint
 * DrawingML fills or outlines, of any colour kind; that is separate work.
 *
 * <h2>How it was measured</h2>
 *
 * Every rule here was read from Office's own renderings: probes of 1,068 cases run through
 * Word, PowerPoint and Excel, and the 36,919 WordprocessingML theme attributes of four
 * real-document corpora against the values Word wrote beside them.  The probes, Office's
 * readings and the corpus tint and shade triples are in docx4j-core-tests'
 * {@code src/test/resources/theme-colors}, the test vectors for docx4j's TypeScript and Python
 * ports.  The design and the readings are in
 * {@code docs/developer/change-requests/CR-034-theme-colours-resolved.md}.
 *
 * @since 17.3.3
 */
package org.docx4j.model.color;
