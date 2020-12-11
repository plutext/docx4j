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
package org.docx4j.convert.out.fo;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.convert.out.common.writer.AbstractPagerefHandler;
import org.docx4j.convert.out.common.writer.HyperlinkUtil;
import org.docx4j.convert.out.common.writer.RefHandler;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.properties.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FldSimpleWriter extends AbstractFldSimpleWriter {
	// NB see super class for definition of other handlers.
	protected static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	protected static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
	
	protected static class PageHandler implements FldSimpleNodeWriterHandler {
		@Override
		public String getName() { return "PAGE"; }
		@Override
		public int getProcessType() { return PROCESS_APPLY_STYLE; }

		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
			return doc.createElementNS(FO_NS, "fo:page-number");
		}
	}
	
	protected abstract static class AbstractPagesHandler  implements FldSimpleNodeWriterHandler {
		protected String fieldName = null;
		protected AbstractPagesHandler(String fieldName) {
			this.fieldName = fieldName;
		}
		
		@Override
		public String getName() { return fieldName; }
		@Override
		public int getProcessType() { return PROCESS_APPLY_STYLE; }
		
		@Override
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
		Element ret = null;
			if (((FOConversionContext)context).isRequires2Pass()) {
				ret = doc.createElementNS(FO_NS, "fo:inline");
				ret.appendChild(doc.createTextNode("${" + getParameterName(context) + "}"));
			}
			else {
//				ret = doc.createElementNS(FO_NS, "fo:page-number-citation-last");
//				String refId = getRefid(context);
//				ret.setAttribute("ref-id", getRefid(context));
				
				// Workaround for missing space before fo:page-number-citation-last in FOP 1.1 output;
				// See http://apache-fop.1065347.n5.nabble.com/preserving-a-trailing-space-in-inline-td40644.html
				// Since this method returns a node, wrap the two nodes in an fo:wrapper

				ret = doc.createElementNS(FO_NS, "fo:wrapper");
				
				Element pncl = doc.createElementNS(FO_NS, "fo:page-number-citation-last");
				String refId = getRefid(context);
				pncl.setAttribute("ref-id", getRefid(context));
				
				ret.appendChild(pncl);
				
				ret.appendChild(
						// &#x200b;
						doc.createTextNode("\u200b")
				);
				
			}
			return ret;
		}

		protected abstract String getRefid(AbstractWmlConversionContext context);

		protected abstract String getParameterName(AbstractWmlConversionContext context);
	}
	
	protected static class NumpagesHandler extends AbstractPagesHandler {
		protected NumpagesHandler() {
			super("NUMPAGES");
		}

		@Override
		protected String getParameterName(AbstractWmlConversionContext context) {
			//The value of the numpages should be the same throughout the document,
			//but the page number formatting might change depending on the section. 
			//For this reason there is a numpages value per section.
			return "field_numpages_" + context.getSections().getCurrentSection().getId() + "_value";
		}

		@Override
		protected String getRefid(AbstractWmlConversionContext context) {
			/* Apache FOP ignores the id in the fo:root, for this reason 
			 * we pass the id of the last section.
			 * If it isn't a vanilla document then we have a 2 pass and this isn't
			 * used.
			 */
			List<ConversionSectionWrapper> wrappers = context.getSections().getList();
			return "section_" + wrappers.get(wrappers.size() - 1).getId();
		}
	}
	
	protected static class SectionpagesHandler extends AbstractPagesHandler {
		protected SectionpagesHandler() {
			super("SECTIONPAGES");
		}

		@Override
		protected String getParameterName(AbstractWmlConversionContext context) {
			return "field_sectionpages_" + context.getSections().getCurrentSection().getId() + "_value";
		}

		@Override
		protected String getRefid(AbstractWmlConversionContext context) {
			return "section_" + context.getSections().getCurrentSection().getId();
		}
	}
	
	protected static class PagerefHandler extends AbstractPagerefHandler {
		protected PagerefHandler() {
			super(HyperlinkUtil.FO_OUTPUT);
		}

		@Override
		protected Node createPageref(AbstractWmlConversionContext context, Document doc, String bookmarkId) {
		Element ret = doc.createElementNS(FO_NS, "fo:page-number-citation");
			ret.setAttribute("ref-id", bookmarkId);
			return ret;
		}
		
	}
	
	protected FldSimpleWriter() {
		super(FO_NS, "fo:inline");
	}

	@Override
	protected void registerHandlers() {
		super.registerHandlers();
		registerHandler(new PageHandler());
		registerHandler(new HyperlinkWriter());
		registerHandler(new RefHandler(HyperlinkUtil.FO_OUTPUT));
		registerHandler(new PagerefHandler());
		registerHandler(new NumpagesHandler());
		registerHandler(new SectionpagesHandler());
	}

	@Override
	protected void applyProperties(List<Property> properties, Node node) {
		XsltFOFunctions.applyFoAttributes(properties, (Element)node);
	}
}
