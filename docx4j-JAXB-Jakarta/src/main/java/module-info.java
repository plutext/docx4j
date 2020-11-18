/**
 * @author jharrop
 *
 */
module org.docx4j.JAXB_ReferenceImpl {
	
	requires org.slf4j;
	requires org.docx4j.core;
	
	requires org.glassfish.jaxb.runtime; 
	requires jakarta.xml.bind;
	
    exports org.docx4j.jaxb.glassfish;
    
    opens org.docx4j.jaxb.glassfish;

	
}