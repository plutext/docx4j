package org.docx4j.anon;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTHyperlink;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageTiffPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.vml.CTImageData;
import org.docx4j.wml.CTObject;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Pict;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will detect DrawingML or VML which does anything more than
 * link to a safe image (ie one we've replaced) 
 * 
 * @author jharrop
 *
 */
public class DmlVmlAnalyzer extends CallbackImpl {
	
	private static Logger log = LoggerFactory.getLogger(DmlVmlAnalyzer.class);
	
	private JaxbXmlPart sourcePart;
	public void setPart(JaxbXmlPart p) {
		this.sourcePart = p;
	}

	/**
	 * Objects we might not anonymise
	 */
	HashSet<Object> unsafeObjects = null;

	/**
	 * Objects it is interesting to note are present 
	 */
	HashSet<Object> inventoryObjects = null;

	HashSet<String> fieldsPresent = null;
	
	
	boolean containsVML;
	
	public void reinit() {
		unsafeObjects = new HashSet<Object>();
		inventoryObjects = new HashSet<Object>();
		
		containsVML = false;
		
		fieldsPresent = new HashSet<String>(); 
	}
	
	@Override
	public boolean shouldTraverse(Object o) {
		
		if (o instanceof org.docx4j.math.CTOMathPara) {
			// No effort is made to alter formula
			unsafeObjects.add(o);
			return false;
		}
		
		return true;
	}	
		

	@Override
	public List<Object> apply(Object o2) {
		
//		System.out.println(o.getClass().getName());
		
		if (o2 instanceof JAXBElement) {
			
			// record field instruction
			if (((JAXBElement)o2).getName().getLocalPart().equals("instrText")) {
				
				Text instr = (Text)XmlUtils.unwrap(o2);				
				fieldsPresent.add(instr.getValue());
				
				System.out.println(instr.getValue());
			}
		}

		Object o = XmlUtils.unwrap(o2);
		
		if (o instanceof org.docx4j.vml.CTImageData) {
			// remove its title
			((CTImageData)o).setTitle("foo");
			
			if ( ((CTImageData)o).getRelid()!=null ) {
            	
            	String rId = ((CTImageData)o).getRelid();
            	Part embeddedPart = sourcePart.getRelationshipsPart().getPart(rId);
            	if (embeddedPart instanceof ImagePngPart
    					|| embeddedPart instanceof ImageGifPart
    					|| embeddedPart instanceof ImageJpegPart
    					|| embeddedPart instanceof ImageBmpPart
    					|| embeddedPart instanceof ImageTiffPart	
    					// Others treated as unsafe
    					) {
            		// We've handled this
            		
            	} else {
            		// Unsafe, but noted elsewhere
            	}
			}
		} else if (o instanceof org.docx4j.math.CTOMathPara) {
			
			unsafeObjects.add(o.getClass().getName());
			
		} 
		
		return null;
	}

	
	public List<Object> getChildren(Object o2) {
					
		if (o2==null) {
			log.warn("null passed to getChildrenImpl");
			return null;
		}
		
		Object o = XmlUtils.unwrap(o2);

		log.debug("getting children of " + o.getClass().getName() );
		if (o instanceof org.docx4j.wml.Text) return null;
		
		// Short circuit for common elements
		if (o instanceof List) {
			// Handy if you have your own list of objects you wish to process
			return (List<Object>) o;
		} else if (o instanceof org.docx4j.wml.ContentAccessor) {
			return ((org.docx4j.wml.ContentAccessor) o).getContent();
		} else if (o instanceof org.docx4j.wml.SdtElement) {
			return ((org.docx4j.wml.SdtElement) o).getSdtContent().getContent();
			
		} else if (o instanceof org.docx4j.dml.wordprocessingDrawing.Anchor) {
			
			// Similar to wordprocessingDrawing.Inline below
			
			log.debug( sourcePart.getPartName().getName() + "\n"
					+ XmlUtils.marshaltoString(o, true, true, Context.jc, 
							"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "anchor", o.getClass()));
			
            org.docx4j.dml.wordprocessingDrawing.Anchor anchor = (org.docx4j.dml.wordprocessingDrawing.Anchor) o;
            List<Object> artificialList = new ArrayList<Object>();
            CTNonVisualDrawingProps drawingProps = anchor.getDocPr();
            if (drawingProps != null) {
                handleCTNonVisualDrawingProps(drawingProps, artificialList);
            }
            if (anchor.getGraphic() == null) {
    			log.warn("TODO: Handle case of no a:graphic: " + XmlUtils.marshaltoString(o, true, true, Context.jc, 
						"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "anchor", o.getClass()));
            } else {
                log.debug("found a:graphic");
                org.docx4j.dml.Graphic graphic = anchor.getGraphic();
                if (graphic.getGraphicData() == null) {
                	
        			log.warn("TODO: Handle case of no a:graphicData: " + XmlUtils.marshaltoString(o, true, true, Context.jc, "foo", "Inline", o.getClass()));    
                	
                } else {
                	List l = handleGraphicData(graphic.getGraphicData());
                	if (l!=null) {
                		artificialList.addAll(l);
                	}
                }
            }
            if (!artificialList.isEmpty())
                return artificialList;
            
        } else if (o instanceof org.docx4j.dml.wordprocessingDrawing.Inline) {
        	
        	// Done
			
			/*
				<ns34:Inline distR="0" distL="0" distB="0" distT="0" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:ns21="urn:schemas-microsoft-com:office:powerpoint" xmlns:ns23="http://schemas.microsoft.com/office/2006/coverPageProps" xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:odx="http://opendope.org/xpaths" xmlns:odgm="http://opendope.org/SmartArt/DataHierarchy" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:dgm="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:ns17="urn:schemas-microsoft-com:office:excel" xmlns:b="http://schemas.openxmlformats.org/officeDocument/2006/bibliography" xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:odi="http://opendope.org/components" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:ns34="foo" xmlns:ns9="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:ns32="http://schemas.openxmlformats.org/drawingml/2006/compatibility" xmlns:ns33="http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" xmlns:ns12="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:odq="http://opendope.org/questions" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:odc="http://opendope.org/conditions" xmlns:oda="http://opendope.org/answers">
				    <wp:extent cy="2362200" cx="3238500"/>
				    <wp:effectExtent b="0" r="0" t="0" l="19050"/>
				    <wp:docPr descr="C:\Documents and Settings\Jason Harrop\My Documents\tmp-test-docs\pangolin.jpeg" name="Picture 1" id="1"/>
				    <wp:cNvGraphicFramePr>
				        <a:graphicFrameLocks noChangeAspect="true"/>
				    </wp:cNvGraphicFramePr>
				    <a:graphic>
				        <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                			 */
        	
            org.docx4j.dml.wordprocessingDrawing.Inline inline = (org.docx4j.dml.wordprocessingDrawing.Inline) o;
            
            List<Object> artificialList = new ArrayList<Object>();
            CTNonVisualDrawingProps drawingProps = inline.getDocPr();
            if (drawingProps != null) {
            	// handle <wp:docPr descr="
                handleCTNonVisualDrawingProps(drawingProps, artificialList);
            }
            if (inline.getGraphic() == null) {
    			log.warn("TODO: Handle case of no a:graphic: " + XmlUtils.marshaltoString(o, true, true, Context.jc, "foo", "Inline", o.getClass()));                	
            } else {
                log.debug("found a:graphic");
                org.docx4j.dml.Graphic graphic = inline.getGraphic();
                if (graphic.getGraphicData() == null) {
                	
        			log.warn("TODO: Handle case of no a:graphicData: " + XmlUtils.marshaltoString(o, true, true, Context.jc, "foo", "Inline", o.getClass()));    
                	
                } else {
                	List l = handleGraphicData(graphic.getGraphicData());
                	if (l!=null) {
                		artificialList.addAll(l);
                	}
                }
                return artificialList;
            }
            if (!artificialList.isEmpty())
                return artificialList;

        } else if (o instanceof Pict) {
        	
        	/*
            <w:pict>
              <v:shapetype >
              :
              <v:shape  
                               	 */
        	
			log.debug(XmlUtils.marshaltoString(o));        	
        	
			return ((Pict)o).getAnyAndAny(); 
			
		} else if (o instanceof org.docx4j.dml.picture.Pic) { // Post 2.7.1; untested
			
			log.warn("TODO: " + XmlUtils.marshaltoString(o));			
			
			org.docx4j.dml.picture.Pic dmlPic = ((org.docx4j.dml.picture.Pic)o);
			if (dmlPic.getBlipFill()!=null
					&& dmlPic.getBlipFill().getBlip()!=null) {
					log.debug("found DML Blip");
					List<Object> artificialList = new ArrayList<Object>();
					artificialList.add(dmlPic.getBlipFill().getBlip());
					return artificialList;
			} else {
				return null;						
			}		
		} else if (o instanceof org.docx4j.dml.CTGvmlPicture) {  // Post 2.7.1
			
			log.warn("TODO: " + XmlUtils.marshaltoString(o));			
			
			org.docx4j.dml.CTGvmlPicture dmlPic = ((org.docx4j.dml.CTGvmlPicture)o);
			if (dmlPic.getBlipFill()!=null
					&& dmlPic.getBlipFill().getBlip()!=null) {
					log.debug("found DML Blip");
					List<Object> artificialList = new ArrayList<Object>();
					artificialList.add(dmlPic.getBlipFill().getBlip());
					return artificialList;
			} else {
				return null;						
			}		

		} else if (o instanceof org.docx4j.vml.CTShapetype ) {
			
			// NB, may not be triggered, depending on parent.
			
			/* eg
		          <v:shapetype id="_x0000_t75" coordsize="21600,21600" o:spt="75" o:preferrelative="t" 
		          				path="m@4@5l@4@11@9@11@9@5xe" filled="f" stroked="f">
		            <v:stroke joinstyle="miter"/>
		            <v:formulas>
		              <v:f eqn="if lineDrawn pixelLineWidth 0"/>
		              <v:f eqn="sum @0 1 0"/>
              			 */
			
			containsVML = true;
			
			// Generally nothing sensitive here
			log.debug( XmlUtils.marshaltoString(o, true, true, Context.jc, 
					Namespaces.VML, "shapetype", o.getClass()));
			inventoryObjects.add(o);
			
			return null;
			
		} else if (o instanceof org.docx4j.vml.CTShape) {

			containsVML = true;
			
			log.debug(XmlUtils.marshaltoString(o));
			
//				return ((org.docx4j.vml.CTShape)o).getAny();
			List<Object> artificialList = new ArrayList<Object>();
			for (JAXBElement<?> j : ((org.docx4j.vml.CTShape)o).getPathOrFormulasOrHandles() ) {
//					System.out.println(XmlUtils.unwrap(j).getClass().getName() );
				artificialList.add(j);				
			}
			return artificialList;
			
		} else if (o instanceof CTDataModel) {
			
			log.warn("TODO: " + XmlUtils.marshaltoString(o));
			
			CTDataModel dataModel = (CTDataModel)o;
			List<Object> artificialList = new ArrayList<Object>();
			// We're going to create a list merging two children ..			
			artificialList.addAll(dataModel.getPtLst().getPt());
			artificialList.addAll(dataModel.getCxnLst().getCxn());			
			return artificialList;
			
		} else if (o instanceof org.docx4j.dml.diagram2008.CTDrawing) {
			
			log.warn("TODO: " + XmlUtils.marshaltoString(o));
			
			return ((org.docx4j.dml.diagram2008.CTDrawing)o).getSpTree().getSpOrGrpSp();
			
		} else if (o instanceof org.docx4j.vml.CTTextbox) {		
			
			// We anon inside 
			/*
					<v:textbox style="mso-fit-shape-to-text:t">
						<w:txbxContent>
							<w:p			 */
			
			log.debug(XmlUtils.marshaltoString(o, true, true, Context.jc, 
					Namespaces.VML, "textbox", o.getClass()));    
			
			org.docx4j.vml.CTTextbox textBox = ((org.docx4j.vml.CTTextbox)o);
			if (textBox.getTxbxContent()==null) {
				return null;
			} else {
				return textBox.getTxbxContent().getEGBlockLevelElts();
				// grandchildren
			}
			
		} else if (o instanceof CTObject) {
			
			// w:object is not of itself interesting here
			log.debug(XmlUtils.marshaltoString(o, true, true, Context.jc, Namespaces.NS_WORD12, "object", o.getClass()));
			
			CTObject ctObject = (CTObject)o;
			List<Object> artificialList = new ArrayList<Object>();
			artificialList.addAll(ctObject.getAnyAndAny());
			if (ctObject.getControl()!=null) {
				artificialList.add(ctObject.getControl() ); // CTControl
			}
			return artificialList;
			
		} else if (o instanceof org.docx4j.dml.CTGvmlGroupShape) {
			
			log.warn("TODO: " + XmlUtils.marshaltoString(o));
			
			return ((org.docx4j.dml.CTGvmlGroupShape)o).getTxSpOrSpOrCxnSp();
			
		} else if(o instanceof FldChar) {

			// Interesting to analyse fields; we record instrText above
			
			FldChar fldChar = ((FldChar)o);
			List<Object> artificialList = new ArrayList<Object>();
			artificialList.add(fldChar.getFldCharType());
			if(fldChar.getFfData() != null) {
				artificialList.add(fldChar.getFfData());
			}
			if(fldChar.getFldData() != null) {
				artificialList.add(fldChar.getFldData());
			}
			if(fldChar.getNumberingChange() != null) {
				artificialList.add(fldChar.getNumberingChange());
			}
			return artificialList;
		}

		// OK, what is this? Use reflection ..
		// This should work for things including w:drawing
		log.debug(".. looking for method which returns list "  );
		try {
			Method[] methods = o.getClass().getDeclaredMethods();
			for (int i = 0; i<methods.length; i++) {
				Method m = methods[i];
				if (m.getReturnType().getName().equals("java.util.List") ) {					
					return (List<Object>)m.invoke(o);					
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug(".. no list member");
		return null;
	}
		
	private  List<Object> handleGraphicData(org.docx4j.dml.GraphicData graphicData) {
		
		/*
				        <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
				            <pic:pic>
				                <pic:nvPicPr>
				                    <pic:cNvPr descr="C:\Documents and Settings\Jason Harrop\My Documents\tmp-test-docs\pangolin.jpeg" name="Picture 1" id="0"/>
				                    <pic:cNvPicPr>
				                        <a:picLocks noChangeArrowheads="true" noChangeAspect="true"/>
				                    </pic:cNvPicPr>
				                </pic:nvPicPr>
				                <pic:blipFill>
				                    <a:blip r:embed="rId8" cstate="print"/>
				                    <a:srcRect/>
				                    <a:stretch>
				                        <a:fillRect/>
				                    </a:stretch>
				                </pic:blipFill>		 */
		
        List<Object> tmpArtificialList = new ArrayList<Object>();
        if (graphicData.getPic() != null) {
            //GraphicData can have a hyperlink reference, which can be found this way
            CTNonVisualDrawingProps picNonVisual = graphicData.getPic().getNvPicPr().getCNvPr();
            if (picNonVisual != null) {
                handleCTNonVisualDrawingProps(picNonVisual, tmpArtificialList);
            }
        }
        // Its not graphicData.getAny() we're typically interested in
        if (graphicData.getPic() != null && graphicData.getPic().getBlipFill() != null
                && graphicData.getPic().getBlipFill().getBlip() != null) {
        	
            CTBlip blip = graphicData.getPic().getBlipFill().getBlip();
            if (blip.getLink()!=null) {
            	// Assume OK.  Either its on the public internet, or its inaccessible.
            	log.debug("blip contained a link .. assumed ok");
            } else if (blip.getEmbed()!=null ) {
            	
            	String rId = blip.getEmbed();
            	Part embeddedPart = sourcePart.getRelationshipsPart().getPart(rId);
            	if (embeddedPart instanceof ImagePngPart
    					|| embeddedPart instanceof ImageGifPart
    					|| embeddedPart instanceof ImageJpegPart
    					|| embeddedPart instanceof ImageBmpPart
    					|| embeddedPart instanceof ImageTiffPart	
    					// Others treated as unsafe
    					) {
            		
            		// We've handled this
            		
            	} else {
            		
            		// Unsafe, but noted elsewhere
            		
            	}
            	
            }
			
        	return null;
        } else {
        	
        	// Unsafe; Charts and other stuff is in here
        	addUnsafe(graphicData, "http://schemas.openxmlformats.org/drawingml/2006/main", "graphicData", org.docx4j.dml.GraphicData.class);
        	
            return graphicData.getAny();
        }
    }

	private void addUnsafe(Object o, 
			String uri, String local, Class declaredType)  {

		// For now, we'll use marshalled content
		unsafeObjects.add(XmlUtils.marshaltoString(o, true, true, Context.jc, 
				uri, local, declaredType));
	}
	
//	private void addUnsafe(Object o) {
//		
//		// For now, we'll use marshalled content
//		unsafeObjects.add(XmlUtils.marshaltoString(o));
//	}
	
	 
	/**
	 * There can be hyperlinks references in CTNonVisualDrawingProps.
	 * @param drawingProps
	 * @param artificialList
	 */
	private  void handleCTNonVisualDrawingProps(CTNonVisualDrawingProps drawingProps, List<Object> artificialList){
      if (drawingProps != null) {
    	  
		    // <wp:docPr descr="C:\Documents and Settings\Jason Harrop\My Documents\tmp-test-docs\pangolin.jpeg" 
    	  	// name="Picture 1" id="1"/>
    	  
    	  if (drawingProps.getDescr()!=null) {
    		  drawingProps.setDescr(null);
    	  }
    	  // Name is probably ok
    	  
    	  
          CTHyperlink docPrHyperLink = drawingProps.getHlinkClick();
          if (docPrHyperLink != null)
              artificialList.add(docPrHyperLink);
      } 
	}	
	
    
}
