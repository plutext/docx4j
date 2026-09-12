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
import org.docx4j.model.listnumbering.NumberingState;
import org.docx4j.openpackaging.exceptions.Docx4JException;
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

import jakarta.xml.bind.JAXBException;
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
	
	private boolean numStyleLinkIsPresent = false;

	private StyleDefinitionsPart getStyleDefinitionsPart() {
		return ((WordprocessingMLPackage)this.getPackage()).getMainDocumentPart().getStyleDefinitionsPart();
	}
	
	public void initialiseMaps()
    {
    	initialiseMaps(false);
    	if (numStyleLinkIsPresent) {
    		resolveLinkedAbstractNum();
        	log.info("Having encountered NumStyleLink, now performing second pass");
    		initialiseMaps(true);
    	}
    }

    
	
    public void initialiseMaps(boolean resolveNumStyleLink) {

    	Numbering numbering = getJaxbElement();
    	
        // count the number of different list numbering schemes
    	if (numbering.getNum().size() == 0)
        {
    		log.debug("No num defined");
    		// Don't return; init empty lists.
        }
        
    	
    	boolean needSecondPass = false;
    	if (/* pass 1 */ !resolveNumStyleLink) {
	        // initialize the abstract number list
	        abstractListDefinitions 
	        	= new HashMap<String, AbstractListNumberingDefinition>(numbering.getAbstractNum().size() );
	                
	        // initialize the instance number list
	        instanceListDefinitions 
	        	= new HashMap<String, ListNumberingDefinition>( numbering.getNum().size() );
	
	        // store the abstract list type definitions
	        for (Numbering.AbstractNum abstractNumNode : numbering.getAbstractNum() )
	        {
	        	AbstractListNumberingDefinition absNumDef = new AbstractListNumberingDefinition(abstractNumNode);
	            abstractListDefinitions.put(absNumDef.getID(), absNumDef);
	            
	            if (abstractNumNode.getNumStyleLink()!=null) {
	            	// set flag
	            	log.info("Encountered NumStyleLink, will need second pass");
	            	needSecondPass = true;
	            }
	        }
    	}
        
        // instantiate the list number definitions
        for( Numbering.Num numNode : numbering.getNum() )
        {
            ListNumberingDefinition listDef 
            	= new ListNumberingDefinition(numNode, abstractListDefinitions, resolveNumStyleLink);

            listDef.setDefaultStateSupplier(this::getNumberingState);
            instanceListDefinitions.put(listDef.getListNumberId(), listDef); // on pass 2, this overwrites the existing instance list
//            log.debug("Added list: " + listDef.getListNumberId() );
        }

    	numStyleLinkIsPresent = needSecondPass;
        
    }
    
    private void resolveLinkedAbstractNum() {
    	
    	
    	if (getStyleDefinitionsPart()==null) {
    		log.warn("No StyleDefinitionsPart found");
    		return;
    	}
    	
        for (Numbering.AbstractNum abstractNum : getJaxbElement().getAbstractNum() ) {
        	
            AbstractListNumberingDefinition absNumDef 
            	= abstractListDefinitions.get(abstractNum.getAbstractNumId().toString());
        	
        	resolveLinkedAbstractNum(abstractNum, absNumDef );
        }
    }

    public void resolveLinkedAbstractNum(Numbering.AbstractNum abstractNum, AbstractListNumberingDefinition absNumDef ) {
        	
    	// <w:numStyleLink w:val="MyListStyle"/>
    	if (abstractNum.getNumStyleLink()==null) return;
    	
    	// there is also abstractNum.getStyleLink(), but ignore that
    	
    	String numStyleId = abstractNum.getNumStyleLink().getVal();
    	
    	Style s = getStyleDefinitionsPart().getStyleById(numStyleId);
    	if (s==null) {
    		log.warn("For w:numStyleLink, couldn't find style " + numStyleId);
    		return;
    	}
    	if (s.getPPr()==null || s.getPPr().getNumPr()==null) {
    		log.warn("For w:numStyleLink, style " + numStyleId + " has no w:numPr");
    		return;        		
    	}
    	
    	NumPr styleNumPr = s.getPPr().getNumPr();
    	
    	// Get the concrete list this point to
    	if (styleNumPr.getNumId()==null) {
    		log.warn("For w:numStyleLink, style " + numStyleId + " w:numPr has no w:numId");
    		return;        		        		
    	}
    	BigInteger concreteListId = styleNumPr.getNumId().getVal();
    	
    	// Get the target abstract num
    	ListNumberingDefinition lnd = getInstanceListDefinitions().get(concreteListId.toString());
    	if (lnd==null) {
    		log.warn("No ListNumberingDefinition entry with ID " + concreteListId.toString());
    	}
    	Numbering.AbstractNum linkedNum = lnd.getAbstractListDefinition().getAbstractNumNode();

    	// OK, update
//        AbstractListNumberingDefinition absNumDef 
//        	= abstractListDefinitions.get(abstractNum.getAbstractNumId().toString());
        
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
    		= new ListNumberingDefinition(newNum, abstractListDefinitions, false);
        listDef.setDefaultStateSupplier(this::getNumberingState);
        instanceListDefinitions.put(listDef.getListNumberId(), listDef);		
    	
    	// Return the new numId
    	return newNumId;
    	
    }
    
	
	
	private Emulator em;

	/** The counters the no-state overloads of {@link Emulator#getNumber} use: one
	 *  set per part, as before 17.1.1.  A traversal that wants its own (per story,
	 *  or side-effect free) passes a {@link NumberingState} instead.  @since 17.1.1 */
	private NumberingState numberingState = new NumberingState();

	/** The part's default numbering state; {@link #getEmulator(boolean)} with
	 *  {@code true} replaces it with a fresh one.  @since 17.1.1 */
	public NumberingState getNumberingState() {
		return numberingState;
	}
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
    		numberingState = new NumberingState(); // reset: every list starts again (@since 17.1.1)
    	}
		
		return em;
	}
	
	/**
	 * The paragraph style this level is linked to - the {@code w:pStyle} of
	 * ECMA-376 17.9.24, on the instance's {@code w:lvlOverride/w:lvl} where it has one
	 * and on the abstract level otherwise - or null where the level names none.
	 *
	 * @since 17.1.0
	 */
	public String getLinkedStyleId(String numId, String ilvl) {

		ListNumberingDefinition lnd = getInstanceListDefinitions().get(numId);
		if (lnd == null) return null;
		org.docx4j.model.listnumbering.ListLevel ll = lnd.getLevel(ilvl == null || ilvl.length() == 0 ? "0" : ilvl);
		if (ll == null) return null;
		Lvl override = ll.getJaxbOverrideLvl();
		if (override != null && override.getPStyle() != null) return override.getPStyle().getVal();
		Lvl abstractLvl = ll.getJaxbAbstractLvl();
		if (abstractLvl != null && abstractLvl.getPStyle() != null) return abstractLvl.getPStyle().getVal();
		return null;
	}

	public Ind getInd(NumPr numPr) { //, StyleDefinitionsPart sdp, String styleId) {

		// w:ilvl (and its w:val) is optional; its absence means level 0
		String ilvlString = "0";
		if (numPr.getIlvl()!=null && numPr.getIlvl().getVal()!=null) ilvlString = numPr.getIlvl().getVal().toString();
		
		if (numPr.getNumId()==null || numPr.getNumId().getVal()==null) {
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
	
	/**
	 * The indent this level contributes.
	 *
	 * <p><b>The level's own {@code w:pPr/w:ind} comes first</b> (ECMA-376 17.9.24: a
	 * {@code w:lvl/w:pPr} states the paragraph properties applied to a paragraph at this
	 * level).  A {@code w:lvl/w:pStyle} only <em>links</em> the level to a paragraph
	 * style; it does not make that style's indent the level's.  Reading the style first
	 * put an indent from a style the paragraph does not use into every paragraph whose
	 * own {@code w:numPr} named the numbering: measured on a document whose level
	 * overrides ilvl 0 with {@code <w:ind w:left="198" w:hanging="198"/>} beside a
	 * {@code w:pStyle} naming a List Bullet style carrying
	 * {@code <w:ind w:left="0" w:firstLine="0"/>}, Word draws the bullets at x=79.46 -
	 * the level's 198 twips from a 79.4pt margin - where the style's indent has no
	 * hanging indent at all, so the label column fell back to the default tab stop and
	 * put the bullet 14pt further left again.  Where the level states no indent of its
	 * own the linked style's is still used, which is what this did since 2.7.
	 *
	 * @since 17.1.0 the level's own indent is preferred
	 */
	private Ind getIndFromLvl(Lvl lvl) {

		// A w:ind the level states itself is the level's indent
		if (lvl.getPPr()!=null
				&& lvl.getPPr().getInd() !=null ) {
			return lvl.getPPr().getInd();
		}

		// Otherwise, if there is a style reference in the instance, as a sibling of
		// pPr, use any w:ind in it, or in the styles it is based on (@since 17.1.1:
		// the w:basedOn chain is followed, as it is for every other property the
		// style contributes)
		if (lvl.getPStyle()!=null) {

			log.debug("override level has linked style: " + lvl.getPStyle().getVal() );

			PropertyResolver propertyResolver = null;
			try {
				propertyResolver = ((WordprocessingMLPackage)this.getPackage()).getMainDocumentPart().getPropertyResolver(false);
			} catch (Docx4JException e) {
				log.error(e.getMessage(), e);
			} 
				/* avoids invoking it during init - the recursion below, as traced in 17.0.x
				   (PropertyResolver's internals have been renamed since: it has no
				   addNormalToResolvedStylePPrComponent, and init does not resolve styles):
					at org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getPropertyResolver(MainDocumentPart.java:163)
					at org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart.getIndFromLvl(NumberingDefinitionsPart.java:417)
					at org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart.getInd(NumberingDefinitionsPart.java:401)
					at org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart.getInd(NumberingDefinitionsPart.java:365)
					at org.docx4j.model.styles.StyleUtil.apply(StyleUtil.java:1941)
					at org.docx4j.model.styles.StyleUtil.apply(StyleUtil.java:1896)
					at org.docx4j.model.PropertyResolver.applyPPr(PropertyResolver.java:842)
					at org.docx4j.model.PropertyResolver.addNormalToResolvedStylePPrComponent(PropertyResolver.java:234)
					at org.docx4j.model.PropertyResolver.init(PropertyResolver.java:212)
					at org.docx4j.model.PropertyResolver.<init>(PropertyResolver.java:145)
					at org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.getPropertyResolver(MainDocumentPart.java:163)
				*/
			// less efficient, but usable during init
			StyleDefinitionsPart stylesPart = propertyResolver == null
					? ((WordprocessingMLPackage)this.getPackage()).getMainDocumentPart().getStyleDefinitionsPart() : null;

			java.util.Set<String> seen = new java.util.HashSet<String>();
			String id = lvl.getPStyle().getVal();
			while (id != null && seen.add(id)) {
				org.docx4j.wml.Style style = propertyResolver == null
						? stylesPart.getStyleById(id) : propertyResolver.getStyle(id);
				if (style==null) {
					log.warn("Couldn't find style " + id);
					return null;
				}
				// If the style has a w:ind, return it.  Otherwise, the style it is based on.
				if (style.getPPr() != null
						&& style.getPPr().getInd()!=null ) {
					return style.getPPr().getInd();
				}
				id = style.getBasedOn() == null ? null : style.getBasedOn().getVal();
			}
		}

		// A style reference in the level's pPr, but not also one as a sibling of pPr,
		// contributes nothing here (and numbers nothing, CR-014 P4: Emulator.resolve
		// is where "not numbered" is decided).
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
        ListNumberingDefinition listDef = new ListNumberingDefinition(num, abstractListDefinitions, false);
        listDef.setDefaultStateSupplier(this::getNumberingState);
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
