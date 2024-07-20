module org.docx4j.docx_anon {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.openxml_objects;
    requires lorem;
	requires jakarta.xml.bind;
	
	requires org.apache.commons.codec; // MOXy needs this
	
	//requires docx4j_JAXB_ReferenceImpl;
    
//	requires docx4j_JAXB_MOXy;
//	requires org.eclipse.persistence.moxy;
//	requires org.eclipse.persistence.core;
	
	exports org.docx4j.anon;
	
	
}
