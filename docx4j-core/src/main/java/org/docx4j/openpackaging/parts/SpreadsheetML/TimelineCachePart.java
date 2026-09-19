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
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTTimelineCacheDefinition;

/**
 * A timeline cache ([MS-XLSX] 2.1.7, x15:timelineCacheDefinition): the date field a timeline filters, and its state.
 * Related from the workbook (Excel 365 writes the 2011 relationship type and the lowercase content type).
 *
 * @since 17.1.1 (CR-022)
 */
public class TimelineCachePart extends JaxbSmlPart<CTTimelineCacheDefinition> {

	public TimelineCachePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public TimelineCachePart() throws InvalidFormatException {
		super(new PartName("/xl/timelineCaches/timelineCache1.xml"));
		init();
	}

	public void init() {
		// Used if this Part is added to [Content_Types].xml
		setContentType(new ContentType(ContentTypes.SPREADSHEETML_TIMELINE_CACHE));
		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.SPREADSHEETML_TIMELINE_CACHE);
	}

}
