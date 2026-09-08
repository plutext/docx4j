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
package org.docx4j.model.images;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;

/** The ImageHandler stores (if necessary) any images in an conversion. 
 *  It returns the uri for the image saved, or null
 */
public interface ConversionImageHandler {

	/**
	 * @param picture 
	 * @param relationship of the image 
	 * @param part of the image, if it is an internal image, otherwise null
	 * @return uri for the image we've saved, or null
	 * @throws Docx4JException this exception will be logged, but not propagated
	 */
	public String handleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart part) throws Docx4JException;

	/**
	 * Whether this handler embeds the image in the output document (an RFC 2397
	 * data URI, a cid: reference) rather than writing a file beside it.
	 *
	 * <p>It decides how a Windows metafile is represented in HTML: a handler which
	 * embeds gets an inline {@code <svg>} - vectors, no extra round trip - and one
	 * which writes files gets a PNG it can write like any other picture (CR-011).
	 * Defaults to false, so a third-party handler behaves as it did.</p>
	 *
	 * @since 17.1.0
	 */
	default boolean isInline() {
		return false;
	}

	/**
	 * Delete the image files this handler wrote, where docx4j chose the directory
	 * they went into (that is, where no imageDirPath was supplied, so they were
	 * written to {@code java.io.tmpdir}).  Called at the end of a conversion.
	 *
	 * <p>Files written to a directory the <b>caller</b> named are the caller's
	 * output, and are never deleted.</p>
	 *
	 * <p>Whether the files can go now depends on what docx4j handed back:</p>
	 * <ul>
	 * <li><b>PDF</b> ({@code deferToJvmExit} false): FOP rendered the FO in
	 * process, and the images are in the PDF; nothing refers to the files any
	 * more, so they are deleted at once.</li>
	 * <li><b>An FO document</b> ({@link org.docx4j.convert.out.FOSettings#INTERNAL_FO_MIME},
	 * or a dumped FO file) <b>or HTML</b> ({@code deferToJvmExit} true): the
	 * document the caller receives still points at those files, by
	 * {@code file:} URL or {@code <img src>}, and deleting them would break it.
	 * docx4j cannot know when the caller has finished with them, so the files are
	 * registered with {@link java.io.File#deleteOnExit()} instead: they survive
	 * as long as the JVM, but they don't accumulate across restarts.</li>
	 * </ul>
	 *
	 * <p>Does nothing by default, so a handler implemented outside docx4j is
	 * unaffected (and keeps compiling).</p>
	 *
	 * @param deferToJvmExit whether the output still references the files, so
	 * that deletion must wait until the JVM exits
	 *
	 * @since 17.1.1
	 */
	default void cleanupTemporaryImages(boolean deferToJvmExit) {
		// nothing to do
	}
}
