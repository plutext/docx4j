package org.docx4j.model.datastorage.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.customXmlProperties.SchemaRefs;
import org.docx4j.customXmlProperties.SchemaRefs.SchemaRef;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will help you to migrate
 * from a string replacement approach,
 * to use of content control data bindings.
 * 
 * After migrating, you'll be able to
 * use the OpenDoPE authoring tool to
 * add repeats, conditionals, and other
 * OpenDoPE features, if you need them.
 * 
 * It assumes your magic strings take the
 * form ${--}.  If they don't, you'll
 * need to modify this class, or use Word
 * to search/replace to change them so they
 * take that form.
 * 
 * We'd be happy to accept a patch which
 * specifies a regex the magic string
 * must match.
 * 
 * Limitations: this first version
 * operates only on the main document part
 * (ie it won't process variables in
 *  headers/footers, footnotes/endnotes,
 *  or comments)
 * 
 * @author jharrop
 * @since 3.0.0
 */
public class FromVariableReplacement extends AbstractMigratorUsingAnswersFormat {
	
	private static Logger log = LoggerFactory.getLogger(FromVariableReplacement.class);
		
	public WordprocessingMLPackage migrate(WordprocessingMLPackage pkgIn) throws Exception {
		
		// TODO - test that OpenDoPE parts aren't already present
		// or if they are, that this docx is using our answer format
		// (since only that format is supported here)
		
		// Clone it
		WordprocessingMLPackage pkgOut = (WordprocessingMLPackage)pkgIn.clone();
		
		// Run the cleaner first
		VariablePrepare.prepare(pkgOut);		
		
		// Create the CustomXML parts
		createParts(pkgOut);
		
		// Operate at the p level
		PFinder pFinder = new PFinder();
        new TraversalUtil(pkgOut.getMainDocumentPart().getContent(), pFinder);

        for ( P p : pFinder.pList) { 
        	
        	List<Object> replacementContent = new ArrayList<Object>(); 
        	
        	for (Object o : p.getContent() ) {
        		
        		o = XmlUtils.unwrap(o);
        		
        		if ( o instanceof R) {
        			
        			for (Object o2 : ((R)o).getContent() ) {
                		Object o3 = XmlUtils.unwrap(o2);
        				
        				if ( o3 instanceof Text ) {
        					handle((R)o, ((Text)o3).getValue(), 0, replacementContent);
        				} else {
        					// Just add this bit
        					R rnew = new R();
        					rnew.setRPr( ((R)o).getRPr() ); // point at old rPr, if any
        					rnew.getContent().add(o2);
        					replacementContent.add(rnew);
        				}        				
        			}
        		} else {
        			replacementContent.add(o);
        		}
        	}
        	
        	p.getContent().clear();
        	p.getContent().addAll(replacementContent);
        	
        }
				
		return pkgOut;

	}
	

	private void handle(R r, String s, int offset,
			List<Object> replacementContent) {

		int startKey = s.indexOf("${", offset);
		if (startKey == -1) {
			addTextRun(r, s.substring(offset), replacementContent);
			return;
		} else {
			addTextRun(r, s.substring(offset, startKey), replacementContent);
			int keyEnd = s.indexOf('}', startKey);
			String key = s.substring(startKey + 2, keyEnd);

			createContentControl(r.getRPr(), replacementContent, key);
			
			handle(r, s, keyEnd + 1, replacementContent);
		}
	}


	
	
	 private void addTextRun(R r, String val, List<Object> replacementContent) {
		 
			R rnew = new R();
			rnew.setRPr( r.getRPr() ); // point at old rPr, if any
			
			Text text = Context.getWmlObjectFactory().createText();
			text.setValue(val);
			if (val.startsWith(" ")
					|| val.endsWith(" ") ) {
				text.setSpace("preserve");
			}
			
			rnew.getContent().add(text);
			replacementContent.add(rnew);
		 
	 }
	 

//	public String addPropertiesPart(JaxbCustomXmlDataStoragePart<?> customXmlDataStoragePart, String ns)
//			throws InvalidFormatException {
//
//		CustomXmlDataStoragePropertiesPart part = new CustomXmlDataStoragePropertiesPart();
//
//		org.docx4j.customXmlProperties.ObjectFactory of = new org.docx4j.customXmlProperties.ObjectFactory();
//
//		DatastoreItem dsi = of.createDatastoreItem();
//		String newItemId = "{" + UUID.randomUUID().toString().toUpperCase() + "}";
//		dsi.setItemID(newItemId);
//		
//		SchemaRefs srefs = of.createSchemaRefs();
//		dsi.setSchemaRefs(srefs);
//		
//		SchemaRef sref = of.createSchemaRefsSchemaRef();
//		sref.setUri(ns);
//		
//		srefs.getSchemaRef().add(sref);
//		
//		part.setJaxbElement(dsi);
//
//		customXmlDataStoragePart.addTargetPart(part, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
//		
//		return newItemId;
//	}
	
	  static class PFinder extends CallbackImpl {

          List<P> pList = new ArrayList<P>();  

          @Override
          public List<Object> apply(Object o) {

              if (o instanceof P ) {
            	  pList.add((P)o);
              }                      
              return null;
          }
	  }

	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/unmarshallFromTemplateExample.docx";

		String outputfilepath = System.getProperty("user.dir")
				+ "/OUT_VariableReplace.docx";

		WordprocessingMLPackage pkgIn = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		FromVariableReplacement migrator = new FromVariableReplacement();
		WordprocessingMLPackage pkgOut = migrator.migrate(pkgIn);
		
		SaveToZipFile saver = new SaveToZipFile(pkgOut);
		saver.save(outputfilepath);
		
	}

}
