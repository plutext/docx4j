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
	//requires org.eclipse.persistence.core;
	
    exports org.docx4j.jaxb.moxy;
    
    opens org.docx4j.jaxb.moxy;	
	
}