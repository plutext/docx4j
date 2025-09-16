module docx4j_samples_docx_diffx {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.openxml_objects;
	requires org.docx4j.docx4j_diffx;
	
	requires jakarta.xml.bind;
	
	// Uncomment ONE of the following:
	requires org.docx4j.JAXB_ReferenceImpl;
//	 requires org.docx4j.JAXB_MOXy;
	
//	requires xercesImpl;
	
	opens org.docx4j.samples.diffx; // required for mbassador	
}
