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

 */
package org.docx4j.openpackaging.parts.WordprocessingML;

import java.io.InputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;

/**
 * An altChunk can appear wherever EG_BlockLevelElts is allowed 
 * ie in tc, footnotes/endnotes, txbxContent, body, comment, 
 * hdr/ftr.  Also a content control???
 *
 * <p>Since 17.3.4, an {@code Html} or {@code Xhtml} chunk is added without a leading XML
 * declaration, which Word refuses (it repairs the document), and the encoding the declaration
 * named is kept by a byte order mark (CR-053): see {@link AltChunkHtml}. Other types are added
 * as they are given.</p>
 * @author jharrop
 * @since 2.8

 */
public interface AltChunkInterface {
	
	/**
	 * Add content from byte array of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param bytes
	 * @return
	 * @throws Docx4JException
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes ) throws Docx4JException;
	
	/**
	 * Add at index content from byte array of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param bytes
	 * @param index
	 * @return
	 * @throws Docx4JException
	 * @since 8.1.0
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes, int index ) throws Docx4JException;
	
	/**
	 * Add content from InputStream of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param is
	 * @return
	 * @throws Docx4JException
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is) throws Docx4JException;

	/**
	 * Add at index content from InputStream of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param is
	 * @param index
	 * @return
	 * @throws Docx4JException
	 * @since 8.1.0
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is, int index) throws Docx4JException;
	
	/**
	 * Add content from byte array of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param bytes
	 * @param attachmentPoint
	 * @return
	 * @throws Docx4JException
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes, ContentAccessor attachmentPoint) throws Docx4JException;
	
	/**
	 * Add at index content from byte array of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param bytes
	 * @param attachmentPoint
	 * @param index
	 * @return
	 * @throws Docx4JException
	 * @since 8.1.0
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, byte[] bytes, ContentAccessor attachmentPoint, int index) throws Docx4JException;
	
	/**
	 * Add content from InputStream of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param is
	 * @param attachmentPoint
	 * @return
	 * @throws Docx4JException
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is, ContentAccessor attachmentPoint) throws Docx4JException;

	/**
	 * Add at index content from InputStream of type AltChunkType, in a way that leaves it up to downstream application (eg Word)
	 * to convert the content to docx content. 
	 * 
	 * Note re XHTML content: Unless you really want  it
	 * converted by Word, users are advised to use docx4j's XHTMLImporter instead.
	 * @param is
	 * @param attachmentPoint
	 * @param index
	 * @return
	 * @throws Docx4JException
	 * @since 8.1.0
	 */
	public AlternativeFormatInputPart addAltChunk(AltChunkType type, InputStream is, ContentAccessor attachmentPoint, int index) throws Docx4JException;
	
	// TODO: API should provide a method with index into content list, rather than assuming add at end
	
	
	/**
	 * Convert altChunks in this part to ordinary docx WordML content.
	 * 
	 * To convert an altChunk of type HTML, XHTML or MHT, this method requires docx4j-ImportXHTML (LGPL) and its
 * dependencies. Since 17.3.4 such a chunk is converted as Word imports it (CR-053): parsed by jsoup where
 * it is on the class path (else an HTML chunk is left, and an XHTML or MHT chunk read as XML, as before),
 * the CSS Word applies, and what display:none hides kept as hidden text (docx4j-ImportXHTML 17.3.4 on).
 * A chunk that can't be converted is left as it is, and the others are still converted.
 * Since 17.3.5 the import fetches nothing a chunk refers to unless the property docx4j.altchunk.html.fetch
 * is true (CR-057); {@link #convertAltChunks(boolean)} decides it for one call.
	 * 
	 * Since 11.4.8, this method does the conversion in situ (ie rather than cloning the pkg), 
	 * and does not convert docx altChunks.  To handle those, use the MergeDocx component of Docx4j Enterprise.
	 * */
	public void convertAltChunks() throws Docx4JException;

	/**
	 * As {@link #convertAltChunks()}, with {@code fetch} saying for this call whether the import of an
	 * HTML, XHTML or MHT chunk may fetch what the chunk refers to (a stylesheet's link or @import, an
	 * image by URL or by local file), as Word does on open, whatever the property
	 * {@code docx4j.altchunk.html.fetch} (false by default: nothing fetched, CR-057) says. An image not
	 * fetched is its alt text, or nothing.
	 * @since 17.3.5
	 */
	public void convertAltChunks(boolean fetch) throws Docx4JException;
		
	
}
