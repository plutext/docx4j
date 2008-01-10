/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.parts;


import java.util.Iterator;
import java.util.Map;


import org.apache.log4j.Logger;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;



public class DocPropsCustomPart extends AbstractDocPropsPart  {
	
	/* Specification > Shared ML > Metadata > Custom Properties
	 * 
	 */
		
	
	/*
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
		<Properties 
			xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" 
			xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Grouping">
				<vt:lpwstr>p</vt:lpwstr>
			</property>
			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="3" name="CheckinComments">
				<vt:lpwstr>true</vt:lpwstr>
			</property>
		</Properties>
		
	*/
	
	
	
	private static Logger log = Logger.getLogger(DocPropsCustomPart.class);
	
	private static final String fmtidVal = "{D5CDD505-2E9C-101B-9397-08002B2CF9AE}";
	
	 /** 
	 * @throws InvalidFormatException
	 */
	//public MainDocumentPart(Package pack, PackagePartName partUri) {
	public DocPropsCustomPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMPROPERTIES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PROPERTIES_CUSTOM);

	}

	public void setDocument(Document document) {
		this.document = document;
		unmarshall(document);
	}
	
	public Document getDocument() {
		return marshall();
	}
	
		
	/* Create a Java object tree from the XML document 
	 */
	public void unmarshall(Document doc) {
		
		String name;
		String value;
		
		try {
			//debugPrint(doc);
		    Element root = doc.getRootElement();
		    Iterator elementIterator = root.elementIterator();
		    while(elementIterator.hasNext()){
		      Element element = (Element)elementIterator.next();
		      
			  if (element.getName()=="property" ) {
				  
//					<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Grouping">
//						<vt:lpwstr>p</vt:lpwstr>
//					</property>
				  
				  name = element.attributeValue("name");
				  
				  if (element.element("lpwstr")!=null ) {
					  value = element.element("lpwstr").getText();
					  setProperty(name, value);					  
					  log.info("Registering property " + name + "=" + value);
				  } else {
					  	// TODO - handle 
					  log.warn("Handle " + name );
					  debugPrint(doc);
					  
				  }
			  } else {
			      log.warn("Encountered unexpected element '" + element.getName() + "'");
			  }
		    }
		} catch (Exception e ) {
			e.printStackTrace();
		}
		
	}
	

		
	
	/* Create an XML document from the Java object tree
	 * 
	 */
	private Document marshall() {

//		<?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
//		<Properties 
//			xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" 
//			xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
//			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="2" name="Grouping">
//				<vt:lpwstr>p</vt:lpwstr>
//			</property>
//			<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="3" name="CheckinComments">
//				<vt:lpwstr>true</vt:lpwstr>
//			</property>
//		</Properties>

		Document doc = DocumentHelper.createDocument();

		// Note carefully how namespaces are handled here.
		// It seems we have to be syntactically identical to how
		// Word does it - semantic equivalence is not enough!
		
		Namespace docPropsVTypes = new Namespace("vt",
				"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes");
		
		Element elDocument = doc.addElement("Properties",
				"http://schemas.openxmlformats.org/officeDocument/2006/custom-properties");
		elDocument.addNamespace("vt",
				"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes");

		Integer pid = 2; // NB @pid="1" will cause Word to regard the document as corrupt.
		Iterator i = properties.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();	
			Element property = elDocument.addElement("property",
			"http://schemas.openxmlformats.org/officeDocument/2006/custom-properties");
			property.addAttribute( "fmtid", fmtidVal);
			property.addAttribute( "pid", pid.toString() );
			property.addAttribute( "name", (String)e.getKey() );
			Element lpwstr = new DefaultElement(new QName("lpwstr", docPropsVTypes));
			lpwstr.setText( (String)e.getValue() );
			property.add(lpwstr);
			pid++;
		}
		return doc;
	}
			
	
}

	
