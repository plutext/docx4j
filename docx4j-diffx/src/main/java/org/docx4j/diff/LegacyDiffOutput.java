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
package org.docx4j.diff;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.format.XMLDiffOutput;
import org.pageseeder.diffx.token.AttributeToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.XMLTokenType;
import org.pageseeder.diffx.token.impl.SpaceToken;
import org.pageseeder.diffx.xml.Namespace;
import org.pageseeder.diffx.xml.NamespaceSet;
import org.pageseeder.xmlwriter.XMLWriterNSImpl;

/**
 * An {@link XMLDiffOutput} producing the markup which the bundled
 * (pre-17.x) com.topologi.diffx formatters produced, so that the XSLTs
 * in this package (diffx2wml.xslt etc.) keep working unchanged:
 *
 * <ul>
 * <li>the legacy Diff-X namespaces (http://www.topologi.com/2005/Diff-X
 * and its /Insert and /Delete descendants, prefixes dfx/ins/del),
 * declared upfront so they land on the first element written;</li>
 * <li>edited elements marked with dfx:insert="true" / dfx:delete="true"
 * attributes;</li>
 * <li>edited text wrapped in ins/del elements: in the legacy Diff-X
 * namespace (as the patched SafeXMLFormatter did) when
 * prefixInsDelElements is true, or unprefixed (as SmartXMLFormatter,
 * used by Docx4jDriver's divide-and-conquer, did) when false;</li>
 * <li>inserted attributes reported with an ins:name="true" attribute,
 * deleted attributes with del:name="value" only;</li>
 * <li>an XML declaration written at construction, and a flush after
 * every token, so that output interleaves correctly with direct writes
 * to the underlying Writer (see Docx4jDriver.openResult).</li>
 * </ul>
 *
 * Unlike org.pageseeder.diffx.format.DefaultXMLDiffOutput (which this
 * class is otherwise modelled on), namespaces are declared only once,
 * not on every top-level fragment.
 */
public class LegacyDiffOutput implements XMLDiffOutput {

	public static final String BASE_NS_URI = "http://www.topologi.com/2005/Diff-X";
	public static final String INSERT_NS_URI = BASE_NS_URI + "/Insert";
	public static final String DELETE_NS_URI = BASE_NS_URI + "/Delete";

	private final XMLWriterNSImpl xml;

	private final boolean prefixInsDelElements;

	/**
	 * All namespaces declared so far.  The writer forgets a namespace
	 * once the element it was declared on is closed, so when output
	 * consists of several top-level fragments (Docx4jDriver's
	 * divide-and-conquer), these are re-registered at the start of each
	 * fragment; namespaces the writer still knows are not re-declared.
	 */
	private final NamespaceSet known = new NamespaceSet();

	/**
	 * Element depth; 0 means the next element starts a new top-level
	 * fragment.
	 */
	private int level = 0;

	public LegacyDiffOutput(Writer w, boolean prefixInsDelElements) throws IOException {
		this.xml = new XMLWriterNSImpl(w, false);
		this.prefixInsDelElements = prefixInsDelElements;
		this.xml.xmlDecl();
		declare(new Namespace(BASE_NS_URI, "dfx"));
		declare(new Namespace(DELETE_NS_URI, "del"));
		declare(new Namespace(INSERT_NS_URI, "ins"));
	}

	@Override
	public void setWriteXMLDeclaration(boolean show) {
		// the declaration is written at construction, as the legacy formatters did
	}

	@Override
	public void setNamespaces(NamespaceSet namespaces) {
		addNamespaces(namespaces);
	}

	/**
	 * Declare these namespaces; they are written on the next element opened
	 * (the legacy declarePrefixMapping behaviour).
	 */
	public void addNamespaces(NamespaceSet namespaces) {
		for (Namespace namespace : namespaces) {
			declare(namespace);
		}
	}

	private void declare(Namespace namespace) {
		this.known.add(namespace);
		this.xml.setPrefixMapping(namespace.getUri(), namespace.getPrefix());
	}

	@Override
	public void handle(Operator operator, XMLToken token) throws UncheckedIOException {
		if (this.level == 0) {
			// starting a new top-level fragment: the writer has forgotten
			// namespaces declared on earlier fragments
			for (Namespace namespace : this.known) {
				this.xml.setPrefixMapping(namespace.getUri(), namespace.getPrefix());
			}
		}
		if (token.getType() == XMLTokenType.START_ELEMENT) this.level++;
		else if (token.getType() == XMLTokenType.END_ELEMENT) this.level--;
		try {
			switch (operator) {
			case MATCH:
				token.toXML(this.xml);
				break;
			case INS:
				if (token.getType() == XMLTokenType.START_ELEMENT) {
					token.toXML(this.xml);
					this.xml.attribute("dfx:insert", "true");
				} else if (token == SpaceToken.NEW_LINE) {
					token.toXML(this.xml);
				} else if (token.getType() == XMLTokenType.TEXT) {
					openInsDel("ins");
					token.toXML(this.xml);
					this.xml.closeElement();
				} else if (token.getType() == XMLTokenType.ATTRIBUTE) {
					token.toXML(this.xml);
					this.xml.attribute("ins:" + ((AttributeToken) token).getName(), "true");
				} else {
					token.toXML(this.xml);
				}
				break;
			case DEL:
				if (token.getType() == XMLTokenType.START_ELEMENT) {
					token.toXML(this.xml);
					this.xml.attribute("dfx:delete", "true");
				} else if (token == SpaceToken.NEW_LINE) {
					token.toXML(this.xml);
				} else if (token.getType() == XMLTokenType.TEXT) {
					openInsDel("del");
					token.toXML(this.xml);
					this.xml.closeElement();
				} else if (token.getType() == XMLTokenType.ATTRIBUTE) {
					AttributeToken attribute = (AttributeToken) token;
					this.xml.attribute("del:" + attribute.getName(), attribute.getValue());
				} else {
					token.toXML(this.xml);
				}
				break;
			}
			this.xml.flush();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private void openInsDel(String name) throws IOException {
		if (this.prefixInsDelElements) {
			this.xml.openElement(BASE_NS_URI, name, false);
		} else {
			this.xml.openElement(name, false);
		}
	}

}
