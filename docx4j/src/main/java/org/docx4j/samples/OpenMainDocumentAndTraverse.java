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

import org.docx4j.XmlUtils;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.w3c.dom.Element;


/**
 * This sample shows how a JAXB object tree can be traversed.
 * 
 * This is useful if you want to get a particular JAXB node,
 * and also to see what objects are used in your document.
 * 
 * In practice, if you want to find/manipulate elements in
 * your main document part, the method getJAXBNodesViaXPath
 * is more convenient.  
 * 
 * @author jharrop
 *
 */
public class OpenMainDocumentAndTraverse extends AbstractSample {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		/*
		 * You can invoke this from an OS command line with
		 * something like:
		 * 
		 *  java -cp dist/docx4j.jar:dist/log4j-1.2.15.jar org.docx4j.samples.OpenMainDocumentAndTraverse inputdocx
		 *  
		 * Note the minimal set of supporting jars. 
		 * 
		 * If there are any images in the document, you will also need:
		 * 
		 *  dist/xmlgraphics-commons-1.4.jar:dist/commons-logging-1.1.1.jar  
		 * 
		 */

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.xml";
		}
		
		
		boolean save = false;
		try {
			getOutputFilePath(args);
			save = true;
		} catch (IllegalArgumentException e) {
			outputfilepath = System.getProperty("user.dir") + "/test-out.docx";
//			save = true;
		}
		
		
		// Open a document from the file system
		// 1. Load the Package - .docx or Flat OPC .xml
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		
		// 2. Fetch the document part 		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		
		
		// Display its contents 
		System.out.println( "\n\n OUTPUT " );
		System.out.println( "====== \n\n " );	
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		walkJAXBElements(bodyChildren, "");		
		
		org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart commentsPart = documentPart.getCommentsPart();
		
		if (commentsPart!=null ) {
		
			Object o = commentsPart.getJaxbElement();
			System.out.println( o.getClass().getName() );
	//		System.out.println( ((JAXBElement)o).getName() );
	//		System.out.println( ((JAXBElement)o).getDeclaredType().getName() + "\n\n");
			
			org.docx4j.wml.Comments comments = (org.docx4j.wml.Comments)o;
			
			List<org.docx4j.wml.Comments.Comment> commentList = comments.getComment();
			
			//walkJAXBElements(comments.getComment());		
		}
		
//		// Get the document settings
//		System.out.println(documentPart.getDocumentSettingsPart().getJaxbElement().getClass().getName() );
//		
//		Relationship r = documentPart.getRelationshipsPart().getRelationshipByType(Namespaces.SETTINGS);
//		DocumentSettingsPart dsp = (DocumentSettingsPart)documentPart.getRelationshipsPart().getPart(r);
//		System.out.println(dsp.getJaxbElement().getClass().getName() );
		
		// Look at our document model
		System.out.println("Registered " + wordMLPackage.getDocumentModel().getSections().size() + " sections");
		
				
		// Save it
		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
		}
				
	}
	
	static void walkJAXBElements(List <Object> children, String indent){
	
		for (Object o : children ) {
			inspectObject(o, indent + "  ");
		}
	}
	
	static void inspectObject(Object o, String indent) {
		
		// if its wrapped in javax.xml.bind.JAXBElement, get its value
		o = XmlUtils.unwrap(o);

		if (o instanceof org.docx4j.wml.SdtBlock) {

			System.out.println(indent+"SDT (Content Control): ");

			walkJAXBElements(((org.docx4j.wml.SdtBlock) o).getSdtContent()
					.getEGContentBlockContent(), indent + "  ");
		} else if (o instanceof org.docx4j.wml.P) {
			System.out.println(indent+"Paragraph object: ");

			// if (((org.docx4j.wml.P)o).getPPr() != null ) {
			//				
			// org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
			// if (ppr.getSectPr()!=null) {
			// System.out.println(indent+ "paragraph contains sectpr");
			// }
			//				
			// if ( ppr.getRPr() != null
			// && ppr.getRPr().getB() !=null ) {
			// System.out.println(indent+ "For a ParaRPr bold!");
			// }
			// }

			walkJAXBElements(((org.docx4j.wml.P) o).getParagraphContent(), indent + "  ");

		} else if (o instanceof org.docx4j.wml.R) {
			org.docx4j.wml.R run = (org.docx4j.wml.R) o;
			// if (run.getRPr()!=null) {
			// System.out.println(indent+"      " + "Properties...");
			// if (run.getRPr().getB()!=null) {
			// System.out.println(indent+"      " + "B not null ");
			// System.out.println(indent+"      " + "--> " +
			// run.getRPr().getB().isVal() );
			// } else {
			// System.out.println(indent+"      " + "B null.");
			// }
			// }
			walkJAXBElements(run.getRunContent(), indent + "  ");
		} else if (o instanceof org.docx4j.wml.Text) {
			org.docx4j.wml.Text t = (org.docx4j.wml.Text) o;
			System.out.println(indent+"      " + t.getValue());

		} else if (o instanceof org.docx4j.wml.Tbl) {
			describeTable((org.docx4j.wml.Tbl) o, indent);
		} else if (o instanceof org.docx4j.wml.Drawing) {
			describeDrawing((org.docx4j.wml.Drawing) o, indent);
		} else if (o instanceof org.docx4j.wml.Tr) {

			System.out.println(indent+"in w:tr .. ");
			org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr) o;

			for (Object o2 : tr.getEGContentCellContent()) {

				inspectObject(o2, indent + "  ");
			}

		} else if (o instanceof org.docx4j.wml.Tc) {
			System.out.println(indent+"in w:tc .. ");
			org.docx4j.wml.Tc tc = (org.docx4j.wml.Tc) o;

			// Look at the paragraphs in the tc
			walkJAXBElements(tc.getEGBlockLevelElts(), indent + "  ");

		} else if (o instanceof org.docx4j.wml.Pict) {
			org.docx4j.wml.Pict pic = (org.docx4j.wml.Pict) o;
			walkJAXBElements(pic.getAnyAndAny(), indent + "  ");

		} else if (o instanceof org.docx4j.wml.CTBookmark) {
			org.docx4j.wml.CTBookmark bs = (org.docx4j.wml.CTBookmark) o;
			System.out.println(indent+" .. bookmarkStart");

		} else if (o instanceof org.docx4j.wml.CTMarkupRange) {
			org.docx4j.wml.CTMarkupRange be = (org.docx4j.wml.CTMarkupRange) o;
			System.out.println(indent+" .. bookmarkEnd");

		} else {
			System.out.println(indent+"  " + o.getClass().getName());
			if (o instanceof org.w3c.dom.Node) {
				System.out.println(indent+" IGNORED "
						+ ((org.w3c.dom.Node) o).getNodeName());
			} else {
				System.out.println(indent+" IGNORED " + o.getClass().getName());
			}
		}
		// else if ( o instanceof org.docx4j.jaxb.document.Text) {
		// org.docx4j.jaxb.document.Text t = (org.docx4j.jaxb.document.Text)o;
		// System.out.println(indent+"      " + t.getValue() );
		// }
		
	}
	
	static void describeTable( org.docx4j.wml.Tbl tbl, String indent ) {
		
		System.out.println(indent+"w:tbl ");

		// What does a table look like?
		boolean suppressDeclaration = false;
		boolean prettyprint = true;
		// Uncomment to see table xml
		//System.out.println(indent+ org.docx4j.XmlUtils.marshaltoString(tbl, suppressDeclaration, prettyprint) );
		
		// Could get the TblPr if we wanted them
		 org.docx4j.wml.TblPr tblPr = tbl.getTblPr();
		 
		 // Could get the TblGrid if we wanted it
		 org.docx4j.wml.TblGrid tblGrid = tbl.getTblGrid();
		 
		 // But here, let's look at the table contents
		 for (Object o : tbl.getEGContentRowContent() ) {
					 
			 inspectObject(o, indent + "  ");					 			 
		 }
		
	}
	
	static void describeDrawing( org.docx4j.wml.Drawing d, String indent ) {
	
		System.out.println(indent+" describeDrawing " );
		
		if ( d.getAnchorOrInline().get(0) instanceof Anchor ) {
			
			System.out.println(indent+" ENCOUNTERED w:drawing/wp:anchor " );
			// That's all for now...
			
		} else if ( d.getAnchorOrInline().get(0) instanceof Inline ) {
			
			// Extract w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed
			
			Inline inline = (Inline )d.getAnchorOrInline().get(0);
			
			Pic pic = inline.getGraphic().getGraphicData().getPic();
				
			if (pic!=null) {
				System.out.println(indent+ "image relationship: " +  pic.getBlipFill().getBlip().getEmbed() );
			}
			
			
		} else {
			
			System.out.println(indent+" Didn't get Inline :(  How to handle " + d.getAnchorOrInline().get(0).getClass().getName() );
		}
		
	}
	
}
