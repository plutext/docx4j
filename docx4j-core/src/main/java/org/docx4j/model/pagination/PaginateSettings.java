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
package org.docx4j.model.pagination;

import org.docx4j.convert.out.FOSettings;

/**
 * Settings for {@link Paginate}: the FO export settings the layout is made with, whether
 * paragraphs lacking a {@code w14:paraId} are given one, and whether markers go inside
 * paragraphs.
 *
 * @since 17.1.1
 */
public class PaginateSettings {

	private FOSettings foSettings;
	private Boolean writeParaIds;
	private Boolean lineBreaks;

	/**
	 * The export-fo settings the document is laid out with: font mapper, conversion
	 * features, the fop configuration.  Null (the default) means
	 * {@code Docx4J.createFOSettings()}.  {@link Paginate} sets the output format to
	 * FOP's area tree and adds the features it needs itself.
	 */
	public FOSettings getFoSettings() {
		return foSettings;
	}

	public PaginateSettings setFoSettings(FOSettings foSettings) {
		this.foSettings = foSettings;
		return this;
	}

	/**
	 * Whether paragraphs without a {@code w14:paraId} (or with one another paragraph
	 * already carries) are given one, so that the map's keys are in the file.  Null
	 * (the default) takes the entry point's default: on for {@link Paginate#paginate},
	 * which rewrites the document's runs anyway; off for {@link Paginate#compute}, which
	 * is a query and keys such paragraphs {@code P<n>} for the duration of the call.
	 */
	public Boolean getWriteParaIds() {
		return writeParaIds;
	}

	public PaginateSettings setWriteParaIds(Boolean writeParaIds) {
		this.writeParaIds = writeParaIds;
		return this;
	}

	/**
	 * Whether {@link Paginate#paginate} writes markers inside paragraphs that span pages
	 * (splitting the run at the boundary, as Word does), or at paragraph boundaries only.
	 * Null (the default) means on.
	 *
	 * @since 17.1.1 (CR-012 phase 2)
	 */
	public Boolean getLineBreaks() {
		return lineBreaks;
	}

	public PaginateSettings setLineBreaks(Boolean lineBreaks) {
		this.lineBreaks = lineBreaks;
		return this;
	}

	boolean writeParaIds(boolean defaultValue) {
		return writeParaIds == null ? defaultValue : writeParaIds.booleanValue();
	}

	boolean lineBreaks() {
		return lineBreaks == null ? true : lineBreaks.booleanValue();
	}
}
