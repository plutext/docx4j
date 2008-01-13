package org.docx4j.utils;

public class ResourceUtils {
	
    public static java.io.InputStream getResource(String filename) throws java.io.IOException
    {
        // Try to load resource from jar
        java.net.URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        
        
        if (url == null ) {
        	System.out.println("Couldn't get resource!!");
        }
        
        // Get the jar file
//        JarURLConnection conn = (JarURLConnection) url.openConnection();
        java.io.InputStream is = url.openConnection().getInputStream();
        return is;
    }
	

}
