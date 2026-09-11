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
package org.docx4j.model.datastorage;

import org.docx4j.wml.SdtPr;

/**
 * Hook for configuring the docx4j-ImportXHTML importer that OpenDoPE binding creates
 * for each XHTML content control (od:ContentType=application/xhtml+xml), beyond what
 * the <tt>docx4j.model.datastorage.BindingTraverser.XHTML.*Formatting</tt> properties
 * cover (see {@link XHTMLImporterFormatting}).
 *
 * Register with {@link BindingHandler#setXHTMLImporterCustomizer(XHTMLImporterCustomizer)};
 * like the other BindingHandler hooks it is static, and so applies to all subsequent
 * binds in the JVM.  It is invoked after docx4j's own configuration of the importer
 * (bookmark and sequence counters, max width in table cells, the property-driven
 * FormattingOptions, hyperlink style) and immediately before <tt>convert</tt>, so
 * whatever it sets wins.
 *
 * docx4j-core cannot name ImportXHTML's types, so the importer arrives as Object; cast it:
 * <pre>
 *   BindingHandler.setXHTMLImporterCustomizer((importer, sdtPr, inTableCell) -> {
 *       XHTMLImporterImpl impl = (XHTMLImporterImpl) importer;
 *       impl.setRunFormatting(FormattingOption.CLASS_TO_STYLE_ONLY);
 *       impl.setCssWhiteList(myWhiteList);
 *   });
 * </pre>
 *
 * @since 8.3.16 (backport of 17.1.1; CR-013 phase 4)
 */
public interface XHTMLImporterCustomizer {

	/**
	 * @param xhtmlImporter the org.docx4j.convert.in.xhtml.XHTMLImporterImpl about to convert
	 *                      this content control's XHTML
	 * @param sdtPr         the content control's properties (its tag carries the od:xpath id)
	 * @param inTableCell   whether the content control sits in a table cell
	 */
	void customize(Object xhtmlImporter, SdtPr sdtPr, boolean inTableCell);
}
