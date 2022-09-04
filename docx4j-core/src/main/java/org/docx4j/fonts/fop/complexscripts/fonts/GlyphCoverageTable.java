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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// CSOFF: LineLengthCheck

/**
 * <p>.Base class implementation of glyph coverage table.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public final class GlyphCoverageTable extends GlyphMappingTable implements GlyphCoverageMapping {

    /* logging instance */
    private static final  Logger log = LoggerFactory.getLogger(GlyphCoverageTable.class);

    /** empty mapping table */
    public static final int GLYPH_COVERAGE_TYPE_EMPTY = GLYPH_MAPPING_TYPE_EMPTY;

    /** mapped mapping table */
    public static final int GLYPH_COVERAGE_TYPE_MAPPED = GLYPH_MAPPING_TYPE_MAPPED;

    /** range based mapping table */
    public static final int GLYPH_COVERAGE_TYPE_RANGE = GLYPH_MAPPING_TYPE_RANGE;

    private GlyphCoverageMapping cm;

    private GlyphCoverageTable(GlyphCoverageMapping cm) {
        assert cm != null;
        assert cm instanceof GlyphMappingTable;
        this.cm = cm;
    }

    /** {@inheritDoc} */
    public int getType() {
        return ((GlyphMappingTable) cm) .getType();
    }

    /** {@inheritDoc} */
    public List getEntries() {
        return ((GlyphMappingTable) cm) .getEntries();
    }

    /** {@inheritDoc} */
    public int getCoverageSize() {
        return cm.getCoverageSize();
    }

    /** {@inheritDoc} */
    public int getCoverageIndex(int gid) {
        return cm.getCoverageIndex(gid);
    }

    /**
     * Create glyph coverage table.
     * @param entries list of mapped or ranged coverage entries, or null or empty list
     * @return a new covera table instance
     */
    public static GlyphCoverageTable createCoverageTable(List entries) {
        GlyphCoverageMapping cm;
        if ((entries == null) || (entries.size() == 0)) {
            cm = new EmptyCoverageTable(entries);
        } else if (isMappedCoverage(entries)) {
            cm = new MappedCoverageTable(entries);
        } else if (isRangeCoverage(entries)) {
            cm = new RangeCoverageTable(entries);
        } else {
            cm = null;
        }
        assert cm != null : "unknown coverage type";
        return new GlyphCoverageTable(cm);
    }

    private static boolean isMappedCoverage(List entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            for (Object o : entries) {
                if (!(o instanceof Integer)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isRangeCoverage(List entries) {
        if ((entries == null) || (entries.size() == 0)) {
            return false;
        } else {
            for (Object o : entries) {
                if (!(o instanceof MappingRange)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class EmptyCoverageTable extends GlyphMappingTable.EmptyMappingTable implements GlyphCoverageMapping {
        public EmptyCoverageTable(List entries) {
            super(entries);
        }
        /** {@inheritDoc} */
        public int getCoverageSize() {
            return 0;
        }
        /** {@inheritDoc} */
        public int getCoverageIndex(int gid) {
            return -1;
        }
    }

    private static class MappedCoverageTable extends GlyphMappingTable.MappedMappingTable implements GlyphCoverageMapping {
        private int[] map;
        public MappedCoverageTable(List entries) {
            populate(entries);
        }
        /** {@inheritDoc} */
        public List getEntries() {
            List entries = new java.util.ArrayList();
            if (map != null) {
                entries = Arrays.stream(map).boxed().collect(Collectors.toList());
            }
            return entries;
        }
        /** {@inheritDoc} */
        public int getMappingSize() {
            return (map != null) ? map.length : 0;
        }
        public int getMappedIndex(int gid) {
            int i;
            if ((i = Arrays.binarySearch(map, gid)) >= 0) {
                return i;
            } else {
                return -1;
            }
        }
        /** {@inheritDoc} */
        public int getCoverageSize() {
            return getMappingSize();
        }
        /** {@inheritDoc} */
        public int getCoverageIndex(int gid) {
            return getMappedIndex(gid);
        }
        private void populate(List entries) {
            int i = 0;
            int skipped = 0;
            int n = entries.size();
            int gidMax = -1;
            int[] map = new int [ n ];
            for (Object o : entries) {
                if (o instanceof Integer) {
                    int gid = (Integer) o;
                    if ((gid >= 0) && (gid < 65536)) {
                        if (gid > gidMax) {
                            map[i++] = gidMax = gid;
                        } else {
                            log.info("ignoring out of order or duplicate glyph index: " + gid);
                            skipped++;
                        }
                    } else {
                        throw new AdvancedTypographicTableFormatException("illegal glyph index: " + gid);
                    }
                } else {
                    throw new AdvancedTypographicTableFormatException("illegal coverage entry, must be Integer: " + o);
                }
            }
            assert (i + skipped) == n;
            assert this.map == null;
            this.map = map;
        }
        /** {@inheritDoc} */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            for (int i = 0, n = map.length; i < n; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(Integer.toString(map [ i ]));
            }
            sb.append('}');
            return sb.toString();
        }
    }

    private static class RangeCoverageTable extends GlyphMappingTable.RangeMappingTable implements GlyphCoverageMapping {
        public RangeCoverageTable(List entries) {
            super(entries);
        }
        /** {@inheritDoc} */
        public int getMappedIndex(int gid, int s, int m) {
            return m + gid - s;
        }
        /** {@inheritDoc} */
        public int getCoverageSize() {
            return getMappingSize();
        }
        /** {@inheritDoc} */
        public int getCoverageIndex(int gid) {
            return getMappedIndex(gid);
        }
    }

}
