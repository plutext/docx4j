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
				
		List<org.opendope.SmartArt.dataHierarchy.Node> childModelIds = createNodesForChildren(pt);
		
		for (org.opendope.SmartArt.dataHierarchy.Node thisNode : childModelIds) {
			
			
			node.getNode().add(thisNode);
			
			String modelId = thisNode.getId();
			
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
			CTPt sibTrans = getPoint(thisNode.getSibTransContentRef() );
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
				thisNode.setSibTransContentRef(null);
			}				
			
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
	
	public List<org.opendope.SmartArt.dataHierarchy.Node> createNodesForChildren(CTPt parent) {
		
		List<org.opendope.SmartArt.dataHierarchy.Node> childNodeList 
			= new ArrayList<org.opendope.SmartArt.dataHierarchy.Node>();
		
		String parentId = parent.getModelId();
		
		for (CTCxn cxn : cxnList.getCxn() ) {
			
			if (cxn.getSrcId().equals(parentId) && !cxn.getSibTransId().equals("0") ) {
				
				// Create a corresponding node
				org.opendope.SmartArt.dataHierarchy.Node thisNode = factory.createNode();
				thisNode.setId(cxn.getDestId());
				thisNode.setSibTransContentRef(cxn.getSibTransId()); // we'll manipulate this more later

				childNodeList.add(thisNode);
				//childModelIds.add(cxn.getDestId() );
			}
		}
		
		return childNodeList;
	}
	
	
}
