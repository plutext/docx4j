package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.TransformState;
import org.docx4j.wml.STFldCharType;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class InField  implements TransformState {
	
	protected static Logger log = Logger.getLogger(InField.class);		
	
	boolean inField = false;
	
	public static void updateState( 
			HashMap<String, TransformState> modelStates,
			NodeIterator fldCharNodeIt) {

		InField state = (InField)modelStates.get(Conversion.FIELD_TRACKER);
		
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
				state.inField= true;
			} else if (fieldCharType==STFldCharType.END) {
				state.inField= false;
			} 
			// else ignore STFldCharType.SEPARATE 			
		}
		
	}
	
	public static boolean getState( HashMap<String, TransformState> modelStates) {
		
		return ((InField)modelStates.get(Conversion.FIELD_TRACKER)).inField;
	}
}
