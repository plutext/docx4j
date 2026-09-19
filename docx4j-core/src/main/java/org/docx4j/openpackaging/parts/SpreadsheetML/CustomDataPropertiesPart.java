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
package org.docx4j.openpackaging.parts.SpreadsheetML;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDatastoreItem;

/**
 * The properties of an add-in's custom data ([MS-XLSX] 2.1.3, x14:datastoreItem), which relate to the binary CustomDataPart.
 * Related from the workbook.
 *
 * @since 17.1.1 (CR-022)
 */
public class CustomDataPropertiesPart extends JaxbSmlPart<CTDatastoreItem> {

	public CustomDataPropertiesPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public CustomDataPropertiesPart() throws InvalidFormatException {
		super(new PartName("/xl/customDataProps/customDataProps1.xml"));
		init();
	}

	public void init() {
		// Used if this Part is added to [Content_Types].xml
		setContentType(new ContentType(ContentTypes.SPREADSHEETML_CUSTOM_DATA_PROPERTIES));
		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.SPREADSHEETML_CUSTOM_DATA_PROPERTIES);
	}

}
