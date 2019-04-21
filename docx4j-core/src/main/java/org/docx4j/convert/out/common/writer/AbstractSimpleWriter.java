package org.docx4j.convert.out.common.writer;

import org.docx4j.convert.out.common.Writer;

public abstract class AbstractSimpleWriter implements Writer {
	protected String writerId = null;
	
	protected AbstractSimpleWriter(String writerId) {
		this.writerId = writerId;
	}

	@Override
	public String getID() {
		return writerId;
	}

	@Override
	public TransformState createTransformState() {
		//no transform state required
		return null;
	}

}
