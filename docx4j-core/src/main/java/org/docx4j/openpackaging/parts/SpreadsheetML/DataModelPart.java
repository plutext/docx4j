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
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * The Excel data model ([MS-XLDM]; content type application/vnd.openxmlformats-officedocument.model+data),
 * the binary store behind x15:dataModel in the workbook's extLst. Related from the workbook. Kept as bytes.
 *
 * @since 17.1.1 (CR-022)
 */
public class DataModelPart extends BinaryPart {

	public DataModelPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DataModelPart() throws InvalidFormatException {
		super(new PartName("/xl/model/item.data"));
		init();
	}

	public void init() {
		setContentType(new ContentType(ContentTypes.SPREADSHEETML_DATA_MODEL));
		setRelationshipType(Namespaces.SPREADSHEETML_DATA_MODEL);
	}

}
