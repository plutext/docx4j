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


import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Numbering.Num;
import org.docx4j.wml.Numbering.Num.AbstractNumId;
import org.docx4j.wml.Numbering.Num.LvlOverride;
import org.docx4j.wml.Numbering.Num.LvlOverride.StartOverride;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;



public final class NumberingDefinitionsPart extends JaxbXmlPartXPathAware<Numbering> {
	
	private static Logger log = LoggerFactory.getLogger(NumberingDefinitionsPart.class);	
	
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
	
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {

		// NB if you add ignorable content, it is up to you to jaxbElement.setIgnorable correctly; see further McIgnorableNamespaceDeclarator
		// You don't need to worry about this if you are merely loading an existing part.
		namespacePrefixMapper.setMcIgnorable(
				this.getJaxbElement().getIgnorable() );
	}

	@Override
	public String getMceIgnorable() {
    	return this.getJaxbElement().getIgnorable();
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
    	Numbering numbering = getJaxbElement();
    	
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

        }

        // instantiate the list number definitions
        for( Numbering.Num numNode : numbering.getNum() )
        {
            ListNumberingDefinition listDef 
            	= new ListNumberingDefinition(numNode, abstractListDefinitions);

            instanceListDefinitions.put(listDef.getListNumberId(), listDef);
//            log.debug("Added list: " + listDef.getListNumberId() );
        }

    }
    
    public void resolveLinkedAbstractNum(StyleDefinitionsPart sdp) {
    	
    	if (sdp==null) {
    		log.warn("No StyleDefinitionsPart found");
    		return;
    	}
    	
        for (Numbering.AbstractNum abstractNum : getJaxbElement().getAbstractNum() ) {
        	
        	// <w:numStyleLink w:val="MyListStyle"/>
        	if (abstractNum.getNumStyleLink()==null) continue;
        	
        	// there is also abstractNum.getStyleLink(), but ignore that
        	
        	String numStyleId = abstractNum.getNumStyleLink().getVal();
        	
        	Style s = sdp.getStyleById(numStyleId);
        	if (s==null) {
        		log.warn("For w:numStyleLink, couldn't find style " + numStyleId);
        		continue;
        	}
        	if (s.getPPr()==null || s.getPPr().getNumPr()==null) {
        		log.warn("For w:numStyleLink, style " + numStyleId + " has no w:numPr");
        		continue;        		
        	}
        	
        	NumPr styleNumPr = s.getPPr().getNumPr();
        	
        	// Get the concrete list this point to
        	if (styleNumPr.getNumId()==null) {
        		log.warn("For w:numStyleLink, style " + numStyleId + " w:numPr has no w:numId");
        		continue;        		        		
        	}
        	BigInteger concreteListId = styleNumPr.getNumId().getVal();
        	
        	// Get the target abstract num
        	ListNumberingDefinition lnd = getInstanceListDefinitions().get(concreteListId.toString());
        	if (lnd==null) {
        		log.warn("No ListNumberingDefinition entry with ID " + concreteListId.toString());
        	}
        	Numbering.AbstractNum linkedNum = lnd.getAbstractListDefinition().getAbstractNumNode();

        	// OK, update
            AbstractListNumberingDefinition absNumDef 
            	= abstractListDefinitions.get(abstractNum.getAbstractNumId().toString());
            
            absNumDef.updateDefinitionFromLinkedStyle(linkedNum);
            
            // Also update the underlying abstract list
            if (abstractNum.getLvl().size()>0) {
            	log.warn("Cowardly refusing to overwrite existing List<Lvl>" );
            } else {
            	abstractNum.getLvl().clear();
            	abstractNum.getLvl().addAll(linkedNum.getLvl());
            		// This list is treated as a separate list by Word (ie its numbers are incremented
            		// independently), and this code honours that.
            }
            
            
            log.info("Updated abstract list def " + abstractNum.getAbstractNumId().toString() + " based on w:numStyleLink " + numStyleId );
        }
    	
    	
    }
    
    /**
     * For the given *concrete* list numId, restart the numbering on the specified
     * level at value val.  This is done by creating a new list (ie &lt;w:num&gt;)
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
    		throw new InvalidOperationException("Concrete/instance list " + numId + " does not exist");
    	}
    	
    	return restart(existingLnd.getNumNode(), ilvl, val);
    }
    

    private long restart(Num num, long ilvl, long val) 
    	throws InvalidOperationException {

    	if (num==null) {
    		throw new InvalidOperationException("Abstract List does not exist!");
    	}
    	
    	// (Ensure maps are initialised)
    	if (em == null ) { 
    		getEmulator();
    	}
    	
    	// Get the abstract list    	
    	BigInteger abstractNumIdVal = num.getAbstractNumId().getVal();
    	
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
		((Numbering)getJaxbElement()).getNum().add(newNum);
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
		    	
		return getEmulator(false);
	}

	
	/**
	 * @param reset
	 * @return
	 * @since 3.0.0
	 */
	public Emulator getEmulator(boolean reset) {
		
    	if (em == null 
    			|| reset) { 
    		initialiseMaps();
    		em = new Emulator();    		
    	}
		
		return em;
	}
	
	public Ind getInd(NumPr numPr) { //, StyleDefinitionsPart sdp, String styleId) {
		
		String ilvlString = "0";
		if (numPr.getIlvl()!=null) ilvlString = numPr.getIlvl().getVal().toString();
		
		if (numPr.getNumId()==null) {
            if(log.isWarnEnabled()) {
                log.warn("numPr without numId: " + XmlUtils.marshaltoString(numPr, true, true));
            }
						
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
		if (ll==null) {
			log.warn("No ListLevel defined for level " + ilvl + " in list " + numId);
			return null;
		}
		
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
			
			if (style==null) {
				log.error("Couldn't find style " + lvl.getPStyle().getVal());
				return null;
			}
			
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
	 * this associated ListNumberingDefinition (since that is
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
    	this.getJaxbElement().getAbstractNum().add(abstractNum);
    	
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
    	this.getJaxbElement().getNum().add(num);
    	
    	// Add it to our hashmap
        ListNumberingDefinition listDef = new ListNumberingDefinition(num, abstractListDefinitions);
        instanceListDefinitions.put(listDef.getListNumberId(), listDef);
        
        // 
    	return num;
		
	}

	public void addAbstractListNumberingDefinitionLevel(Numbering.AbstractNum abstractNum, Lvl lvl) {
		
		abstractNum.getLvl().add( lvl ); 
		
		// update the corresponding structure
		AbstractListNumberingDefinition absNumDef = abstractListDefinitions.get(abstractNum.getAbstractNumId().toString());
		absNumDef.readLevel(lvl);

	}
	
    public Numbering unmarshalDefaultNumbering() throws JAXBException {
    	    	    	 
		java.io.InputStream is = null;
		try {
			is = ResourceUtils.getResourceViaProperty(
					"docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart.DefaultNumbering",
					"org/docx4j/openpackaging/parts/WordprocessingML/numbering.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    		
    	
    	return unmarshal( is );    // side-effect is to set jaxbElement 	
    }

}
