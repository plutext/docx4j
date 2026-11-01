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
package org.docx4j.utils;

import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.sax.SAXSource;

import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * The element-depth limit docx4j sets on the XML parsers it creates, so that how deep a part may be
 * nested is docx4j's decision and the same on every Java version.
 *
 * <p>Java 25 ships {@code jdk.xml.maxElementDepth=100} in {@code conf/jaxp.properties} (Java 24 and
 * before: no limit). A part nested deeper (32 tables in tables, say, or an FO 25 tables deep) failed to
 * parse: its content was unreadable, and PDF output failed. docx4j's own limit,
 * {@value #DEFAULT_MAX_ELEMENT_DEPTH}, is well above any real document (the deepest of 1,118 in the
 * layout-fidelity corpora is 39) and below the depth at which docx4j's recursive code runs out of stack
 * (about 700 on a 1 MB stack, in JAXB's marshal), so a hostile part is refused as unreadable rather
 * than killing the thread with a StackOverflowError.</p>
 *
 * <p>{@code docx4j.xml.maxElementDepth} in docx4j.properties changes it, 0 meaning no limit. Where that
 * is not set and the JVM's own {@code jdk.xml.maxElementDepth} system property is, docx4j leaves the
 * JVM's setting in force. A parser that does not know the property (another StAX or DOM
 * implementation) keeps its own default, with a warning logged once.</p>
 *
 * @since 17.3.6
 */
public final class XmlParserLimits {

	private static final Logger log = LoggerFactory.getLogger(XmlParserLimits.class);

	/** The docx4j.properties key */
	public static final String MAX_ELEMENT_DEPTH_PROPERTY = "docx4j.xml.maxElementDepth";

	public static final int DEFAULT_MAX_ELEMENT_DEPTH = 256;

	/** The JDK parsers' name for the limit, the one Java 8 to 26 all accept, through SAX, StAX and DOM */
	static final String JDK_MAX_ELEMENT_DEPTH = "http://www.oracle.com/xml/jaxp/properties/maxElementDepth";
	private static final String JDK_SYSTEM_PROPERTY = "jdk.xml.maxElementDepth";
	private static final String WOODSTOX_MAX_ELEMENT_DEPTH = "com.ctc.wstx.maxElementDepth";
	private static final String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

	private static final AtomicBoolean WARNED = new AtomicBoolean(false);

	private XmlParserLimits() {}

	/**
	 * The limit docx4j sets: docx4j.properties' value, else {@value #DEFAULT_MAX_ELEMENT_DEPTH};
	 * null where docx4j.properties sets none and the JVM's jdk.xml.maxElementDepth is set, so
	 * that the JVM's setting stands. 0 is no limit.
	 */
	public static Integer maxElementDepth() {
		String own = Docx4jProperties.getProperty(MAX_ELEMENT_DEPTH_PROPERTY);
		if (own != null && !own.trim().isEmpty()) {
			try {
				int n = Integer.parseInt(own.trim());
				if (n >= 0) return n;
			} catch (NumberFormatException e) {
				// as below
			}
			log.warn(MAX_ELEMENT_DEPTH_PROPERTY + "=" + own + " is not a whole number of 0 or more; using "
					+ DEFAULT_MAX_ELEMENT_DEPTH);
			return DEFAULT_MAX_ELEMENT_DEPTH;
		}
		if (System.getProperty(JDK_SYSTEM_PROPERTY) != null) return null;
		return DEFAULT_MAX_ELEMENT_DEPTH;
	}

	/** Sets the limit on a StAX factory: the JDK's property, else Woodstox's. */
	public static XMLInputFactory apply(XMLInputFactory factory) {
		Integer depth = maxElementDepth();
		if (depth == null) return factory;
		try {
			factory.setProperty(JDK_MAX_ELEMENT_DEPTH, depth);
		} catch (IllegalArgumentException e) {
			try {
				// Woodstox: 0 is not "no limit" there
				factory.setProperty(WOODSTOX_MAX_ELEMENT_DEPTH, depth == 0 ? Integer.MAX_VALUE : depth);
			} catch (IllegalArgumentException e2) {
				warnOnce(factory.getClass());
			}
		}
		return factory;
	}

	/** Sets the limit on a DOM factory (after any secure-processing feature, which resets the JDK's limits). */
	public static DocumentBuilderFactory apply(DocumentBuilderFactory factory) {
		Integer depth = maxElementDepth();
		if (depth == null) return factory;
		try {
			factory.setAttribute(JDK_MAX_ELEMENT_DEPTH, depth);
		} catch (IllegalArgumentException e) {
			warnOnce(factory.getClass());
		}
		return factory;
	}

	/** Sets the limit on a SAX parser. */
	public static SAXParser apply(SAXParser parser) {
		Integer depth = maxElementDepth();
		if (depth == null) return parser;
		try {
			parser.setProperty(JDK_MAX_ELEMENT_DEPTH, depth);
		} catch (SAXException e) {
			warnOnce(parser.getClass());
		}
		return parser;
	}

	/**
	 * XML text as a Source that a Transformer parses with docx4j's limit, where a StreamSource would be
	 * parsed with the JDK's defaults. The reader is the one a Transformer makes for a StreamSource
	 * (namespace-aware, without namespace-prefix attributes), with the limit: Xalan asks a reader it is
	 * given to report namespace prefixes too, which this one declines, so the events are the same.
	 */
	public static SAXSource source(Reader xml) throws javax.xml.parsers.ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XMLReader reader = apply(factory.newSAXParser()).getXMLReader();
		XMLFilterImpl asTransformerMakesIt = new XMLFilterImpl(reader) {
			@Override
			public void setFeature(String name, boolean value)
					throws org.xml.sax.SAXNotRecognizedException, SAXNotSupportedException {
				if (NAMESPACE_PREFIXES.equals(name) && value) {
					throw new SAXNotSupportedException(name);
				}
				super.setFeature(name, value);
			}
		};
		return new SAXSource(asTransformerMakesIt, new InputSource(xml));
	}

	private static void warnOnce(Class<?> parser) {
		if (!WARNED.getAndSet(true)) {
			log.warn("Could not set the XML element-depth limit on " + parser.getName() + "; its own default applies");
		}
	}
}
