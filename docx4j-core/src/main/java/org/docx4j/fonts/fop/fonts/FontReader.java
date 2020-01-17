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

/* $Id: FontReader.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts;

//Java
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.docx4j.fonts.fop.apps.FOPException;
import org.docx4j.fonts.fop.fonts.apps.TTFReader;

/**
 * Class for reading a metric.xml file and creating a font object.
 * Typical usage:
 * <pre>
 * FontReader reader = new FontReader(<path til metrics.xml>);
 * reader.setFontEmbedPath(<path to a .ttf or .pfb file or null to diable embedding>);
 * reader.useKerning(true);
 * Font f = reader.getFont();
 * </pre>
 */
public class FontReader extends DefaultHandler {

    private Locator locator = null;
    private boolean isCID = false;
    private CustomFont returnFont = null;
    private MultiByteFont multiFont = null;
    private SingleByteFont singleFont = null;
    private StringBuffer text = new StringBuffer();

    private List cidWidths = null;
    private int cidWidthIndex = 0;

    private Map currentKerning = null;

    private List bfranges = null;

    private void createFont(InputSource source) throws FOPException {
        XMLReader parser = null;

        try {
            final SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            throw new FOPException(e);
        }
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }

        try {
            parser.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception e) {}
        
        try {
            parser.setProperty("http://xml.org/sax/features/external-general-entities", false);
            parser.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
        	
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXException e) {
            throw new FOPException("You need a SAX parser which supports SAX version 2",
                                   e);
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
    public void setFontEmbedPath(String path) {
        returnFont.setEmbedFileName(path);
    }

    /**
     * Enable/disable use of kerning for the font
     * @param enabled true to enable kerning, false to disable
     */
    public void setKerningEnabled(boolean enabled) {
        returnFont.setKerningEnabled(enabled);
    }

    /**
     * Sets the font resolver. Needed for URI resolution.
     * @param resolver the font resolver
     */
    public void setResolver(FontResolver resolver) {
        returnFont.setResolver(resolver);
    }


    /**
     * Get the generated font object
     * @return the font
     */
    public Typeface getFont() {
        return returnFont;
    }

    /**
     * Construct a FontReader object from a path to a metric.xml file
     * and read metric data
     * @param source Source of the font metric file
     * @throws FOPException if loading the font fails
     */
    public FontReader(InputSource source) throws FOPException {
        createFont(source);
    }

    /**
     * {@inheritDoc}
     */
    public void startDocument() {
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if (localName.equals("font-metrics")) {
            if ("TYPE0".equals(attributes.getValue("type"))) {
                multiFont = new MultiByteFont();
                returnFont = multiFont;
                isCID = true;
                TTFReader.checkMetricsVersion(attributes);
            } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TRUETYPE);
                returnFont = singleFont;
                isCID = false;
                TTFReader.checkMetricsVersion(attributes);
            } else {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TYPE1);
                returnFont = singleFont;
                isCID = false;
            }
        } else if ("embed".equals(localName)) {
            returnFont.setEmbedFileName(attributes.getValue("file"));
            returnFont.setEmbedResourceName(attributes.getValue("class"));
        } else if ("cid-widths".equals(localName)) {
            cidWidthIndex = getInt(attributes.getValue("start-index"));
            cidWidths = new java.util.ArrayList();
        } else if ("kerning".equals(localName)) {
            currentKerning = new java.util.HashMap();
            returnFont.putKerningEntry(new Integer(attributes.getValue("kpx1")),
                                        currentKerning);
        } else if ("bfranges".equals(localName)) {
            bfranges = new java.util.ArrayList();
        } else if ("bf".equals(localName)) {
            BFEntry entry = new BFEntry(getInt(attributes.getValue("us")),
                                        getInt(attributes.getValue("ue")),
                                        getInt(attributes.getValue("gi")));
            bfranges.add(entry);
        } else if ("wx".equals(localName)) {
            cidWidths.add(new Integer(attributes.getValue("w")));
        } else if ("widths".equals(localName)) {
            //singleFont.width = new int[256];
        } else if ("char".equals(localName)) {
            try {
                singleFont.setWidth(Integer.parseInt(attributes.getValue("idx")),
                        Integer.parseInt(attributes.getValue("wdt")));
            } catch (NumberFormatException ne) {
                throw new SAXException("Malformed width in metric file: "
                                   + ne.getMessage(), ne);
            }
        } else if ("pair".equals(localName)) {
            currentKerning.put(new Integer(attributes.getValue("kpx2")),
                               new Integer(attributes.getValue("kern")));
        }
    }

    private int getInt(String str) throws SAXException {
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
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String content = text.toString().trim();
        if ("font-name".equals(localName)) {
            returnFont.setFontName(content);
        } else if ("full-name".equals(localName)) {
            returnFont.setFullName(content);
        } else if ("family-name".equals(localName)) {
            Set s = new java.util.HashSet();
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
            for (int count = 0; count < cidWidths.size(); count++) {
                Integer i = (Integer)cidWidths.get(count);
                wds[j++] = i.intValue();
            }

            //multiFont.addCIDWidthEntry(cidWidthIndex, wds);
            multiFont.setWidthArray(wds);

        } else if ("bfranges".equals(localName)) {
            multiFont.setBFEntries((BFEntry[])bfranges.toArray(new BFEntry[0]));
        }
        text.setLength(0); //Reset text buffer (see characters())
    }

    /**
     * {@inheritDoc}
     */
    public void characters(char[] ch, int start, int length) {
        text.append(ch, start, length);
    }
}


