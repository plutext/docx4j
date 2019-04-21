/*
 *  Copyright 2011, Plutext Pty Ltd.
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
package org.pptx4j.samples;


import java.util.List;

import javax.xml.bind.JAXBContext;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;


/**
 * This sample is useful if you want to see what objects are used in your Slide part.
 * 
 * This shows a general approach for traversing the JAXB object tree in
 * a slide part.  
 * 
 * It is an alternative to XSLT, and doesn't require marshalling/unmarshalling.
 * 
 * If many cases, the method getJAXBNodesViaXPath
 * may be more convenient.  
 * 
 * @author jharrop
 *
 */
public class TraverseSlide {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	/**
	 * @param args
	 */
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

		String inputfilepath = System.getProperty("user.dir")
			+ "/sample-docs/pptx-basic.xml";

		PresentationMLPackage pMLPackage = 
			(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));

		SlidePart slide = (SlidePart)pMLPackage.getParts().get(new PartName("/ppt/slides/slide1.xml") );
		
		new TraversalUtil(slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(),

		new Callback() {

			String indent = "";

//			@Override
			public List<Object> apply(Object o) {

				String text = "";

				try {
					System.out.println(indent + o.getClass().getName() + "\n\n" + XmlUtils.marshaltoString(o, true, org.pptx4j.jaxb.Context.jcPML));					
				} catch (RuntimeException me) {					
					System.out.println(indent + o.getClass().getName() );										
				}
				
				if (o instanceof org.pptx4j.pml.Shape) {
					CTTextBody txBody = ((org.pptx4j.pml.Shape)o).getTxBody();
					if (txBody!=null) {
						for (CTTextParagraph tp : txBody.getP() ) {
							
							System.out.println(indent + tp.getClass().getName() + "\n\n" + XmlUtils.marshaltoString(tp, true, true, org.pptx4j.jaxb.Context.jcPML,
									"http://schemas.openxmlformats.org/presentationml/2006/main", "txBody", CTTextParagraph.class));
							
						}
					}
				}
				
				return null;
			}

//			@Override
			public boolean shouldTraverse(Object o) {
				return true;
			}

			// Depth first
//			@Override
			public void walkJAXBElements(Object parent) {

				indent += "    ";

				List children = getChildren(parent);
				if (children != null) {

					for (Object o : children) {

						// if its wrapped in javax.xml.bind.JAXBElement, get its
						// value
						o = XmlUtils.unwrap(o);

						this.apply(o);

						if (this.shouldTraverse(o)) {
							walkJAXBElements(o);
						}

					}
				}

				indent = indent.substring(0, indent.length() - 4);
			}

//			@Override
			public List<Object> getChildren(Object o) {
				return TraversalUtil.getChildrenImpl(o);
			}

		}

		);

	}

}
