package org.apache.xml.serializer;

public class Docx4jNamespaceMappings extends NamespaceMappings /* to make things visible */ {
	

	public Docx4jMappingRecord docx4jGetMappingFromPrefix(String prefix) {
		
		return new Docx4jMappingRecord(this, prefix);
		
	}
}
