module org.docx4j.services.legacy {

	requires org.docx4j.core;
	
    requires com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.core;
    requires transitive org.apache.httpcomponents.httpclient;
    requires transitive org.apache.httpcomponents.httpcore;
	requires org.slf4j;
    
    exports org.docx4j.services.client;
    
    
}
