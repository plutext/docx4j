package org.docx4j.convert.out.common.writer;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractFldSimpleWriter;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FldSimpleUnitsHelper;
import org.docx4j.model.fields.HyperlinkModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RefHandler implements AbstractFldSimpleWriter.FldSimpleNodeWriterHandler {
	protected int outputType = -1;
	
	public RefHandler(int outputType) {
		this.outputType = outputType;
	}
	
	@Override
	public String getName() { return "REF"; }

	@Override
	public int getProcessType() {
		return PROCESS_NONE;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
	Node ret = model.getContent();
	HyperlinkModel hyperlinkModel = null;
		if (FldSimpleUnitsHelper.hasSwitch("\\h", model.getFldParameters())) {
			hyperlinkModel = new HyperlinkModel();
			hyperlinkModel.setup(context.getWmlPackage(), context.getCurrentPart());
			hyperlinkModel.build(model, model.getContent()); //the bookmark is the target, \h gets ignored
			ret = HyperlinkUtil.toNode(outputType, context, hyperlinkModel, model.getContent(), doc);
		}
		return ret;
	}
}