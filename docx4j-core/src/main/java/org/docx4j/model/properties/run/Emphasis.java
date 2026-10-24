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
package org.docx4j.model.properties.run;

import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.wml.CTEm;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STEm;
import org.w3c.dom.Element;

/**
 * A run's emphasis mark ({@code w:em}) as CSS draws it (CR-062 D4): {@code dot} a filled dot above, {@code comma} a
 * filled sesame (CSS's nearest to Word's comma), {@code circle} an open circle, {@code underDot} a filled dot below.
 * XSL-FO has no emphasis marks, so PDF draws none (D3).
 *
 * @since 17.3.5
 */
public class Emphasis extends AbstractRunProperty {

	public final static String CSS_NAME = "text-emphasis-style";

	public Emphasis(CTEm em) {
		this.setObject(em);
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public String getCssProperty() {
		STEm val = ((CTEm) this.getObject()).getVal();
		if (val == null) return CSS_NULL;
		switch (val) {
		case DOT: return composeCss(CSS_NAME, "filled dot");
		case COMMA: return composeCss(CSS_NAME, "filled sesame");
		case CIRCLE: return composeCss(CSS_NAME, "open circle");
		case UNDER_DOT: return composeCss(CSS_NAME, "filled dot") + composeCss("text-emphasis-position", "under right");
		default: return composeCss(CSS_NAME, "none");
		}
	}

	@Override
	public void setXslFO(Element foElement) {
		// XSL-FO has no emphasis marks
	}

	@Override
	public void set(RPr rPr) {
		rPr.setEm((CTEm) this.getObject());
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		// DrawingML has no emphasis marks
	}
}
