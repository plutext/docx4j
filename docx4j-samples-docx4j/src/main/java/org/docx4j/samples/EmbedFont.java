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
package org.docx4j.samples;

import java.io.File;

import org.docx4j.Docx4J;
import org.docx4j.fonts.FontEmbedder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Fonts;

/**
 * Embed a TrueType font file in a docx, so that Word draws the document in it on a
 * machine that lacks the font (CR-028).
 *
 * <pre>EmbedFont in.docx NotoNaskhArabic-Regular.ttf out.docx</pre>
 *
 * <p>The font is embedded whole, obfuscated as Word does it, with the font table entry
 * (signature, charset, family, pitch, panose) Word needs before it will use an embedded
 * font, and the w:embedTrueTypeFonts setting.  Runs then reference it by its family name
 * in w:rFonts, as they would an installed font; the entry's name is printed.</p>
 *
 * <p>The font's OS/2 fsType is honoured: a restricted-licence or bitmap-only font is
 * refused with a {@link FontEmbedder.RefusedException}.  The flag is not the licence; make
 * sure the font's licence permits embedding in a document you distribute (open-licence
 * fonts such as the OFL and Apache ones do).</p>
 */
public class EmbedFont {

	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			System.err.println("usage: EmbedFont in.docx font.ttf out.docx [Family Name] [REGULAR|BOLD|ITALIC|BOLD_ITALIC]");
			return;
		}
		String fontName = args.length > 3 ? args[3] : null;
		FontEmbedder.Style style = args.length > 4 ? FontEmbedder.Style.valueOf(args[4]) : FontEmbedder.Style.REGULAR;

		WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(args[0]));

		Fonts.Font entry = FontEmbedder.embed(wordMLPackage, new File(args[1]), fontName, style);
		System.out.println("Embedded as w:font w:name=\"" + entry.getName() + "\" (" + style + ")");

		Docx4J.save(wordMLPackage, new File(args[2]));
	}
}
