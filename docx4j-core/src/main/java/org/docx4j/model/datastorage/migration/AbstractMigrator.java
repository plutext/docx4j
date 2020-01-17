package org.docx4j.model.datastorage.migration;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.customXmlProperties.SchemaRefs;
import org.docx4j.customXmlProperties.SchemaRefs.SchemaRef;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SdtPr.Alias;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Text;
import org.opendope.conditions.Conditions;
import org.opendope.xpaths.Xpaths;

public class AbstractMigrator {
	
	protected XPathsPart xPathsPart;
	protected ConditionsPart conditionsPart;
	
	protected String storeItemID; // of the bound part
		
	protected void createParts(WordprocessingMLPackage pkgOut) throws InvalidFormatException {
	
		createConditionsPart(pkgOut, new Conditions());
		createXPathsPart(pkgOut, new Xpaths());
		
	}

	protected ConditionsPart createConditionsPart(WordprocessingMLPackage pkgOut, Conditions conditions) throws InvalidFormatException {
		
		conditionsPart = new ConditionsPart(new PartName("/customXml/item1.xml")); // name doesn't matter
		pkgOut.getMainDocumentPart().addTargetPart(conditionsPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); // Word will silently drop the CXPs if they aren't added to the MDP!
		addPropertiesPart(pkgOut, conditionsPart, "http://opendope.org/conditions");
		conditionsPart.setJaxbElement(conditions);
		return conditionsPart;		
	}
	
	protected XPathsPart createXPathsPart(WordprocessingMLPackage pkgOut, Xpaths xpaths) throws InvalidFormatException {
		
		xPathsPart = new XPathsPart(new PartName("/customXml/item1.xml")); 
		pkgOut.getMainDocumentPart().addTargetPart(xPathsPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		addPropertiesPart(pkgOut, xPathsPart, "http://opendope.org/xpaths");
		xPathsPart.setJaxbElement(xpaths);
		return xPathsPart;
	}
	
	
	protected String addPropertiesPart(WordprocessingMLPackage pkgOut, 
			Part customXmlDataStoragePart, String ns)
			throws InvalidFormatException {

		CustomXmlDataStoragePropertiesPart part = new CustomXmlDataStoragePropertiesPart();

		org.docx4j.customXmlProperties.ObjectFactory of = new org.docx4j.customXmlProperties.ObjectFactory();

		DatastoreItem dsi = of.createDatastoreItem();
		String newItemId = "{" + UUID.randomUUID().toString().toUpperCase() + "}";
		dsi.setItemID(newItemId);
		
		if (ns!=null) {
			SchemaRefs srefs = of.createSchemaRefs();
			dsi.setSchemaRefs(srefs);
			
			SchemaRef sref = of.createSchemaRefsSchemaRef();
			sref.setUri(ns);
			
			srefs.getSchemaRef().add(sref);
		}
		
		part.setJaxbElement(dsi);

		customXmlDataStoragePart.addTargetPart(part, AddPartBehaviour.RENAME_IF_NAME_EXISTS);

		pkgOut.getCustomXmlDataStorageParts().put(newItemId.toLowerCase(), (CustomXmlPart)customXmlDataStoragePart);
		
		return newItemId;
	}
	
	/**
	 * create an SdtRun, and add it to replacementContent
	 * 
	 * @param r
	 * @param replacementContent
	 * @param key
	 */
	protected void createContentControl(RPr rPr, List<Object> replacementContent,
			String key, String xpath, String prefixmappings) {

		SdtRun sdtRun = this.createSdtRun(rPr, key, xpath, prefixmappings);
		replacementContent.add(sdtRun);
		
	}
	
	/**
	 * create an SdtRun
	 * 
	 * @param r
	 * @param key
	 */
	protected SdtRun createSdtRun(RPr rPr, 
			String key, String xpath, String prefixmappings) {
	
		// create the content control
		SdtRun sdtRun = Context.getWmlObjectFactory().createSdtRun();
		
		// set SdtPr
		SdtPr sdtPr = createSdtPr(key, xpath, prefixmappings, storeItemID,
				false);
		sdtRun.setSdtPr(sdtPr);
		// <w:text w:multiLine="1"/>
		CTSdtText sdtText = Context.getWmlObjectFactory().createCTSdtText();
		sdtText.setMultiLine(true);
		sdtPr.getRPrOrAliasOrLock().add(
				Context.getWmlObjectFactory().createSdtPrText(sdtText));
		
		// set contents		
		CTSdtContentRun sdtContent = Context.getWmlObjectFactory().createCTSdtContentRun();			
		sdtRun.setSdtContent(sdtContent);

		R rnew = new R();
		rnew.setRPr( rPr ); // point at old rPr, if any
		Text text = Context.getWmlObjectFactory().createText();
		text.setValue(key);
		rnew.getContent().add(text);
		
		sdtContent.getContent().add(rnew);
		
		return sdtRun;
		
	}
	
	protected static SdtPr createSdtPr( String key, String xpath, String prefixmappings,
			String storeItemID,
			boolean isRepeat) {
	
		
		SdtPr sdtPr = Context.getWmlObjectFactory().createSdtPr();
		/* <w:sdtPr>
		    <w:alias w:val="Title of document"/>
		    <w:tag w:val="od:xpath=cktpD"/>
		    <w:id w:val="289095091"/>
		    <w:dataBinding w:prefixMappings="xmlns:oda='http://opendope.org/answers'" 
		    w:xpath="/oda:answers/oda:answer[@id='Title_of_document_nM']" 
		    w:storeItemID="{183E9AF4-65AB-46DF-8044-944891825721}"/>
		    <w:text w:multiLine="1"/>
		  </w:sdtPr>
		  */
		
		Alias alias = Context.getWmlObjectFactory().createSdtPrAlias();
		alias.setVal(key);
		sdtPr.getRPrOrAliasOrLock().add(alias);
		
		Tag tag = Context.getWmlObjectFactory().createTag();
		if (isRepeat) {
			tag.setVal("od:repeat=" + key);
		} else {
			tag.setVal("od:xpath=" + key);			
		}
		sdtPr.setTag(tag);
		sdtPr.setId();
		
		if (isRepeat) {
			sdtPr.getRPrOrAliasOrLock().add(new SdtPr.RichText());
		} else {
			CTDataBinding ctDataBinding = Context.getWmlObjectFactory().createCTDataBinding();
			JAXBElement<CTDataBinding> jaxbDB = 
					Context.getWmlObjectFactory().createSdtPrDataBinding(ctDataBinding);
			sdtPr.setDataBinding(ctDataBinding);
			ctDataBinding.setXpath(xpath);
			ctDataBinding.setPrefixMappings(prefixmappings);
			ctDataBinding.setStoreItemID(storeItemID);			
		}
				
		return sdtPr;
					
	}
	
}
