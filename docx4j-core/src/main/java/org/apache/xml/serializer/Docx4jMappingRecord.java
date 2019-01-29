package org.apache.xml.serializer;

import org.apache.xml.serializer.NamespaceMappings.MappingRecord;

public class Docx4jMappingRecord {
	
	private static final String EMPTYSTRING = "";	
	
    final String m_prefix;  // the prefix
    public final String m_uri;     // the uri, possibly "" but never null
    // the depth of the element where declartion was made
    public final int m_declarationDepth;
    
//    Docx4jMappingRecord(String prefix, String uri, int depth) {
//        m_prefix = prefix;
//        m_uri = (uri==null)? EMPTYSTRING : uri;
//        m_declarationDepth = depth;
//    }
	
	
	public Docx4jMappingRecord(NamespaceMappings namespaceMappings, String prefix) {
		
		MappingRecord mappingRecord = namespaceMappings.getMappingFromPrefix(prefix);

        m_prefix = mappingRecord.m_prefix;
        m_uri = mappingRecord.m_uri;
        m_declarationDepth = mappingRecord.m_declarationDepth;

	}

}
