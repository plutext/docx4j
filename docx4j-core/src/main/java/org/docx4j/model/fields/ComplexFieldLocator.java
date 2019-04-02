package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.STFldCharType;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

	
public class ComplexFieldLocator extends CallbackImpl {
	
	private static Logger log = LoggerFactory.getLogger(ComplexFieldLocator.class);
	
	/**
	 * A list of paragraphs containing field begins.
	 * 
	 * If the paragraph contains 2 fields or nested fields, 
	 * it will still be listed just once
	 */
	List<P> starts = new ArrayList<P>();	
	public List<P> getStarts() {
		return starts;
	}

//	P currentP;
	int depth=0;

	@Override
	public List<Object> apply(Object o) {

//		System.out.println(o.getClass().getName());
		
//		if (o instanceof P) {
//			currentP = (P)o;
//		}
		
		if (o instanceof org.docx4j.wml.FldChar) {
			FldChar fldChar = (FldChar)o;
			if (fldChar.getFldCharType().equals(STFldCharType.BEGIN) ) {
				//System.out.println("Found a BEGIN");
				depth++;
				P currentP = pStack.peek();
				if (depth==1 && !starts.contains(currentP)) {
					starts.add(currentP);
//					System.out.println("Adding " + XmlUtils.marshaltoString(currentP));
				}
			}
			if (fldChar.getFldCharType().equals(STFldCharType.END) ) {
				depth--;
			}
		}
		
		return null;
	}
	
	/*
	 * Need a paragraph stack, to accommodate:
	 * 
	 *         <w:p >
		            <w:r>
		                <w:pict>
		                    <v:shape stroked="f" o:spid="_x0000_s1026" id="Text Box 2" style="position:absolute;left:0;text-align:left;margin-left:383.8pt;margin-top:-19.9pt;width:117.35pt;height:25.25pt;z-index:251658240;visibility:visible;mso-wrap-style:square;mso-width-percent:0;mso-height-percent:0;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:absolute;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:0;mso-height-percent:0;mso-width-relative:page;mso-height-relative:page;v-text-anchor:top" type="#_x0000_t202" o:gfxdata="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">
		                        <v:textbox>
		                            <w:txbxContent>
		                                <w:p>
                                    
	 * 
	 */
	private LinkedList<P> pStack = new LinkedList<P>(); 
	
	@Override // so we can manage the stack
	public void walkJAXBElements(Object parent) {
		
		if (parent instanceof P) {
			pStack.push((P)parent);
		}

		super.walkJAXBElements(parent);
		
		if (parent instanceof P) {
			pStack.pop();
		}
		
	}	
}
	

