package org.docx4j.samples;

import javax.xml.bind.JAXBElement;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.ClassFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.math.CTOMathPara;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;


/**
 * Basic demo handling Office Math.
 *
 */
public class MathsEquationsFormulae {
    
	public static void main(String[] args) throws Exception {
		
		// First, create/save a docx containing a formula.
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();		
		P p = new P();
		mdp.getContent().add(p);
		javax.xml.bind.JAXBElement omathpara = (JAXBElement) XmlUtils.unmarshalString(openXML); // or could have used generated code which uses JAXB factory approach
		p.getContent().add(omathpara);
		String filename = System.getProperty("user.dir") + "/OUT_maths.docx";
		Docx4J.save(wordMLPackage, new java.io.File(filename), Docx4J.FLAG_SAVE_ZIP_FILE); 
		System.out.println("Saved " + filename);
		
		// The above shows how to unmarshall it
		
		// Now, show how to marshall existing maths content
		// first, find it
		ClassFinder finder = new ClassFinder(CTOMathPara.class);
		new TraversalUtil(mdp.getContent(), finder);
		// Get the first result
		Object o = finder.results.get(0);
		System.out.println(o.getClass().getName());
		// Can't use XmlUtils.marshaltoString(o) because
		// CTOMathPara is missing an @XmlRootElement annotation, 
		// so be explicit 
		String result = XmlUtils.marshaltoString( o, true, true,
				Context.jc,
				"http://schemas.openxmlformats.org/officeDocument/2006/math", "oMathPara", org.docx4j.math.CTOMathPara.class);
		System.out.println(result);
						
	}

	// generated using docx4j code gen at http://webapp.docx4java.org/OnlineDemo/PartsList.html or the Word AddIn
	private static String openXML = "<m:oMathPara xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\">"
            + "<m:oMath>"
                + "<m:sSup>"
                    + "<m:sSupPr>"
                        + "<m:ctrlPr>"
                            + "<w:rPr>"
                                + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                            +"</w:rPr>"
                        +"</m:ctrlPr>"
                    +"</m:sSupPr>"
                    + "<m:e>"
                        + "<m:d>"
                            + "<m:dPr>"
                                + "<m:ctrlPr>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                +"</m:ctrlPr>"
                            +"</m:dPr>"
                            + "<m:e>"
                                + "<m:r>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                    + "<m:t>x+a</m:t>"
                                +"</m:r>"
                            +"</m:e>"
                        +"</m:d>"
                    +"</m:e>"
                    + "<m:sup>"
                        + "<m:r>"
                            + "<w:rPr>"
                                + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                            +"</w:rPr>"
                            + "<m:t>n</m:t>"
                        +"</m:r>"
                    +"</m:sup>"
                +"</m:sSup>"
                + "<m:r>"
                    + "<w:rPr>"
                        + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                    +"</w:rPr>"
                    + "<m:t>=</m:t>"
                +"</m:r>"
                + "<m:nary>"
                    + "<m:naryPr>"
                        + "<m:chr m:val=\"∑\"/>"
                        + "<m:grow m:val=\"1\"/>"
                        + "<m:ctrlPr>"
                            + "<w:rPr>"
                                + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                            +"</w:rPr>"
                        +"</m:ctrlPr>"
                    +"</m:naryPr>"
                    + "<m:sub>"
                        + "<m:r>"
                            + "<w:rPr>"
                                + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                            +"</w:rPr>"
                            + "<m:t>k=0</m:t>"
                        +"</m:r>"
                    +"</m:sub>"
                    + "<m:sup>"
                        + "<m:r>"
                            + "<w:rPr>"
                                + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                            +"</w:rPr>"
                            + "<m:t>n</m:t>"
                        +"</m:r>"
                    +"</m:sup>"
                    + "<m:e>"
                        + "<m:d>"
                            + "<m:dPr>"
                                + "<m:ctrlPr>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                +"</m:ctrlPr>"
                            +"</m:dPr>"
                            + "<m:e>"
                                + "<m:f>"
                                    + "<m:fPr>"
                                        + "<m:type m:val=\"noBar\"/>"
                                        + "<m:ctrlPr>"
                                            + "<w:rPr>"
                                                + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                            +"</w:rPr>"
                                        +"</m:ctrlPr>"
                                    +"</m:fPr>"
                                    + "<m:num>"
                                        + "<m:r>"
                                            + "<w:rPr>"
                                                + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                            +"</w:rPr>"
                                            + "<m:t>n</m:t>"
                                        +"</m:r>"
                                    +"</m:num>"
                                    + "<m:den>"
                                        + "<m:r>"
                                            + "<w:rPr>"
                                                + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                            +"</w:rPr>"
                                            + "<m:t>k</m:t>"
                                        +"</m:r>"
                                    +"</m:den>"
                                +"</m:f>"
                            +"</m:e>"
                        +"</m:d>"
                        + "<m:sSup>"
                            + "<m:sSupPr>"
                                + "<m:ctrlPr>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                +"</m:ctrlPr>"
                            +"</m:sSupPr>"
                            + "<m:e>"
                                + "<m:r>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                    + "<m:t>x</m:t>"
                                +"</m:r>"
                            +"</m:e>"
                            + "<m:sup>"
                                + "<m:r>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                    + "<m:t>k</m:t>"
                                +"</m:r>"
                            +"</m:sup>"
                        +"</m:sSup>"
                        + "<m:sSup>"
                            + "<m:sSupPr>"
                                + "<m:ctrlPr>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                +"</m:ctrlPr>"
                            +"</m:sSupPr>"
                            + "<m:e>"
                                + "<m:r>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                    + "<m:t>a</m:t>"
                                +"</m:r>"
                            +"</m:e>"
                            + "<m:sup>"
                                + "<m:r>"
                                    + "<w:rPr>"
                                        + "<w:rFonts w:ascii=\"Cambria Math\" w:cs=\"Cambria Math\" w:eastAsia=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/>"
                                    +"</w:rPr>"
                                    + "<m:t>n-k</m:t>"
                                +"</m:r>"
                            +"</m:sup>"
                        +"</m:sSup>"
                    +"</m:e>"
                +"</m:nary>"
            +"</m:oMath>"
        +"</m:oMathPara>";
}
