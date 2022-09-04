package org.docx4j.openpackaging.parts.WordprocessingML;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * wp:docPr/@id must be unique across all parts in a docx,
 * otherwise Word can't open the docx.  This problem is 
 * difficult to track down if you aren't aware of it,
 * so this class attempts to keep track of Ids in use,
 * and dispense unique values.
 * 
 * There is one DocPropIdTracker per package.
 * 
 * Relies on Docx4jUnmarshallerListener so is susceptible
 * to those cases where that listener is not listening.
 * 
 * @author jharrop
 * @since 11.4.6
 */
public class DrawingPropsIdTracker {
	
	protected static final Logger log = LoggerFactory.getLogger(DrawingPropsIdTracker.class);
	
	/*
 		<w:drawing>
            <wp:inline >
                <wp:docPr  id="1" />
	 */
	// CTNonVisualDrawingProps nonvisualdrawingprops2 
	
	private Set<JaxbXmlPart> jaxbXmlParts = new HashSet<JaxbXmlPart>();  
	
	private Set<Long> usedIds = new HashSet<Long>(); 
	
	/**
	 * Keep track of parts which are yet to be unmarshalled.
	 * 
	 * @param part
	 */
	public void registerPart(Part part) {
		
//		log.info("Registering " + part.getPartName().getName());
		if (part instanceof JaxbXmlPart) {
			jaxbXmlParts.add((JaxbXmlPart)part);
		}
	}

	/**
	 * If its been unmarshalled, we'll already know 
	 * about any docPr ids it may contain, so
	 * forget about it.
	 * @param part
	 */
	public void deregisterPart(Part part) {
		
		if (part instanceof JaxbXmlPart) {
			markedForDereg.add((JaxbXmlPart)part);
			// avoid ConcurrentModificationException in jaxbXmlParts 
		}
	}
	
	public void registerId(long num) {
		
		if (usedIds.add(num)) {
			log.debug("registering docPr/@id " + num);
		} else {
			log.debug("already present? docPr/@id " + num);
		}
		
	}
	
	private long lastDispensed = 1;  // don't dispense 0!
	
	List<JaxbXmlPart> markedForDereg = new ArrayList<JaxbXmlPart>();
	
	/**
	 * If this throws an exception, you'll have to
	 * create an Id on your own.
	 * 
	 * @return
	 * @throws Docx4JException
	 */
	public long generateId() throws Docx4JException {
				
		if (!jaxbXmlParts.isEmpty()) {
			// force unmarshall so we get any ids in use
			for( JaxbXmlPart jaxbXmlPart : jaxbXmlParts ) {
				
				jaxbXmlPart.getContents();
				
				// remove it from the set, whether we were really able to detect ids or not,
				// since we don't want to keep doing this again and again.  ie reasonable efforts only
				// avoid ConcurrentModificationException:
				markedForDereg.add(jaxbXmlPart);
			}
			
			// now dereg.  Actually as things currently stand we
			// could just clear the set entirely.  
//			for(JaxbXmlPart jaxbXmlPart : markedForDereg ) {
//				jaxbXmlParts.remove(jaxbXmlPart);
//			}
			jaxbXmlParts.clear();
		}
		
		long newId = lastDispensed + 1;
		while (usedIds.contains((Long)newId)) {
			newId++;
		}
		
		lastDispensed=newId;
		usedIds.add(newId);
		return newId;
		
	}
	
	
}
