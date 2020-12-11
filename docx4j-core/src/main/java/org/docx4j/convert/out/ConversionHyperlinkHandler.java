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
package org.docx4j.convert.out;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.w3c.dom.Node;

/** 
 * The ConversionHyperlinkHandler gets called everytime a converter generates a hyperlink.<br/>
 * The ConversionHyperlinkHandler gets passed via the conversion settings and the implementor
 * may change the passed model before it gets converted to a link. 
 * 
 */
public interface ConversionHyperlinkHandler {
	public interface Model {
		
		/** field-argument of the Hyperlink field or the targetUri of the relationship.
		 * corresponds to switch \l field-argument, the \l switch might be omitted.
		 */
		public String getTarget();
		public void setTarget(String target);

		/** Only avaiable if the information is read from a relationship. 
		 *  Otherwise always false.
		 */
		public boolean isExternal();
		public void setExternal(boolean external);
		
		/**
		 * Specifies the name of a bookmark in the current document which shall be the target of
		 * this hyperlink.<br> 
		 * If this attribute is omitted, then the default behavior shall be to navigate to the start of
		 * the document. If a hyperlink target is also specified using the r:id attribute, then this
		 * attribute shall be ignored.<br>
		 */
		public String getAnchor();
		public void setAnchor(String anchor);

		/**
		 * Specifies a location in the target of the hyperlink that has no bookmarks. The method by
		 * which the contents of this attribute are linked to document text is outside the scope of
		 * ECMA-376.
		 */
		public String getDocLocation();
		public void setDocLocation(String docLocation);

		/** 
		 * Specifies the ID of the relationship whose target shall be used as the target for this
		 * hyperlink.
		 */
		public String getRId();
		public void setRId(String rId);

		/**
		 * Specifies a frame within the parent HTML frameset for the target of the parent hyperlink
		 * when one exists.
		 * corresponds to switch \t field-argument
		 * corresponds to switch \n with tgtFrame = "_blank"
		 */
		public String getTgtFrame();
		public void setTgtFrame(String tgtFrame);

		/**
		 * Specifies a string which can be surfaced in a user interface as associated with the parent
		 * hyperlink.
		 * corresponds to switch \o field-argument
		 */
		public String getTooltip();
		public void setTooltip(String tooltip);
		
		public Node getContent();
	}
	
	public void handleHyperlink(Model hyperlinkModel, OpcPackage opcPackage, Part currentPart) throws Docx4JException;
}
