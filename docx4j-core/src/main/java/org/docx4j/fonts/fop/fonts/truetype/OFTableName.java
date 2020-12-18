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

package org.docx4j.fonts.fop.fonts.truetype;


/**
 * Represents table names as found in a TrueType font's Table Directory.
 * TrueType fonts may have custom tables so we cannot use an enum.
 */
public final class OFTableName {

    /** The first table in a TrueType font file containing metadata about other tables. */
    public static final OFTableName TABLE_DIRECTORY = new OFTableName("tableDirectory");

    /** Baseline data */
    public static final OFTableName BASE = new OFTableName("BASE");

    /** CFF data/ */
    public static final OFTableName CFF = new OFTableName("CFF ");

    /** Embedded bitmap data. */
    public static final OFTableName EBDT = new OFTableName("EBDT");

    /** Embedded bitmap location data. */
    public static final OFTableName EBLC = new OFTableName("EBLC");

    /** Embedded bitmap scaling data. */
    public static final OFTableName EBSC = new OFTableName("EBSC");

    /** A FontForge specific table. */
    public static final OFTableName FFTM = new OFTableName("FFTM");

    /** Divides glyphs into various classes that make using the GPOS/GSUB tables easier. */
    public static final OFTableName GDEF = new OFTableName("GDEF");

    /** Provides kerning information, mark-to-base, etc. for opentype fonts. */
    public static final OFTableName GPOS = new OFTableName("GPOS");

    /** Provides ligature information, swash, etc. for opentype fonts. */
    public static final OFTableName GSUB = new OFTableName("GSUB");

    /** Linear threshold table. */
    public static final OFTableName LTSH = new OFTableName("LTSH");

    /** OS/2 and Windows specific metrics. */
    public static final OFTableName OS2 = new OFTableName("OS/2");

    /** PCL 5 data. */
    public static final OFTableName PCLT = new OFTableName("PCLT");

    /** Vertical Device Metrics table. */
    public static final OFTableName VDMX = new OFTableName("VDMX");

    /** Character to glyph mapping. */
    public static final OFTableName CMAP = new OFTableName("cmap");

    /** Control Value Table. */
    public static final OFTableName CVT = new OFTableName("cvt ");

    /** Font program. */
    public static final OFTableName FPGM = new OFTableName("fpgm");

    /** Grid-fitting and scan conversion procedure (grayscale). */
    public static final OFTableName GASP = new OFTableName("gasp");

    /** Glyph data. */
    public static final OFTableName GLYF = new OFTableName("glyf");

    /** Horizontal device metrics. */
    public static final OFTableName HDMX = new OFTableName("hdmx");

    /** Font header. */
    public static final OFTableName HEAD = new OFTableName("head");

    /** Horizontal header. */
    public static final OFTableName HHEA = new OFTableName("hhea");

    /** Horizontal metrics. */
    public static final OFTableName HMTX = new OFTableName("hmtx");

    /** Kerning. */
    public static final OFTableName KERN = new OFTableName("kern");

    /** Index to location. */
    public static final OFTableName LOCA = new OFTableName("loca");

    /** Maximum profile. */
    public static final OFTableName MAXP = new OFTableName("maxp");

    /** Naming table. */
    public static final OFTableName NAME = new OFTableName("name");

    /** PostScript information. */
    public static final OFTableName POST = new OFTableName("post");

    /** CVT Program. */
    public static final OFTableName PREP = new OFTableName("prep");

    /** Vertical Metrics header. */
    public static final OFTableName VHEA = new OFTableName("vhea");

    /** Vertical Metrics. */
    public static final OFTableName VMTX = new OFTableName("vmtx");

    private final String name;

    private OFTableName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the table as it should be in the Directory Table.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an instance of this class corresponding to the given string representation.
     * @param tableName table name as in the Table Directory
     * @return TTFTableName
     */
    public static OFTableName getValue(String tableName) {
        if (tableName != null) {
            return new OFTableName(tableName);
        }
        throw new IllegalArgumentException("A TrueType font table name must not be null");
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OFTableName)) {
            return false;
        }
        OFTableName to = (OFTableName) o;
        return this.name.equals(to.getName());
    }

    @Override
    public String toString() {
        return name;
    }

}
