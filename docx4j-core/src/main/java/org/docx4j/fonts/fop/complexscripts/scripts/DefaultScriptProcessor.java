/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
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

import org.docx4j.fonts.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.docx4j.fonts.fop.complexscripts.util.CharAssociation;
import org.docx4j.fonts.fop.complexscripts.util.GlyphSequence;
import org.docx4j.fonts.fop.complexscripts.util.ScriptContextTester;

// CSOFF: LineLengthCheck

/**
 * <p>Default script processor, which enables default glyph composition/decomposition, common ligatures, localized forms
 * and kerning.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public class DefaultScriptProcessor extends ScriptProcessor {

    /** features to use for substitutions */
    private static final String[] GSUB_FEATURES =
    {
        "ccmp",                                                 // glyph composition/decomposition
        "liga",                                                 // common ligatures
        "locl"                                                  // localized forms
    };

    /** features to use for positioning */
    private static final String[] GPOS_FEATURES =
    {
        "kern",                                                 // kerning
        "mark",                                                 // mark to base or ligature positioning
        "mkmk"                                                  // mark to mark positioning
    };

    DefaultScriptProcessor(String script) {
        super(script);
    }

    @Override
    /** {@inheritDoc} */
    public String[] getSubstitutionFeatures() {
        return GSUB_FEATURES;
    }

    @Override
    /** {@inheritDoc} */
    public ScriptContextTester getSubstitutionContextTester() {
        return null;
    }

    @Override
    /** {@inheritDoc} */
    public String[] getPositioningFeatures() {
        return GPOS_FEATURES;
    }

    @Override
    /** {@inheritDoc} */
    public ScriptContextTester getPositioningContextTester() {
        return null;
    }

    @Override
    /** {@inheritDoc} */
    public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] unscaledWidths, int[][] gpa, String script, String language) {
        int   ng  = gs.getGlyphCount();
        int[] ga  = gs.getGlyphArray(false);
        int   nm  = 0;
        // count combining marks
        for (int i = 0; i < ng; i++) {
            int gid = ga [ i ];
            int gw = unscaledWidths [ i ];
            if (isReorderedMark(gdef, ga, unscaledWidths, i)) {
                nm++;
            }
        }
        // only reorder if there is at least one mark and at least one non-mark glyph
        if ((nm > 0) && ((ng - nm) > 0)) {
            CharAssociation[] aa = gs.getAssociations(0, -1);
            int[] nga = new int [ ng ];
            int[][] npa = (gpa != null) ? new int [ ng ][] : null;
            CharAssociation[] naa = new CharAssociation [ ng ];
            int k = 0;
            CharAssociation ba = null;
            int bg = -1;
            int[] bpa = null;
            for (int i = 0; i < ng; i++) {
                int gid = ga [ i ];
                int[] pa = (gpa != null) ? gpa [ i ] : null;
                CharAssociation ca = aa [ i ];
                if (isReorderedMark(gdef, ga, unscaledWidths, i)) {
                    nga [ k ] = gid;
                    naa [ k ] = ca;
                    if (npa != null) {
                        npa [ k ] = pa;
                    }
                    k++;
                } else {
                    if (bg != -1) {
                        nga [ k ] = bg;
                        naa [ k ] = ba;
                        if (npa != null) {
                            npa [ k ] = bpa;
                        }
                        k++;
                        bg = -1;
                        ba = null;
                        bpa = null;
                    }
                    if (bg == -1) {
                        bg = gid;
                        ba = ca;
                        bpa = pa;
                    }
                }
            }
            if (bg != -1) {
                nga [ k ] = bg;
                naa [ k ] = ba;
                if (npa != null) {
                    npa [ k ] = bpa;
                }
                k++;
            }
            assert k == ng;
            if (npa != null) {
                System.arraycopy(npa, 0, gpa, 0, ng);
            }
            return new GlyphSequence(gs, null, nga, null, null, naa, null);
        } else {
            return gs;
        }
    }

    protected boolean isReorderedMark(GlyphDefinitionTable gdef, int[] glyphs, int[] unscaledWidths, int index) {
        return gdef.isGlyphClass(glyphs[index], GlyphDefinitionTable.GLYPH_CLASS_MARK) && (unscaledWidths[index] != 0);
    }

}
