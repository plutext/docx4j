package org.docx4j.samples;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * org.docx4j.model.fields.merge.MailMerger has
 * options for what to do with MERGEFIELD after processing,
 * and that is ordinarily what you would use.
 * 
 * The purpose of this example is simply to remove MERGEFIELD
 * without performing any merge.  
 * 
 * This is useful where you want to clean up a pre-existing
 * docx before working on it.
 *   
 * @author jharrop
 *
 */
public class FieldsRemoveMERGEFIELD  {
	
	private static Logger log = LoggerFactory.getLogger(FieldsRemoveMERGEFIELD.class);
	
	public WordprocessingMLPackage migrate(WordprocessingMLPackage pkgIn) throws Exception {
		
		// Clone it
		WordprocessingMLPackage pkgOut = (WordprocessingMLPackage)pkgIn.clone();
						
		FieldsPreprocessor.complexifyFields(pkgOut.getMainDocumentPart() );
        if(log.isDebugEnabled()) {
            log.debug("complexified: "
                    + XmlUtils.marshaltoString(pkgOut.getMainDocumentPart().getJaxbElement(), true));
        }
		
		// find fields
		ComplexFieldLocator fl = new ComplexFieldLocator();
		new TraversalUtil(pkgOut.getMainDocumentPart().getContent(), fl);
		log.info("Found " + fl.getStarts().size() + " fields ");
		
		
		// canonicalise and setup fieldRefs 
		List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		for( P p : fl.getStarts() ) {
			int index = ((ContentAccessor)p.getParent()).getContent().indexOf(p);
			P newP = FieldsPreprocessor.canonicalise(p, fieldRefs);
			((ContentAccessor)p.getParent()).getContent().set(index, newP);
			
			/*
			 *   <w:p xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:ns23="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math">
				    <w:r>
				      <w:t xml:space="preserve">Hallo, lower </w:t>
				    </w:r>
				    <w:r>
				      <w:rPr>
				        <w:noProof/>
				      </w:rPr>
				      <w:fldChar w:fldCharType="begin"/>
				      <w:instrText xml:space="preserve"> MERGEFIELD  kundenname  \* MERGEFORMAT  </w:instrText>
				      <w:fldChar w:fldCharType="separate"/>
				    </w:r>
				    <w:r>
				      <w:rPr>
				        <w:noProof/>
				      </w:rPr>
				      <w:t>«Kundenname»</w:t>
				    </w:r>
				    <w:r/>
				    <w:r>
				      <w:fldChar w:fldCharType="end"/>
				    </w:r>
				  </w:p>
			 */
		}
		
		// Remove
		for (FieldRef fr : fieldRefs) {
			
			if ( fr.getFldName().equals("MERGEFIELD") ) {
				String instr = extractInstr(fr.getInstructions() );

				// eg <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse \* MERGEFORMAT </w:instrText>
				// or <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse</w:instrText>
				
				String tmp = instr.substring( instr.indexOf("MERGEFIELD") + 10);
				tmp = tmp.trim();
				String key  = tmp.indexOf(" ") >-1 ? tmp.substring(0, tmp.indexOf(" ")) : tmp ;
				log.info("Key: '" + key + "'");
						
				R r = new R();
				Text t = new Text();
				t.setValue(key);
				r.getContent().add(t);
				
				
				// Remove the field related runs
				int end = fr.getParent().getContent().indexOf(fr.getEndRun());
				int begin = fr.getParent().getContent().indexOf(fr.getBeginRun());
				for (int i = end; i>=begin; i--) {
					fr.getParent().getContent().remove(i);
				}
				
				
				fr.getParent().getContent().add(begin, r);
				
//				System.out.println(XmlUtils.marshaltoString(
//						fr.getParent(), true, true));
				
			}
		}
				
		return pkgOut;
	}
	
	private static String extractInstr(List<Object> instructions) {
		// For MERGEFIELD, expect the list to contain a simple string
		
		if (instructions.size()!=1) {
			log.error("TODO MERGEFIELD field contained complex instruction");
			return null;
		}
		
		Object o = XmlUtils.unwrap(instructions.get(0));
		if (o instanceof Text) {
			return ((Text)o).getValue();
		} else {
            if(log.isErrorEnabled()) {
                log.error("TODO: extract field name from " + o.getClass().getName());
                log.error(XmlUtils.marshaltoString(instructions.get(0), true, true));
            }
			return null;
		}
	}
	
//	public static boolean isMergeField(String type) {
//		
//		if (type.contains("MERGEFIELD")) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") 
									+ "/in.docx";

		String outputfilepath = System.getProperty("user.dir")
				+ "/out.docx";

		WordprocessingMLPackage pkgIn = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		FieldsRemoveMERGEFIELD migrator = new FieldsRemoveMERGEFIELD();
		WordprocessingMLPackage pkgOut = migrator.migrate(pkgIn);
		
		SaveToZipFile saver = new SaveToZipFile(pkgOut);
		saver.save(outputfilepath);
		
	}

}
