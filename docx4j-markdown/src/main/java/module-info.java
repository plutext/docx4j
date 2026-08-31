module org.docx4j.markdown {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.generated_objects;
	requires jakarta.xml.bind;

	requires org.commonmark;
	requires org.commonmark.ext.gfm.tables;
	requires org.commonmark.ext.gfm.strikethrough;
	requires org.commonmark.ext.task.list.items;
	requires org.commonmark.ext.footnotes;
	requires org.commonmark.ext.front.matter;

	exports org.docx4j.markdown;

}
