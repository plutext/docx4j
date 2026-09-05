
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
package org.docx4j.convert.out.common.writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.XsltCommonFunctions;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.FieldValueException;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.fields.docproperty.DocPropertyResolver;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractFldSimpleWriter extends AbstractSimpleWriter {
	public static final String WRITER_ID = "w:fldSimple";
	private static Logger log = LoggerFactory.getLogger(AbstractFldSimpleWriter.class);			
	
	public interface FldSimpleWriterHandler {
		public String getName();
	}
	
	public interface FldSimpleNodeWriterHandler extends FldSimpleWriterHandler {

		public static final int PROCESS_NONE = 0;
		public static final int PROCESS_APPLY_STYLE = 1;
		public static final int PROCESS_WRAP_APPLY_STYLE = 2;

		public int getProcessType();

		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc)
				throws TransformerException;

		/**
		 * Text representative of what the node will render as (eg "๑" for an
		 * fo:page-number in a section using thaiNumbers), so the right font can be
		 * selected for it; null (the default) is treated as the digit "1".
		 *
		 * @since 17.0.3
		 */
		default String getSampleText(AbstractWmlConversionContext context, FldSimpleModel model) {
			return null;
		}
	}
	
	public interface FldSimpleStringWriterHandler extends FldSimpleWriterHandler {
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException;
	}

	
	protected static class DateHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "DATE"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return FormattingSwitchHelper.formatDate(model);
		}
	}
	
	protected static class TimeHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "TIME"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return FormattingSwitchHelper.formatDate(model);
		}
	}
	
	//PRINTDATE is treated as the current date, the conversion process is seen as 'printing'
	protected static class PrintdateHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "PRINTDATE"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return FormattingSwitchHelper.formatDate(model);
		}
	}

	protected static class DocPropertyHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "DOCPROPERTY"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			
			// First, get the value
			DocPropertyResolver dpr = new DocPropertyResolver(context.getWmlPackage());
			String key = model.getFldArgument();
			
			try {
				String value = dpr.getValue(key);
				if (value == null) {
					throw new FieldValueException("No value found for DOCPROPERTY " + key);
				}
				log.debug("= " + value);
				return FormattingSwitchHelper.applyFormattingSwitch(context.getWmlPackage(), model, value);
			} catch (FieldValueException e) {

				// The document property doesn't exist (or has no value).  Word displays
				// the field's cached result in that case, so we do the same, rather than
				// failing the export.  @since 17.0.5
				String cached = cachedResultText(model);
				log.warn(e.getMessage() + "; using the cached field result '" + cached + "'");
				return cached;

			} catch (Docx4JException e) {

				throw new TransformerException(e);
			}
		}
	}

	/**
	 * The text of the field's cached result (the content of the w:fldSimple, which for a
	 * complex field is its result runs; see FieldsCombiner), or "" if there is none.
	 * It is what Word shows for a field it can't evaluate.
	 *
	 * @since 17.0.5
	 */
	protected static String cachedResultText(FldSimpleModel model) {
		Node content = (model == null ? null : model.getContent());
		String text = (content == null ? null : content.getTextContent());
		return (text == null ? "" : text);
	}
	
	//NB, see also FldSimpleWriter, where PAGE handler is defined.
	
	
	//These are the specific handlers, that are implemented
	protected Map<String, FldSimpleWriterHandler> handlers = 
			new HashMap<String, FldSimpleWriterHandler>();
	
	//This handler get's called, if a specific handler isn't available
	//it should just return the result in the document as it's own result.
	protected FldSimpleNodeWriterHandler defaultHandler = null;
	
	protected String elementNs = null;
	protected String elementName = null;
	
	protected AbstractFldSimpleWriter(String elementNs, String elementName) {
		super(WRITER_ID);
		registerHandlers();
		defaultHandler = createDefaultHandler();
		this.elementNs = elementNs;
		this.elementName = elementName;
	}

	protected void registerHandlers() {
		registerHandler(new DateHandler());
		registerHandler(new TimeHandler());
		registerHandler(new PrintdateHandler());
		registerHandler(new DocPropertyHandler());
	}
	
	protected void registerHandler(FldSimpleWriterHandler handler) {
		handlers.put(handler.getName(), handler);
	}
	
	protected FldSimpleNodeWriterHandler createDefaultHandler() {
		return new FldSimpleNodeWriterHandler() {
			@Override
			public String getName() {
				return "*";
			}
			
			@Override
			public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
				return model.getContent();
			}

			@Override
			public int getProcessType() {
				return PROCESS_NONE;
			}
		};
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, Node content, TransformState state, Document doc) throws TransformerException {
	FldSimpleModel fldSimpleModel = new FldSimpleModel();
		fldSimpleModel.build((CTSimpleField)unmarshalledNode, content);
		
		log.debug("looking for handler for " + fldSimpleModel.getFldName());
		
		FldSimpleWriterHandler handler = handlers.get(fldSimpleModel.getFldName());
		FldSimpleNodeWriterHandler nodeHandler = null;
		Node ret = null;
		String value = null;
		if (handler == null) {
			handler = defaultHandler;
			log.debug(".. using  defaultHandler" );
		} else {
			log.debug(".. got it .. " + handler.getClass().getName());			
		}
		
		/*
		 * There are three cases for the formatting of the result:
		 * 
		 * \* CHARFORMAT applies the formatting of the first character of 
		 *    the instrText (not the existing result!) to the entire result
		 * 
		 * \* MERGEFORMAT reuses the formatting of the existing result
		 * 
		 * If neither is present, the existing formatting of the 
		 * instrText is used (the formatting of the existing formatting is
		 * ignored), ie CHARFORMAT is assumed.
		 * 
		 * The above concepts only apply to complex fields!!
		 * We could honour them in our simple fields though, if
		 * we carried the appropriate formatting over
		 * in FieldsCombiner (maybe it does that already?)
		 * 
		 * As these general formatting switches don't apply
		 * to a field which is simple, we'd have to distinguish 
		 * converted ones from ones which were already simple.
		 * 
		 */		
		if (handler instanceof FldSimpleNodeWriterHandler) {
			
			nodeHandler = (FldSimpleNodeWriterHandler)handler;
			ret = nodeHandler.toNode(context, fldSimpleModel, doc);
			switch (nodeHandler.getProcessType()) {
				case FldSimpleNodeWriterHandler.PROCESS_NONE:
					break;
				case FldSimpleNodeWriterHandler.PROCESS_APPLY_STYLE:
					applyStyle(context, fldSimpleModel, ret, nodeHandler.getSampleText(context, fldSimpleModel));
					break;
				case FldSimpleNodeWriterHandler.PROCESS_WRAP_APPLY_STYLE:
					ret = wrap(context, ret, doc);
					applyStyle(context, fldSimpleModel, ret, nodeHandler.getSampleText(context, fldSimpleModel));
					break;
			}
		}
		else { // FldSimpleStringWriterHandler

			value = ((FldSimpleStringWriterHandler)handler).toString(context, fldSimpleModel);
			ret = wrap(context, value, doc);
			// applyStyle treats all 3 cases like CHARFORMAT,
			// so implementing MERGEFORMAT is a TODO
			applyStyle(context, fldSimpleModel, ret, value);
		}
		return ret;
	}

	protected Node wrap(AbstractWmlConversionContext context, String result, Document doc) {
		
		RPr rPr = null;
		Node node = null;
		if (result != null) {
			node = createNode(doc);
			if (result.length() > 0) {
				node.setTextContent(result);
			}
		}
		return node;
	}

	protected Node wrap(AbstractWmlConversionContext context, Node node, Document doc) {
		
		Node wrapper = null;
		if (node != null) {
			wrapper = createNode(doc);
			wrapper.appendChild(node);
		}
		return wrapper;
	}

	/**
	 * Apply the formatting specified in the rPr node (if any).
	 *
	 * @param context
	 * @param fldSimpleModel
	 * @param node
	 */
	protected void applyStyle(AbstractWmlConversionContext context, FldSimpleModel fldSimpleModel, Node node) {
		applyStyle(context, fldSimpleModel, node, null);
	}

	/**
	 * Apply the formatting specified in the rPr node (if any), including the font.
	 *
	 * @param context
	 * @param fldSimpleModel
	 * @param node
	 * @param resultText the text of the field result, where it is known; it is used
	 *        to select the font (which of w:ascii, w:hAnsi, w:eastAsia, w:cs applies
	 *        depends on the characters).  May be null.
	 * @since 17.0.3
	 */
	protected void applyStyle(AbstractWmlConversionContext context, FldSimpleModel fldSimpleModel, Node node, String resultText) {

		CTSimpleField ctSimpleField = fldSimpleModel.getFldSimple();
		RPr rPr = null;
		if (node != null) {
			R r = getR(ctSimpleField.getContent());
			rPr = (r == null ? null : r.getRPr());
			if (rPr != null) {
				List<Property> properties = PropertyFactory.createProperties(context.getWmlPackage(), rPr);
				if ((properties != null) && (!properties.isEmpty())) {
					applyProperties(properties, node);
				}
			}
			// NB rPr may be null here; RunFontSelector will still resolve a font
			// from docDefaults/the default paragraph style
			applyFont(context, rPr, resultText, node);
		}
	}

	/**
	 * Set the font on a node we generated for a field result.
	 *
	 * PropertyFactory.createProperties doesn't handle w:rFonts, since for an ordinary
	 * run, the font is chosen per w:t (by RunFontSelector, which needs the actual
	 * characters in order to choose between w:ascii, w:hAnsi, w:eastAsia and w:cs).
	 * The nodes we generate for a field have no w:t, so unless we do this, they'd be
	 * rendered in the renderer's default font, and a PAGE field in a footer would be
	 * in a different font to the surrounding text.
	 *
	 * @since 17.0.3
	 */
	protected void applyFont(AbstractWmlConversionContext context, RPr rPr, String resultText, Node node) {

		if (!(node instanceof Element)) return;

		RunFontSelector runFontSelector = context.getRunFontSelector();
		if (runFontSelector == null) return;

		// Use the actual field result where we know it, so that the font selection
		// algorithm sees the characters which will be rendered. Otherwise assume a
		// digit, since these fields (PAGE, NUMPAGES, SECTIONPAGES) are numeric.
		Text sample = Context.getWmlObjectFactory().createText();
		sample.setValue(((resultText == null) || (resultText.length() == 0)) ? "1" : resultText);

		Object fontResult = null;
		try {
			/* The containing paragraph isn't reachable from the field itself (it is
			 * unmarshalled on its own, so the result run's parent is the CTSimpleField),
			 * so the pPr is passed to us; see AbstractWmlConversionContext#getCurrentPPr.
			 * Where it is null, RunFontSelector falls back to the default paragraph style. */
			fontResult = runFontSelector.fontSelector(context.getCurrentPPr(), rPr, sample);
		} catch (Exception e) {
			// Not fatal; the field is simply rendered in the default font, as it was before
			log.warn("Couldn't determine font for field result: " + e.getMessage(), e);
			return;
		}
		if (!(fontResult instanceof DocumentFragment)) return;

		Node styled = ((DocumentFragment)fontResult).getFirstChild();
		if (styled instanceof Element) {
			/* The font may not actually have these characters - an embedded font is
			 * commonly a subset, and a page number is produced at render time, so the
			 * author's subset need not contain the digits.  Leave it unset in that case,
			 * so the result is rendered in an inherited font rather than as .notdef. */
			if (!XsltCommonFunctions.fontCanRender(
					context.getWmlPackage().getFontMapper(), (Element)styled, sample.getValue())) return;
			applyFont((Element)styled, (Element)node);
		}
	}

	/**
	 * Copy the font which RunFontSelector set on 'source' (an element it created,
	 * belonging to some other document) to 'target'.  How that is represented
	 * differs between output formats, so this is a no-op here.
	 *
	 * @since 17.0.3
	 */
	protected void applyFont(Element source, Element target) {
		// see subclasses
	}

	private Node createNode(Document doc) {
		return ((elementNs != null) && (elementNs.length() > 0) ?
				doc.createElementNS(elementNs, elementName) :
				doc.createElement(elementName));
	}

	private R getR(List<Object> content) {
		for (int i=0; i<content.size(); i++) {
			if (content.get(i) instanceof R) {
				return (R)content.get(i);
			}
		}
		return null;
	}

	protected abstract void applyProperties(List<Property> properties, Node node);

}
