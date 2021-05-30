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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.fonts.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphPositioningTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphTable;
import org.docx4j.fonts.fop.complexscripts.util.CharScript;
import org.docx4j.fonts.fop.complexscripts.util.GlyphSequence;
import org.docx4j.fonts.fop.complexscripts.util.ScriptContextTester;
import org.docx4j.fonts.fop.fonts.MultiByteFont;

// CSOFF: LineLengthCheck

/**
 * <p>Abstract script processor base class for which an implementation of the substitution and positioning methods
 * must be supplied.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public abstract class ScriptProcessor {

    private final String script;

    private final Map<AssembledLookupsKey, GlyphTable.UseSpec[]> assembledLookups;

    /**
     * Instantiate a script processor.
     * @param script a script identifier
     */
    protected ScriptProcessor(String script) {
        if ((script == null) || (script.length() == 0)) {
            throw new IllegalArgumentException("script must be non-empty string");
        } else {
            this.script = script;
            this.assembledLookups = new HashMap<AssembledLookupsKey, GlyphTable.UseSpec[]>();
        }
    }

    /** @return script identifier */
    public final String getScript() {
        return script;
    }

    /**
     * Obtain script specific required substitution features.
     * @return array of suppported substitution features or null
     */
    public abstract String[] getSubstitutionFeatures();

    /**
     * Obtain script specific optional substitution features.
     * @return array of suppported substitution features or null
     */
    public String[] getOptionalSubstitutionFeatures() {
        return new String[0];
    }

    /**
     * Obtain script specific substitution context tester.
     * @return substitution context tester or null
     */
    public abstract ScriptContextTester getSubstitutionContextTester();

    /**
     * Perform substitution processing using a specific set of lookup tables.
     * @param gsub the glyph substitution table that applies
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param lookups a mapping from lookup specifications to glyph subtables to use for substitution processing
     * @return the substituted (output) glyph sequence
     */
    public final GlyphSequence substitute(GlyphSubstitutionTable gsub, GlyphSequence gs, String script, String language,
                                          Map<GlyphTable.LookupSpec, List<GlyphTable.LookupTable>> lookups) {
        return substitute(gs, script, language, assembleLookups(gsub, getSubstitutionFeatures(), lookups), getSubstitutionContextTester());
    }

    /**
     * Perform substitution processing using a specific set of ordered glyph table use specifications.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param usa an ordered array of glyph table use specs
     * @param sct a script specific context tester (or null)
     * @return the substituted (output) glyph sequence
     */
    public GlyphSequence substitute(GlyphSequence gs, String script, String language, GlyphTable.UseSpec[] usa, ScriptContextTester sct) {
        assert usa != null;
        for (GlyphTable.UseSpec us : usa) {
            gs = us.substitute(gs, script, language, sct);
        }
        return gs;
    }

    /**
     * Reorder combining marks in glyph sequence so that they precede (within the sequence) the base
     * character to which they are applied. N.B. In the case of RTL segments, marks are not reordered by this,
     * method since when the segment is reversed by BIDI processing, marks are automatically reordered to precede
     * their base glyph.
     * @param gdef the glyph definition table that applies
     * @param gs an input glyph sequence
     * @param unscaledWidths associated unscaled advance widths (also reordered)
     * @param gpa associated glyph position adjustments (also reordered)
     * @param script a script identifier
     * @param language a language identifier
     * @return the reordered (output) glyph sequence
     */
    public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] unscaledWidths, int[][] gpa, String script, String language) {
        return gs;
    }

    /**
     * Obtain script specific required positioning features.
     * @return array of suppported positioning features or null
     */
    public abstract String[] getPositioningFeatures();

    /**
     * Obtain script specific optional positioning features.
     * @return array of suppported positioning features or null
     */
    public String[] getOptionalPositioningFeatures() {
        return new String[0];
    }

    /**
     * Obtain script specific positioning context tester.
     * @return positioning context tester or null
     */
    public abstract ScriptContextTester getPositioningContextTester();

    /**
     * Perform positioning processing using a specific set of lookup tables.
     * @param gpos the glyph positioning table that applies
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param fontSize size in device units
     * @param lookups a mapping from lookup specifications to glyph subtables to use for positioning processing
     * @param widths array of default advancements for each glyph
     * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
     * with one 4-tuple for each element of glyph sequence
     * @return true if some adjustment is not zero; otherwise, false
     */
    public final boolean position(GlyphPositioningTable gpos, GlyphSequence gs, String script, String language, int fontSize,
                                  Map<GlyphTable.LookupSpec, List<GlyphTable.LookupTable>> lookups, int[] widths, int[][] adjustments) {
        return position(gs, script, language, fontSize, assembleLookups(gpos, getPositioningFeatures(), lookups), widths, adjustments, getPositioningContextTester());
    }

    /**
     * Perform positioning processing using a specific set of ordered glyph table use specifications.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param fontSize size in device units
     * @param usa an ordered array of glyph table use specs
     * @param widths array of default advancements for each glyph in font
     * @param adjustments accumulated adjustments array (sequence) of 4-tuples of placement [PX,PY] and advance [AX,AY] adjustments, in that order,
     * with one 4-tuple for each element of glyph sequence
     * @param sct a script specific context tester (or null)
     * @return true if some adjustment is not zero; otherwise, false
     */
    public boolean position(GlyphSequence gs, String script, String language, int fontSize, GlyphTable.UseSpec[] usa, int[] widths, int[][] adjustments, ScriptContextTester sct) {
        assert usa != null;
        boolean adjusted = false;
        for (GlyphTable.UseSpec us : usa) {
            if (us.position(gs, script, language, fontSize, widths, adjustments, sct)) {
                adjusted = true;
            }
        }
        return adjusted;
    }

    /**
     * Assemble ordered array of lookup table use specifications according to the specified features and candidate lookups,
     * where the order of the array is in accordance to the order of the applicable lookup list.
     * @param table the governing glyph table
     * @param features array of feature identifiers to apply
     * @param lookups a mapping from lookup specifications to lists of look tables from which to select lookup tables according to the specified features
     * @return ordered array of assembled lookup table use specifications
     */
    public final GlyphTable.UseSpec[] assembleLookups(GlyphTable table, String[] features,
                                                      Map<GlyphTable.LookupSpec, List<GlyphTable.LookupTable>> lookups) {
        AssembledLookupsKey key = new AssembledLookupsKey(table, features, lookups);
        GlyphTable.UseSpec[] usa;
        if ((usa = assembledLookupsGet(key)) != null) {
            return usa;
        } else {
            return assembledLookupsPut(key, table.assembleLookups(features, lookups));
        }
    }

    private GlyphTable.UseSpec[] assembledLookupsGet(AssembledLookupsKey key) {
        return assembledLookups.get(key);
    }

    private GlyphTable.UseSpec[]  assembledLookupsPut(AssembledLookupsKey key, GlyphTable.UseSpec[] usa) {
        assembledLookups.put(key, usa);
        return usa;
    }

    /**
     * Obtain script processor instance associated with specified script.
     * @param script a script identifier
     * @return a script processor instance or null if none found
     */
    public static synchronized ScriptProcessor getInstance(String script, Map<String, ScriptProcessor> processors) {
        ScriptProcessor sp = null;
        assert processors != null;
        if ((sp = processors.get(script)) == null) {
            processors.put(script, sp = createProcessor(script));
        }
        return sp;
    }

    // [TBD] - rework to provide more configurable binding between script name and script processor constructor
    private static ScriptProcessor createProcessor(String script) {
        ScriptProcessor sp = null;
        int sc = CharScript.scriptCodeFromTag(script);
        if (sc == CharScript.SCRIPT_ARABIC) {
            sp = new ArabicScriptProcessor(script);
        } else if (CharScript.isIndicScript(sc)) {
            sp = IndicScriptProcessor.makeProcessor(script);
        } else {
            sp = new DefaultScriptProcessor(script);
        }
        return sp;
    }

    private static class AssembledLookupsKey {

        private final GlyphTable table;
        private final String[] features;
        private final Map<GlyphTable.LookupSpec, List<GlyphTable.LookupTable>> lookups;

        AssembledLookupsKey(GlyphTable table, String[] features, Map<GlyphTable.LookupSpec, List<GlyphTable.LookupTable>> lookups) {
            this.table = table;
            this.features = features;
            this.lookups = lookups;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            int hc = 0;
            hc =  7 * hc + (hc ^ table.hashCode());
            hc = 11 * hc + (hc ^ Arrays.hashCode(features));
            hc = 17 * hc + (hc ^ lookups.hashCode());
            return hc;
        }

        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if (o instanceof AssembledLookupsKey) {
                AssembledLookupsKey k = (AssembledLookupsKey) o;
                if (!table.equals(k.table)) {
                    return false;
                } else if (!Arrays.equals(features, k.features)) {
                    return false;
                } else {
                    return lookups.equals(k.lookups);
                }
            } else {
                return false;
            }
        }

    }

    public CharSequence preProcess(CharSequence charSequence, MultiByteFont font, List associations) {
        return charSequence;
    }
}
