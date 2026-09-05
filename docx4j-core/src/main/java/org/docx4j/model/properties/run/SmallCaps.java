/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

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
import org.docx4j.dml.STTextCapsType;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * w:smallCaps: lower case letters are displayed as smaller capitals.
 *
 * <p>HTML says this with font-variant.  XSL FO has no such property (and FOP
 * would ignore it), so for PDF the text is upper-cased and the originally lower
 * case stretches are put in a smaller inline; see
 * {@link org.docx4j.fonts.RunFontSelector}.  Nothing is emitted here.</p>
 *
 * @since 17.0.5
 */
public class SmallCaps extends AbstractRunProperty {

	public final static String CSS_NAME = "font-variant";

	public String getCssName() {
		return CSS_NAME;
	}

	public SmallCaps(BooleanDefaultTrue val) {
		this.setObject(val);
	}

	public SmallCaps(CSSValue value) {
		BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
		bdt.setVal("small-caps".equalsIgnoreCase(value.getCssText().trim()));
		this.setObject(bdt);
	}

	@Override
	public String getCssProperty() {
		return composeCss(CSS_NAME, ((BooleanDefaultTrue)this.getObject()).isVal() ? "small-caps" : "normal");
	}

	@Override
	public void setXslFO(Element foElement) {
		// nothing: RunFontSelector transforms the text instead
	}

	@Override
	public void set(CTTextCharacterProperties rPr) { // DrawingML
		rPr.setCap(((BooleanDefaultTrue)this.getObject()).isVal() ? STTextCapsType.SMALL : STTextCapsType.NONE);
	}

	@Override
	public void set(RPr rPr) {
		rPr.setSmallCaps((BooleanDefaultTrue)this.getObject());
	}
}
