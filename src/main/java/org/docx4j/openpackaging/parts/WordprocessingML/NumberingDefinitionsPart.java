/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.openpackaging.parts.WordprocessingML;


import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Numbering.Num;
import org.docx4j.wml.Numbering.Num.AbstractNumId;
import org.docx4j.wml.Numbering.Num.LvlOverride;
import org.docx4j.wml.Numbering.Num.LvlOverride.StartOverride;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;



public final class NumberingDefinitionsPart extends JaxbXmlPart<Numbering> {
	
	private static Logger log = Logger.getLogger(NumberingDefinitionsPart.class);	
	
	public NumberingDefinitionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
	public NumberingDefinitionsPart() throws InvalidFormatException {
		super(new PartName("/word/numbering.xml"));
		init();
	}

	public void init() {	
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_NUMBERING));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.NUMBERING);
	}
	
    HashMap<String, AbstractListNumberingDefinition> abstractListDefinitions; 
	public HashMap<String, AbstractListNumberingDefinition> getAbstractListDefinitions() {
		if (abstractListDefinitions==null) initialiseMaps();
		return abstractListDefinitions;
	}

    HashMap<String, ListNumberingDefinition> instanceListDefinitions; 
	public HashMap<String, ListNumberingDefinition> getInstanceListDefinitions() {
		
		if (instanceListDefinitions==null) initialiseMaps();
		
		return instanceListDefinitions;
	}
	
    public void initialiseMaps()
    {
    	Numbering numbering = jaxbElement;
    	
        // count the number of different list numbering schemes
    	if (numbering.getNum().size() == 0)
        {
    		log.debug("No num defined");
    		// Don't return; init empty lists.
        }
        
        // initialize the abstract number list
        abstractListDefinitions 
        	= new HashMap<String, AbstractListNumberingDefinition>(numbering.getAbstractNum().size() );
                
        // initialize the instance number list
        instanceListDefinitions 
        	= new HashMap<String, ListNumberingDefinition>( numbering.getNum().size() );

        // store the abstract list type definitions
        for (Numbering.AbstractNum abstractNumNode : numbering.getAbstractNum() )
        {
            AbstractListNumberingDefinition absNumDef 
            	= new AbstractListNumberingDefinition(abstractNumNode);

            abstractListDefinitions.put(absNumDef.getID(), absNumDef);

            // now go through the abstract list definitions and update those that are linked to other styles
            if (absNumDef.hasLinkedStyle() )
            {
//                String linkStyleXPath = "/w:document/w:numbering/w:abstractNum/w:styleLink[@w:val=\"" + absNumDef.Value.LinkedStyleId + "\"]";
//                XmlNode linkedStyleNode = mainDoc.SelectSingleNode(linkStyleXPath, nsm);
//
//                if (linkedStyleNode != null)
//                {
//                    absNumDef.Value.UpdateDefinitionFromLinkedStyle(linkedStyleNode.ParentNode, nsm);
//                }
                
                // find the linked style
                // TODO - review
                absNumDef.UpdateDefinitionFromLinkedStyle(abstractNumNode);
            }
        }

        // instantiate the list number definitions
        for( Numbering.Num numNode : numbering.getNum() )
        {
            ListNumberingDefinition listDef 
            	= new ListNumberingDefinition(numNode, abstractListDefinitions);

            instanceListDefinitions.put(listDef.getListNumberId(), listDef);
            log.debug("Added list: " + listDef.getListNumberId() );
        }

    }
    
    /**
     * For the given list numId, restart the numbering on the specified
     * level at value val.  This is done by creating a new list (ie <w:num>)
     * which uses the existing w:abstractNum.
     * @param numId
     * @param ilvl
     * @param val
     * @return 
     */
    public long restart(long numId, long ilvl, long val) 
    	throws InvalidOperationException {
    	
    	// Find the abstractNumId
    	
    	// (Ensure maps are initialised)
    	if (em == null ) { 
    		getEmulator();
    	}
    	ListNumberingDefinition existingLnd = instanceListDefinitions.get( Long.toString(numId) );
    	if (existingLnd==null) {
    		throw new InvalidOperationException("List " + numId + " does not exist");
    	}
    	BigInteger abstractNumIdVal = existingLnd.getNumNode().getAbstractNumId().getVal();
    	
    	// Generate the new <w:num
    	long newNumId = instanceListDefinitions.size() + 1;
    	
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		
		Num newNum = factory.createNumberingNum();
		newNum.setNumId( BigInteger.valueOf(newNumId) );
		AbstractNumId abstractNumId = factory.createNumberingNumAbstractNumId();
		abstractNumId.setVal(abstractNumIdVal);
		newNum.setAbstractNumId(abstractNumId);
		
		LvlOverride lvlOverride = factory.createNumberingNumLvlOverride();
		lvlOverride.setIlvl(BigInteger.valueOf(ilvl));
		newNum.getLvlOverride().add(lvlOverride);
		
		StartOverride start = factory.createNumberingNumLvlOverrideStartOverride();
		start.setVal(BigInteger.valueOf(val));
		lvlOverride.setStartOverride(start);
    	
    	// Add it to the jaxb object and our hashmap
		((Numbering)jaxbElement).getNum().add(newNum);
        ListNumberingDefinition listDef 
    		= new ListNumberingDefinition(newNum, abstractListDefinitions);
        instanceListDefinitions.put(listDef.getListNumberId(), listDef);		
    	
    	// Return the new numId
    	return newNumId;
    	
    }
	
	
	private Emulator em;
//	public void setEmulator(Emulator em) {
//		this.em = em;
//	}
	public Emulator getEmulator() {
		
    	if (em == null ) { 
    		initialiseMaps();
    		em = new Emulator();    		
    	}
		
		return em;
	}

	public Ind getInd(NumPr numPr) { //, StyleDefinitionsPart sdp, String styleId) {
		
		String ilvlString = "0";
		if (numPr.getIlvl()!=null) ilvlString = numPr.getIlvl().getVal().toString();
		
		if (numPr.getNumId()==null) {
			log.warn("numPr without numId: " + XmlUtils.marshaltoString(numPr, true, true));
						
			return null;
		} else {		
			return getInd(numPr.getNumId().getVal().toString(), ilvlString );
		}
	}
	
	public Ind getInd(String numId, String ilvl) {

		// Operating on the docx4j.listnumbering plane,
		// not the JAXB plane..
		ListNumberingDefinition lnd = getInstanceListDefinitions().get(numId );
		if (lnd==null) {
			log.debug("couldn't find list for numId: " + numId);
			return null;
		}
		if (ilvl==null) ilvl = "0";
		ListLevel ll = lnd.getLevel(ilvl);
		
		// OK, now on the JAXB plane
		Lvl jaxbOverrideLvl = ll.getJaxbOverrideLvl();
		
		log.debug("Looking at override/instance definition..");
		if (jaxbOverrideLvl!=null) {
			
			Ind ind = getIndFromLvl(jaxbOverrideLvl);
			if (ind!=null) {
				log.debug("Got it..");
				return ind;
			}
		}
		
		// Now do the same for the abstract definition
		log.debug("Looking at abstract definition..");
		Lvl abstractLvl = ll.getJaxbAbstractLvl();
		Ind ind = getIndFromLvl(abstractLvl);
		
		return ind;
	}
	
	private Ind getIndFromLvl(Lvl lvl) {
		
		// If there is a style reference in the instance,
		// as a sibling of pPr,
		// use any w:ind in it (or TODO styles it is based on)
		if (lvl.getPStyle()!=null) {
			
			// Get the style
//			StyleDefinitionsPart stylesPart = ((WordprocessingMLPackage)this.getPackage()).
//				getMainDocumentPart().getStyleDefinitionsPart();
			PropertyResolver propertyResolver 
				= ((WordprocessingMLPackage)this.getPackage()).getMainDocumentPart().getPropertyResolver(); 
			
			log.debug("override level has linked style: " + lvl.getPStyle().getVal() );
			
			org.docx4j.wml.Style style = propertyResolver.getStyle( lvl.getPStyle().getVal() );
			
			// If the style has a w:ind, return it.
			// Otherwise, continue
			if (style.getPPr() != null
					&& style.getPPr().getInd()!=null ) {
				return style.getPPr().getInd();
			}
		}
		
		// If there is a style reference in pPr,
		// but not also one as a sibling of pPr,
		// then no number appears at all!
		
			// TODO: throw ShouldNotBeNumbered??
		
		// If there is a w:ind in the instance use that
		if ( lvl.getPPr()!=null
				&& lvl.getPPr().getInd() !=null ) {
			return lvl.getPPr().getInd();
		}
		
		return null;		
		
	}
	
	/**
	 * Add the specified definition, allocating it a new w:abstractNumId.
	 * 
	 * Also create and add an associated ListNumberingDefinition, and return
	 * the w:numId of this associated ListNumberingDefinition (since that is
	 * what you are likely to use in the document). 
	 * 
	 * @param abstractNum
	 * @return
	 */
	public Numbering.Num addAbstractListNumberingDefinition(Numbering.AbstractNum abstractNum) {
		
		//////////////////////////////////////////////
		// Numbering.AbstractNum abstractNum
		
		// Generate a unique w:abstractNumId for it
		int nextId = getAbstractListDefinitions().size();		
    	do {
    		nextId++;    		
    	} while (getAbstractListDefinitions().containsKey( "" + nextId ));    	
    	abstractNum.setAbstractNumId( BigInteger.valueOf(nextId) );
    	
    	// Add it to our JAXB object
    	this.jaxbElement.getAbstractNum().add(abstractNum);
    	
    	// Add it to our hashmap
        AbstractListNumberingDefinition absNumDef = new AbstractListNumberingDefinition(abstractNum);
        abstractListDefinitions.put(absNumDef.getID(), absNumDef);

		//////////////////////////////////////////////
		// Numbering.Num num
        
        // Now make an associated ListNumberingDefinition
		//	<w:num w:numId="1">
		//	  <w:abstractNumId w:val="1"/>
		//	</w:num>"        
        Numbering.Num num = Context.getWmlObjectFactory().createNumberingNum();
        Numbering.Num.AbstractNumId abstractNumId = Context.getWmlObjectFactory().createNumberingNumAbstractNumId();
        abstractNumId.setVal(BigInteger.valueOf(nextId) );
        num.setAbstractNumId(abstractNumId);
        
        nextId = getInstanceListDefinitions().size();		
    	do {
    		nextId++;    		
    	} while (getInstanceListDefinitions().containsKey( "" + nextId ));    	
    	num.setNumId( BigInteger.valueOf(nextId) );  
    	
    	// Add it to our JAXB object
    	this.jaxbElement.getNum().add(num);
    	
    	// Add it to our hashmap
        ListNumberingDefinition listDef = new ListNumberingDefinition(num, abstractListDefinitions);
        instanceListDefinitions.put(listDef.getListNumberId(), listDef);
        
        // 
    	return num;
		
	}
	
    public Numbering unmarshalDefaultNumbering() throws JAXBException {
    	    	    	 
		java.io.InputStream is = null;
		try {
			is = org.docx4j.utils.ResourceUtils.getResource(
					"org/docx4j/openpackaging/parts/WordprocessingML/numbering.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    		
    	
    	return unmarshal( is );    // side-effect is to set jaxbElement 	
    }

}
