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
package org.docx4j.wml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An on/off value ({@code ST_OnOff}) read as Word 365 reads it: off for exactly {@code false}, {@code off} or
 * {@code 0}, case-sensitive and untrimmed; on for anything else, the empty string included (measured, CR-060
 * section 1). Written back as {@code true} or {@code false} (CR-060 D2).
 *
 * <p>docx4j's schema has typed {@code ST_OnOff} as {@code xsd:boolean} since 2008, whose lexical parse rejected
 * {@code on} and {@code off} (allowed by ECMA-376's 1st edition, and written by Apache POI) and every other spelling
 * silently, leaving the field null: a getter then answered its default, so {@code w:b w:val="off"} read bold. It also
 * trimmed, so {@code " false"} read off where Word reads on. Bound in {@code wml.xsd} to the attributes of that
 * origin, 46 of the 48: the two required ones ({@code checkStyle}, {@code actualPg}) are primitive booleans, which an
 * adapter cannot bind. An absent attribute never reaches it, so what absence means (CR-059) is unchanged.</p>
 *
 * @since 17.3.5
 */
public class OnOffAdapter extends XmlAdapter<String, Boolean> {

	/** Word's reading of a written on/off value; null only for null */
	public static Boolean read(String v) {
		if (v == null) return null;
		return !(v.equals("false") || v.equals("off") || v.equals("0"));
	}

	@Override
	public Boolean unmarshal(String v) {
		return read(v);
	}

	@Override
	public String marshal(Boolean v) {
		return v == null ? null : v.toString();
	}
}
