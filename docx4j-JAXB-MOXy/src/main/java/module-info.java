/**
 * @author jharrop
 *
 */
module org.docx4j.JAXB_MOXy {
	
	requires org.slf4j;
	requires org.docx4j.core;
	
//	requires transitive jakarta.xml.bind;
	requires transitive org.eclipse.persistence.moxy;
	requires com.sun.tools.xjc;
	requires org.eclipse.persistence.core; // required for MOXy 3.0.1; not necessary with 4.0.2, but doesn't hurt, so leave it here
	
    exports org.docx4j.jaxb.moxy;
    
    opens org.docx4j.jaxb.moxy;	
	
}