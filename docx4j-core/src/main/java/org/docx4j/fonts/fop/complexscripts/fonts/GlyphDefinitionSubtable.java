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

package org.docx4j.fonts.fop.complexscripts.fonts;

// CSOFF: LineLengthCheck

/**
 * <p>The <code>GlyphDefinitionSubtable</code> implements an abstract base of a glyph definition subtable,
 * providing a default implementation of the <code>GlyphDefinition</code> interface.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public abstract class GlyphDefinitionSubtable extends GlyphSubtable implements GlyphDefinition {

    /**
     * Instantiate a <code>GlyphDefinitionSubtable</code>.
     * @param id subtable identifier
     * @param sequence subtable sequence
     * @param flags subtable flags
     * @param format subtable format
     * @param mapping subtable coverage table
     */
    protected GlyphDefinitionSubtable(String id, int sequence, int flags, int format, GlyphMappingTable mapping) {
        super(id, sequence, flags, format, mapping);
    }

    /** {@inheritDoc} */
    public int getTableType() {
        return GlyphTable.GLYPH_TABLE_TYPE_DEFINITION;
    }

    /** {@inheritDoc} */
    public String getTypeName() {
        return GlyphDefinitionTable.getLookupTypeName(getType());
    }

    /** {@inheritDoc} */
    public boolean usesReverseScan() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean hasDefinition(int gi) {
        GlyphCoverageMapping cvm;
        if ((cvm = getCoverage()) != null) {
            if (cvm.getCoverageIndex(gi) >= 0) {
                return true;
            }
        }
        GlyphClassMapping clm;
        if ((clm = getClasses()) != null) {
            if (clm.getClassIndex(gi, 0) >= 0) {
                return true;
            }
        }
        return false;
    }

}
