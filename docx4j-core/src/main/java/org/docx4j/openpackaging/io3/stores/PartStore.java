/*
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

 */
package org.docx4j.openpackaging.io3.stores;

import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;

/**
 * A PartStore is a connection to a repository for
 * a particular document/package, enabling it to
 * be loaded or saved.
 *
 * @author jharrop
 * @since 3.0
 */
public interface PartStore {

	/**
	 * The current content of a part will be as follows:
	 *
	 * - for a JaxbXmlPart, whatever is in the
	 *   part store, unless
	 *   it has been unmarshalled (in which case
	 *   it is that part's JaxbXmlElement)
	 *
	 * - for a BinaryPart, whatever is in the
	 *   part store, unless it has fetched (in which
	 *   case it is that part's bytebuffer)
	 *
	 * - for any other part (including
	 *   CustomXmlDataStoragePart, XmlPart),
	 *   as contained in that part.
	 *
	 */


	/**
	 * Returns null if the part does not exist. This will happen when calling
	 * code optimistically tries to fetch a rels part (which may or may not
	 * exist).
	 * 
	 * Note: JaxbXmlPart and JaxbXmlPartXPathAware both unmarshal lazily
	 * (ie can invoke this sometime after the docx is loaded). 
	 * 
	 * Caller (generally docx4j itself) should close the resulting InputStream after use.
	 * @param partName
	 * @return
	 * @throws Docx4JException
	 */
	public InputStream loadPart(String partName) throws Docx4JException;
	
	/**
	 * 
	 * Rename a part in the part store. Useful where a part is being renamed,
	 * but its content has not been loaded.  (The existing approach is to 
	 * manually force the content to be loaded)
	 * 
	 * @param oldName
	 * @param newName
	 * @since 8.1.4
	 */
	public void rename(PartName oldName, PartName newName);
	
	
	/**
	 * The size of this part in bytes.
	 * Return -1 if the part does not exist.
	 * 
	 * @param partName
	 * @return
	 * @throws Docx4JException
	 */
	public long getPartSize(String partName) throws Docx4JException, java.lang.UnsupportedOperationException;

	/*
	 * If the implementation closes resources here,
	 * loadPart, if subsequently called, will need a way to re-open
	 * them. So its better just to have a notion of package unload,
	 * at which time resources are closed.
	 * 
	 * public void finishLoad() throws Docx4JException;
	 */
	
	public void setOutputStream(OutputStream os) throws Docx4JException;

	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException;

	public void saveJaxbXmlPart(JaxbXmlPart part) throws Docx4JException;

	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part) throws Docx4JException;

	public void saveXmlPart(XmlPart part) throws Docx4JException;

	public void saveBinaryPart(Part part) throws Docx4JException;

	/**
	 * Anything necessary to perfect the save operation.
	 * For example, 
	 */
	public void finishSave() throws Docx4JException;
	
	/**
	 * Use in a save operation if the source part store (ie part store 
	 * from which pkg loaded) is different to this target part store.
	 * In this case it is necessary to be able to fetch parts which 
	 * hadn't need to have been loaded up until this point.
	 */
	public void setSourcePartStore(PartStore partStore);
	// if this was instead set on pkg, PartStore would need a reference to pkg 	
	
	// whether pkg should have reference to part store or not, we need
	public void dispose();	
}
