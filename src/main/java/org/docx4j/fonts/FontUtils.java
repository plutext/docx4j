package org.docx4j.fonts;

public class FontUtils {
	
	public final static java.lang.CharSequence target = (new String("%20")).subSequence(0, 3);
	public final static java.lang.CharSequence replacement = (new String(" ")).subSequence(0, 1);
	
	public static String pathFromURL(String path) {
		
		if (path.startsWith("file:/")) {
			if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
				path = path.substring(6);
			} else {
				path = path.substring(5);
			}
		}
		
		// Convert %20 to spaces
		if (path.indexOf("%20")>-1) {
					               
               path = path.toString().replace(target, replacement);					
		}
		
		return path;
		
	}

}
