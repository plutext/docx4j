package org.docx4j.samples;

import java.io.File;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;

public class Watermark
{

    public static void main(String[] args) throws Exception
    {

    	Watermark sample = new Watermark();
        sample.addWaterMark();
    }

    static ObjectFactory factory = Context.getWmlObjectFactory();
    
    public void addWaterMark() throws Exception
    {

        WordprocessingMLPackage wmlPackage = WordprocessingMLPackage
                .createPackage();

		Relationship relationship = createHeaderPart(wmlPackage);
		createHeaderReference(wmlPackage, relationship);
        File f = new File(System.getProperty("user.dir") + "/waterMarksample.docx");
        wmlPackage.save(f);

    }
    
	public static Relationship createHeaderPart(
			WordprocessingMLPackage wordprocessingMLPackage)
			throws Exception {
		
		HeaderPart headerPart = new HeaderPart();
		Relationship rel =  wordprocessingMLPackage.getMainDocumentPart()
				.addTargetPart(headerPart);
		
		headerPart.setJaxbElement(getHdr(wordprocessingMLPackage, headerPart));

		return rel;
	}

	public static void createHeaderReference(
			WordprocessingMLPackage wordprocessingMLPackage,
			Relationship relationship )
			throws InvalidFormatException {

		List<SectionWrapper> sections = wordprocessingMLPackage.getDocumentModel().getSections();
		   
		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr==null ) {
			sectPr = factory.createSectPr();
			wordprocessingMLPackage.getMainDocumentPart().addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}

		HeaderReference headerReference = factory.createHeaderReference();
		headerReference.setId(relationship.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);

	}
	
	public static Hdr getHdr(WordprocessingMLPackage wordprocessingMLPackage,
			Part sourcePart) throws Exception {

		Hdr hdr = factory.createHdr();
		
		String openXML = "<w:p xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\">"
                + "<w:pPr>"
                      + "<w:pStyle w:val=\"Header\"/>"

                +"</w:pPr>" 
                + "<w:sdt>" 
                +   "<w:sdtPr>"
                      + "<w:id w:val=\"-1589924921\"/>"

                      + "<w:lock w:val=\"sdtContentLocked\"/>"

                      + "<w:docPartObj>"
                            + "<w:docPartGallery w:val=\"Watermarks\"/>"

                            + "<w:docPartUnique/>"

                      +"</w:docPartObj>"

                +"</w:sdtPr>"

                + "<w:sdtEndPr/>"

                + "<w:sdtContent>"
                      + "<w:r>"
                            + "<w:rPr>"
                                  + "<w:noProof/>"

                                  + "<w:lang w:eastAsia=\"zh-TW\"/>"

                            +"</w:rPr>"

                            + "<w:pict>"
                                  + "<v:shapetype adj=\"10800\" coordsize=\"21600,21600\" id=\"_x0000_t136\" o:spt=\"136\" path=\"m@7,l@8,m@5,21600l@6,21600e\">"
                                        + "<v:formulas>"
                                              + "<v:f eqn=\"sum #0 0 10800\"/>"

                                              + "<v:f eqn=\"prod #0 2 1\"/>"

                                              + "<v:f eqn=\"sum 21600 0 @1\"/>"

                                              + "<v:f eqn=\"sum 0 0 @2\"/>"

                                              + "<v:f eqn=\"sum 21600 0 @3\"/>"

                                              + "<v:f eqn=\"if @0 @3 0\"/>"

                                              + "<v:f eqn=\"if @0 21600 @1\"/>"

                                              + "<v:f eqn=\"if @0 0 @2\"/>"

                                              + "<v:f eqn=\"if @0 @4 21600\"/>"

                                              + "<v:f eqn=\"mid @5 @6\"/>"

                                              + "<v:f eqn=\"mid @8 @5\"/>"

                                              + "<v:f eqn=\"mid @7 @8\"/>"

                                              + "<v:f eqn=\"mid @6 @7\"/>"

                                              + "<v:f eqn=\"sum @6 0 @5\"/>"

                                        +"</v:formulas>"

                                        + "<v:path o:connectangles=\"270,180,90,0\" o:connectlocs=\"@9,0;@10,10800;@11,21600;@12,10800\" o:connecttype=\"custom\" textpathok=\"t\"/>"

                                        + "<v:textpath fitshape=\"t\" on=\"t\"/>"

                                        + "<v:handles>"
                                              + "<v:h position=\"#0,bottomRight\" xrange=\"6629,14971\"/>"

                                        +"</v:handles>"

                                        + "<o:lock shapetype=\"t\" text=\"t\" v:ext=\"edit\"/>"

                                  +"</v:shapetype>"

                                  + "<v:shape fillcolor=\"silver\" id=\"PowerPlusWaterMarkObject357476642\" o:allowincell=\"f\" o:spid=\"_x0000_s2049\" stroked=\"f\" style=\"position:absolute;margin-left:0;margin-top:0;width:527.85pt;height:131.95pt;rotation:315;z-index:-251658752;mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;mso-position-vertical-relative:margin\" type=\"#_x0000_t136\">"
                                        + "<v:fill opacity=\".5\"/>"

                                        + "<v:textpath string=\"MY WATERMARK\" style=\"font-family:&quot;Calibri&quot;;font-size:1pt\"/>"

                                        + "<w10:wrap anchorx=\"margin\" anchory=\"margin\"/>"

                                  +"</v:shape>"

                            +"</w:pict>"

                      +"</w:r>"

                +"</w:sdtContent>"

          +"</w:sdt>"
          + "</w:p>";
			
		P p = (P)XmlUtils.unmarshalString(openXML);			

	        hdr.getContent().add(p);
			return hdr;

		}
	
    
}
