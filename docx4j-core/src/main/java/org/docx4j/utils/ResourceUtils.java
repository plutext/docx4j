/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.utils;

import java.io.IOException;
import java.io.InputStream;

import org.docx4j.Docx4jProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {
	
	protected static Logger log = LoggerFactory.getLogger(ResourceUtils.class);	

    /**
     * Get this resource from the location specified in docx4j.properties;
     * if none is specified, fallback to the default specified
     * 
     * @param propName
     * @param defaultPath
     * @return
     * @throws java.io.IOException
     * @since 3.2.0
     */
    public static java.io.InputStream getResourceViaProperty(String propName, String defaultPath) throws java.io.IOException
    {
    	String resourcePath= Docx4jProperties.getProperty(propName, defaultPath);
    	log.debug(propName + " resolved to " + resourcePath);
    	InputStream resourceIS = null;
    	try {
    		resourceIS = getResource(resourcePath);
    	} catch (IOException ioe) {
    		log.warn(resourcePath + ": " + ioe.getMessage());
    	}
    	if (resourceIS==null) {
    		log.warn("Property " + propName + " resolved to missing resource " + resourcePath + "; using " +  defaultPath);
    		return getResource(defaultPath);
    	} else {
    		return resourceIS;
    	}
    }
    
    /**
     * Use ClassLoader.getResource to get the named resource
     * @param filename
     * @return
     * @throws java.io.IOException if resource not found
     */
    public static java.io.InputStream getResource(String filename) throws java.io.IOException
    {
    	log.debug("Attempting to load: " + filename);
    	
        // Try to load resource from jar.  
        ClassLoader loader = ResourceUtils.class.getClassLoader();
        
        java.net.URL url = loader.getResource(filename);
    	// For Java 9, the package must be open in module-info!  
        // See https://stackoverflow.com/questions/45166757/loading-classes-and-resources-in-java-9/45173837#45173837  
        
		if (url == null
				&& System.getProperty("java.vendor").contains("Android")) {
			url = loader.getResource("assets/" + filename);
			if (url!=null) {
				if (log.isDebugEnabled() ) {
					log.debug("found " + filename + " in assets");
				}
			}
		}

        if (url == null) {
        	// this is convenient when trying to load a resource from an arbitrary path,
        	// since in IKVM you can setContextClassLoader to a URLClassLoader,
        	// which in turn can be configured at run time to search some dir.
        	log.debug("Trying Thread.currentThread().getContextClassLoader()");
        	loader = Thread.currentThread().getContextClassLoader();
        	url = loader.getResource(filename);
        }
        
        if (url == null) {
        	log.warn("Couldn't get resource: " + filename);
        	throw new IOException(filename + " not found via classloader.");
        }
        
        // Get the jar file
//      JarURLConnection conn = (JarURLConnection) url.openConnection();
        java.io.InputStream is = url.openConnection().getInputStream();
        return is;
    }
	
//    public static void main(String[] args)
//            throws Exception {
//
//    	InputStream is = getResource("org/docx4j/convert/out/html/docx2xhtml.xslt");
//    	System.out.println(is.available());
//    	
//    }

}
