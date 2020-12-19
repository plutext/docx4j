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
package org.docx4j.fonts.fop.fonts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.type1.PFBData;
import org.docx4j.fonts.fop.fonts.type1.PFBParser;
import org.docx4j.fonts.fop.fonts.type1.Type1SubsetFile;
import org.docx4j.fonts.fop.render.ps.Type1FontFormatter;

public class CFFToType1Font extends MultiByteFont {

    public CFFToType1Font(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
        super(resourceResolver, embeddingMode);
        setEmbeddingMode(EmbeddingMode.FULL);
        setFontType(FontType.TYPE1);
    }

    public InputStream getInputStream() throws IOException {
        return null;
    }

    public List<InputStream> getInputStreams() throws IOException {
        InputStream cff = super.getInputStream();
        return convertOTFToType1(cff);
    }

    private List<InputStream> convertOTFToType1(InputStream in) throws IOException {
        CFFFont f = new CFFParser().parse(IOUtils.toByteArray(in)).get(0);
        List<InputStream> fonts = new ArrayList<InputStream>();
        Map<Integer, Integer> glyphs = cidSet.getGlyphs();
        int i = 0;
        for (Map<Integer, Integer> x : splitGlyphs(glyphs)) {
            String iStr = "." + i;
            fonts.add(convertOTFToType1(x, f, iStr));
            i++;
        }
        return fonts;
    }

    private List<Map<Integer, Integer>> splitGlyphs(Map<Integer, Integer> glyphs) {
        List<Map<Integer, Integer>> allGlyphs = new ArrayList<Map<Integer, Integer>>();
        for (Map.Entry<Integer, Integer> x : glyphs.entrySet()) {
            int k = x.getKey();
            int v = x.getValue();
            int pot = v / 256;
            v =  v % 256;
            while (allGlyphs.size() < pot + 1) {
                Map<Integer, Integer> glyphsPerFont = new HashMap<Integer, Integer>();
                glyphsPerFont.put(0, 0);
                allGlyphs.add(glyphsPerFont);
            }
            allGlyphs.get(pot).put(k, v);
        }
        return allGlyphs;
    }

    private InputStream convertOTFToType1(Map<Integer, Integer> glyphs, CFFFont cffFont, String splitGlyphsId)
        throws IOException {
        byte[] type1Bytes = new Type1FontFormatter(glyphs).format(cffFont, splitGlyphsId);
        PFBData pfb = new PFBParser().parsePFB(new ByteArrayInputStream(type1Bytes));
        ByteArrayOutputStream s1 = new ByteArrayOutputStream();
        s1.write(pfb.getHeaderSegment());
        ByteArrayOutputStream s2 = new ByteArrayOutputStream();
        s2.write(pfb.getEncryptedSegment());
        ByteArrayOutputStream s3 = new ByteArrayOutputStream();
        s3.write(pfb.getTrailerSegment());
        byte[] out = new Type1SubsetFile().stitchFont(s1, s2, s3);
        return new ByteArrayInputStream(out);
    }
}
