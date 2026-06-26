module org.docx4j.export_fo {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.generated_objects;
	requires org.plutext.jaxb.xslfo;

	requires org.apache.xmlgraphics.fop.core;
	requires org.apache.xmlgraphics.fop.events;
	requires org.apache.xmlgraphics.fop.util;
	
	// deps of org.apache.xmlgraphics:fop-core:jar:2.5
		requires org.apache.xmlgraphics.batik.anim;
		requires org.apache.xmlgraphics.batik.css;
		requires org.apache.xmlgraphics.batik.dom;
		requires org.apache.xmlgraphics.batik.ext;
		requires org.apache.xmlgraphics.batik.parser;
//			requires org.apache.xmlgraphics.batik.shared.resources;
//			requires org.apache.xmlgraphics.batik.svg.dom;
		requires org.apache.xmlgraphics.batik.util;
		requires org.apache.xmlgraphics.batik.constants;
		requires org.apache.xmlgraphics.batik.i18n;
		requires jakarta.xml.bind;
		
	exports org.docx4j.convert.out.fo;
	exports org.docx4j.convert.out.fo.renderers;
	exports org.docx4j.convert.out.pdf;
	exports org.docx4j.convert.out.pdf.viaXSLFO;
	exports org.docx4j.convert.out.XSLFO;
	
    // Resource folders must be open! See https://stackoverflow.com/questions/45166757/loading-classes-and-resources-in-java-9/45173837#45173837  
	opens org.docx4j.convert.out.fo;
	opens org.docx4j.convert.out.fo.renderers;
	
}
