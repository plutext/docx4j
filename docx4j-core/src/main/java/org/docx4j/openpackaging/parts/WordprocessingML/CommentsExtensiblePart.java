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

package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * word/commentsExtensible.xml: Word 2018's durable id and UTC date per comment,
 * which the comment reactions part ([MS-OREACTXML]) anchors on.  Before 17.1.1
 * there was no schema for the w16cex namespace, so this part loaded as a
 * generic XML part - its bytes preserved, but nothing able to read a comment's
 * durableId or dateUtc through the model.
 *
 * @author jharrop
 * @since 17.1.1
 */
public final class CommentsExtensiblePart extends JaxbXmlPart<org.docx4j.w16cex.CTCommentsExtensible> {

	public CommentsExtensiblePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public CommentsExtensiblePart() throws InvalidFormatException {
		super(new PartName("/word/commentsExtensible.xml"));
		init();
	}

	public void init() {
		// Used if this Part is added to [Content_Types].xml
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType(
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_COMMENTS_EXTENSIBLE));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.COMMENTS_EXTENSIBLE);
	}

	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {

		// May not be necessary with RI since it seems to write every namespace under the sun,
		// but MOXy (tested 2.7.4) needs setMceIgnorable.

		// NB it is up to you to jaxbElement.setIgnorable; see further McIgnorableNamespaceDeclarator
		namespacePrefixMapper.setMcIgnorable(
				this.getJaxbElement().getIgnorable() );
	}

	@Override
	public String getMceIgnorable() {
    	return this.getJaxbElement().getIgnorable();
    }

}
