module org.docx4j.samples_pptx4j {

	requires org.slf4j;
	requires org.docx4j.openxml_objects;
	requires org.docx4j.openxml_objects_pml;
	requires org.docx4j.openxml_objects_sml;
	requires org.docx4j.core;
	requires jakarta.xml.bind; // order seems to matter here!?
    
}
