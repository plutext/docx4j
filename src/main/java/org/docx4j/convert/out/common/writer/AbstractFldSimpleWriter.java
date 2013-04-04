package org.docx4j.convert.out.common.writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.model.Model;
import org.docx4j.model.TransformState;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FldSimpleUnitsHelper;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class AbstractFldSimpleWriter implements ModelConverter {
	public interface FldSimpleWriterHandler {
		public String getName();
	}
	
	public interface FldSimpleNodeWriterHandler extends FldSimpleWriterHandler {
		public static final int PROCESS_NONE = 0;
		public static final int PROCESS_APPLY_STYLE = 1;
		public static final int PROCESS_WRAP_APPLY_STYLE = 2;
		
		public int getProcessType();
		
		public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException;
	}
	
	public interface FldSimpleStringWriterHandler extends FldSimpleWriterHandler {
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException;
	}

	
	protected static class DateHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "DATE"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return FldSimpleUnitsHelper.formatDate(model);
		}
	}
	
	protected static class TimeHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "TIME"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return FldSimpleUnitsHelper.formatDate(model);
		}
	}
	
	//PRINTDATE is treated as the current date, the conversion process is seen as 'printing'
	protected static class PrintdateHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "PRINTDATE"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return FldSimpleUnitsHelper.formatDate(model);
		}
	}

	
	
	
	//These are the specific handlers, that are implemented
	protected Map<String, FldSimpleWriterHandler> handlers = 
			new HashMap<String, FldSimpleWriterHandler>();
	
	//This handler get's called, if a specific handler isn't avaiable
	//it should just return the result in the document as it's own result.
	protected FldSimpleNodeWriterHandler defaultHandler = null;
	
	protected String elementNs = null;
	protected String elementName = null;
	
	protected AbstractFldSimpleWriter(String elementNs, String elementName) {
		registerHandlers();
		defaultHandler = createDefaultHandler();
		this.elementNs = elementNs;
		this.elementName = elementName;
	}

	protected void registerHandlers() {
		registerHandler(new DateHandler());
		registerHandler(new TimeHandler());
		registerHandler(new PrintdateHandler());
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
	public String getID() {
		return FldSimpleModel.MODEL_ID;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, Model model, TransformState state, Document doc) throws TransformerException {
	FldSimpleModel fldSimpleModel = (FldSimpleModel)model;
	FldSimpleWriterHandler handler = handlers.get(fldSimpleModel.getFldName());
	FldSimpleNodeWriterHandler nodeHandler = null;
	Node ret = null;
	String value = null;
		if (handler == null) {
			handler = defaultHandler;
		}
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
		else {
			value = ((FldSimpleStringWriterHandler)handler).toString(context, fldSimpleModel);
			ret = wrap(context, value, doc);
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
	
	@Override
	public TransformState createTransformState() {
		// no TransformState used 
		return null;
	}

}
