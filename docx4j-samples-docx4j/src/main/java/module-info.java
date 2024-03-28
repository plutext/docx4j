module docx4j_samples_docx4j {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.openxml_objects;
	
	requires org.docx4j.docx_anon;
	requires jakarta.xml.bind;
	
	// Uncomment ONE of the following:
	 requires org.docx4j.JAXB_ReferenceImpl;
//	 requires org.docx4j.JAXB_MOXy;
	
	
	opens org.docx4j.samples; // required for mbassador	
}
