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
package org.docx4j.convert.out.html;

/**
 * A document value written into the HTML stylesheet (CR-056). A style ID becomes a class
 * selector ({@code .Heading1}, {@code span.Strong}, {@code table.TableGrid}) and was written as it
 * is, so a style ID of {@code x</style><script>alert(1)</script>} closed the {@code <style>} element
 * in a browser and ran the script when the page loaded (CWE-79). {@link #escape} is CSSOM's
 * {@code CSS.escape()}: the selector {@code .x\3c \/style\3e …} matches the {@code class} attribute's
 * decoded token, which the serializer already escapes, so the {@code class} attributes do not change
 * and no two IDs collide; a style ID that begins with a digit, which gave an invalid selector before,
 * starts matching. {@link #guard} is the second line for the stylesheet as a whole: no {@code <}
 * leaves it unescaped, whatever path a value took to get there.
 *
 * @since 17.3.5
 */
public final class CssIdentifiers {

	private CssIdentifiers() {}

	/**
	 * CSSOM {@code CSS.escape()}: NUL becomes U+FFFD; a control character, a leading digit, and a
	 * digit after a leading {@code -} become a hex escape ({@code \31 }); a lone {@code -} becomes
	 * {@code \-}; {@code [A-Za-z0-9_-]} and code points at or above U+0080 stay; anything else is
	 * backslash-escaped. One deviation: {@code <} is written as a hex escape ({@code \3c }) rather
	 * than {@code \<}, so that {@link #guard} finds no {@code <} to touch in an escaped identifier.
	 */
	public static String escape(String identifier) {
		if (identifier == null) return null;
		StringBuilder sb = new StringBuilder(identifier.length() + 8);
		int length = identifier.length();
		for (int i = 0; i < length; i++) {
			char c = identifier.charAt(i);
			if (c == 0) {
				sb.append('�');
			} else if ((c >= 1 && c <= 0x1F) || c == 0x7F
					|| (c >= '0' && c <= '9' && (i == 0 || (i == 1 && identifier.charAt(0) == '-')))
					|| c == '<') {
				sb.append('\\').append(Integer.toHexString(c)).append(' ');
			} else if (c == '-' && length == 1) {
				sb.append("\\-");
			} else if (c >= 0x80 || c == '-' || c == '_' || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')
					|| (c >= 'a' && c <= 'z')) {
				sb.append(c);
			} else {
				sb.append('\\').append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * The stylesheet text with every {@code <} written as the CSS escape {@code \3c }, which reads
	 * the same inside a CSS string or identifier and cannot end the {@code <style>} element. Applied
	 * to the stylesheet docx4j composes, not to a caller's own CSS.
	 */
	public static String guard(CharSequence css) {
		if (css == null) return null;
		String s = css.toString();
		return s.indexOf('<') < 0 ? s : s.replace("<", "\\3c ");
	}
}
