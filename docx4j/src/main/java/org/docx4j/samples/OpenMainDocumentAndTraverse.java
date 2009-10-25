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


import java.io.FileInputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;


public class OpenMainDocumentAndTraverse {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/tmp/Default TOC with 3 levels.docx";
		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/jbtemplate.docx";
		//String inputfilepath = "/home/dev/s.docx";
		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/AutoOpen.docm";
		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/math.docx";
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/table-spans.xml";
		
		boolean save = false;
		String outputfilepath = System.getProperty("user.dir") + "/test-out.docx";		
		
		
		// Open a document from the file system
		// 1. Load the Package - .docx or .xml
		WordprocessingMLPackage wordMLPackage;// = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		if (inputfilepath.endsWith(".xml")) {
			// You can create one of these in Word 2007, by 
			// choosing Save As .xml
			// These are easier to look at / edit in a text editor than a zipped up docx
			JAXBContext jc = Context.jcXmlPackage;
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
					new javax.xml.transform.stream.StreamSource(new FileInputStream(inputfilepath)))).getValue(); 

			org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 

			wordMLPackage = (WordprocessingMLPackage)xmlPackage.get(); 
		
		} else {
			// Its just a docx
			wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		}
		
		
		// 2. Fetch the document part 		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// Display its contents 
		System.out.println( "\n\n OUTPUT " );
		System.out.println( "====== \n\n " );	
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		walkJAXBElements(bodyChildren);		
		
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
				
		// Save it
		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
		}
	}
	
	static void walkJAXBElements(List <Object> bodyChildren){
	
		for (Object o : bodyChildren ) {

			if ( o instanceof javax.xml.bind.JAXBElement) {
			
				System.out.println("\n"+ XmlUtils.JAXBElementDebug((JAXBElement)o) );
					
				if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Tbl") ) {
					org.docx4j.wml.Tbl tbl = (org.docx4j.wml.Tbl)((JAXBElement)o).getValue();
					describeTable(tbl);
				}
			} else if (o instanceof org.docx4j.wml.P) {
				System.out.println( "Paragraph object: ");
				
				if (((org.docx4j.wml.P)o).getPPr() != null
						&& ((org.docx4j.wml.P)o).getPPr().getRPr() != null
						&& ((org.docx4j.wml.P)o).getPPr().getRPr().getB() !=null) {
					System.out.println( "For a ParaRPr bold!");
				}
				
				
				walkList( ((org.docx4j.wml.P)o).getParagraphContent());
			}
		}
	}
	
	static void walkList(List children){
		
		for (Object o : children ) {					
			if ( o instanceof javax.xml.bind.JAXBElement) {
				
				System.out.println( "\n" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
				
				// TODO - unmarshall directly to Text.
				if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
					org.docx4j.wml.Text t = (org.docx4j.wml.Text)((JAXBElement)o).getValue();
					System.out.println("      " +  t.getValue() );
					
				} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Drawing") ) {
					describeDrawing( (org.docx4j.wml.Drawing)((JAXBElement)o).getValue() );

				} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Pict") ) {
					org.docx4j.wml.Pict pic = (org.docx4j.wml.Pict)((JAXBElement)o).getValue();
					walkList(pic.getAnyAndAny());
				}
				
				
				if ( ((JAXBElement)o).getName().getLocalPart().equals("bookmarkStart") ) {
					org.docx4j.wml.CTBookmark bs = (org.docx4j.wml.CTBookmark)((JAXBElement)o).getValue(); 
					System.out.println(" .. bookmarkStart" );
				}
				
				if ( ((JAXBElement)o).getName().getLocalPart().equals("bookmarkEnd") ) {
					org.docx4j.wml.CTMarkupRange be = (org.docx4j.wml.CTMarkupRange)((JAXBElement)o).getValue(); 
					System.out.println(" .. bookmarkEnd" );
				}
				
			} else {
				System.out.println("  " + o.getClass().getName() );
				if ( o instanceof org.docx4j.wml.R) {
					org.docx4j.wml.R  run = (org.docx4j.wml.R)o;
					if (run.getRPr()!=null) {
						System.out.println("      " +   "Properties...");
						if (run.getRPr().getB()!=null) {
							System.out.println("      " +   "B not null ");						
							System.out.println("      " +   "--> " + run.getRPr().getB().isVal() );
						} else {
							System.out.println("      " +   "B null.");												
						}
					}
					walkList(run.getRunContent());									
				} else if (o instanceof org.w3c.dom.Node) {
						System.out.println(" IGNORED " + ((org.w3c.dom.Node)o).getNodeName() );					
				} else {					
					System.out.println(" IGNORED " + o.getClass().getName() );					
				}
			}
//			else if ( o instanceof org.docx4j.jaxb.document.Text) {
//				org.docx4j.jaxb.document.Text  t = (org.docx4j.jaxb.document.Text)o;
//				System.out.println("      " +  t.getValue() );					
//			}
		}
	}

	static void describeTable( org.docx4j.wml.Tbl tbl ) {
		
		// What does a table look like?
		boolean suppressDeclaration = false;
		boolean prettyprint = true;
		System.out.println( org.docx4j.XmlUtils.marshaltoString(tbl, suppressDeclaration, prettyprint) );
		
		// Could get the TblPr if we wanted them
		 org.docx4j.wml.TblPr tblPr = tbl.getTblPr();
		 
		 // Could get the TblGrid if we wanted it
		 org.docx4j.wml.TblGrid tblGrid = tbl.getTblGrid();
		 
		 // But here, let's look at the table contents
		 for (Object o : tbl.getEGContentRowContent() ) {
			 
			 if (o instanceof org.docx4j.wml.Tr) {
				 
				 System.out.println( "\n in w:tr .. ");
				 org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr)o;
				 
				 for (Object o2 : tr.getEGContentCellContent() ) {
					 
						if ( o2 instanceof javax.xml.bind.JAXBElement) {
							
							if ( ((JAXBElement)o2).getDeclaredType().getName().equals("org.docx4j.wml.Tc") ) {
								System.out.println( "\n  in w:tc .. ");
								org.docx4j.wml.Tc tc = (org.docx4j.wml.Tc)((JAXBElement)o2).getValue();
								
								// Look at the paragraphs in the tc
								walkJAXBElements( tc.getEGBlockLevelElts() );
								
							} else {
								// What is it, if it isn't a Tc?
								System.out.println( "\n  NOT Tc: " + XmlUtils.JAXBElementDebug((JAXBElement)o) );
							}
						} else {
							System.out.println("  " + o.getClass().getName() );							
						}
					 
				 }
				 
				 
			 } else {
				System.out.println("  " + o.getClass().getName() );
			 }
			 
		 }
		 
		 
		
	}
	
	static void describeDrawing( org.docx4j.wml.Drawing d ) {
	
		System.out.println(" describeDrawing " );
		
		if ( d.getAnchorOrInline().get(0) instanceof org.docx4j.dml.Anchor ) {
			
			System.out.println(" ENCOUNTERED w:drawing/wp:anchor " );
			// That's all for now...
			
		} else if ( d.getAnchorOrInline().get(0) instanceof org.docx4j.dml.Inline ) {
			
			// Extract w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed
			
			org.docx4j.dml.Inline inline = (org.docx4j.dml.Inline )d.getAnchorOrInline().get(0);
			
			org.docx4j.dml.Pic pic = inline.getGraphic().getGraphicData().getPic();
						
			System.out.println( "image relationship: " +  pic.getBlipFill().getBlip().getEmbed() );
			
			
		} else {
			
			System.out.println(" Didn't get Inline :(  How to handle " + d.getAnchorOrInline().get(0).getClass().getName() );
		}
		
	}

}
