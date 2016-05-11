package org.docx4j.anon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageTiffPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Anonymize {
	
	private static Logger log = LoggerFactory.getLogger(Anonymize.class);
	
	
	public Anonymize(WordprocessingMLPackage wordMLPackage) {
		
		this.pkg = wordMLPackage;
		
		result = new AnonymizeResult();
	}
	
	private WordprocessingMLPackage pkg;
	
	ScrambleText latinizer = null;   
	DmlVmlAnalyzer dmlVmlAnalyzer = null;
	
	AnonymizeResult result;
		
	// We'll replace images with 2x2 pixels	
    private static byte[] PNG_IMAGE_DATA;
    private static byte[] GIF_IMAGE_DATA;
    private static byte[] JPEG_IMAGE_DATA;
    private static byte[] BMP_IMAGE_DATA;
    private static byte[] TIF_IMAGE_DATA;
    
    static {
    	
    	PNG_IMAGE_DATA = Base64.decodeBase64("iVBORw0KGgoAAAANSUhEUgAAAAIAAAACAgMAAAAP2OW3AAAADFBMVEUDAP//AAAA/wb//AAD4Tw1AAAACXBIWXMAAAsTAAALEwEAmpwYAAAADElEQVQI12NwYNgAAAF0APHJnpmVAAAAAElFTkSuQmCC");
    	GIF_IMAGE_DATA = Base64.decodeBase64("R0lGODdhAgACAKEEAAMA//8AAAD/Bv/8ACwAAAAAAgACAAACAww0BQA7");
        JPEG_IMAGE_DATA = Base64.decodeBase64(
        						"/9j/4AAQSkZJRgABAQEASABIAAD/4QCMRXhpZgAATU0AKgAAAAgABwEaAAUAAAABAAAAYgEbAAUA"
        						+"AAABAAAAagEoAAMAAAABAAIAAAExAAIAAAASAAAAclEQAAEAAAABAQAAAFERAAQAAAABAAALE1ES"
								+"AAQAAAABAAALEwAAAAAAARlIAAAD6AABGUgAAAPoUGFpbnQuTkVUIHYzLjUuMTAA/9sAQwACAQEC"
								+"AQECAgICAgICAgMFAwMDAwMGBAQDBQcGBwcHBgcHCAkLCQgICggHBwoNCgoLDAwMDAcJDg8NDA4L"
								+"DAwM/9sAQwECAgIDAwMGAwMGDAgHCAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwM"
								+"DAwMDAwMDAwMDAwMDAwM/8AAEQgAAgACAwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAAB"
								+"AgMEBQYHCAkKC//EALUQAAIBAwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNC"
								+"scEVUtHwJDNicoIJChYXGBkaJSYnKCkqNDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0"
								+"dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY"
								+"2drh4uPk5ebn6Onq8fLz9PX29/j5+v/EAB8BAAMBAQEBAQEBAQEAAAAAAAABAgMEBQYHCAkKC//E"
								+"ALURAAIBAgQEAwQHBQQEAAECdwABAgMRBAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoW"
								+"JDThJfEXGBkaJicoKSo1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWG"
								+"h4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm5+jp"
								+"6vLz9PX29/j5+v/aAAwDAQACEQMRAD8A/QL4W/sD/ArXfhl4dvr74K/CW8vbzS7ae4uJ/CGnySzy"
								+"NErM7sYiWYkkknkk0UUV5+U/7jR/wR/JHRP4mf/Z");
        BMP_IMAGE_DATA = Base64.decodeBase64("Qk1GAAAAAAAAADYAAAAoAAAAAgAAAAIAAAABABgAAAAAAAAAAAATCwAAEwsAAAAAAAAAAAAABv8AAPz///8AAP//AAMD/w==");
        TIF_IMAGE_DATA = Base64.decodeBase64("");
    }

	
    
	/*
	 * 
	 * TODO: Info leakage via external parts eg hyperlinks
	 * 
	 */
	public AnonymizeResult go() throws Docx4JException {
		
		
		filterMDPRels();
		
		handleMetadata();
		
		result.unsafeParts = PartsAnalyzer.identifyUnsafeParts(pkg.getParts().getParts().entrySet());

		detectDmlVmlContent();  // doing this before scramble lets us analyze field types
		
		/* content stories:
		 * 
		 * - MDP
		 * - Header/Footer
		 * - Footnotes/Endnotes
		 * - Comments
		 * 
		 * replace with latin text */
		applyScrambleCallbackToParts();
		
		// Next, images
		handleImages();
		
		
		return result;
		
	}
	
	/**
	 * Remove customXml, glossaryDocument, and stylesWithEffects
	 */
	private void filterMDPRels() {
		
		if (pkg.getMainDocumentPart().getRelationshipsPart()==null) return;
		
		if (log.isDebugEnabled()) {
			for (Relationship r  : pkg.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship()) {
				System.out.println(r.getType());
			}
		}
		
		pkg.getMainDocumentPart().getRelationshipsPart().removeRelationshipsByType(
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml"); 

		pkg.getMainDocumentPart().getRelationshipsPart().removeRelationshipsByType(
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/glossaryDocument"); 

		pkg.getMainDocumentPart().getRelationshipsPart().removeRelationshipsByType(
				"http://schemas.microsoft.com/office/2007/relationships/stylesWithEffects"); 
		
	}

	/**
	 * @throws Docx4JException
	 */
	protected void handleMetadata() throws Docx4JException {
				
		// docProps/app.xml (Extended Properties) 
		if (pkg.getDocPropsExtendedPart()!=null
				&& pkg.getDocPropsExtendedPart().getContents()!=null) {
			
			pkg.getDocPropsExtendedPart().getContents().setCompany(null);
			pkg.getDocPropsExtendedPart().getContents().setManager(null);
			pkg.getDocPropsExtendedPart().getContents().setHeadingPairs(null);
		}
		
		if (pkg.getDocPropsCorePart()!=null
				&& pkg.getDocPropsCorePart().getContents()!=null) {
			
			pkg.getDocPropsCorePart().getContents().setCategory(null);
			pkg.getDocPropsCorePart().getContents().setCreator(null);
			pkg.getDocPropsCorePart().getContents().setDescription(null);
			pkg.getDocPropsCorePart().getContents().setIdentifier(null);
			pkg.getDocPropsCorePart().getContents().setKeywords(null);
			pkg.getDocPropsCorePart().getContents().setSubject(null);
			pkg.getDocPropsCorePart().getContents().setTitle(null);
		}

		if (pkg.getDocPropsCustomPart()!=null) {
			// Remove this
			pkg.getRelationshipsPart().removePart(pkg.getDocPropsCustomPart().getPartName());
		}
		
		// /docProps/thumbnail.emf, always delete this!
		pkg.getRelationshipsPart().removePart(new PartName("/docProps/thumbnail.emf"));

	}
	
    /**
     * This method replaces images with 2x2 pixels (which Word scales appropriately)
     * 
     * @throws InvalidFormatException
     */
    private void handleImages() 
    		throws InvalidFormatException {
        
	    // Apply map to headers/footers
		for (Entry<PartName, Part> entry : pkg.getParts().getParts().entrySet()) {

			Part p = entry.getValue(); 

			if (p instanceof ImagePngPart
					|| p instanceof ImageGifPart
					|| p instanceof ImageJpegPart
					|| p instanceof ImageBmpPart
					|| p instanceof ImageTiffPart	
					// Others treated as unsafe
					) {
				
				((BinaryPart)p).setBinaryData(PNG_IMAGE_DATA);
				
			} 
			
		}
    }	
    
    private void detectDmlVmlContent() 
    		throws InvalidFormatException {

    	dmlVmlAnalyzer = new DmlVmlAnalyzer();
    	
	    // Apply map to MDP                
    	detectDmlVml( pkg.getMainDocumentPart() );        							
        
	    // Apply map to headers/footers
		for (Entry<PartName, Part> entry : pkg.getParts().getParts().entrySet()) {

			Part p = entry.getValue(); 

			if (p instanceof HeaderPart) {
				detectDmlVml( (HeaderPart)p );        							
			}

			if (p instanceof FooterPart) {
				detectDmlVml( (FooterPart)p );        							
			}
			
		}
        
	    // endnotes/footnotes
		if (pkg.getMainDocumentPart().getFootnotesPart()!=null) {
			detectDmlVml( pkg.getMainDocumentPart().getFootnotesPart() );
		}
		if (pkg.getMainDocumentPart().getEndNotesPart()!=null) {
			detectDmlVml( pkg.getMainDocumentPart().getEndNotesPart() );
		}
		
		
        // Comments
		if (pkg.getMainDocumentPart().getCommentsPart()!=null) {
			detectDmlVml( pkg.getMainDocumentPart().getCommentsPart() );
		}
   		
		return;

    }  	
    
	public void detectDmlVml(JaxbXmlPart p) {
		
		log.info("\n\n Inspecting " + p.getPartName().getName());
		
		dmlVmlAnalyzer.reinit();
		dmlVmlAnalyzer.setPart(p);
		new TraversalUtil(p.getJaxbElement(), dmlVmlAnalyzer);
		
		result.unsafeObjectsByPart.put(p, dmlVmlAnalyzer.unsafeObjects);
		if (dmlVmlAnalyzer.unsafeObjects.size()>0){
			result.anyUnsafeObjects = true;
		}
		result.inventoryObjectsByPart.put(p, dmlVmlAnalyzer.inventoryObjects);
		
		if (!result.containsVML) {
			result.containsVML = dmlVmlAnalyzer.containsVML;
		}
		
		result.fieldsPresent = this.dmlVmlAnalyzer.fieldsPresent;
		
	}
    
    
	
    private void applyScrambleCallbackToParts() 
    		throws InvalidFormatException {
    	
    	try {

			latinizer = new ScrambleText(pkg);
	    	
	    	
		    // Apply map to MDP                
			scramble( pkg.getMainDocumentPart() );        							
	        
		    // Apply map to headers/footers
			for (Entry<PartName, Part> entry : pkg.getParts().getParts().entrySet()) {
	
				Part p = entry.getValue(); 
	
				if (p instanceof HeaderPart) {
		    		scramble( (HeaderPart)p );        							
				}
	
				if (p instanceof FooterPart) {
		    		scramble( (FooterPart)p );        							
				}
				
			}
	        
		    // endnotes/footnotes
			if (pkg.getMainDocumentPart().getFootnotesPart()!=null) {
				scramble( pkg.getMainDocumentPart().getFootnotesPart() );
			}
			if (pkg.getMainDocumentPart().getEndNotesPart()!=null) {
				scramble( pkg.getMainDocumentPart().getEndNotesPart() );
			}
			
			
	        // Comments
			if (pkg.getMainDocumentPart().getCommentsPart()!=null) {
				scramble( pkg.getMainDocumentPart().getCommentsPart() );
			}
	
			// collected at the package level
//			result.mostPopularLang = latinizer.langFromLangStats();
			
			result.hasGreek = latinizer.hasGreek;
			result.hasCyrillic = latinizer.hasCyrillic;
			result.hasHebrew = latinizer.hasHebrew;
			result.hasArabic = latinizer.hasArabic;
			result.hasHiragana = latinizer.hasHiragana;
			result.hasKatakana = latinizer.hasKatakana;
			result.hasCJK = latinizer.hasCJK;

			
			return;
		
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new InvalidFormatException(e.getMessage(), e);
    	}

    }  	

	public void scramble(JaxbXmlPart p) {
		
		log.info("\n\n Scrambling " + p.getPartName().getName());
		
		new TraversalUtil(p.getJaxbElement(), latinizer);
		
		
	}
    
	
	

	
}
