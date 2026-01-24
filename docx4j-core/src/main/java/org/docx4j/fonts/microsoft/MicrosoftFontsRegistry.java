package org.docx4j.fonts.microsoft;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.docx4j.Docx4jProperties;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrosoftFontsRegistry {

	protected static Logger log = LoggerFactory.getLogger(MicrosoftFontsRegistry.class);

	private static HashMap<String, MicrosoftFonts.Font> msFontsByName;
	public final static Map<String, MicrosoftFonts.Font> getMsFonts() {
		return msFontsByName;
	}		

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
		
		msFontsByName = new HashMap<>();
		
		java.lang.ClassLoader classLoader = MicrosoftFontsRegistry.class.getClassLoader();		
		JAXBContext msFontsContext = JAXBContext.newInstance("org.docx4j.fonts.microsoft", classLoader);
		
		Unmarshaller u = msFontsContext.createUnmarshaller();		
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		org.docx4j.fonts.microsoft.MicrosoftFonts msFonts = null;
		
		// Mechanism to allow the user to replace the contents of MicrosoftFonts.xml entirely
		 try (InputStream is = ResourceUtils.getResourceViaProperty("docx4j.fonts.microsoft.MicrosoftFonts"  , "org/docx4j/fonts/microsoft/MicrosoftFonts.xml")) {
			 msFonts = (org.docx4j.fonts.microsoft.MicrosoftFonts)u.unmarshal( is );
		 } // throws
		
		List<MicrosoftFonts.Font> msFontsList = msFonts.getFont();		
		for (MicrosoftFonts.Font font : msFontsList ) {			
			msFontsByName.put( (font.getName()), font); 
		}
		
		// Mechanism to allow the user to supplement the standard MicrosoftFonts.xml
		String supplemental = Docx4jProperties.getProperty("docx4j.fonts.microsoft.MicrosoftFonts.supplemental");
		if (supplemental==null) return;
		
		try (InputStream is = ResourceUtils.getResource(supplemental)) {
			msFonts = (org.docx4j.fonts.microsoft.MicrosoftFonts)u.unmarshal( is );
		 } catch (Exception e) {
			// Log warning instead of throwing, so standard fonts still work
			 log.warn("Problem with " +  supplemental, e);
			 return;
		 }
		msFontsList = msFonts.getFont();		
		for (MicrosoftFonts.Font font : msFontsList ) {			
			msFontsByName.put( (font.getName()), font); 
		}
		 log.info(msFontsList.size() + " supplemental fonts registered");
		
	}
	
	
	
	
	
}





