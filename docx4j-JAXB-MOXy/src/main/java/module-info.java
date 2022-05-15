/**
 * @author jharrop
 *
 */
module org.docx4j.JAXB_MOXy {
	
	requires org.slf4j;
	requires org.docx4j.core;

	requires org.eclipse.persistence.moxy;
	requires org.eclipse.persistence.core;
	requires jakarta.xml.bind;
	
    exports org.docx4j.jaxb.moxy;
    
    opens org.docx4j.jaxb.moxy;	
	
}