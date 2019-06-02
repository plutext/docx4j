module org.docx4j.export_fo {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.openxml_objects;

	// TODO ADD fop, batik etc
	
	exports org.docx4j.convert.out.fo;
	exports org.docx4j.convert.out.fo.renderers;
	exports org.docx4j.convert.out.pdf;
	exports org.docx4j.convert.out.pdf.viaXSLFO;
	exports org.docx4j.convert.out.XSLFO;
	
}
