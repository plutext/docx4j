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
 * {@code w:u/@w:val} read as Word 365 reads it (CR-060 sections 12 and 13): an ST_Underline value, compared
 * case-exactly; the six on/off spellings {@code true}, {@code false}, {@code on}, {@code off}, {@code 1} and
 * {@code 0}, exactly, as {@code none}, true and false alike (over an underlining style Word draws no underline, and
 * saves {@code none}); anything else as absent (null), so a style's underline beneath still shows ({@code bogus},
 * {@code Single}, {@code True}, {@code yes}, {@code ""}).
 *
 * <p>Without it JAXB read the on/off spellings as absent too, so docx4j drew a style's underline where Word draws
 * none, and a save wrote a bare {@code w:u}, which turned the underline on in Word. Written back as the value it
 * read, so {@code true} is saved as {@code none}, as Word saves it.</p>
 *
 * @since 17.3.5
 */
public class UnderlineValueAdapter extends XmlAdapter<String, UnderlineEnumeration> {

	@Override
	public UnderlineEnumeration unmarshal(String v) {
		if (v == null) return null;
		try {
			return UnderlineEnumeration.fromValue(v);
		} catch (IllegalArgumentException e) {
			switch (v) {
			case "true": case "false": case "on": case "off": case "1": case "0":
				return UnderlineEnumeration.NONE;
			default:
				return null;
			}
		}
	}

	@Override
	public String marshal(UnderlineEnumeration v) {
		return v == null ? null : v.value();
	}
}
