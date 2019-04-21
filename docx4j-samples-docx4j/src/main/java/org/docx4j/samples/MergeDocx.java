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
 * be used to merge docx documents. 
 * 
 * The MergeDocx utility is a paid extension to docx4j.
 * Purchases of this extension support the docx4j project.
 * @see <a href="http://www.docx4java.org/blog/2010/11/merging-word-documents/">
 * merging-word-documents blog post</a> for more info, or 
 * @see <a href="http://www.plutext.com/">www.plutext.com</a>
 * or email sales@plutext.com if you want to buy it.
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
public class MergeDocx extends AbstractSample {

	final static String BASE_DIR = System.getProperty("user.dir") + "/sample-docs/";

	final static String[] sourceDocxNames = { "tables.docx", "Images.docx"};  

	static boolean save = true;
	static String outputfilepath = System.getProperty("user.dir") +"OUT_MergeDocx.docx";		
	
	/**
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		
		// Create list of docx packages to merge
		List<WordprocessingMLPackage> wmlPkgList=new ArrayList<WordprocessingMLPackage>();
		for (int i=0; i<sourceDocxNames.length; i++){
			String filename = BASE_DIR + sourceDocxNames[i] ;
			System.out.println("Loading " + filename); 
			wmlPkgList.add(WordprocessingMLPackage
					.load(new java.io.File(filename)));
		}
		
		try {
			// Use reflection, so docx4j can be built
			// by users who don't have the MergeDocx utility
			Class<?> documentBuilder = Class.forName("com.plutext.merge.DocumentBuilder");			
			//Method method = documentBuilder.getMethod("merge", wmlPkgList.getClass());			
			Method[] methods = documentBuilder.getMethods(); 
			Method method = null;
			for (int j=0; j<methods.length; j++) {
				System.out.println(methods[j].getName());
				if (methods[j].getName().equals("merge")) {
					method = methods[j];
					break;
				}
			}			
			if (method==null) throw new NoSuchMethodException();
			
			WordprocessingMLPackage resultPkg = (WordprocessingMLPackage)method.invoke(null, wmlPkgList);

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
		System.out.println("* which is necessary to merge docx, or process altChunks (of type docx).");
		System.out.println("* Purchases of this extension support the docx4j project.");
		System.out.println("* Please visit www.plutext.com if you want to buy it.");
	}

}
