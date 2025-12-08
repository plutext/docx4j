package org.docx4j.openpackaging;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageRelsUtil {  
	
	protected static Logger log = LoggerFactory.getLogger(PackageRelsUtil.class);
	
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
			else if (rel.getType().equals(
					"http://purl.oclc.org/ooxml/officeDocument/relationships/officeDocument") ) {
				return rel.getTarget();
			} 
//			else {
//				System.out.println(rel.getType());
//			}
		}
		log.error(packageRels.getXML());
		throw new Docx4JException("No relationship of type officeDocument");
	}

	public static boolean isStrict(RelationshipsPart packageRels) throws Docx4JException  {
		
		for (Relationship rel : packageRels.getRelationships().getRelationship() ) {
			
			if (rel.getType().equals(
					"http://purl.oclc.org/ooxml/officeDocument/relationships/officeDocument") ) {
				return true;
			} 
		}
		return false;
	}
	
}
