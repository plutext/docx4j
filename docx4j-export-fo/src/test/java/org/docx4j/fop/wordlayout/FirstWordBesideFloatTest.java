/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * FOP-3331 (FOP 2.12, db3602d17), carried into docx4j's line manager by CR-071 phase 0: a paragraph whose line
 * breaking restarts at its first element, beside a left float, keeps that element where it is a word. FOP's own
 * test case (layoutengine float_12.xml), laid out with docx4j's Word layout.
 */
public class FirstWordBesideFloatTest {

    private static final String FO = "<fo:root writing-mode=\"lr\" xml:lang=\"dflt\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
            + "<fo:layout-master-set><fo:simple-page-master master-name=\"sample\" page-height=\"11in\" page-width=\"8.5in\">"
            + "<fo:region-body margin=\"1in\"/></fo:simple-page-master></fo:layout-master-set>"
            + "<fo:page-sequence master-reference=\"sample\"><fo:flow flow-name=\"xsl-region-body\">"
            + "<fo:block font-family=\"serif\" font-size=\"12pt\">"
            + "<fo:block>Lorem ipsum dolor sit amet, consectetur adipiscing elit. <fo:float float=\"left\">"
            + "<fo:block><fo:block-container background-color=\"yellow\" border-bottom-color=\"red\" border-bottom-style=\"solid\""
            + " border-bottom-width=\"1pt\" border-left-color=\"red\" border-left-style=\"solid\" border-left-width=\"1pt\""
            + " border-right-color=\"red\" border-right-style=\"solid\" border-right-width=\"1pt\" border-top-color=\"red\""
            + " border-top-style=\"solid\" border-top-width=\"1pt\" margin-left=\"0\" margin-right=\"0\" width=\"40pt\">"
            + "<fo:block end-indent=\"0\" start-indent=\"0\">float left</fo:block>"
            + "</fo:block-container></fo:block></fo:float> Nulla dignissim felis sed sapien condimentum, laoreet consectetur dui eleifend.</fo:block>"
            + "</fo:block></fo:flow></fo:page-sequence></fo:root>";

    @Test
    public void theFirstWordBesideALeftFloatIsKept() throws Exception {
        FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
        b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
        FopFactory factory = b.build();
        FOUserAgent ua = factory.newFOUserAgent();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
        TransformerFactory.newInstance().newTransformer().transform(
                new StreamSource(new ByteArrayInputStream(FO.getBytes("UTF-8"))), new SAXResult(fop.getDefaultHandler()));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
        StringBuilder text = new StringBuilder();
        NodeList words = doc.getElementsByTagName("word");
        for (int i = 0; i < words.getLength(); i++) text.append(((Element) words.item(i)).getTextContent()).append(' ');
        String all = text.toString().replaceAll(" +", " ").trim();
        assertTrue(all, all.startsWith("float left Lorem ipsum") || all.startsWith("Lorem ipsum"));
        assertTrue(all, all.contains("Lorem"));
    }
}
