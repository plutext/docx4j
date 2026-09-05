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
package org.docx4j.toc;

/**
 * There is no table of contents in this document: neither a ToC content
 * control, nor a "bare" TOC field which could be wrapped in one.
 *
 * A TocException (so existing catch blocks are unaffected), but a distinct
 * type, so that "this document has no ToC" can be told apart from "the ToC
 * couldn't be updated".  Docx4J.updateToc uses it to return false rather
 * than to throw.
 *
 * @since 17.0.6
 */
public class NoTocFoundException extends TocException {

	public NoTocFoundException(String message) {
		super(message);
	}
}
