package org.docx4j.finders;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

/**
 * Find w:instrText piece of complex field.
 * 
 * @author jharrop
 * @since 11.5.4
 */
public class InstrTextFinder extends CallbackImpl {
	
	// Example:
	// <w:instrText xml:space="preserve"> SEQ Figure \* ARABIC </w:instrText>
	// Object factory:
	// JAXBElement<Text>(_RInstrText_QNAME, Text.class, R.class, value)
	  
	private static Logger log = LoggerFactory.getLogger(InstrTextFinder.class);
		
	public List<Object> results = new ArrayList<Object>(); 
	
	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof jakarta.xml.bind.JAXBElement) {	
			
			jakarta.xml.bind.JAXBElement jbe = (jakarta.xml.bind.JAXBElement)o;
			if (jbe.getName().getLocalPart().equals("instrText")) {
				results.add(o);
			}
			
		}
		return null;
	}
	
	@Override // since sensitive to JAXBElement name
	public void walkJAXBElements(Object parent) {
		
		List children = getChildren(parent);
		if (children != null) {

			for (Object o : children) {
				
				this.apply(o);

				if (this.shouldTraverse(o)) {
					walkJAXBElements(o);
				}

			}
		}
	}
		
}
