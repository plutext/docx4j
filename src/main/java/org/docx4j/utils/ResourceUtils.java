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

import org.apache.log4j.Logger;

public class ResourceUtils {
	
	protected static Logger log = Logger.getLogger(ResourceUtils.class);	
	
    public static java.io.InputStream getResource(String filename) throws java.io.IOException
    {
        // Try to load resource from jar.
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {  // IKVM (v.0.44.0.5) doesn't set context classloader 
            loader = ResourceUtils.class.getClassLoader();
        }
        
        java.net.URL url = loader.getResource(filename);
                
        if (url == null) {
        	if (filename.contains("jaxb.properties")){
        		log.info("Not using MOXy, since no resource: " + filename);        		
        	} else {
        		log.warn("Couldn't get resource: " + filename);
        	}
        }
        
        // Get the jar file
//      JarURLConnection conn = (JarURLConnection) url.openConnection();
        java.io.InputStream is = url.openConnection().getInputStream();
        return is;
    }
	

}
