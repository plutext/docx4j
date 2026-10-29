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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.docx4j.docProps.core.dc.elements.ObjectFactory;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * The text model's INFO behaves as the field it names does in the text model (CR-067 §12.3, python-64's Q9,
 * 2026-10-28): {@code INFO Title} is computed as TITLE is; {@code INFO Date}, {@code INFO Time} and
 * {@code INFO PrintDate} keep their cached result as DATE, TIME and PRINTDATE do, where the registry's INFO
 * would evaluate them.
 */
public class FieldValuesInfoTest {

	@Test
	public void infoNamesOnlyWhatTheTextModelComputes() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		SimpleLiteral title = new SimpleLiteral();
		title.getContent().add("The Title");
		pkg.getDocPropsCorePart().getJaxbElement().setTitle(new ObjectFactory().createTitle(title));
		FieldValues values = new FieldValues(pkg);
		assertEquals("The Title", values.compute(" INFO Title "));
		assertEquals("The Title", values.compute(" TITLE "));
		assertNull(values.compute(" DATE "));
		assertNull("as DATE", values.compute(" INFO Date "));
		assertNull("as TIME", values.compute(" INFO Time "));
		assertNull("as PRINTDATE", values.compute(" INFO PrintDate "));
		assertNull(values.compute(" INFO "));
		assertNull(values.compute(" INFO Nope "));
	}
}
