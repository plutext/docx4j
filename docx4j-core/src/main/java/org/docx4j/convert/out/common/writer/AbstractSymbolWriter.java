package org.docx4j.convert.out.common.writer;


public abstract class AbstractSymbolWriter extends AbstractSimpleWriter {
	public static final String WRITER_ID = "w:sym";

	protected AbstractSymbolWriter() {
		super(WRITER_ID);
	}

}
