package org.docx4j.convert.out.html;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.properties.Property;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FldSimpleWriter extends AbstractFldSimpleWriter {
	
	//In HTML there is only one page - therefore the result is allways a (more or less formatted) "1"
	protected static class PageHandler implements FldSimpleStringWriterHandler {
		@Override
		public String getName() { return "PAGE"; }

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
			return "1";
		}
	}
	
	protected FldSimpleWriter() {
		super(null, "span");
	}

	@Override
	protected void registerHandlers() {
		super.registerHandlers();
		registerHandler(new PageHandler());
	}

	@Override
	protected void applyProperties(List<Property> properties, Node node) {
		HtmlCssHelper.applyAttributes(properties, (Element)node);
	}
}
