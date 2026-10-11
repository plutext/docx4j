/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.color;

/**
 * A DrawingML fill as far as colour resolution goes: none, a solid colour (resolved), or a
 * gradient, pattern, picture or group fill, which is reported as such and not guessed at -
 * drawing one is a renderer's business.  For a line, the fill is the line's.
 *
 * @since 17.3.3 (CR-034)
 */
public final class StyleFill {

	public enum Kind {
		/** a:noFill, or a style reference with index 0 */
		NONE,
		/** a:solidFill: {@link #getColor()} is its colour */
		SOLID,
		/** a:gradFill */
		GRADIENT,
		/** a:pattFill */
		PATTERN,
		/** a:blipFill */
		PICTURE,
		/** a:grpFill: the group's fill */
		GROUP,
		/** nothing stated, or a style reference whose index is outside the theme's lists */
		UNKNOWN
	}

	static final StyleFill NONE = new StyleFill(Kind.NONE, null, null);
	static final StyleFill UNKNOWN = new StyleFill(Kind.UNKNOWN, null, null);

	private final Kind kind;
	private final ResolvedColor color;
	private final Object fill;

	StyleFill(Kind kind, ResolvedColor color, Object fill) {
		this.kind = kind;
		this.color = color;
		this.fill = fill;
	}

	public Kind getKind() {
		return kind;
	}

	/** @return the colour of a {@link Kind#SOLID} fill, else null */
	public ResolvedColor getColor() {
		return color;
	}

	/** @return the JAXB fill object (a:gradFill and so on), for a renderer; null for none */
	public Object getFill() {
		return fill;
	}

	@Override
	public String toString() {
		return kind == Kind.SOLID ? "solid " + color : kind.name().toLowerCase();
	}
}
