/*
 *  Copyright 2009, Plutext Pty Ltd.
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
package org.docx4j.convert.out.fo;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractBookmarkStartWriter;
import org.docx4j.wml.CTBookmark;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Generate a reference so that the w:bookmarkStart it can be referenced.<br>
 *  The preprocessing step BookmarkMover should have moved any Bookmarks 
 *  to the beginning of a paragraph, therefore an inline should always work.
 */
public class BookmarkStartWriter extends AbstractBookmarkStartWriter {
	private final static Logger log = LoggerFactory.getLogger(BookmarkStartWriter.class);
	
	public BookmarkStartWriter() {
		super();
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
	CTBookmark modelData = (CTBookmark)unmarshalledNode;
	
	if (modelData.getName().equals("_GoBack")) {
		return null;
	}
	
		Element ret = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
		ret.setAttribute("id", modelData.getName());
		return ret;
  }
  
}
