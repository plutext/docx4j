module org.docx4j.fop_word_layout {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.export_fo;
	requires org.apache.xmlgraphics.fop.core;

	exports org.docx4j.fop.wordlayout;

	provides org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer
		with org.docx4j.fop.wordlayout.WordLayoutCustomizer;
}
