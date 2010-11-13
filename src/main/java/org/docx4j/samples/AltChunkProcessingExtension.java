package org.docx4j.samples;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * This sample demonstrates how the MergeDocx utility can
 * be used to convert a w:altChunk to normal docx
 * content. 
 * 
 * The MergeDocx utility is a paid extension to docx4j.
 * Purchases of this extension support the docx4j project.
 * Please visit www.plutext.com if you want to buy it.
 * 
 * To run the utility, you simply pass it a list of the 
 * docx you want to merge; it returns a new pkg containing
 * the merged documents. 
 * 
 * This example looks a little more complex, since it
 * has to use reflection, so that docx4j can still be
 * built by users who don't have the MergeDocx code.
 *
 */
public class AltChunkProcessingExtension extends AbstractSample {

	final static String BASE_DIR = System.getProperty("user.dir") + "/sample-docs/";

	static String inputfilepath = BASE_DIR+"altChunk_docx.docx";  

	static boolean save = true;
	static String outputfilepath = BASE_DIR+"altChunk_docx_OUT.docx";		
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		
		// Create list of docx packages to merge
		WordprocessingMLPackage srcPackage = WordprocessingMLPackage
					.load(new java.io.File(inputfilepath));
		
		try {
			// Use reflection, so docx4j can be built
			// by users who don't have the MergeDocx utility
			Class<?> documentBuilder = Class.forName("com.plutext.merge.ProcessAltChunk");			
			//Method method = documentBuilder.getMethod("merge", wmlPkgList.getClass());			
			Method[] methods = documentBuilder.getMethods(); 
			Method method = null;
			for (int j=0; j<methods.length; j++) {
				System.out.println(methods[j].getName());
				if (methods[j].getName().equals("process")) {
					method = methods[j];
					break;
				}
			}			
			if (method==null) throw new NoSuchMethodException();
			
			WordprocessingMLPackage resultPkg = (WordprocessingMLPackage)method.invoke(null, srcPackage);

			if (save) {		
				SaveToZipFile saver = new SaveToZipFile(resultPkg);
				saver.save(outputfilepath);
				System.out.println("Generated " + outputfilepath);
			} else {
				String result = XmlUtils.marshaltoString(resultPkg.getMainDocumentPart().getJaxbElement(), true, true);
				System.out.println(result);				
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			extensionMissing(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			extensionMissing(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 

	}
	
	public static void extensionMissing(Exception e) {
		System.out.println("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		System.out.println("* You don't appear to have the MergeDocx paid extension,");
		System.out.println("* which is necessary to merge docx, or process altChunk.");
		System.out.println("* Purchases of this extension support the docx4j project.");
		System.out.println("* Please visit www.plutext.com if you want to buy it.");
	}

}

