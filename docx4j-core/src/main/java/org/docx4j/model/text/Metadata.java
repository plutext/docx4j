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
package org.docx4j.model.text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.OpcPackage;

/**
 * The document properties of a result's metadata (WD REQ-515): core, application and custom, by
 * their element or property names, read without changing the package (no part is added).
 */
final class Metadata {

	private Metadata() {}

	/** The properties into {@code metadata}, under {@link TextResult#CORE}, {@link TextResult#APP} and {@link TextResult#CUSTOM}. */
	static void read(OpcPackage pkg, Map<String, Object> metadata) {
		if (pkg == null) return;
		if (pkg.getDocPropsCorePart() != null && pkg.getDocPropsCorePart().getJaxbElement() != null) {
			metadata.put(TextResult.CORE, core(pkg.getDocPropsCorePart().getJaxbElement()));
		}
		if (pkg.getDocPropsExtendedPart() != null && pkg.getDocPropsExtendedPart().getJaxbElement() != null) {
			metadata.put(TextResult.APP, app(pkg.getDocPropsExtendedPart().getJaxbElement()));
		}
		if (pkg.getDocPropsCustomPart() != null && pkg.getDocPropsCustomPart().getJaxbElement() != null) {
			metadata.put(TextResult.CUSTOM, custom(pkg.getDocPropsCustomPart().getJaxbElement()));
		}
	}

	/** The core properties in the schema's order. */
	private static Map<String, Object> core(CoreProperties c) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		put(m, "title", literal(c.getTitle()));
		put(m, "subject", literal(c.getSubject()));
		put(m, "creator", literal(c.getCreator()));
		put(m, "keywords", c.getKeywords());
		put(m, "description", literal(c.getDescription()));
		put(m, "category", c.getCategory());
		put(m, "contentStatus", c.getContentStatus());
		put(m, "language", literal(c.getLanguage()));
		put(m, "identifier", literal(c.getIdentifier()));
		put(m, "version", c.getVersion());
		put(m, "revision", c.getRevision());
		put(m, "lastModifiedBy", c.getLastModifiedBy());
		put(m, "created", literal(c.getCreated()));
		put(m, "modified", literal(c.getModified()));
		put(m, "lastPrinted", date(c.getLastPrinted()));
		return m;
	}

	/** The application properties that describe the document; the statistics, which depend on layout, left out. */
	private static Map<String, Object> app(org.docx4j.docProps.extended.Properties p) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		put(m, "Application", p.getApplication());
		put(m, "AppVersion", p.getAppVersion());
		put(m, "Company", p.getCompany());
		put(m, "Manager", p.getManager());
		put(m, "Template", p.getTemplate());
		put(m, "HyperlinkBase", p.getHyperlinkBase());
		put(m, "TotalTime", p.getTotalTime());
		put(m, "DocSecurity", p.getDocSecurity());
		return m;
	}

	/** Each custom property's typed value (a string, number, boolean or W3C date), in the part's order; values of other types left out. */
	private static Map<String, Object> custom(org.docx4j.docProps.custom.Properties p) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		for (org.docx4j.docProps.custom.Properties.Property property : p.getProperty()) {
			if (property.getName() == null) continue;
			put(m, property.getName(), value(property));
		}
		return m;
	}

	private static Object value(org.docx4j.docProps.custom.Properties.Property p) {
		if (p.getLpwstr() != null) return p.getLpwstr();
		if (p.getLpstr() != null) return p.getLpstr();
		if (p.getBstr() != null) return p.getBstr();
		if (p.isBool() != null) return p.isBool();
		if (p.getI1() != null) return p.getI1();
		if (p.getI2() != null) return p.getI2();
		if (p.getI4() != null) return p.getI4();
		if (p.getI8() != null) return p.getI8();
		if (p.getInt() != null) return p.getInt();
		if (p.getUi1() != null) return p.getUi1();
		if (p.getUi2() != null) return p.getUi2();
		if (p.getUi4() != null) return p.getUi4();
		if (p.getUi8() != null) return p.getUi8();
		if (p.getUint() != null) return p.getUint();
		if (p.getR4() != null) return p.getR4();
		if (p.getR8() != null) return p.getR8();
		if (p.getDecimal() != null) return p.getDecimal();
		if (p.getFiletime() != null) return date(p.getFiletime());
		if (p.getDate() != null) return date(p.getDate());
		if (p.getCy() != null) return p.getCy();
		if (p.getError() != null) return p.getError();
		return null;
	}

	private static void put(Map<String, Object> m, String name, Object value) {
		if (value == null) return;
		if (value instanceof String && ((String) value).isEmpty()) return;
		m.put(name, value);
	}

	private static String literal(Object o) {
		Object v = o instanceof JAXBElement ? ((JAXBElement<?>) o).getValue() : o;
		if (!(v instanceof SimpleLiteral)) return null;
		List<String> content = ((SimpleLiteral) v).getContent();
		return content == null || content.isEmpty() ? null : String.join("", content);
	}

	private static String date(XMLGregorianCalendar c) {
		return c == null ? null : c.toXMLFormat();
	}
}
