package org.docx4j.convert.out.common.writer;

public abstract class AbstractBrWriter extends AbstractSimpleWriter {
	public static final String WRITER_ID = "w:br";
	
	protected AbstractBrWriter() {
		super(WRITER_ID);
	}
}
