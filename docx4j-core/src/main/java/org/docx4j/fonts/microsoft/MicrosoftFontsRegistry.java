package org.docx4j.fonts.microsoft;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrosoftFontsRegistry {

	protected static Logger log = LoggerFactory.getLogger(MicrosoftFontsRegistry.class);

	private static HashMap<String, MicrosoftFonts.Font> msFontsByName;
	public final static Map<String, MicrosoftFonts.Font> getMsFonts() {
		return msFontsByName;
	}		

//	private static HashMap<String, String> filenamesToMsFontNames;

	static {
		
		try {
			
			setupMicrosoftFontsRegistry();
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	
	/**
	 * Get Microsoft fonts; this is used by PhysicalFonts.getBoldForm etc, 
	 * and also in docx4all - all platforms - to populate font dropdown list */	
	private final static void setupMicrosoftFontsRegistry() throws Exception {

		
		msFontsByName = new HashMap<String, MicrosoftFonts.Font>();
//		filenamesToMsFontNames = new HashMap<String, String>();
		
		java.lang.ClassLoader classLoader = MicrosoftFontsRegistry.class.getClassLoader();		
		JAXBContext msFontsContext = JAXBContext.newInstance("org.docx4j.fonts.microsoft", classLoader);
		
		Unmarshaller u = msFontsContext.createUnmarshaller();		
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		InputStream is = ResourceUtils.getResourceViaProperty("docx4j.fonts.microsoft.MicrosoftFonts"  , "org/docx4j/fonts/microsoft/MicrosoftFonts.xml");
		
		org.docx4j.fonts.microsoft.MicrosoftFonts msFonts = (org.docx4j.fonts.microsoft.MicrosoftFonts)u.unmarshal( is );
		
		List<MicrosoftFonts.Font> msFontsList = msFonts.getFont();
		
		for (MicrosoftFonts.Font font : msFontsList ) {			
			msFontsByName.put( (font.getName()), font); // 20080318 - normalised
			//log.debug( "put msFontsFilenames: " + normalise(font.getName()) );
			
//			filenamesToMsFontNames.put( font.getFilename().toLowerCase() , font.getName());
////			log.debug( "put msFontsFilenames: " + font.getName() );
//			
//			if (font.getBold()!=null) {
//				filenamesToMsFontNames.put( font.getBold().getFilename().toLowerCase(), font.getName()+SEPARATOR+Mapper.BOLD);
////				log.debug( "put bold: " +  font.getName()+SEPARATOR+Substituter.BOLD );				
//			}
//			if (font.getItalic()!=null) {
//				filenamesToMsFontNames.put( font.getItalic().getFilename().toLowerCase(), font.getName()+SEPARATOR+Mapper.ITALIC);
////				log.debug( "put italic: " + font.getName()+SEPARATOR+Substituter.ITALIC );				
//			}
//			if (font.getBolditalic() !=null) {
//				filenamesToMsFontNames.put( font.getBolditalic().getFilename().toLowerCase(), font.getName()+SEPARATOR+Mapper.BOLD_ITALIC);
////				log.debug( "put bold italic: " + font.getName()+SEPARATOR+Substituter.BOLD_ITALIC );				
//			}
			
		}
		
	}
	
	
	
	
	
}





