/**
 * @author jharrop
 *
 */
module docx4j_JAXB_MOXy {
	
	requires org.slf4j;
	requires docx4j_core;

	requires org.eclipse.persistence.moxy;
	requires org.eclipse.persistence.core;
	
    exports org.docx4j.jaxb.moxy;
    
    opens org.docx4j.jaxb.moxy;	
	
}