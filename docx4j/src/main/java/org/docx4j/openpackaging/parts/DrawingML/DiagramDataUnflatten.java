/**
 * 
 */
package org.docx4j.openpackaging.parts.DrawingML;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.diagram.CTCxn;
import org.docx4j.dml.diagram.CTCxnList;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTPt;
import org.docx4j.dml.diagram.CTPtList;
import org.docx4j.dml.diagram.STPtType;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Images;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Texts;
import org.opendope.SmartArt.dataHierarchy.SmartArtDataHierarchy.Texts.IdentifiedText;

/**
 * Convert dgm:dataModel to OpenDoPE SmartArtDataHierarchy
 * format.
 * 
 * @author jharrop
 */
public class DiagramDataUnflatten {
	
	private static Logger log = Logger.getLogger(DiagramDataUnflatten.class);	

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
	
	public DiagramDataUnflatten(CTDataModel dataModel) {
		
		// Source structures
		ptList = dataModel.getPtLst();		
		cxnList = dataModel.getCxnLst();
		
		// Target structures
		factory = new org.opendope.SmartArt.dataHierarchy.ObjectFactory();		
		texts = factory.createSmartArtDataHierarchyTexts();
		images = factory.createSmartArtDataHierarchyImages();
		
	}
	
	// Source structures
	private CTPtList ptList;
	private CTCxnList cxnList;	

	// Target structures
	private org.opendope.SmartArt.dataHierarchy.ObjectFactory factory;
	private Texts texts;
	private Images images;
	
	public SmartArtDataHierarchy convert() {
		
		CTPt docPt = ptList.getPt().get(0);
		
		org.opendope.SmartArt.dataHierarchy.Node docNode = factory.createNode();
		docNode.setId(docPt.getModelId());
		
		processChildrenOf(docPt, docNode);
		
		SmartArtDataHierarchy smartArtDataHierarchy 
			= factory.createSmartArtDataHierarchy();
		
		smartArtDataHierarchy.setNode(docNode);
		smartArtDataHierarchy.setImages(images);
		smartArtDataHierarchy.setTexts(texts);
		
		return smartArtDataHierarchy;
	}

	private void processChildrenOf(CTPt pt, org.opendope.SmartArt.dataHierarchy.Node node) {
				
		List<String> childModelIds = findChildModelIds(pt);
		
		for (String modelId : childModelIds) {
			
			// Create a corresponding node
			org.opendope.SmartArt.dataHierarchy.Node thisNode = factory.createNode();
			thisNode.setId(modelId);
			
			node.getNode().add(thisNode);
			
			// Find the pt
			CTPt thisPoint = getPoint(modelId);
			
			// attach its text
			CTTextBody textBody = thisPoint.getT();
			if (textBody!=null) {
				IdentifiedText wrapper = factory.createSmartArtDataHierarchyTextsIdentifiedText();
				wrapper.setId(modelId); // just use that?
				wrapper.setT(textBody);
				texts.getIdentifiedText().add(wrapper);
				thisNode.setValRef(modelId);
			}
			
			// attach its image
			// TODO
			
			// attach its sibTrans text (if applicable)
			
			// recurse
			processChildrenOf(thisPoint, thisNode);
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
	
	public List<String> findChildModelIds(CTPt parent) {
		
		List<String> childModelIds = new ArrayList<String>();
		
		String parentId = parent.getModelId();
		
		for (CTCxn cxn : cxnList.getCxn() ) {
			
			if (cxn.getSrcId().equals(parentId) && !cxn.getSibTransId().equals("0") ) {
				
				// TODO:later, capture sibTransId
				
				//childModelIds.add(cxn.getSibTransId());
				childModelIds.add(cxn.getDestId() );
			}
		}
		
		return childModelIds;
	}
	
	
}
