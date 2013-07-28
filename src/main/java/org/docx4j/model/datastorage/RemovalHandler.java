/*
 * Copyright 2011 Plutext Pty Ltd and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.model.datastorage;

import static org.docx4j.XmlUtils.getTransformerTemplate;
import static org.docx4j.XmlUtils.marshaltoW3CDomDocument;
import static org.docx4j.XmlUtils.prepareJAXBResult;
import static org.docx4j.XmlUtils.transform;
import static org.docx4j.model.datastorage.RemovalHandler.Quantifier.ALL;
import static org.docx4j.model.datastorage.RemovalHandler.Quantifier.NAMED;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.ArrayUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Document;

/**
 * Tool to remove Structured Document Tags from an OpenXML document part.
 *
 * <p>
 * This tool removes SDTs tagged with a certain quantifier from the document part. It supports to remove any set of
 * <code>od:xpath</code>, <code>od:repeat</code> and <code>od:condition</code> tags.
 * </p>
 * <p>
 * Restrictions:
 * </p>
 * <ul>
 * <li>
 * As it does text processing and no real namespace qualification exist, the quantifiers must be prefixed exactly be
 * "od", not any other prefix.</li>
 * <li>
 * In case of qualified removal (in effect not {@link Quantifier#ALL}), bindings containing more than one qualifier are
 * not supported, that is, when you tag <code>od:repeat=/this&amp;od:xpath=/that</code>, the SDT is removed whenever you
 * specify to remove either repeat or bind tags.</li>
 * </ul>
 *
 * @author Karsten Tinnefeld
 * @version $Revision: $ $Date: $
 */
public class RemovalHandler {

        private static final String templateFile = RemovalHandler.class.getName()
                        .replace('.', '/')
                        + ".xslt";

        private final Templates removalTemplate;

        /**
         * Initializes the removal handler.
         *
         * This tool is thread safe and should be reused, as initialization is
         * relatively expensive.
         */
        public RemovalHandler() {

                final InputStream templateStream = getClass().getClassLoader()
                                .getResourceAsStream(templateFile);
                final Source templateSource = new StreamSource(templateStream);
                try {
                        this.removalTemplate = getTransformerTemplate(templateSource);

                } catch (TransformerConfigurationException e) {
                        throw new IllegalStateException(
                                        "Error instantiating SDT removal stylesheet", e);
                }
        }

        public void removeSDTs(WordprocessingMLPackage wordMLPackage,
                        final Quantifier quantifier, final String... keys) throws Docx4JException {

                // A component can apply in both the main document part,
                // and in headers/footers. See further
                // http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html


                removeSDTs(wordMLPackage.getMainDocumentPart(), quantifier, keys);

                // Add headers/footers
                RelationshipsPart rp = wordMLPackage.getMainDocumentPart()
                                .getRelationshipsPart();
                for (Relationship r : rp.getRelationships().getRelationship()) {

                        if (r.getType().equals(Namespaces.HEADER)) {
                                removeSDTs((HeaderPart) rp.getPart(r), quantifier, keys);
                        } else if (r.getType().equals(Namespaces.FOOTER)) {
                                removeSDTs((FooterPart) rp.getPart(r), quantifier, keys);
                        }
                }
        }

        /**
         * Removes Structured Document Tags from a document part, preserving their
         * contents.
         *
         * In case key "empty" is specified, value bindings (xpath) are removed only
         * if they have void contents (e.g. the XML points nowhere).
         *
         * @param part
         *            The document part to modify (in situ).
         * @param quantifier
         *            The quantifier regarding which kinds of parts are to be
         *            removed.
         * @param keys
         *            In case of {@link Quantifier#NAMED}, quantifier names. All
         *            strings expect "xpath", "condition", "repeat", "empty" are
         *            ignored.
         * @throws Docx4JException
         *             In case any transformation error occurs.
         */
        public void removeSDTs(final JaxbXmlPart<? extends Object> part,
                        final Quantifier quantifier, final String... keys)
                        throws Docx4JException {

                final Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("all", quantifier == ALL);
                if (quantifier == NAMED)
                        parameters.put("types", ArrayUtils.toString(keys));

                final Document partDOM = marshaltoW3CDomDocument(part.getJaxbElement());
                final JAXBResult result = prepareJAXBResult(Context.jc);

                transform(partDOM, removalTemplate, parameters, result);

                try {
                        part.setJaxbElement(result);

                } catch (JAXBException e) {
                        throw new Docx4JException(
                                        "Error unmarshalling document part for SDT removal", e);
                }
        }

        /**
         * A quantifier specifying kinds of SDTs.
         */
        public static enum Quantifier {

                /**
                 * Every SDT shall be removed.
                 */
                ALL,

                /**
                 * Named SDTs shall be removed, the names given separately.
                 */
                NAMED,

                /**
                 * The default SDTs shall be removed, that is, condition and repeat.
                 */
                DEFAULT;
        }
}
