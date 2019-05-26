module org.docx4j.openxml_objects_sml {
	
    requires java.xml.bind;
	requires org.slf4j;
	requires org.docx4j.openxml_objects;
    
    exports org.xlsx4j.sml;
    exports org.xlsx4j.schemas.microsoft.com.office.excel_2006.main;
    exports org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main;

}