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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.diagram.CTCxn;
import org.docx4j.dml.diagram.CTCxnList;
import org.docx4j.dml.diagram.CTElemPropSet;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.CTPtList;
import org.docx4j.dml.diagram.STPtType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.opendope.SmartArt.dataHierarchy.SibTransBody;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Images;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Images.Image;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Texts;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Texts.IdentifiedText;
import org.opendope.SmartArt.dataHierarchy.TextBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert dgm:dataModel to OpenDoPE SmartArtDataHierarchy
 * format.
 * 
 * @author jharrop
 */
public class DiagramDataUnflatten {
	
	private static Logger log = LoggerFactory.getLogger(DiagramDataUnflatten.class);	
	
	protected static String PRESNAME_FOR_IMAGE = "image"; 

	/* Generating SmartArtDataHierarchy xml from
	 * a real SmartArt drawing is an easy way for
	 * users to explore our format.
	 * 
	 * Generating SmartArtDataHierarchy xml from
	 * a real SmartArt drawing, and then using it
	 * to generate the SmartArt drawing again, is
	 * a good way to test things.
	 * 
	 */
	
	public DiagramDataUnflatten(DiagramDataPart diagramDataPart) {
		
		// Source structures
		this.diagramDataPart = diagramDataPart;
		
		ptList = diagramDataPart.getJaxbElement().getPtLst();		
		cxnList = diagramDataPart.getJaxbElement().getCxnLst();
		
		// Target structures
		factory = new org.opendope.SmartArt.dataHierarchy.ObjectFactory();		
		texts = factory.createSmartArtDataHierarchyTexts();
		images = factory.createSmartArtDataHierarchyImages();
		
		textFormats = new ArrayList<JAXBElement<CTTextBody>>();
		
		dmlFactory = new org.docx4j.dml.ObjectFactory();
		diagramFactory = new org.docx4j.dml.diagram.ObjectFactory();
	}
	
	private org.docx4j.dml.ObjectFactory dmlFactory;
	private org.docx4j.dml.diagram.ObjectFactory diagramFactory;
	
	// Source structures
	private DiagramDataPart diagramDataPart;
	private CTPtList ptList;
	private CTCxnList cxnList;	

	// Target structures
	private org.opendope.SmartArt.dataHierarchy.ObjectFactory factory;
	public org.opendope.SmartArt.dataHierarchy.ObjectFactory getDataHierarchyObjectFactory() {
		return factory;
	}

	private Texts texts;
	public Texts getTexts() {
		return texts;
	}
	private Images images;
	
	private List<JAXBElement<CTTextBody>> textFormats;
	public List<JAXBElement<CTTextBody>> getTextFormats() {
		return textFormats;
	}
	
	public SmartArtDataHierarchy convert() {
		
		SmartArtDataHierarchy smartArtDataHierarchy 
		= factory.createSmartArtDataHierarchy();

		CTPt docPt = ptList.getPt().get(0);
		
		if (docPt.getPrSet()!=null
				&& docPt.getPrSet().getLoTypeId()!=null) {

			smartArtDataHierarchy.setLoTypeId(
					docPt.getPrSet().getLoTypeId() );			
		} else {
			log.error("Couldn't read @loTypeId");
		}
		
		org.opendope.SmartArt.dataHierarchy.List docList = factory.createList();
		// dgm:pt[@type="doc" and @modelId="0"]
		org.opendope.SmartArt.dataHierarchy.ListItem listItem = factory.createListItem();
		docList.getListItem().add(listItem);
		
		listItem.setId(docPt.getModelId());
		listItem.setDepth(0);
		
		processChildrenOf(docPt, listItem);
		
		
		smartArtDataHierarchy.setList(docList);
		smartArtDataHierarchy.setImages(images);
		smartArtDataHierarchy.setTexts(texts);
		
		
		return smartArtDataHierarchy;
	}

	public CTTextBody processText(org.opendope.SmartArt.dataHierarchy.ListItem thisListItem,
			CTPt thisPoint) {
		
		CTTextBody textBody = thisPoint.getT();
		if (textBody!=null) {
			
			TextBody tb = getDataHierarchyObjectFactory().createTextBody();
			thisListItem.setTextBody(tb);
			
			for(CTTextParagraph p : textBody.getP() ) {
				
				if (!p.getEGTextRun().isEmpty()
						&& p.getEGTextRun().get(0) instanceof org.docx4j.dml.CTRegularTextRun 
						) {
					// TODO; assumes a single r child, which is
					// all we handle.  ie this model doesn't support
					// multiple runs, some of which formatted
					
					org.docx4j.dml.CTRegularTextRun run = (org.docx4j.dml.CTRegularTextRun)p.getEGTextRun().get(0);
					
					tb.getP().add( run.getT() );					
				}
			}
			
		}
		return textBody;
	}
	
	private void processChildrenOf(CTPt pt, org.opendope.SmartArt.dataHierarchy.ListItem listItem) {
				
		List<org.opendope.SmartArt.dataHierarchy.ListItem> childModelIds = createListItemsForChildren(pt);
		
		if (childModelIds.isEmpty()) return;
		
		org.opendope.SmartArt.dataHierarchy.List list = factory.createList();
		listItem.setList(list);		
		
		for (org.opendope.SmartArt.dataHierarchy.ListItem thisListItem : childModelIds) {
			
			
			list.getListItem().add(thisListItem);
			
			thisListItem.setDepth(listItem.getDepth()+1);
			
			String modelId = thisListItem.getId();
			
			// Find the pt
			CTPt thisPoint = getPoint(modelId);
			
			// attach its text
			CTTextBody textBody = processText(thisListItem, thisPoint);
			
			if (textFormats.size()<thisListItem.getDepth() ) {
				// we don't have a template for this level yet,
				// so add this one as the template for this level
				if (textBody==null) {
					// Add an empty one
					textFormats.add(
							diagramFactory.createT(
									dmlFactory.createCTTextBody()));
				} else {
					textFormats.add(
							diagramFactory.createT(textBody));
				}
				
			}
			
			// attach its image
			CTPt imgPt = getAssociatedPres(modelId, PRESNAME_FOR_IMAGE);
			if (imgPt!=null 
					&& imgPt.getSpPr()!=null 
					&& imgPt.getSpPr().getBlipFill()!=null 
					&& imgPt.getSpPr().getBlipFill().getBlip()!=null) {
				
				// Get the relId
				CTBlip blip = imgPt.getSpPr().getBlipFill().getBlip();				
				if (blip.getEmbed()!=null) {
					
					String relId = blip.getEmbed();
					//Relationship r = diagramDataPart.getRelationshipsPart().getRelationshipByID(relId);
					BinaryPartAbstractImage bpai = (BinaryPartAbstractImage)diagramDataPart.getRelationshipsPart().getPart(relId);

					// Add it
					Image image = factory.createSmartArtDataHierarchyImagesImage();
					image.setContentType(bpai.getContentType());
					image.setId(imgPt.getModelId());
					
					image.setValue(bpai.getBytes());
					
					images.getImage().add(image);
					
					// reference
					org.opendope.SmartArt.dataHierarchy.ImageRef imageRef = factory.createImageRef();
					imageRef.setContentRef(imgPt.getModelId());
					// Other attributes
					if (imgPt.getPrSet()!=null ) {
						CTElemPropSet props = imgPt.getPrSet();
						if (props.getCustLinFactNeighborX()!=null) {
							imageRef.setCustLinFactNeighborX(props.getCustLinFactNeighborX());
						}
						if (props.getCustLinFactNeighborY()!=null) {
							imageRef.setCustLinFactNeighborY(props.getCustLinFactNeighborY());
						}
						if (props.getCustScaleX()!=null) {
							imageRef.setCustScaleX(props.getCustScaleX());
						}
						if (props.getCustScaleY()!=null) {
							imageRef.setCustScaleY(props.getCustScaleY());
						}
					}
					
					thisListItem.setImageRef(imageRef);
					
				} else if  (blip.getLink()!=null) {
					// TODO
					// -check a linked image actually works in SmartArt.
					// -our export format could be extended to support linked images
					// -and/or we could fetch the image and embed it
					
				}
			}
			
			// attach its sibTrans text (if applicable)
			CTPt sibTrans = getPoint(thisListItem.getSibTransBody().getContentRef() );
			// Don't clutter up our export, if it doesn't contain content
			if (sibTrans.getT()!=null 
					&& sibTrans.getT().getP() !=null 
					&& sibTrans.getT().getP().get(0) !=null 
					&& sibTrans.getT().getP().get(0).getEGTextRun() !=null 
					&& !sibTrans.getT().getP().get(0).getEGTextRun().isEmpty() ) {
				
				IdentifiedText wrapper = factory.createSmartArtDataHierarchyTextsIdentifiedText();
				wrapper.setId(sibTrans.getModelId()); // = @sibTransContentRef
				wrapper.setT( sibTrans.getT() );
				texts.getIdentifiedText().add(wrapper);
			} else {
				// remove the reference
				thisListItem.setSibTransBody(null);
			}				
			
			// recurse
			processChildrenOf(thisPoint, thisListItem);
		}
	}

	public CTPt getPoint(String modelId) {
		
		for (CTPt pt : ptList.getPt()) {
			
			if (pt.getModelId().equals(modelId)) {
				return pt;
			}			
		}
		return null;
	}
	
	public CTPt getAssociatedPres(String modelId, String presNamePrefix) {
		
		for (CTPt pt : ptList.getPt()) {
			
			if (pt.getType().equals(STPtType.PRES) 
					&& pt.getPrSet()!=null
					&& pt.getPrSet().getPresName()!=null 
					&& pt.getPrSet().getPresName().startsWith(presNamePrefix)
					&& pt.getPrSet().getPresAssocID().equals(modelId)) {
				return pt;
			}			
		}
		return null;
	}
	
	public List<org.opendope.SmartArt.dataHierarchy.ListItem> createListItemsForChildren(CTPt parent) {
		
		List<org.opendope.SmartArt.dataHierarchy.ListItem> childListItemList 
			= new ArrayList<org.opendope.SmartArt.dataHierarchy.ListItem>();
		
		String parentId = parent.getModelId();
		
		for (CTCxn cxn : cxnList.getCxn() ) {
			
			if (cxn.getSrcId().equals(parentId) && !cxn.getSibTransId().equals("0") ) {
				
				// Create a corresponding ListItem
				org.opendope.SmartArt.dataHierarchy.ListItem thisListItem = factory.createListItem();
				thisListItem.setId(cxn.getDestId());
				
				// we'll manipulate this more later
				SibTransBody sibTransBody = factory.createSibTransBody();
				sibTransBody.setContentRef(cxn.getSibTransId());				
				thisListItem.setSibTransBody(sibTransBody); 

				childListItemList.add(thisListItem);
				//childModelIds.add(cxn.getDestId() );
			}
		}
		
		return childListItemList;
	}
	
	public static void main(String[] args) throws Exception {

		OpcPackage pkg = OpcPackage
				.load(new java.io.File(
						System.getProperty("user.dir")
						+ "/sample-docs/glox/extracted/apes.pptx"));
		

		DiagramDataPart thisPart = null;
		for (Entry<PartName,Part> entry : pkg.getParts().getParts().entrySet() ) {
			
			if (entry.getValue().getContentType().equals( ContentTypes.DRAWINGML_DIAGRAM_DATA )) {
				thisPart = (DiagramDataPart)entry.getValue();
				break;
			}
		}
		if (thisPart==null) {
			System.out.println("No SmartArt found in this docx.");
			return;	
		}
		
		thisPart.setFriendlyIds(thisPart.getJaxbElement());
		
//		System.out.println( XmlUtils.marshaltoString(thisPart.getJaxbElement(), true, true));

		// What does it look like in our format?
		DiagramDataUnflatten diagramDataUnflatten = new DiagramDataUnflatten(thisPart);
		String exchange= XmlUtils.marshaltoString(diagramDataUnflatten.convert(), true, true);
		System.out.println( exchange );		
//		PrintWriter out = new PrintWriter(System.getProperty("user.dir")
//				+ "/SmartArt/12hi.xml");
//		out.println(exchange);
//		out.flush();
//		out.close();
		
	}	
	
}
