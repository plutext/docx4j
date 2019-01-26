package org.docx4j.openpackaging;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;

public class PackageRelsUtil {  
	
	public static String getNameOfMainPart(RelationshipsPart packageRels) throws Docx4JException  {
		
		// find rel of type officeDocument
		for (Relationship rel : packageRels.getRelationships().getRelationship() ) {
			
			if (rel.getType().equals(
					"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument") ) {
				return rel.getTarget();
			} 
			else if (rel.getType().equals(
					"http://schemas.microsoft.com/office/2006/relationships/graphicFrameDoc") ) {
				// v:shape/@o:gfxdata
				return rel.getTarget();
			} 
			else if (rel.getType().equals(
					"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramLayout") ) {
				// Glox
				return rel.getTarget();
			} 
//			else {
//				System.out.println(rel.getType());
//			}
		}
		throw new Docx4JException("No relationship of type officeDocument");
	}
	
}
