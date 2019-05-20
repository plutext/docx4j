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
	 * Convert the AltChunks to ordinary 
	 * docx WordML content, returning result as
	 * a new WordprocessingMLPackage.
	 * 
	 */
	public WordprocessingMLPackage convertAltChunks() throws Docx4JException;
		
	
}
