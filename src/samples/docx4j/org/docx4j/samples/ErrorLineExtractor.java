package org.docx4j.samples;

import java.io.FileInputStream;
import java.util.Scanner;

/**
 * Where Word reports an error in a very large file,
 * XML-aware tools such as Visual Studio might have trouble
 * opening it.  This simple class will print out specified
 * lines of document.xml.
 * 
 * Note: this is really only useful if the file was
 * pretty printed at the time Word reported the error
 * (since otherwise the error will typically be somewhere
 *  on a very long line 1). 
 * 
 * @author jharrop
 *
 */
public class ErrorLineExtractor {

	public static void main(String[] args) throws Exception {

	   String path = System.getProperty("user.dir") + "/document.xml";

	   long count = 0;
	   
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {
		    inputStream = new FileInputStream(path);
		    sc = new Scanner(inputStream, "UTF-8");
		    while (sc.hasNextLine()) {
		        String line = sc.nextLine();
		        // System.out.println(line);
		        count++;
		        
		        if (count>1714250) {
		        	System.out.println(count + ": " + line);
		        }
		        if (count>1714400) {
		        	return;
		        }
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}		
		
	}
}
