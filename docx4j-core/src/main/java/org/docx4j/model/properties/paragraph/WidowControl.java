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
package org.docx4j.model.properties.paragraph;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.PPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * w:widowControl.  Word's default is on, and so is XSL FO's (widows and orphans
 * default to 2), so this property only needs to be emitted when it is off:
 * widows="1" orphans="1" (CSS: widows/orphans 1).
 *
 * @since 17.0.5
 */
public class WidowControl extends AbstractParagraphProperty {

	public final static String CSS_NAME = "widows";

	public WidowControl(BooleanDefaultTrue val) {
		this.setObject(val);
	}

	public WidowControl(CSSValue value) {
		BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
		bdt.setVal(!"1".equals(value.getCssText().trim()));
		this.setObject(bdt);
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	private boolean isOn() {
		return ((BooleanDefaultTrue) this.getObject()).isVal();
	}

	@Override
	public String getCssProperty() {
		String n = isOn() ? "2" : "1";
		return composeCss("widows", n) + composeCss("orphans", n);
	}

	@Override
	public void setXslFO(Element foElement) {
		String n = isOn() ? "2" : "1";
		foElement.setAttribute("widows", n);
		foElement.setAttribute("orphans", n);
	}

	@Override
	public void set(PPr pPr) {
		pPr.setWidowControl((BooleanDefaultTrue) this.getObject());
	}
}
