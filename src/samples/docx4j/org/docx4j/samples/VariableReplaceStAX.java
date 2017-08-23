/*
 *  Copyright 2016, Plutext Pty Ltd.
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

package org.docx4j.samples;


import java.io.File;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.StAXHandlerAbstract;
import org.docx4j.openpackaging.parts.StAXHandlerInterface;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


/**
 * This is an example of using StAX rather than JAXB for
 * processing.  StAX is useful for very large files, since
 * it uses less memory than JAXB.  There are also some
 * things StAX is better adapted for.
 * 
 * NB: There are at least 3 approaches for replacing variables in 
 * a docx.
 * 
 * 1. as shows in this example
 * 2. using Merge Fields (see org.docx4j.model.fields.merge.MailMerger)
 * 3. binding content controls to an XML Part (via XPath)
 * 
 * and this is not the recommended approach!
 * 
 * It is just a good example of using StAX.
 *
 */
public class VariableReplaceStAX {

	final static HashMap<String, String> mappings = new HashMap<String, String>();
	
	public static void main(String[] args) throws Exception {
		
		// Exclude context init from timing
		org.docx4j.wml.ObjectFactory foo = Context.getWmlObjectFactory();

		// Input docx has variables in it: ${colour}, ${icecream}
		String inputfilepath = System.getProperty("user.dir") + "/devicecount.docx";

		boolean save = true;
		String outputfilepath = System.getProperty("user.dir")
				+ "/OUT_VariableReplaceStAX.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		mappings.put("colour", "green");
		mappings.put("icecream", "chocolate");
		
		long start = System.currentTimeMillis();
		
			documentPart.pipe( new MyStAXHandler() );
		
			
		long end = System.currentTimeMillis();
		long total = end - start;
		System.out.println("Time: " + total);

		// Save it
		if (save) {
//			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
//			saver.save(outputfilepath);
			
			Docx4J.save(wordMLPackage, new File(outputfilepath));
			
		} else {
			System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true,
					true));
		}
	}
	
	public static class MyStAXHandler extends StAXHandlerAbstract {
		
		public void handleCharacters(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {
			
			StringBuilder sb = new StringBuilder();
			sb.append(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
											
			String wmlString = replace(sb.toString(), 0, new StringBuilder(), mappings).toString();
//			System.out.println(wmlString);
			
			char[] charOut = wmlString.toCharArray();
			writer.writeCharacters(charOut, 0, charOut.length);
			
//			writer.writeCharacters(xmlr.getTextCharacters(),
//					xmlr.getTextStart(), xmlr.getTextLength());
			
		}
		
		 private StringBuilder replace(String wmlTemplateString, int offset, StringBuilder strB, 
				 java.util.Map<String, ?> mappings) {
			 
		    int startKey = wmlTemplateString.indexOf("${", offset);
		    if (startKey == -1)
		       return strB.append(wmlTemplateString.substring(offset));
		    else {
		       strB.append(wmlTemplateString.substring(offset, startKey));
		       int keyEnd = wmlTemplateString.indexOf('}', startKey);
		       if (keyEnd>0) {
			       String key = wmlTemplateString.substring(startKey + 2, keyEnd);
			       Object val = mappings.get(key);
			       if (val==null) {
			    	   System.out.println("Invalid key '" + key + "' or key not mapped to a value");
			    	   strB.append(key );
			       } else {
			    	   strB.append(val.toString()  );
			       }
			       return replace(wmlTemplateString, keyEnd + 1, strB, mappings);
		       } else {
		    	   System.out.println("Invalid key: could not find '}' ");
			       strB.append("$");
			       return replace(wmlTemplateString, offset + 1, strB, mappings);		    	   
		       }
		    }
		 }		
	}

}
