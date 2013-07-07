package org.docx4j.convert.out.pdf.viaXSLFO;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.STFldCharType;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class InField {
	protected static Logger log = LoggerFactory.getLogger(InField.class);		
	
	private boolean inField = false;
	
	public void updateState(NodeIterator fldCharNodeIt) {
    	org.docx4j.wml.FldChar field = null;
    	
    	Node node = fldCharNodeIt.nextNode();
    	
		try {
			field = (org.docx4j.wml.FldChar)XmlUtils.unmarshal(
						node, 
						Context.jc, 
						org.docx4j.wml.FldChar.class);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}			
		
		STFldCharType fieldCharType = field.getFldCharType();
		
		if (fieldCharType==null) {
			
			log.debug("Ignoring unrecognised: " + XmlUtils.w3CDomNodeToString(node));
			
		} else {
			
			if (fieldCharType==STFldCharType.BEGIN) {
				inField= true;
			} else if (fieldCharType==STFldCharType.END) {
				inField= false;
			} 
			// else ignore STFldCharType.SEPARATE 			
		}
		
	}
	
	public boolean getState() {
		return inField;
	}
}
