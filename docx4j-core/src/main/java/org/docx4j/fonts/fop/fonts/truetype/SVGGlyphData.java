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
package org.docx4j.fonts.fop.fonts.truetype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.xmlgraphics.util.uri.DataURLUtil;

public class SVGGlyphData {
    protected long svgDocOffset;
    protected long svgDocLength;
    private String svg;
    public float scale = 1;

    public void setSVG(String svg) {
        this.svg = svg;
    }

    public String getDataURL(int height) {
        try {
            String modifiedSVG = updateTransform(svg, height);
            return DataURLUtil.createDataURL(
                    new ByteArrayInputStream(modifiedSVG.getBytes(StandardCharsets.UTF_8)), "image/svg");
        } catch (IOException | TransformerException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String updateTransform(String svg, int height)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(new StringReader(svg));
        Document doc = documentBuilder.parse(inputSource);
        NodeList nodes = doc.getElementsByTagName("g");
        Element gElement = (Element) nodes.item(0);
        if (gElement != null) {
            String transform = gElement.getAttribute("transform");
            if (transform.split("scale\\(").length == 3) {
                String scaleStr = transform.split("scale\\(")[1].split("\\)")[0].trim();
                scale = Float.parseFloat(scaleStr);
                gElement.removeAttribute("transform");
            } else {
                gElement.setAttribute("transform", "translate(0," + height + ")");
            }
        } else {
            Element svgElement = (Element) doc.getElementsByTagName("svg").item(0);
            svgElement.setAttribute("viewBox", "0 0 1000 " + height);
            gElement = doc.createElement("g");
            gElement.setAttribute("transform", "translate(0," + height + ")");
            NodeList paths = doc.getElementsByTagName("path");
            for (int i = 0; i < paths.getLength(); i++) {
                Node path = paths.item(i);
                if (i == 0) {
                    path.getParentNode().insertBefore(gElement, path);
                }
                gElement.appendChild(path);
            }
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return result.getWriter().toString();
    }
}
