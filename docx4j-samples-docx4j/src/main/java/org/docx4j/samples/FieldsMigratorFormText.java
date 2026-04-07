package org.docx4j.samples;

import org.docx4j.model.datastorage.migration.FromFormText;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public class FieldsMigratorFormText {

	/**
	 * Migrate FORMTEXT fields to data bound content controls, using
	 * textInput/default as the field name.  The generated XML 
	 * part uses OpenDoPE Answer format.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		/*
	    <w:ffData>
	        <w:name w:val=""/>
	        <w:enabled/>
	        <w:calcOnExit w:val="false"/>
	        <w:textInput>
	            <w:default w:val="sellerFax"/> <-------- data field name
	        </w:textInput>
	    </w:ffData>
	 */
		
		String inputfilepath = System.getProperty("user.dir") + "/T.docx";

		String outputfilepath = System.getProperty("user.dir")
				+ "/OUT_TEST1.docx";

		WordprocessingMLPackage pkgIn = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		FromFormText migrator = new FromFormText();
		WordprocessingMLPackage pkgOut = migrator.migrate(pkgIn);
		
		SaveToZipFile saver = new SaveToZipFile(pkgOut);
		saver.save(outputfilepath);
		
	}	
}
