/*
 *  Copyright 2010, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.DrawingML;


import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.dml.diagram.CTCxn;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTElemPropSet;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.STCxnType;
import org.docx4j.dml.diagram.STPtType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;


public final class DiagramDataPart extends JaxbDmlPart<CTDataModel> {
	
	private static Logger log = Logger.getLogger(DiagramDataPart.class);			
	
	public DiagramDataPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public DiagramDataPart() throws InvalidFormatException {
		super(new PartName("/word/diagrams/data1.xml")); 
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DIAGRAM_DATA));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DRAWINGML_DIAGRAM_DATA);
	}
		
		
	/**
	 * This method changes the IDs from GUID to ints,
	 * so that the contents of the part is easier to understand.
	 * 
	 * That's all its for.  Use at your own risk.
	 */
	@Deprecated
	public void setFriendlyIds() {
		
		// Go through once creating a map of IDs
		generateIdMap();
		
		// Go through again, replacing
		ReplaceIds();
		
	}
	
	HashMap<String, String> map = new HashMap<String, String>();
	int index = 0;
	private void generateIdMap() {
		
		new TraversalUtil(getJaxbElement(),

				new Callback() {
			
					// Unfortunately, the schema says
					// a model id must be an int or a GUID.
					// Word 2007 won't open a docx which violates that.
					final static boolean IGNORE_XSD_RESTRICTION = false;

					@Override
					public List<Object> apply(Object o) {
						
						if (o instanceof CTPt) {
							
							CTPt pt = (CTPt)o;
							
							
							String from = pt.getModelId();
							
							// Everything goes invisible in Word 2007 if you map these,
							// and images disappear (not shown as broken)
							if (pt.getType()!=null
									&& pt.getType().equals(STPtType.PRES)) {
								map.put(from, generateNiceGuid(index));
								index++;
								return null;
							}
							
							String to = null;
							
							if (IGNORE_XSD_RESTRICTION) {
							
								if (pt.getType().equals(STPtType.DOC)) {
										to = "doc";
								} else if (pt.getType().equals(STPtType.PAR_TRANS)) {
									to="parTrans"+index;
									index++;
								} else if (pt.getType().equals(STPtType.SIB_TRANS)) {
									to="sibTrans"+index;
									index++;
								} else if (pt.getType().equals(STPtType.PRES)) {
									to="pres"+index;
									index++;
								} else {									
									to="pt"+index;
									index++;
								}
								
							} else {
								to = "" + index;
								index++;								
							}
							map.put(from, to);							
						}

						if (o instanceof CTCxn) {
							
							CTCxn cxn = (CTCxn)o;
							String from = cxn.getModelId();
							String to = null;
							
							if (IGNORE_XSD_RESTRICTION) {
								
								if (cxn.getType().equals(STCxnType.PRES_OF)) {
									to="presOf"+index;
									index++;
								} else if (cxn.getType().equals(STCxnType.PAR_OF)) {
									to="parOf"+index;
									index++;
								} else if (cxn.getType().equals(STCxnType.PRES_PAR_OF)) {
									to="presParOf"+index;
									index++;
								} else {								
									to="cxn"+index;
									index++;
								}
								
							} else {
								to = "" + index;
								index++;								
							}
							
							map.put(from, to);							
						}
						
						return null;
					}

					@Override
					public boolean shouldTraverse(Object o) {
						return true;
					}

					// Depth first
					@Override
					public void walkJAXBElements(Object parent) {


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

					}

					@Override
					public List<Object> getChildren(Object o) {
						return TraversalUtil.getChildrenImpl(o);
					}

				}

				);
		
		
	}
	
	private String generateNiceGuid(int index) {
		
		if (index<10) {
			return "{00000000-0000-0000-0000-00000000000" + index + "}";
		} else if (index<100) {
			return "{00000000-0000-0000-0000-0000000000" + index + "}";			
		} else {
			return "{00000000-0000-0000-0000-000000000" + index + "}";			
		}
		
	}

	private void ReplaceIds() {
		
		new TraversalUtil(getJaxbElement(),

				new Callback() {

					@Override
					public List<Object> apply(Object o) {
						
						if (o instanceof CTPt) {
							
							CTPt pt = (CTPt)o;
														
							pt.setModelId( 
									map.get(pt.getModelId() ));
							
							if (pt.getPrSet()!=null) {
								CTElemPropSet pr = (CTElemPropSet)pt.getPrSet();
								if (pr.getPresAssocID()!=null) {
									pr.setPresAssocID(
											map.get(pr.getPresAssocID() ));
								}
							}
							
							if (pt.getCxnId()!=null) {
								pt.setCxnId( 
										map.get(pt.getCxnId() ));
							}							
						}

						if (o instanceof CTCxn) {
							
							CTCxn cxn = (CTCxn)o;
							cxn.setModelId(map.get(cxn.getModelId()));
							
							cxn.setSrcId(map.get(cxn.getSrcId()));
							cxn.setDestId(map.get(cxn.getDestId()));

							if (cxn.getSibTransId()!=null) {
								cxn.setSibTransId( 
										map.get(cxn.getSibTransId() ));
							}
							if (cxn.getParTransId()!=null) {
								cxn.setParTransId( 
										map.get(cxn.getParTransId() ));
							}
//							if (cxn.getPresId()!=null) {
//								cxn.setPresId( 
//										map.get(cxn.getPresId() ));
//							}							
						}
						
						return null;
					}

					@Override
					public boolean shouldTraverse(Object o) {
						return true;
					}

					// Depth first
					@Override
					public void walkJAXBElements(Object parent) {


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

					}

					@Override
					public List<Object> getChildren(Object o) {
						return TraversalUtil.getChildrenImpl(o);
					}

				}

				);
		
		
	}
	
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(
						System.getProperty("user.dir")
						+ "/SmartArt/org4.docx"));
		
		Relationship r = wordMLPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByType(Namespaces.DRAWINGML_DIAGRAM_DATA);
		
		if (r==null) {
			System.out.println("No DDP!");
			return;
		}

		DiagramDataPart thisPart = (DiagramDataPart)wordMLPackage.getMainDocumentPart().getRelationshipsPart().getPart(r);			
		
		thisPart.setFriendlyIds();
		
		System.out.println( XmlUtils.marshaltoString(thisPart.getJaxbElement(), true, true));

		// Now fix the IDs in the drawing part to match
		// TODO: just drop this part altogether; we don't need it
		Relationship r2 = wordMLPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByType(Namespaces.DRAWINGML_DIAGRAM_DRAWING);		
		if (r2==null) {
			System.out.println("No DDrawingP!");
		} else {
			DiagramDrawingPart drawingPart = (DiagramDrawingPart)wordMLPackage.getMainDocumentPart().getRelationshipsPart().getPart(r2);
			drawingPart.setFriendlyIds(thisPart.map);			
			System.out.println( XmlUtils.marshaltoString(drawingPart.getJaxbElement(), true, true));
		}		
		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(System.getProperty("user.dir")
				+ "/SmartArt/org4-guid.docx");
		
	}	
		
}
