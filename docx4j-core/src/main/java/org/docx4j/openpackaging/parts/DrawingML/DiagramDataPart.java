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


import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.diagram.CTCxn;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTElemPropSet;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.STPtType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DiagramDataPart extends JaxbDmlPart<CTDataModel> {
	
	private static Logger log = LoggerFactory.getLogger(DiagramDataPart.class);			
	
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
	 * This method ensures generated IDs are
	 * valid and (subject to that constraint)
	 * easy-ish to understand.
	 */
	public static void setFriendlyIds(Object jaxbElement) {
				
		map = new HashMap<String, String>();
		
		// Go through once creating a map of IDs
		generateIdMap(jaxbElement);
		
		// Go through again, replacing
		ReplaceIds(jaxbElement);
		
	}
	
	static HashMap<String, String> map;
	static int index = 0;
	protected static void generateIdMap(Object jaxbElement) {
		
		new TraversalUtil(jaxbElement,

				new Callback() {
			
					// Unfortunately, the schema says
					// a model id must be an int or a GUID.
					// Word 2007 won't open a docx which violates that.

					@Override
					public List<Object> apply(Object o) {
						
						if (o instanceof CTPt) {
							
							CTPt pt = (CTPt)o;
							
							
							String from = pt.getModelId();
							
							// Everything goes invisible in Word 2007 if you map these
							// to an int (rather than a guid),
							// and images disappear (not shown as broken)
							if (pt.getType()!=null
									&& pt.getType().equals(STPtType.PRES)) {
								map.put(from, generateNiceGuid(index));
								index++;
								return null;
							}
							
//							String to = generateNiceGuid(index);
							String to = "" + index;
							index++;								
							map.put(from, to);							
						}

						if (o instanceof CTCxn) {
							
							CTCxn cxn = (CTCxn)o;
							String from = cxn.getModelId();
//							String to = generateNiceGuid(index);
							String to = "" + index;
							index++;								
							
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
	
	private static String generateNiceGuid(int index) {
		
		/* It seems that if there are more than about 100
		 * items, Word won't open a docx with friendly ids
		 * of the following form:
		 * 
			if (index<10) {
				return "{00000000-0000-0000-0000-00000000000" + index + "}";
			} else if (index<100) {
				return "{00000000-0000-0000-0000-0000000000" + index + "}";			
			} else {
				return "{00000000-0000-0000-0000-000000000" + index + "}";			
			}

		 * Speculate that this is because M$ internally
		 * uses some broken equality test.
		 * 
		 * The following works better ...
		 */
		

		if (index<10) {
			return "{0000000" + index + "-0000-0000-0000-000000000000}";
		} else if (index<100) {
			return "{000000" +  index + "-0000-0000-0000-000000000000}";			
		} else {
			return "{00000" +   index + "-0000-0000-0000-000000000000}";			
		}
		
		// This works as well...
//		return "{" + UUID.randomUUID().toString().toUpperCase() + "}";
		 
		
	}

	private static String mapGet(HashMap<String, String> map, String key) {
		
		if (key==null) {
			System.out.println("Key not found!");
			return null;
		}
		
		String val = map.get(key);
		if (val==null) {
			System.out.println("No val for Key " + key);
			return key;
		}
		
		return val;
	}
	
	protected static void ReplaceIds(Object jaxbElement) {
		
		new TraversalUtil(jaxbElement,

				new Callback() {

					@Override
					public List<Object> apply(Object o) {
												
						if (o instanceof CTPt) {
							
							CTPt pt = (CTPt)o;
														
							pt.setModelId( 
									mapGet(map, pt.getModelId() ));
							
							if (pt.getPrSet()!=null) {
								CTElemPropSet pr = (CTElemPropSet)pt.getPrSet();
								if (pr.getPresAssocID()!=null) {
									pr.setPresAssocID(
											mapGet(map, pr.getPresAssocID() ));
								}
							}
							
							if (!pt.getCxnId().equals("0")) {
								pt.setCxnId( 
										mapGet(map, pt.getCxnId() ));
							}							
						}

						if (o instanceof CTCxn) {
							
							CTCxn cxn = (CTCxn)o;
							if (cxn.getModelId()!=null) {
								cxn.setModelId(mapGet(map,cxn.getModelId()));
							}
							
							cxn.setSrcId(mapGet(map, cxn.getSrcId()));
							cxn.setDestId( mapGet(map,cxn.getDestId() ));

							if (!cxn.getSibTransId().equals("0")) {
								cxn.setSibTransId( 
										mapGet(map, cxn.getSibTransId() ));
							}
							if (!cxn.getParTransId().equals("0")) {
								cxn.setParTransId( 
										mapGet(map, cxn.getParTransId() ));
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
	
	public static String addImage(DiagramDataPart ddp, String base64) {
		// No need to pass content type.
				
		log.debug("Adding image: " + base64);
		
		BinaryPartAbstractImage imagePart = null;
		try {
			// Base64 decode it
			byte[] bytes = Base64.decodeBase64( base64.getBytes("UTF8") );
			
			// Create image part and add it
			imagePart = BinaryPartAbstractImage.createImagePart(
					ddp.getPackage(), ddp, bytes);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);

			// Can't use this image, so insert a placeholder
			log.info(".. attempting to use broken image placeholder");
			try {
				byte[] bytes = IOUtils.toByteArray(org.docx4j.utils.ResourceUtils.getResource(
						"image_broken.gif"));
				
				imagePart = BinaryPartAbstractImage.createImagePart(
						ddp.getPackage(), ddp, bytes);

				log.info(".. used broken image placeholder");
				
			} catch (Exception e1) {
				e1.printStackTrace();
				return "";
			}
		}
		
        return imagePart.getSourceRelationships().get(0).getId();
	}
	
	public static void debug(String message) {
		
		System.out.println(message);
	}
	
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(
						System.getProperty("user.dir")
						+ "/SmartArt/OUT-xx.docx"));
		
		Relationship r = wordMLPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByType(Namespaces.DRAWINGML_DIAGRAM_DATA);
		
		if (r==null) {
			System.out.println("No DDP!");
			return;
		}

		DiagramDataPart thisPart = (DiagramDataPart)wordMLPackage.getMainDocumentPart().getRelationshipsPart().getPart(r);			
		
		thisPart.setFriendlyIds(thisPart.getJaxbElement());
		
		System.out.println( XmlUtils.marshaltoString(thisPart.getJaxbElement(), true, true));

		// What does it look like in our format?
		DiagramDataUnflatten diagramDataUnflatten = new DiagramDataUnflatten(thisPart);
		String exchange= XmlUtils.marshaltoString(diagramDataUnflatten.convert(), true, true);
		System.out.println( exchange );		
		PrintWriter out = new PrintWriter(System.getProperty("user.dir")
				+ "/SmartArt/12hi.xml");
		out.println(exchange);
		out.flush();
		out.close();

		// Check our format templates
		List<JAXBElement<CTTextBody>> textFormats = diagramDataUnflatten.getTextFormats();
		System.out.println("Template list =============== ");
		for (JAXBElement<CTTextBody> tb : textFormats) {
			System.out.println( XmlUtils.marshaltoString(tb, true, true));				
		}
		System.out.println("============================= ");
		
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
				+ "/SmartArt/OUT-clean.docx");
		
	}	
		
}
