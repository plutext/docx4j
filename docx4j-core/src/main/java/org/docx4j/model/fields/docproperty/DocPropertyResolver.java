package org.docx4j.model.fields.docproperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.FieldFormattingException;
import org.docx4j.model.fields.FieldValueException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;

public class DocPropertyResolver {
	
	private static Logger log = LoggerFactory.getLogger(DocPropertyResolver.class);		
	

	/* Needs access to the following parts:
		- core props
		- custom props
		- extended props
		
		But not CoverPageProps (if available), since those are
		accessible via content control data binding, but not
		DOCPROPERTY field (at least via Word 2010 UI).

		Notes:
		
			 DOCPROPERTY  Pages  \* MERGEFORMAT 
			--> 54
			 DOCPROPERTY  PAGES / NUMPAGES  \* MERGEFORMAT 
			!-> NOT FOUND! 
	
	
	*/	
	
	private DocPropsCustomPart docPropsCustomPart;	
	private org.docx4j.docProps.core.CoreProperties coreProperties;
	private org.docx4j.docProps.extended.Properties extendedProperties;
	
	private final static String BUILT_IN_FIELDS_CORE = "Author, Comments, CreateTime, LastPrinted, LastSavedBy, LastSavedTime, RevisionNumber, Subject, Title";

	private final static String BUILT_IN_FIELDS_EXTENDED = "Characters, CharactersWithSpaces, Company, Lines, NameofApplication, Pages, Paragraphs, Security, Template, TotalEditingTime, Words";

	private final static String BUILT_IN_FIELDS_UNSUPPORTED = "Bytes";
	
	public DocPropertyResolver(WordprocessingMLPackage wordMLPackage) {
		
		coreProperties = wordMLPackage.getDocPropsCorePart() == null ? null : wordMLPackage.getDocPropsCorePart().getJaxbElement(); 
		extendedProperties = wordMLPackage.getDocPropsExtendedPart() == null ? null : wordMLPackage.getDocPropsExtendedPart().getJaxbElement();

		docPropsCustomPart = wordMLPackage.getDocPropsCustomPart();
		
	}
	
	public String getValue(String key) throws FieldFormattingException, FieldValueException {
		
		Object value = null;
		
		// Most likely a custom value, so try this first
		if (docPropsCustomPart!=null) {
			value = docPropsCustomPart.getProperty(key);
		}
		
		if (BUILT_IN_FIELDS_CORE.contains(key)) {
			value = getCoreValue(key);
		} else if (BUILT_IN_FIELDS_EXTENDED.contains(key)) {
			value = getExtendedValue(key);
		} else if (BUILT_IN_FIELDS_UNSUPPORTED.contains(key)) {
			log.warn("No support for DOCPROPERTY field " + key);
			return null;
		} 
		
		if (value==null) {
			throw new FieldValueException("No value found for DOCPROPERTY " + key);			
		} else {
			
			if (value instanceof String) {
				return (String)value;
			} else if (value instanceof Integer) {
				return ((Integer)value).toString();
			} else if (value instanceof org.docx4j.docProps.custom.Properties.Property) {
				
				org.docx4j.docProps.custom.Properties.Property property = (org.docx4j.docProps.custom.Properties.Property)value;
				if (property.getLpwstr()!=null) {
					// eg <vt:lpwstr>Martin</vt:lpwstr>
					return property.getLpwstr();
				} else if (property.getFiletime()!=null) {
					// <vt:filetime>2020-01-01T10:00:00Z</vt:filetime>
					XMLGregorianCalendar date = property.getFiletime();
					// toString and toXMLFormat seem to be the same;
					return date.toXMLFormat();
					
				} else if ( property.getDate()!=null ) {
						return property.getDate().toXMLFormat();
				} else {
					throw new FieldFormattingException(" TODO: handle " + XmlUtils.marshaltoString(property, Context.jcDocPropsCustom));					
				}
				
			} else {
				throw new FieldFormattingException(key + " TODO: convert " + value.getClass().getName() + " to string");
			}
		}

	}

	private Object getCoreValue(String key) {
				
		if ("Author".equals(key)) return getFirstValue(coreProperties.getCreator());
		else if ("Comments".equals(key)) return getFirstValue(coreProperties.getDescription().getValue());
		else if ("CreateTime".equals(key)) return getFirstValue(coreProperties.getCreated());
		else if ("LastPrinted".equals(key)) return coreProperties.getLastPrinted(); // XMLGregorianCalendar
		else if ("LastSavedBy".equals(key)) return getFirstValue(coreProperties.getModified());
		else if ("LastSavedTime".equals(key)) return coreProperties.getLastModifiedBy();
		else if ("RevisionNumber".equals(key)) return coreProperties.getRevision();
		else if ("Subject".equals(key)) return getFirstValue(coreProperties.getSubject());
		else if ("Title".equals(key)) return getFirstValue(coreProperties.getTitle().getValue());
		else {
			log.error("Handle " + key);
			return null;
		}
		
	}
	
	private Object getExtendedValue(String key) {
	
		if ("Characters".equals(key)) return extendedProperties.getCharacters();
		else if ("CharactersWithSpaces".equals(key)) return extendedProperties.getCharactersWithSpaces();
		else if ("Company".equals(key)) return extendedProperties.getCompany();
		else if ("HyperlinkBase".equals(key)) return extendedProperties.getHyperlinkBase();
		else if ("Lines".equals(key)) return extendedProperties.getLines();
		else if ("NameofApplication".equals(key)) return extendedProperties.getApplication();
		else if ("Pages".equals(key)) return extendedProperties.getPages();
		else if ("Paragraphs".equals(key)) return extendedProperties.getParagraphs();
		else if ("Security".equals(key)) return extendedProperties.getDocSecurity();
		else if ("Template".equals(key)) return extendedProperties.getTemplate();
		else if ("TotalEditingTime".equals(key)) return extendedProperties.getTotalTime();
		else if ("Words".equals(key)) return extendedProperties.getWords();
		else {
			log.error("Handle " + key);
			return null;
		}
		
	}
	
	private String getFirstValue(SimpleLiteral simpleLiteral) {
		
		if (simpleLiteral.getContent().size()==0) {
			return null;
		} else {
			return simpleLiteral.getContent().get(0);
		}
		
	}

}
