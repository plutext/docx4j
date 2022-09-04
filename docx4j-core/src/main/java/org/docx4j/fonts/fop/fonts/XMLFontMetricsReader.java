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

//Java
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.fonts.fop.apps.FOPException;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.apps.TTFReader;
import org.docx4j.org.apache.poi.util.XMLHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>Class for reading a metric.xml file and creating a font object.
 * Typical usage:</p>
 * <pre>
 * XMLFontMetricsReader reader = new XMLFontMetricsReader(&lt;path til metrics.xml&gt;);
 * reader.setFontEmbedPath(&lt;path to a .ttf or .pfb file or null to diable embedding&gt;);
 * reader.useKerning(true);
 * Font f = reader.getFont();
 * </pre>
 * <p><strong>N.B. This is deprecated functionality and is expected to be
 * removed from a future version of FOP. New applications using FOP should
 * not make direct or implied use of this mechanism.</strong></p>
 */
@Deprecated
public class XMLFontMetricsReader extends DefaultHandler {

    private boolean isCID;
    private CustomFont returnFont;
    private MultiByteFont multiFont;
    private SingleByteFont singleFont;
    private final InternalResourceResolver resourceResolver;
    private StringBuffer text = new StringBuffer();

    private List<Integer> cidWidths;
    //private int cidWidthIndex;

    private Map<Integer, Integer> currentKerning;

    private List<CMapSegment> bfranges;

    /**
     * Construct a XMLFontMetricsReader object from a path to a metric.xml file
     * and read metric data
     * @param source Source of the font metric file
     * @throws FOPException if loading the font fails
     */
    public XMLFontMetricsReader(InputSource source, InternalResourceResolver resourceResolver) throws FOPException {
        this.resourceResolver = resourceResolver;
        createFont(source);
    }

    private void createFont(InputSource source) throws FOPException {
        XMLReader parser = null;

        try {
            parser = XMLHelper.newXMLReader();
        } catch (Exception e) {
            throw new FOPException(e);
        }
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }

        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXException e) {
            throw new FOPException("You need a SAX parser which supports SAX version 2", e);
        }

        parser.setContentHandler(this);

        try {
            parser.parse(source);
        } catch (SAXException e) {
            throw new FOPException(e);
        } catch (IOException e) {
            throw new FOPException(e);
        }

    }

    /**
     * Sets the path to embed a font. A null value disables font embedding.
     * @param path URI for the embeddable file
     */
    public void setFontEmbedURI(URI path) {
        returnFont.setEmbedURI(path);
    }

    /**
     * Enable/disable use of kerning for the font
     * @param enabled true to enable kerning, false to disable
     */
    public void setKerningEnabled(boolean enabled) {
        returnFont.setKerningEnabled(enabled);
    }

    /**
     * Enable/disable use of advanced typographic features for the font
     * @param enabled true to enable, false to disable
     */
    public void setAdvancedEnabled(boolean enabled) {
        returnFont.setAdvancedEnabled(enabled);
    }

    /**
     * Get the generated font object
     * @return the font
     */
    public Typeface getFont() {
        return returnFont;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() {
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (localName.equals("font-metrics")) {
            if ("TYPE0".equals(attributes.getValue("type"))) {
                multiFont = new MultiByteFont(resourceResolver, EmbeddingMode.AUTO);
                returnFont = multiFont;
                isCID = true;
                TTFReader.checkMetricsVersion(attributes);
            } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
                singleFont = new SingleByteFont(resourceResolver, EmbeddingMode.AUTO);
                singleFont.setFontType(FontType.TRUETYPE);
                returnFont = singleFont;
                isCID = false;
                TTFReader.checkMetricsVersion(attributes);
            } else {
                singleFont = new SingleByteFont(resourceResolver, EmbeddingMode.AUTO);
                singleFont.setFontType(FontType.TYPE1);
                returnFont = singleFont;
                isCID = false;
            }
        } else if ("embed".equals(localName)) {
            try {
                returnFont.setEmbedURI(InternalResourceResolver.cleanURI(attributes.getValue("file")));
            } catch (URISyntaxException e) {
                throw new SAXException("URI syntax error in metrics file: " + e.getMessage(), e);
            }
            returnFont.setEmbedResourceName(attributes.getValue("class"));
        } else if ("cid-widths".equals(localName)) {
            // This is unused
            // cidWidthIndex = getInt(attributes.getValue("start-index"));
            cidWidths = new ArrayList<Integer>();
        } else if ("kerning".equals(localName)) {
            currentKerning = new HashMap<Integer, Integer>();
            returnFont.putKerningEntry(getInt(attributes.getValue("kpx1")),
                    currentKerning);
        } else if ("bfranges".equals(localName)) {
            bfranges = new ArrayList<CMapSegment>();
        } else if ("bf".equals(localName)) {
            CMapSegment entry = new CMapSegment(getInt(attributes.getValue("us")),
                                        getInt(attributes.getValue("ue")),
                                        getInt(attributes.getValue("gi")));
            bfranges.add(entry);
        } else if ("wx".equals(localName)) {
            cidWidths.add(getInt(attributes.getValue("w")));
            // } else if ("widths".equals(localName)) {
            //    singleFont.width = new int[256];
        } else if ("char".equals(localName)) {
            try {
                singleFont.setWidth(getInt(attributes.getValue("idx")),
                        getInt(attributes.getValue("wdt")));
            } catch (NumberFormatException ne) {
                throw new SAXException("Malformed width in metric file: " + ne.getMessage(), ne);
            }
        } else if ("pair".equals(localName)) {
            currentKerning.put(getInt(attributes.getValue("kpx2")),
                    getInt(attributes.getValue("kern")));
        }

    }

    private static int getInt(String str) throws SAXException {
        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {
            throw new SAXException("Error while parsing integer value: " + str, e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String content = text.toString().trim();
        if ("font-name".equals(localName)) {
            returnFont.setFontName(content);
        } else if ("full-name".equals(localName)) {
            returnFont.setFullName(content);
        } else if ("family-name".equals(localName)) {
            Set<String> s = new HashSet<String>();
            s.add(content);
            returnFont.setFamilyNames(s);
        } else if ("ttc-name".equals(localName) && isCID) {
            multiFont.setTTCName(content);
        } else if ("encoding".equals(localName)) {
            if (singleFont != null && singleFont.getFontType() == FontType.TYPE1) {
                singleFont.setEncoding(content);
            }
        } else if ("cap-height".equals(localName)) {
            returnFont.setCapHeight(getInt(content));
        } else if ("x-height".equals(localName)) {
            returnFont.setXHeight(getInt(content));
        } else if ("ascender".equals(localName)) {
            returnFont.setAscender(getInt(content));
        } else if ("descender".equals(localName)) {
            returnFont.setDescender(getInt(content));
        } else if ("left".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[0] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("bottom".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[1] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("right".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[2] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("top".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[3] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("first-char".equals(localName)) {
            returnFont.setFirstChar(getInt(content));
        } else if ("last-char".equals(localName)) {
            returnFont.setLastChar(getInt(content));
        } else if ("flags".equals(localName)) {
            returnFont.setFlags(getInt(content));
        } else if ("stemv".equals(localName)) {
            returnFont.setStemV(getInt(content));
        } else if ("italic-angle".equals(localName)) {
            returnFont.setItalicAngle(getInt(content));
        } else if ("missing-width".equals(localName)) {
            returnFont.setMissingWidth(getInt(content));
        } else if ("cid-type".equals(localName)) {
            multiFont.setCIDType(CIDFontType.byName(content));
        } else if ("default-width".equals(localName)) {
            multiFont.setDefaultWidth(getInt(content));
        } else if ("cid-widths".equals(localName)) {
            int[] wds = new int[cidWidths.size()];
            int j = 0;
            for (Integer cidWidth : cidWidths) {
                wds[j++] = cidWidth;
            }

            //multiFont.addCIDWidthEntry(cidWidthIndex, wds);
            multiFont.setWidthArray(wds);

        } else if ("bfranges".equals(localName)) {
            multiFont.setCMap(bfranges.toArray(new CMapSegment[bfranges.size()]));
        }
        text.setLength(0); //Reset text buffer (see characters())
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        text.append(ch, start, length);
    }
}
