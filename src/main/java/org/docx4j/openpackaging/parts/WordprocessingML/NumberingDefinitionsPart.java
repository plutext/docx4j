/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.WordprocessingML;


import java.math.BigInteger;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Numbering.Num;
import org.docx4j.wml.Numbering.Num.AbstractNumId;
import org.docx4j.wml.Numbering.Num.LvlOverride;
import org.docx4j.wml.Numbering.Num.LvlOverride.StartOverride;
import org.docx4j.wml.PPrBase.Ind;



public final class NumberingDefinitionsPart extends JaxbXmlPart {
	
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
		return abstractListDefinitions;
	}

    HashMap<String, ListNumberingDefinition> instanceListDefinitions; 
	public HashMap<String, ListNumberingDefinition> getInstanceListDefinitions() {
		return instanceListDefinitions;
	}
	
    public void initialiseMaps()
    {
    	Numbering numbering = (Numbering)jaxbElement;
    	
        // count the number of different list numbering schemes
    	if (numbering.getNum().size() == 0)
        {
            return;
        }
        
        // initialize the abstract number list
        abstractListDefinitions 
        	= new HashMap<String, AbstractListNumberingDefinition>(numbering.getAbstractNum().size() );
                
        // initialize the instance number list
        instanceListDefinitions 
        	= new HashMap<String, ListNumberingDefinition>( numbering.getAbstractNum().size() );
        		// Hmm, shouldn't the size be based on the number of instance nums?

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
        //foreach (XmlNode numNode in numberNodes)
        for( Numbering.Num numNode : numbering.getNum() )
        {
            ListNumberingDefinition listDef 
            	= new ListNumberingDefinition(numNode, abstractListDefinitions);

            instanceListDefinitions.put(listDef.getListNumberId(), listDef);
        }

    }
    
    /**
     * For the given list numId, restart the numbering on the specified
     * level at value val.  This is done by creating a new list.
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
    	
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			// TODO - have a static wmlFactory defined in Base? 
		
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

	public Ind getInd(String numId, String ilvl) {

		// Operating on the docx4j.listnumbering plane,
		// not the JAXB plane..
		ListNumberingDefinition lnd = instanceListDefinitions.get(numId);
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
     * Unmarshal XML data from the specified InputStream and return the 
     * resulting content tree.  Validation event location information may
     * be incomplete when using this form of the unmarshal API.
     *
     * <p>
     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
     * 
     * @param is the InputStream to unmarshal XML data from
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
    public Object unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
		    		    
			Unmarshaller u = jc.createUnmarshaller();
			
//			javax.xml.validation.SchemaFactory sf = 
//				javax.xml.validation.SchemaFactory.newInstance(
//				      javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//
//			javax.xml.validation.Schema schema = sf.newSchema(new java.io.File("/home/jharrop/workspace200711/docx4j-001/src/main/resources/wml-local-subset.xsd"));			

			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			
			//u.setValidating( false );
			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
						
			jaxbElement = u.unmarshal( is );
			
			log.info("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
    }




	
	
}
