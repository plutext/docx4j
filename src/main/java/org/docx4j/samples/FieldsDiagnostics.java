package org.docx4j.samples;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
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
						System.getProperty("user.dir") + "/a_fields.docx"));

		FieldsPreprocessor.complexifyFields(wordMLPackage.getMainDocumentPart() );

		System.out.println("\n" + wordMLPackage.getMainDocumentPart().getPartName() + "\n");
		performOnInstance(wordMLPackage.getMainDocumentPart().getContent() );

		{ // Headers, footers

			RelationshipsPart rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getJaxbElement().getRelationship()  ) {

				if (r.getType().equals(Namespaces.HEADER)
						|| r.getType().equals(Namespaces.FOOTER)) {

					JaxbXmlPart part = (JaxbXmlPart)rp.getPart(r);

					FieldsPreprocessor.complexifyFields(part );

					System.out.println("\n" + part.getPartName() + "\n");
					performOnInstance(
							((ContentAccessor)part).getContent() );

				}
			}


		}

	}

	private static void performOnInstance(List<Object> contentList) throws Docx4JException {


		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(contentList, fl);
		log.info("Found " + fl.getStarts().size() + " paragraphs containing the following fields: ");


		// canonicalise and setup fieldRefs
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index;
			if (p.getParent() instanceof ContentAccessor) {
				index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//				log.debug("NewP length: " + newP.getContent().size() );
				((ContentAccessor)p.getParent()).getContent().set(index, newP);
			} else if (p.getParent() instanceof java.util.List) {
				index = ((java.util.List)p.getParent()).indexOf(p);
				P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
//				log.debug("NewP length: " + newP.getContent().size() );
//				((java.util.List)p.getParent()).set(index, newP);
			} else {
				throw new Docx4JException ("Unexpected parent: " + p.getParent().getClass().getName() );
			}
		}

		// Populate
		for (FieldRef fr : fieldRefs) {

			String instr = fr.getInstr();
			System.out.println(instr);
		}


	}


}
