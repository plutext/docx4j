package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.properties.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FldSimpleWriter extends AbstractFldSimpleWriter {
	protected static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	
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
	
	protected FldSimpleWriter() {
		super(FO_NS, "fo:inline");
	}

	@Override
	protected void registerHandlers() {
		super.registerHandlers();
		registerHandler(new PageHandler());
	}

	@Override
	protected void applyProperties(List<Property> properties, Node node) {
		Conversion.applyFoAttributes(properties, (Element)node);
	}
}
