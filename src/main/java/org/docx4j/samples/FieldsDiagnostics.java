package org.docx4j.samples;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;

/**
 * List all field instructions found in docx (main document part,
 * headers, footers).
 * 
 * Doesn't list nested fields eg PAGEREF in results part of a TOC field.
 *
 * @author jharrop
 *
 */
public class FieldsDiagnostics {

	private static Logger log = Logger.getLogger(FieldsDiagnostics.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir") + "/Test_TOC.docx"));

		FieldsPreprocessor.complexifyFields(wordMLPackage.getMainDocumentPart() );

		System.out.println("\n" + wordMLPackage.getMainDocumentPart().getPartName() + "\n");
		performOnInstance(wordMLPackage.getMainDocumentPart().getPartName().getName(), 
				wordMLPackage.getMainDocumentPart().getContent() );

		{ // Headers, footers

			RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {

				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {

					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);

					FieldsPreprocessor.complexifyFields(part );

					System.out.println("\n" + part.getPartName() + "\n");
					performOnInstance(part.getPartName().getName(),
							((ContentAccessor)part).getContent() );

				}
			}


		}

	}

	private static void performOnInstance(String partName, List<Object> contentList) throws Docx4JException {

		StringBuilder sb = new StringBuilder(); 

		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(contentList, fl);
		sb.append("In " + partName + " there are " + fl.getStarts().size() + " paragraphs containing the following fields: ");


		// canonicalise and setup fieldRefs
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index;
			P newP;
			if (p.getParent() instanceof ContentAccessor) {
				index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
				newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//				log.debug("NewP length: " + newP.getContent().size() );
				((ContentAccessor)p.getParent()).getContent().set(index, newP);
			} else if (p.getParent() instanceof java.util.List) {
				index = ((java.util.List)p.getParent()).indexOf(p);
				newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//				log.debug("NewP length: " + newP.getContent().size() );
//				((java.util.List)p.getParent()).set(index, newP);
			} else {
				throw new Docx4JException ("Unexpected parent: " + p.getParent().getClass().getName() );
			}
			
			System.out.println(index+ "  " + XmlUtils.marshaltoString(p, true, true));
			System.out.println("RESULT  " + XmlUtils.marshaltoString(newP, true, true));
		}

		// Populate
		for (FieldRef fr : fieldRefs) {

			String instr = fr.getFldName();
			sb.append("\n" + instr);
			
			recurse(sb, fr, "    ");
		}

		System.out.println(sb.toString());
	}
	
	private static void recurse(StringBuilder sb, FieldRef fr, String indent) {
		
		for(Object o : fr.getInstructions() ) {
			if (o instanceof FieldRef) {
				sb.append("\n" + indent +  ((FieldRef)o).getFldName());
				recurse(sb, ((FieldRef)o), indent + "    ");
				
			}
		}
		
	}


}
