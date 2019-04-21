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

package org.docx4j.samples;


import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


/**
 * To see what parts comprise your docx, try the PartsList sample.
 * 
 * There will always be a MainDocumentPart, usually called document.xml.
 * This sample shows you what objects are in that part.
 * 
 * It also shows a general approach for traversing the JAXB object tree in
 * the Main Document part.  It can also be applied to headers, footers etc. 
 * 
 * It is an alternative to XSLT, and doesn't require marshalling/unmarshalling.
 * 
 * If many cases, the method getJAXBNodesViaXPath would be more convenient,
 * but there are 3 JAXB bugs which detract from that (see Getting Started).  
 * 
 * See related classes SingleTraversalUtilVisitorCallback 
 * and CompoundTraversalUtilVisitorCallback
 * 
 * @author jharrop
 *
 */
public class OpenMainDocumentAndTraverse extends AbstractSample {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	public static void main(String[] args) throws Exception {

		/*
		 * You can invoke this from an OS command line with something like:
		 * 
		 * java -cp dist/docx4j.jar:dist/log4j-1.2.15.jar
		 * org.docx4j.samples.OpenMainDocumentAndTraverse inputdocx
		 * 
		 * Note the minimal set of supporting jars.
		 * 
		 * If there are any images in the document, you will also need:
		 * 
		 * dist/xmlgraphics-commons-1.4.jar:dist/commons-logging-1.1.1.jar
		 */

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir")
					+ "/sample-docs/sample-docx.docx";
		}

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		// Uncomment to see the raw XML
		//System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
				.getJaxbElement();
		Body body = wmlDocumentEl.getBody();

		new TraversalUtil(body,

		new Callback() {

			String indent = "";

			@Override
			public List<Object> apply(Object o) {
				
				String wrapped = "";
				if (o instanceof JAXBElement) wrapped =  " (wrapped in JAXBElement)";

				o = XmlUtils.unwrap(o); 
				
				String text = "";
				if (o instanceof org.docx4j.wml.Text)
					text = ((org.docx4j.wml.Text) o).getValue();

				System.out.println(indent + o.getClass().getName() + wrapped + "  \""
						+ text + "\"");
				return null;
			}

			@Override
			public boolean shouldTraverse(Object o) {
				return true;
			}

			// Depth first
			@Override
			public void walkJAXBElements(Object parent) {

				indent += "    ";

				List children = getChildren(parent);
				if (children != null) {

					for (Object o : children) {

						this.apply(o);

						// if its wrapped in javax.xml.bind.JAXBElement, get its
						// value
						o = XmlUtils.unwrap(o);
						
						if (this.shouldTraverse(o)) {
							walkJAXBElements(o);
						}

					}
				}

				indent = indent.substring(0, indent.length() - 4);
			}

			@Override
			public List<Object> getChildren(Object o) {
				return TraversalUtil.getChildrenImpl(o);
			}

		}

		);

	}

}
