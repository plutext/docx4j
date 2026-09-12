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
 * Fonts: which physical font renders a document font, and which of a run's fonts
 * renders each of its characters.
 *
 * <h2>Two steps</h2>
 *
 * <p><b>Mapping</b> is per document.  {@link org.docx4j.fonts.PhysicalFonts} holds the
 * fonts this machine has - discovered once from the system and from every jar with a
 * {@code fonts/} folder ({@code docx4j-export-fo-fonts-*}), keyed case-insensitively by
 * the font's full name ("Carlito Regular").  A {@link org.docx4j.fonts.Mapper} maps the
 * names a document uses to those fonts: {@link org.docx4j.fonts.IdentityPlusMapper} (the
 * default) and {@link org.docx4j.fonts.BestMatchingMapper} share one order of
 * precedence, applied when {@code WordprocessingMLPackage.setFontMapper} populates the
 * mapper from the names discovery finds ({@code MainDocumentPart.fontsInUse}, a walk
 * over every {@code w:rFonts} the document could ask for):</p>
 * <ol>
 * <li>the installed font of that name;</li>
 * <li>the font the document embeds under that name (extracted from the font table's
 *     obfuscated parts; never added to {@code PhysicalFonts});</li>
 * <li>the mapper's own answer: a name variant (IdentityPlusMapper), or a panose match
 *     and {@code FontSubstitutions.xml} (BestMatchingMapper, taken after the measured
 *     passes below);</li>
 * <li>the metric-compatible clones (Carlito for Calibri, Tinos for Times New Roman...);</li>
 * <li>the document's own {@code w:altName}, followed along its chain;</li>
 * <li>a face of the document font's class ({@link org.docx4j.fonts.FontFallback});</li>
 * <li>for a family none of the tables know, what Word itself shows for a font it
 *     cannot find: Cambria, Calibri or Courier New by the font table's family;</li>
 * <li>last, an alias reporting no bold face for a family which has none (Calibri
 *     Light), so that bold is synthesised at the regular advances, as Word does.</li>
 * </ol>
 *
 * <p><b>Selection</b> is per run.  {@link org.docx4j.fonts.RunFontSelector} resolves the
 * run's effective properties once, resolves each of the four {@code w:rFonts} slots
 * (ascii, hAnsi, eastAsia, cs) through the theme part for the document's theme
 * language, and then assigns each character a slot by the character-range table of
 * [MS-OI29500] 17.3.2.26 - with {@code w:cs}/{@code w:rtl} taking the cs font whole, the
 * symbol fonts mapped through {@code SymbolMapper}, and the departures from the table
 * that were measured against Word (CR-016).  One span per stretch of one font; then,
 * for XSL FO, a glyph-coverage pass that sends what the font cannot draw to a face of
 * the same class which can, the line height from {@link org.docx4j.fonts.WordLineMetrics},
 * kerned spaces, {@code w:w} scaling, small caps and the no-ligature twin.  Every font
 * the conversion reaches is declared to FOP late ({@code FopConfigUtil.declareFallbackFonts}).
 * For HTML the span's {@code font-family} names the document font, the physical family
 * and the generic class.</p>
 *
 * <h2>Properties</h2>
 *
 * <ul>
 * <li>{@code docx4j.fonts.discoverPhysicalFonts.enabled}, {@code docx4j.fonts.discoverJarFonts.enabled},
 *     {@code docx4j.fonts.PhysicalFonts.Jars.PathPrefix}, {@code docx4j.fonts.fontcache} - discovery;</li>
 * <li>{@code docx4j.fonts.automap.enabled}, {@code docx4j.fonts.altName.enabled} - the mapping passes;</li>
 * <li>{@code docx4j.fonts.microsoft.MicrosoftFonts} (and {@code .supplemental}) - the Microsoft font registry;</li>
 * <li>{@code docx4j.fonts.fop.util.FopConfigUtil.simulate-style}, {@code docx4j.fonts.fop.util.FopConfigUtil.substitutions}
 *     - the FOP configuration;</li>
 * <li>{@code docx4j.convert.out.fo.kerning}, {@code docx4j.convert.out.fo.ligatures} - per-run kerning and
 *     ligatures, else the kerned and no-ligature twins;</li>
 * <li>{@code docx4j.fonts.wordLineMetrics.deviceGrid} - Word's 1/600 inch line grid;</li>
 * <li>{@code docx4j.fonts.runFontSelector.trimUnpreservedWhitespace} - the white space of a {@code w:t}
 *     without {@code xml:space="preserve"};</li>
 * <li>{@code docx4j.fonts.RunFontSelector.EmojiFont} - the font for the emoji blocks;</li>
 * <li>{@code docx4j.MicrosoftWord.Numeral}, {@code docx4j.MicrosoftWindows.Region.Format.Numbers.NativeDigits}
 *     - Arabic-Indic digit shaping;</li>
 * <li>{@code docx4j.convert.out.html.fontFamily} - {@code document} (the stack) or {@code physical}.</li>
 * </ul>
 *
 * <p>The design, the measurements against Word and the decisions are in
 * {@code docs/developer/change-requests/CR-016-font-selection-and-mapping.md}; {@code fop/}
 * is docx4j's copy of Apache FOP's font machinery and is outside that review.</p>
 */
package org.docx4j.fonts;
