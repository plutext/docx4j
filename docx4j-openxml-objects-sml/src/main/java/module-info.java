module docx4j_openxml_objects_sml {
	
    requires transitive java.xml.bind;
	requires org.slf4j;
	requires docx4j_openxml_objects;
    
    exports org.xlsx4j.sml;
    exports org.xlsx4j.schemas.microsoft.com.office.excel_2006.main;
    exports org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main;

}