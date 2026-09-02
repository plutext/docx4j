module docx4j_samples_docx4j {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.generated_objects;
	
	requires org.docx4j.docx_anon;
	requires jakarta.xml.bind;
	requires jakarta.mail;  // for ConvertOutHtmlToEmail
	requires jakarta.activation;
	
	// Uncomment ONE of the following:
	requires org.docx4j.JAXB_ReferenceImpl;
	requires org.jvnet.jaxb.plugins.runtime;
	requires org.docx4j.markdown;
//	 requires org.docx4j.JAXB_MOXy;
	
//	requires xercesImpl;
	
	opens org.docx4j.samples; // required for mbassador	
}
