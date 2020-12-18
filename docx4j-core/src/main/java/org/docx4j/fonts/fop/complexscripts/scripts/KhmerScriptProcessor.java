/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.docx4j.fonts.fop.complexscripts.scripts;

import java.util.List;

import org.docx4j.fonts.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphTable;
import org.docx4j.fonts.fop.complexscripts.util.CharAssociation;
import org.docx4j.fonts.fop.complexscripts.util.GlyphContextTester;
import org.docx4j.fonts.fop.complexscripts.util.GlyphSequence;
import org.docx4j.fonts.fop.complexscripts.util.ScriptContextTester;
import org.docx4j.fonts.fop.fonts.MultiByteFont;

/**
 * <p>The <code>KhmerScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Khmer script.</p>
 */
public class KhmerScriptProcessor extends IndicScriptProcessor {
    private GlyphSequence unprocessedGS;
    private List associations;
    private int[] chars;

    KhmerScriptProcessor(String script) {
        super(script);
    }

    protected Class<? extends IndicScriptProcessor.DefaultSyllabizer> getSyllabizerClass() {
        return KhmerSyllabizer.class;
    }

    private static class KhmerSyllabizer extends DefaultSyllabizer {
        KhmerSyllabizer(String script, String language) {
            super(script, language);
        }
    }

    @Override
    public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence glyphSequence,
                                               int[] unscaledWidths, int[][] glyphPositionAdjustments, String script,
                                               String language) {
        return glyphSequence;
    }

    public CharSequence preProcess(CharSequence charSequence, MultiByteFont font, List associations) {
        unprocessedGS = font.charSequenceToGlyphSequence(charSequence, associations);
        return new KhmerRenderer().render(charSequence.toString());
    }

    public boolean position(GlyphSequence glyphSequence, String script, String language, int fontSize,
                            GlyphTable.UseSpec[] useSpecs, int[] widths, int[][] adjustments,
                            ScriptContextTester scriptContextTester) {
        glyphSequence.setUnprocessedGS(unprocessedGS);
        return super.position(glyphSequence, script, language, fontSize, useSpecs, widths, adjustments,
                scriptContextTester);
    }

    public GlyphSequence substitute(GlyphSequence glyphSequence, String script, String language,
                                    GlyphTable.UseSpec[] useSpecs, ScriptContextTester scriptContextTester) {
        glyphSequence = super.substitute(glyphSequence, script, language, useSpecs, scriptContextTester);
        associations = glyphSequence.getAssociations();
        chars = glyphSequence.getCharacters().array();
        return glyphSequence;
    }

    private ScriptContextTester scriptContextTester = new ScriptContextTester() {
        private GlyphContextTester tester = new GlyphContextTester() {
            public boolean test(String script, String language, String feature, GlyphSequence glyphSequence, int index,
                                int flags) {
                CharAssociation charAssociation = (CharAssociation) associations.get(index);
                char vowelSignU = '\u17BB';
                for (int i = charAssociation.getStart(); i < charAssociation.getEnd(); i++) {
                    if (chars[i] == vowelSignU) {
                        return false;
                    }
                }
                return true;
            }
        };
        public GlyphContextTester getTester(String feature) {
            return tester;
        }
    };

    public ScriptContextTester getPositioningContextTester() {
        return scriptContextTester;
    }
}
