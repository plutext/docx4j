package org.docx4j.convert.out.common.writer;

public abstract class AbstractBookmarkStartWriter extends AbstractSimpleWriter {
	public static final String WRITER_ID = "w:bookmarkStart";
	protected AbstractBookmarkStartWriter() {
		super(WRITER_ID);
	}

}
