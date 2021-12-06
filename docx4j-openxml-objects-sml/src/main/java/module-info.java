module org.docx4j.openxml_objects_sml {
	
	requires org.slf4j;
	requires org.docx4j.openxml_objects;
	requires jakarta.xml.bind;
    
    exports org.xlsx4j.sml;
    exports org.xlsx4j.schemas.microsoft.com.office.excel_2006.main;
    exports org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main;
    exports org.xlsx4j.schemas.microsoft.com.office.excel.x2010.spreadsheetDrawing;

    opens org.xlsx4j.sml;
    opens org.xlsx4j.schemas.microsoft.com.office.excel_2006.main;
    opens org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main;
    opens org.xlsx4j.schemas.microsoft.com.office.excel.x2010.spreadsheetDrawing;
    
}