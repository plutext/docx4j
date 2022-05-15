/**
 * @author jharrop
 *
 */
module org.docx4j.JAXB_ReferenceImpl {
	
	requires org.slf4j;
	requires org.docx4j.core;
	
//	requires com.sun.xml.bind; 
	requires org.glassfish.jaxb.runtime;
	requires jakarta.xml.bind; 

//	requires transitive org.jvnet.staxex;
//	requires transitive com.sun.xml.txw2;
//	requires transitive com.sun.istack.runtime;
//	requires transitive com.sun.xml.fastinfoset;
	
    exports org.docx4j.jaxb.ri;
    
    opens org.docx4j.jaxb.ri;

	
}