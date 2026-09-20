module org.docx4j.docx_anon {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.generated_objects;
    requires lorem;
	requires jakarta.xml.bind;
	requires java.xml;

	exports org.docx4j.anon;
	
}
