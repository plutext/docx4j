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
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSlicerCacheDefinition;

/**
 * A slicer cache ([MS-XLSX] 2.1.4, x14:slicerCacheDefinition): the pivot or table field a slicer filters, and its items.
 * Related from the workbook; the slicers that show it are in a SlicersPart.
 *
 * @since 17.2.0 (CR-022)
 */
public class SlicerCachePart extends JaxbSmlPart<CTSlicerCacheDefinition> {

	public SlicerCachePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public SlicerCachePart() throws InvalidFormatException {
		super(new PartName("/xl/slicerCaches/slicerCache1.xml"));
		init();
	}

	public void init() {
		// Used if this Part is added to [Content_Types].xml
		setContentType(new ContentType(ContentTypes.SPREADSHEETML_SLICER_CACHE));
		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.SPREADSHEETML_SLICER_CACHE);
	}

}
