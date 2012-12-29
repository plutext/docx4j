package org.docx4j.openpackaging.io3.stores;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.XmlPart;

public interface PartStore {
	
	public boolean partExists(String partName);
	
	public InputStream loadPart(String partName) throws IOException;
	
	
	public void setOutputStream(OutputStream os);
	
	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException;
	
	public void saveJaxbXmlPart(JaxbXmlPart part, String targetName) throws Docx4JException;
	
	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part, String targetName) throws Docx4JException;	
	
	public void saveXmlPart(XmlPart part, String targetName) throws Docx4JException;
	
	public void saveBinaryPart(Part part) throws Docx4JException;	
	
	public void finishSave() throws Docx4JException;

}
