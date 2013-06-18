package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.STFldCharType;

	
public class ComplexFieldLocator extends CallbackImpl {
	
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

	P currentP;
	int depth=0;

	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof P) {
			currentP = (P)o;
		}
		
		if (o instanceof org.docx4j.wml.FldChar) {
			FldChar fldChar = (FldChar)o;
			if (fldChar.getFldCharType().equals(STFldCharType.BEGIN) ) {
				//System.out.println("Found a BEGIN");
				depth++;
				if (depth==1 && !starts.contains(currentP)) {
					starts.add(currentP);
				}
			}
			if (fldChar.getFldCharType().equals(STFldCharType.END) ) {
				depth--;
			}
		}
		
		return null;
	}
}
	

