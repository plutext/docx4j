package org.docx4j.convert.out.html;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.convert.out.common.writer.AbstractPagerefHandler;
import org.docx4j.convert.out.common.writer.HyperlinkUtil;
import org.docx4j.convert.out.common.writer.RefHandler;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.properties.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FldSimpleWriter extends AbstractFldSimpleWriter {
	
	//In HTML there is only one page - therefore the result is always a (more or less formatted) "1"
	//to keep it consistent with what fo does, it uses the formatting of the page numbers
	protected abstract static class AbstractPageHandler implements FldSimpleStringWriterHandler {

		@Override
		public String toString(AbstractWmlConversionContext context, FldSimpleModel model) throws TransformerException {
		String pageFormat = context.getSections().getCurrentSection().getPageNumberInformation().getPageFormat();
			pageFormat = FormattingSwitchHelper.getFoPageNumberFormat(pageFormat);
			return FormattingSwitchHelper.formatFoPageNumber(1, pageFormat);
		}
	}
	
	protected static class PageHandler extends AbstractPageHandler {
		@Override
		public String getName() { return "PAGE"; }
	}
	
	protected static class NumpagesHandler extends AbstractPageHandler {
		@Override
		public String getName() { return "NUMPAGES"; }
	}
	
	protected static class SectionpagesHandler extends AbstractPageHandler {
		@Override
		public String getName() { return "SECTIONPAGES"; }
	}
	
	protected static class PagerefHandler extends AbstractPagerefHandler {
		protected PagerefHandler() {
			super(HyperlinkUtil.HTML_OUTPUT);
		}

		@Override
		protected Node createPageref(AbstractWmlConversionContext context, Document doc, String bookmarkId) {
		Element ret = doc.createElement("span");
		String pageNumberFormat = context.getSections().getCurrentSection().getPageNumberInformation().getNumpagesFormat();
			pageNumberFormat = FormattingSwitchHelper.getFoPageNumberFormat(pageNumberFormat);
			ret.appendChild(doc.createTextNode(FormattingSwitchHelper.formatFoPageNumber(1, pageNumberFormat)));
			return ret;
		}
		
	}
	
	protected FldSimpleWriter() {
		super(null, "span");
	}

	@Override
	protected void registerHandlers() {
		super.registerHandlers();
		registerHandler(new PageHandler());
		//disabled until 2pass is implemented
		//registerHandler(new NumpagesHandler());
		//registerHandler(new SectionpagesHandler());
		registerHandler(new HyperlinkWriter());
		registerHandler(new RefHandler(HyperlinkUtil.HTML_OUTPUT));
		registerHandler(new PagerefHandler());
	}

	@Override
	protected void applyProperties(List<Property> properties, Node node) {
		HtmlCssHelper.applyAttributes(properties, (Element)node);
	}
}
