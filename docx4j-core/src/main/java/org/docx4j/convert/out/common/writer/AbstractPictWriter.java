package org.docx4j.convert.out.common.writer;

public abstract class AbstractPictWriter extends AbstractSimpleWriter {
	public static final String WRITER_ID = "w:pict";
	
	protected AbstractPictWriter() {
		super(WRITER_ID);
	}
}
