package org.docx4j.samples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;

/**
 * List all field instructions found in docx (main document part,
 * headers, footers).
 * 
 * Shortcoming: Doesn't handle fields which cross paragraph boundaries eg TOC field,
 * 
 *
 * @author jharrop
 *
 */
public class FieldsDiagnostics {

	private static Logger log = LoggerFactory.getLogger(FieldsDiagnostics.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		// A docx or a dir containing docx files
		String inputpath = System.getProperty("user.dir") + "/dd/";

		StringBuilder sb = new StringBuilder(); 
		
		File dir = new File(inputpath);
		
		if (dir.isDirectory()) {
			
			String[] files = dir.list();
			
			for (int i = 0; i<files.length; i++  ) {
				
				if (files[i].endsWith("docx")) {
					sb.append("\n\n" + files[i] + "\n");
					WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputpath + "/" + files[i]));		
					pkgReport(wordMLPackage, sb);
				}
			}
			
		} else if (inputpath.endsWith("docx")) {
			sb.append("\n\n" + inputpath + "\n");
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputpath ));		
			pkgReport(wordMLPackage, sb);			
		}
		
		System.out.println(sb.toString());
		
	}
	
	private static void pkgReport(WordprocessingMLPackage wordMLPackage, StringBuilder sb) throws Docx4JException {


		
		FieldsPreprocessor.complexifyFields(wordMLPackage.getMainDocumentPart() );

//		System.out.println("\n" + wordMLPackage.getMainDocumentPart().getPartName() + "\n");
		listFieldsInPart(wordMLPackage.getMainDocumentPart().getPartName().getName(), 
				wordMLPackage.getMainDocumentPart().getContent(), sb );

		{ // Headers, footers

			RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {

				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {

					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);

					FieldsPreprocessor.complexifyFields(part );

					System.out.println("\n" + part.getPartName() + "\n");
					listFieldsInPart(part.getPartName().getName(),
							((ContentAccessor)part).getContent(), sb );

				}
			}


		}
		

	}

	private static void listFieldsInPart(String partName, List<Object> contentList, StringBuilder sb) throws Docx4JException {

		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(contentList, fl);
		
		if (fl.getStarts().size()==0) {
			sb.append("\n\n" + partName + ": no fields detected.");
			return;
		}

		sb.append("\n\n" + partName + ": " + fl.getStarts().size() + " paragraphs containing the following fields: ");

		// canonicalise and setup fieldRefs
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			FieldsPreprocessor.canonicalise(p, fieldRefs);
		}

		// Populate
		for (FieldRef fr : fieldRefs) {
			recurse(sb, fr, "    ");
		}

	}
	
	private static void recurse(StringBuilder sb, FieldRef fr, String indent) {
		
		for(Object o : fr.getInstructions() ) {
			if (o instanceof FieldRef) {
				recurse(sb, ((FieldRef)o), indent + "    ");
			} else {
				o = XmlUtils.unwrap(o);
				if (o instanceof Text) {
					sb.append("\n" + indent + ((Text)o).getValue() );									
				} else {
					sb.append("\n" + indent + XmlUtils.unwrap(o).getClass().getName() );
				}
			}
		}
		
	}


}
