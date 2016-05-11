
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
import org.w3c.dom.Document;
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
				String value = dpr.getValue(key).toString();
				log.debug("= " + value);
				return FormattingSwitchHelper.applyFormattingSwitch(context.getWmlPackage(), model, value);
			} catch (FieldValueException e) {
				
				if (e.getMessage().contains("No value found for DOCPROPERTY PAGES")) {// TODO improve this
					// Handle this case
				}
				throw new TransformerException(e);
				
			} catch (Docx4JException e) {
				
				throw new TransformerException(e);
			}			
		}
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
					applyStyle(context, fldSimpleModel, ret);
					break;
				case FldSimpleNodeWriterHandler.PROCESS_WRAP_APPLY_STYLE:
					ret = wrap(context, ret, doc);
					applyStyle(context, fldSimpleModel, ret);
					break;
			}
		}
		else { // FldSimpleStringWriterHandler
			
			value = ((FldSimpleStringWriterHandler)handler).toString(context, fldSimpleModel);
			ret = wrap(context, value, doc);
			// applyStyle treats all 3 cases like CHARFORMAT,
			// so implementing MERGEFORMAT is a TODO
			applyStyle(context, fldSimpleModel, ret);
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
		
		CTSimpleField ctSimpleField = fldSimpleModel.getFldSimple();
		RPr rPr = null;
		if (node != null) {
			rPr = getRPr(ctSimpleField.getContent());
			if (rPr != null) {
				List<Property> properties = PropertyFactory.createProperties(context.getWmlPackage(), rPr);
				if ((properties != null) && (!properties.isEmpty())) {
					applyProperties(properties, node);
				}
			}
		}
	}

	private Node createNode(Document doc) {
		return ((elementNs != null) && (elementNs.length() > 0) ?
				doc.createElementNS(elementNs, elementName) :
				doc.createElement(elementName));
	}

	private RPr getRPr(List<Object> content) {
		for (int i=0; i<content.size(); i++) {
			if (content.get(i) instanceof R) {
				return ((R)content.get(i)).getRPr();
			}
		}
		return null;
	}

	protected abstract void applyProperties(List<Property> properties, Node node);

}
