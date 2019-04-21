/**
 *  Copyright 2012, Plutext Pty Ltd.
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

 **/
package org.docx4j.model.datastorage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;

public interface BindingTraverserInterface {
	
	public Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException;
	
	
	/**
	 * Provide a way to set the starting bookmark ID number
	 * for the purposes of Binding Traverse.
	 * 
	 * For efficiency, user code needs to pass this value through
	 * from the previous stage (repeats/condition handing).
	 * 
	 * If it isn't, the value will be calculated (less efficient).
	 *  
	 * New bookmarks could be created from XHTML, or renumbered
	 * in Flat OPC XML (TODO).
	 * 
	 * @param bookmarkId
	 * @since 3.2.1
	 */
	public void setStartingIdForNewBookmarks(AtomicInteger bookmarkId);

	/**
	 * Since we are potentially processing multiple parts (ie
	 * main document part, headers, footers), we need to be able 
	 * to pass the number from part to part.
	 * @since 3.2.1
	 */
	public AtomicInteger getNextBookmarkId();
	
}
