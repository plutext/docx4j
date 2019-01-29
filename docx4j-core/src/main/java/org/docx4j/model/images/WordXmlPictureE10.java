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
package org.docx4j.model.images;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.vml.CTImageData;
import org.docx4j.vml.CTShape;
import org.docx4j.wml.Pict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Generate HTML/XSLFO from 
 * 
 * Originally from OpenXmlView project.
 * TODO - add Microsoft Public Licence
 * 
 * TODO - integrate with our other image handling stuff
 * 
 * Amended .. can generate HTML element, or XSL FO.
 *
 * E10 example:
 * 
				<w:pict>
					<v:shapetype id="_x0000_t75" coordsize="21600,21600" 
					o:spt="75" o:preferrelative="t" 
					path="m@4@5l@4@11@9@11@9@5xe" filled="f" stroked="f">
						<v:stroke joinstyle="miter" />
						<v:formulas>
							:
						</v:formulas>
						<v:path o:extrusionok="f" gradientshapeok="t" o:connecttype="rect" />
						<o:lock v:ext="edit" aspectratio="t" />
					</v:shapetype>
					<v:shape id="_x0000_i1025" type="#_x0000_t75" 
						style="width:428.25pt;height:321pt">
						<v:imagedata r:id="rId4" o:title="" />
					</v:shape>
				</w:pict>
 * 
 * 
 *
 */
public class WordXmlPictureE10 extends AbstractWordXmlPicture {
	
	protected static Logger log = LoggerFactory.getLogger(WordXmlPictureE10.class);
	
	Pict pict;
	    
    private WordXmlPictureE10(WordprocessingMLPackage wmlPackage, 
    		Object wpict) {
    	
    	this.wmlPackage = wmlPackage;
    	    	
    	if (wpict!=null) {
    		
			if (wpict instanceof org.docx4j.wml.Pict) {
				this.pict = (org.docx4j.wml.Pict)wpict;
			} else if (wpict instanceof NodeIterator) {
				Node n = ((NodeIterator)wpict).nextNode();
				if (n != null) {
					Object jaxb = null;
					try {
						jaxb = XmlUtils.unmarshal(n, Context.jc, Pict.class);
					} catch (JAXBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						this.pict = (Pict) jaxb;
					} catch (ClassCastException e) {
						log.error("Couldn't cast " + jaxb.getClass().getName()
								+ " to PPr!");
					}
				}
			}
    	}
    	
    }
    
	CTShape shape=null;
	
	private void findShape() {
    	if (pict.getAnyAndAny()!=null) {
    		for (Object o : pict.getAnyAndAny() ) {
    			if (o instanceof JAXBElement ) {
    				JAXBElement jb = (JAXBElement)o;
    				if (jb.getDeclaredType().getName().equals("org.docx4j.vml.CTShapetype")) {
//    					shapeType = (CTShapetype)jb.getValue();
    				} else if (jb.getDeclaredType().getName().equals("org.docx4j.vml.CTShape")) {
    					shape = (CTShape)jb.getValue();
    					break;
    				} else {
	    				log.info("Skipping " +
	    						XmlUtils.JAXBElementDebug((JAXBElement)o)
	    						);
    				}
    			} else {
    				log.error( o.getClass().getName() );
    			}
    		}
    	}
		
	}

	CTImageData imageData = null;
	
	private void findImageData() {
		
    	if (shape.getPathOrFormulasOrHandles()==null) {
            if(log.isDebugEnabled()) {
                log.debug("Shape had no any: " + XmlUtils.marshaltoString(shape, true));
            }
    	} else {
    		for (Object o : shape.getPathOrFormulasOrHandles() ) {
    			
    			Object o2 = XmlUtils.unwrap(o);
    			if (o2 instanceof org.docx4j.vml.CTImageData) {
					imageData = (CTImageData)o2;
					break;
    			} else {
    				log.error( o.getClass().getName() );
    			}
    		}
    	}
		
	}
	
    private static WordXmlPictureE10 createWordXmlPictureFromE10(
    		WordprocessingMLPackage wmlPackage,
    		ConversionImageHandler imageHandler,
    		Object wpict,
    		Part sourcePart) {

    	WordXmlPictureE10 converter = new WordXmlPictureE10(wmlPackage, wpict);

//	  	<xsl:variable name="shape" select="./v:shape"/>
//	  	<xsl:variable name="imageData" select="./v:shape/v:imagedata"/>
    	
    	converter.findShape();    	
    	if (converter.shape==null) {
    		log.error("Couldn't find shape!");
    		return null;
    	}
    	converter.readStandardAttributes( converter.shape );
    	converter.readDimensions(converter.style);
    	
    	converter.findImageData();
    	if (converter.imageData==null) {
    		log.error("Couldn't find imageData!");
    		return null;
    	}
    	
//        String imgRelId = converter.imageData.getOtherAttributes().get(
//        		new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id"));   
    	
    	String imgRelId = converter.imageData.getId();
        if (imgRelId!=null && !imgRelId.equals("")) {
        	log.debug("Handling " + imgRelId);
        	converter.handleImageRel(imageHandler, imgRelId, sourcePart);
        } else {
        	log.error("No relId?!");
        }
        return converter;
    }  
    
    
    /** Extension function to create an <img> element
     * from "E1.0 images" //w:pict
     */
    public static DocumentFragment createHtmlImgE10(
    		AbstractWmlConversionContext context,
    		Object wpict) {
    	
    	return createHtmlImgE10(context, wpict, null);
    }

    /** Extension function to create an <img> element
     * from "E1.0 images" ie //w:pict
     * with a custom ID
     */
    public static DocumentFragment createHtmlImgE10(
    		AbstractWmlConversionContext context,
    		Object wpict, String id) {
    	
    	Part sourcePart = context.getCurrentPart();

    	WordXmlPictureE10 converter = createWordXmlPictureFromE10(context.getWmlPackage(),
        		 context.getImageHandler(),
        		 wpict, sourcePart);
    	
    	if (id!=null) {
    		converter.setID(id);
    	}
    	
    	return getHtmlDocumentFragment(converter);
    }
    
    /** Extension function to create an <img> element
     * from "E1.0 images"
     *  
     *      //w:pict
     * @param wmlPackage
     * @param imageDirPath
     * @param shape
     * @param imageData
     * @return
     */
    public static DocumentFragment createXslFoImgE10(
    		AbstractWmlConversionContext context,
    		NodeIterator wpict) {
    	

    	Part sourcePart = context.getCurrentPart();
    	
    	WordXmlPictureE10 converter = createWordXmlPictureFromE10(context.getWmlPackage(),
        		 context.getImageHandler(),
        		 wpict, sourcePart);
    	
    	//log.debug("imageDirPath: " + imageDirPath);
    	
    	if (converter==null) {
    		
    		log.error("WordXmlPictureE10 object was null!");
    		
            Document d = XmlUtils.getNewDocumentBuilder().newDocument();
	    	return d.createDocumentFragment();
			
    	} else {
    	
	        Document d = converter.createXslFoImageElement();
	
			DocumentFragment docfrag = d.createDocumentFragment();
			docfrag.appendChild(d.getDocumentElement());
	
			return docfrag;
    	}
    }
    
    
    /**
     * For XSLFOExporterNonXSLT
     * @since 3.0
     * 
     */
    public static DocumentFragment createXslFoImgE10(
    		AbstractWmlConversionContext context,
    		Object wpict) {
    	
    	Part sourcePart = context.getCurrentPart();
    	
    	WordXmlPictureE10 converter = createWordXmlPictureFromE10(
    			 context.getWmlPackage(),
        		 context.getImageHandler(),
        		 wpict, sourcePart);
    	
    	//log.debug("imageDirPath: " + imageDirPath);
    	
    	if (converter==null) {
    		
    		log.error("WordXmlPictureE10 object was null!");
    		
            Document d= XmlUtils.getNewDocumentBuilder().newDocument();
	    	return d.createDocumentFragment();
			
    	} else {
    	
	        Document d = converter.createXslFoImageElement();
	
			DocumentFragment docfrag = d.createDocumentFragment();
			docfrag.appendChild(d.getDocumentElement());
	
			return docfrag;
    	}
    }
    
    // -----------------------------------------------------------------
    

//    private byte[] data;
//    // / <summary>
//    // / The decoded data from the corresponding 'w:bindata'
//    /// node of the Word Document.
//    /// </summary>
//    /// <remarks>
//    /// This property is set by the conversion process.
//    /// </remarks>
//    /// <value>
//    /// </value>
//    /// <id guid="130108bf-d980-4753-b674-4d489acf485c" />
//    /// <owner alias="ROrleth" />
//    public byte[] getData() {
//		return this.data;
//	}
//
//	public void setData(byte[] value) {
//		this.data = value;
//	}
	
	
    private void readStandardAttributes(CTShape shape) {
//        this.id = shape.getId();
        this.id = shape.getVmlId();
    	
        this.pType = shape.getType(); 
        this.alt = shape.getAlt();
        this.style = shape.getStyle();
    }
    
    private void readDimensions(String style) {
        // E10: <v:shape style="width:428.25pt;height:321pt"
        // hmm, don't want a whole CSS parser just for this..
        // But if we did, it would be something like
		// CSSStyleDeclaration cssStyleDeclaration = = cssOMParser.parseStyleDeclaration(
		//			new org.w3c.css.sac.InputSource(new StringReader(styleVal)) );
    	
    	if (style==null) {
    		log.warn("can't read dimensions from null shape style");
       		return;
    	}
    	dimensions = new Dimensions();

        if (style.lastIndexOf("width")>=0) {
        	getStyleVal("width");
        }
        if (style.lastIndexOf("height")>=0) {
        	getStyleVal("height");
        }
    	
    }

    private void getStyleVal(String name) {
    	
    	// Assumptions: 1, the named attribute is present
    	//if (style.lastIndexOf(name)<0) return 0;
    	
    	// Assumptions: 2, the dimension is given in pt 
    	
        // E10: <v:shape style="width:428.25pt;height:321pt"
    	log.info("Looking for '" + name + "' in " + style);
    	//System.out.println("Looking for '" + name + "' in " + style);
    	// eg position:absolute;left:0;text-align:left;margin-left:320pt;
    	// margin-top:11.8pt;width:104.85pt;height:2in;z-index:251658240

    	int beginIndex = style.indexOf(name) + name.length()+1; // +1 for the ':'

//    	int oendIndex = style.indexOf("pt", beginIndex);
//    	String oval = style.substring(beginIndex, oendIndex);
//    	System.out.println("old:" + oval);
    	
    	if (beginIndex<0) {
    		// Not found
    		log.debug("No value for '" + name);
    		return;
    		// TODO
    	}
    	String val;
    	int endIndex = style.indexOf(";", beginIndex);
    	if (endIndex<0) {
    		// Last entry
    		val = style.substring(beginIndex);
    	} else {
    		val = style.substring(beginIndex, endIndex);
    	}
    	log.debug("val: " + val);
    	
    	String unit;
    	float f=0;
    	
    	if (val.endsWith("pt") ) {
    		f = Float.parseFloat(val.substring(0, val.length()-2));
    		unit="pt";
    		log.debug(f +"pt");    		
    	} else if (val.endsWith("in") ) {
    		f = Float.parseFloat(val.substring(0, val.length()-2));
    		unit="in";
    		log.debug(f + "in");    		

    	} else {
    		// Unknown units
    		unit="??";
    	}
    	//int endIndex = style.indexOf("pt", beginIndex);
    	
    	if (name.equals("height")) {
    		dimensions.height = Math.round(f);
    		dimensions.heightUnit = unit;
    	} else if (name.equals("width")) {
    		dimensions.width = Math.round(f);
    		dimensions.widthUnit = unit;
    	}
    	
    	// 72 points per inch
    	// so, assuming 72 dpi, there is 1 point per pixel
    	// so no further conversion is necessary
    	// All we need to do is set the units for XSL FO
    	
    	
    }
    
//    WordXmlPictureE10() {}
//    
//	public static void main(String[] args) throws Exception {
//		
//		WordXmlPictureE10 p = new WordXmlPictureE10(); 
//		p.style = "position:absolute;left:0;text-align:left;margin-left:320pt;margin-top:11.8pt;width:104.85pt;height:2in;z-index:251658240";
//		p.readDimensions(p.style);
//	}    
	
	
}

