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
package org.docx4j.model.shapes;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.w14.CTFillTextEffect;
import org.docx4j.w14.CTTextOutlineEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A run's Word 2010 text effects ({@code w14:textFill}, {@code w14:textOutline}) as the DrawingML they mirror, for
 * docx4j's DrawingML painting (CR-083): w14's fills and lines are DrawingML's elements in another namespace, with their
 * attributes qualified. One difference is measured (phase 0, findings section 1): {@code w14:alpha} is a
 * transparency, where DrawingML's {@code a:alpha} is an opacity (a solid fill with {@code w14:alpha w14:val="40000"}
 * is drawn at 60%), so it is converted to {@code a:alpha} of 100000 less its value.
 *
 * @since 17.3.7
 */
public final class TextEffects {

	private static final Logger log = LoggerFactory.getLogger(TextEffects.class);

	static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";

	static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";

	private TextEffects() {}

	/**
	 * A {@code w14:textFill} as a DrawingML fill: CTNoFillProperties, CTSolidColorFillProperties or
	 * CTGradientFillProperties; null where it has none, or it can't be read
	 */
	public static Object fill(CTFillTextEffect fill) {
		if (fill == null) return null;
		if (fill.getNoFill() != null) return new CTNoFillProperties();
		try {
			if (fill.getSolidFill() != null) {
				return convert(new JAXBElement<org.docx4j.w14.CTSolidColorFillProperties>(new QName(W14, "solidFill"),
						org.docx4j.w14.CTSolidColorFillProperties.class, fill.getSolidFill()), CTSolidColorFillProperties.class);
			}
			if (fill.getGradFill() != null) {
				return convert(new JAXBElement<org.docx4j.w14.CTGradientFillProperties>(new QName(W14, "gradFill"),
						org.docx4j.w14.CTGradientFillProperties.class, fill.getGradFill()), org.docx4j.dml.CTGradientFillProperties.class);
			}
		} catch (Exception e) {
			log.warn("A w14:textFill couldn't be read: " + e.getMessage());
		}
		return null;
	}

	/** A {@code w14:textOutline} as DrawingML's {@code a:ln}; null where it can't be read */
	public static CTLineProperties line(CTTextOutlineEffect outline) {
		if (outline == null) return null;
		try {
			return convert(new JAXBElement<CTTextOutlineEffect>(new QName(W14, "textOutline"), CTTextOutlineEffect.class, outline),
					CTLineProperties.class);
		} catch (Exception e) {
			log.warn("A w14:textOutline couldn't be read: " + e.getMessage());
			return null;
		}
	}

	/** Whether a solid fill's colour is less than opaque */
	public static boolean hasAlpha(CTSolidColorFillProperties fill) {
		Object c = fill.getSrgbClr() != null ? fill.getSrgbClr() : fill.getSchemeClr() != null ? fill.getSchemeClr()
				: fill.getPrstClr() != null ? fill.getPrstClr() : fill.getSysClr() != null ? fill.getSysClr()
				: fill.getScrgbClr() != null ? fill.getScrgbClr() : fill.getHslClr();
		if (c == null) return false;
		try {
			java.lang.reflect.Method m = c.getClass().getMethod("getEGColorTransform");
			for (Object t : (java.util.List<?>) m.invoke(c)) {
				if (t instanceof JAXBElement && "alpha".equals(((JAXBElement<?>) t).getName().getLocalPart())) {
					Object v = ((JAXBElement<?>) t).getValue();
					if (v instanceof org.docx4j.dml.CTPositiveFixedPercentage && ((org.docx4j.dml.CTPositiveFixedPercentage) v).getVal() < 100000) return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/** The w14 element, marshalled, renamed into DrawingML's namespace and read back as the DrawingML class */
	private static <T> T convert(JAXBElement<?> w14, Class<T> as) throws Exception {
		Document source = XmlUtils.marshaltoW3CDomDocument(w14, Context.jc);
		Document target = XmlUtils.getNewDocumentBuilder().newDocument();
		target.appendChild(copy(source.getDocumentElement(), target));
		Object o = XmlUtils.unwrap(XmlUtils.unmarshal(target.getDocumentElement(), Context.jc, as));
		return as.cast(o);
	}

	private static Element copy(Element from, Document into) {
		String ns = W14.equals(from.getNamespaceURI()) ? A : from.getNamespaceURI();
		Element to = into.createElementNS(ns, "a:" + from.getLocalName());
		NamedNodeMap attrs = from.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr a = (Attr) attrs.item(i);
			if ("http://www.w3.org/2000/xmlns/".equals(a.getNamespaceURI())) continue;
			String value = a.getValue();
			// w14:alpha is a transparency; a:alpha an opacity
			if ("alpha".equals(from.getLocalName()) && "val".equals(a.getLocalName())) {
				try {
					value = Integer.toString(Math.max(0, 100000 - Integer.parseInt(value.trim())));
				} catch (NumberFormatException e) {
					// left as written
				}
			}
			to.setAttributeNS(null, a.getLocalName(), value);
		}
		for (Node c = from.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element) to.appendChild(copy((Element) c, into));
		}
		return to;
	}
}
