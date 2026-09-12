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

    This file is a translation into Java of a portion of 
    Program.cs from Microsoft's OpenXmlViewer, which was made available to
    the project under the following license:
    
		(c) Microsoft Corporation
		
		Microsoft Public License (Ms-PL)
		
		This license governs use of the accompanying software. If you use the
		software, you accept this license. If you do not accept the license, do
		not use the software.
		
		1. Definitions
		
		The terms "reproduce," "reproduction," "derivative works," and "distribution"
		have the same meaning here as under U.S. copyright law.
		
		A "contribution" is the original software, or any additions or changes to the software.
		
		A "contributor" is any person that distributes its contribution under this license.
		
		"Licensed patents" are a contributor's patent claims that read directly on its contribution.
		
		2. Grant of Rights
		
		(A) Copyright Grant- Subject to the terms of this license, including the
		license conditions and limitations in section 3, each contributor
		grants you a non-exclusive, worldwide, royalty-free copyright license
		to reproduce its contribution, prepare derivative works of its
		contribution, and distribute its contribution or any derivative works
		that you create.
		
		(B) Patent Grant- Subject to the terms of this
		license, including the license conditions and limitations in section 3,
		each contributor grants you a non-exclusive, worldwide, royalty-free
		license under its licensed patents to make, have made, use, sell, offer
		for sale, import, and/or otherwise dispose of its contribution in the
		software or derivative works of the contribution in the software.
		
		3. Conditions and Limitations
		
		(A) No Trademark License- This license does not grant you rights to use any contributors' name, logo, or trademarks.
		
		(B) If you bring a patent claim against any contributor over patents that
		you claim are infringed by the software, your patent license from such
		contributor to the software ends automatically.
		
		(C) If you distribute any portion of the software, you must retain all copyright,
		patent, trademark, and attribution notices that are present in the
		software.
		
		(D) If you distribute any portion of the software in
		source code form, you may do so only under this license by including a
		complete copy of this license with your distribution. If you distribute
		any portion of the software in compiled or object code form, you may
		only do so under a license that complies with this license.
		
		(E) The software is licensed "as-is." You bear the risk of using it. The
		contributors give no express warranties, guarantees or conditions. You
		may have additional consumer rights under your local laws which this
		license cannot change. To the extent permitted under your local laws,
		the contributors exclude the implied warranties of merchantability,
		fitness for a particular purpose and non-infringement.

 */
package org.docx4j.model.listnumbering;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Numbering.Num.LvlOverride.StartOverride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An instance list definition, a {@code w:num}: the abstract definition it names,
 * with its {@code w:lvlOverride}s applied to copies of that definition's levels.
 * <pre>
 *   &lt;w:num w:numId="2"&gt;
 *     &lt;w:abstractNumId w:val="0"/&gt;
 *     &lt;w:lvlOverride w:ilvl="0"&gt;
 *       &lt;w:startOverride w:val="10"/&gt;
 *     &lt;/w:lvlOverride&gt;
 *   &lt;/w:num&gt;
 * </pre>
 * Layered on the JAXB object; holds no counters (see {@link NumberingState}).
 */
public class ListNumberingDefinition {
		
	private Numbering.Num numNode;
	/** The underlying {@code w:num}. */
	public Numbering.Num getNumNode() {
		return numNode;
	}
	
	protected static Logger log = LoggerFactory.getLogger(ListNumberingDefinition.class);
	
    /**
     * Builds the instance definition from its {@code w:num} and the abstract
     * definitions it may name.
     *
     * @param resolveLinkedStyle whether this is the second pass, in which an abstract
     *        definition carrying {@code w:numStyleLink} has been resolved
     */
    public ListNumberingDefinition(Numbering.Num numNode, 
    		HashMap<String, AbstractListNumberingDefinition> abstractListDefinitions,
    		boolean resolveLinkedStyle)
    {
    	this.numNode = numNode;
    	
        this.listNumberId =  numNode.getNumId().toString();

        Numbering.Num.AbstractNumId abstractNumId = numNode.getAbstractNumId();
        if (abstractNumId == null) {
        	log.warn("No abstractNumId on w:numId=" + listNumberId);
        } else {
        	log.debug("concrete " + listNumberId + " points to abstract list " + abstractNumId.getVal().toString());
            this.abstractListDefinition = abstractListDefinitions.get(abstractNumId.getVal().toString() );
            if (abstractListDefinition==null) {
            	log.warn("No abstractListDefinition for w:numId=" + listNumberId);  
            	return;
            }

            if (this.abstractListDefinition.getLevelCount()==0 
            		&& this.abstractListDefinition.hasLinkedStyle()) {
            	
            	/* Something like:
            	 * 
					  <w:abstractNum w:abstractNumId="0">
					    <w:nsid w:val="42FF6222"/>
					    <w:multiLevelType w:val="multilevel"/>
					    <w:tmpl w:val="0409001D"/>
					    <w:numStyleLink w:val="MyListStyle"/>
					  </w:abstractNum>
            	 * 
            	 * We need to go back to the style, which will point to a concrete numId
            	 * which will in turn point to some *other* abstractNum!  
            	 * Why M$ designed things this way defies logic!
            	 * 
            	 * This is done when NDP.resolveLinkedAbstractNum is invoked,
            	 * typically by AbstractWmlConversionContext, prior to converting 
            	 * the docx to HTML or PDF.
            	 */
            	if (resolveLinkedStyle) {
            		log.debug("abstract list has linked style; resolving this pass"); 
            	} else {
            		log.debug("abstract list has linked style; resolve this later"); 
            		return;
            	}
            }

            this.levels = new HashMap<String, ListLevel>(this.abstractListDefinition.getLevelCount() );

            // initialize the levels to the same as the template ("abstract") list level
    		Iterator listLevelIterator = this.abstractListDefinition.getListLevels().entrySet().iterator();
    	    while (listLevelIterator.hasNext()) {
    	        Map.Entry pairs = (Map.Entry)listLevelIterator.next();
    	        ListLevel instanceLevel = new ListLevel( (ListLevel)pairs.getValue() );
    	        instanceLevel.setOwnerNumId(listNumberId);
    	        this.levels.put( (String)pairs.getKey(), instanceLevel ); 
    	    }

            // propagate the level overrides into the current list number level definition

            List<Numbering.Num.LvlOverride> levelOverrideNodes = numNode.getLvlOverride(); 
			if (levelOverrideNodes != null) {
				/*
				 * <w:lvlOverride w:ilvl="0"> 
				 * 		<w:startOverride w:val="10"/>
				 * </w:lvlOverride>
				 */
				for (Numbering.Num.LvlOverride overrideNode : levelOverrideNodes) {
                    if(log.isDebugEnabled()) {
                        log.debug("found LvlOverride "
                                + XmlUtils.marshaltoString(overrideNode, true));
                    }
					
					if (overrideNode.getIlvl() == null) {
                        if(log.isWarnEnabled()) {
                            log.warn("Missing @w:ilvl! " + XmlUtils.marshaltoString(overrideNode, true));
                        }
					} else {
						String overrideLevelId = overrideNode.getIlvl().toString(); 
						log.debug(".. " + overrideLevelId);

						if (!overrideLevelId.equals("")) {
							// Is there a w:startOverride?
							// This is only given effect the first time the instance
							// is encountered in the document
							StartOverride startOverride = overrideNode.getStartOverride();
							if (startOverride != null
									&& startOverride.getVal() != null) {
								
								if (this.levels.get(overrideLevelId)==null) {
									
									if (abstractListDefinition.getAbstractNumNode().getNumStyleLink()!=null) {
										log.error("numStyleLink (Numbering Style Reference) is supported; but should not now be present in this pass!");
										/*
										 * This element specifies an abstract numbering does not contain the actual
										 * numbering properties for its type, but rather serves as a reference to a
										 * numbering style stored in the document, which shall be applied when this
										 * abstract numbering definition is referenced, and itself points at the actual
										 * underlying abstract numbering definition to be used.
										 * 
										 * The numbering style that is to be applied when this abstract numbering
										 * definition is referenced is identified by the string contained in
										 * numStyleLink's val attribute.
										 */
									}
									
									throw new RuntimeException(overrideLevelId + " level missing for abstractListDefinition " 
												+ XmlUtils.marshaltoString(abstractListDefinition.getAbstractNumNode() ));

								}
								this.levels.get(overrideLevelId).setStartValue(
										startOverride.getVal().subtract(BigInteger.ONE));
								log.debug("level " + overrideLevelId + "starts at " + startOverride.getVal());
							}
						}

						Lvl lvl = overrideNode.getLvl();
						if (lvl != null && this.levels.get(overrideLevelId) != null) {
							this.levels.get(overrideLevelId).setOverrides(lvl);
						}
						
					}

				}
			}
        }
    }

    private AbstractListNumberingDefinition abstractListDefinition;
    public AbstractListNumberingDefinition getAbstractListDefinition() {
		return abstractListDefinition;
	}

	private HashMap<String, ListLevel> levels;
	public ListLevel getLevel(String ilvl) {
		return levels == null ? null : levels.get(ilvl);
	}

	/** The level with this ilvl, or null.  @since 17.1.1 */
	public ListLevel getLevel(int ilvl) {
		return levels == null ? null : levels.get(Integer.toString(ilvl));
	}

	private java.util.function.Supplier<NumberingState> defaultStateSupplier;
	private NumberingState orphanState;

	/**
	 * Where the no-state overloads find their {@link NumberingState}: the numbering
	 * part installs its own.  Internal; a caller with a state of its own passes it to
	 * the state-taking overloads instead.
	 *
	 * @since 17.1.1
	 */
	public void setDefaultStateSupplier(java.util.function.Supplier<NumberingState> supplier) {
		this.defaultStateSupplier = supplier;
		if (levels != null) {
			for (ListLevel l : levels.values()) l.setDefaultStateSupplier(supplier);
		}
	}

	NumberingState defaultState() {
		if (defaultStateSupplier != null) {
			NumberingState s = defaultStateSupplier.get();
			if (s != null) return s;
		}
		if (orphanState == null) orphanState = new NumberingState();
		return orphanState;
	}
    

    /**
     * Increments the count at the given level in the default state, and resets the
     * deeper levels that restart after it.
     */
    public void incrementCounter(String level)
    {
    	incrementCounter(level, defaultState());
    }

    /**
     * Increments the count at the given level in the given state, and resets the
     * deeper levels that restart after it.
     *
     * @since 17.1.1
     */
    public void incrementCounter(String level, NumberingState state)
    {
    	int levelInt = Integer.parseInt(level);
    	ListLevel thisLevel = getLevel(levelInt);

    	if (!thisLevel.counter(state).isEncounteredAlready()) {
    		// We haven't encountered this level before, so check that the shallower
    		// levels have been initialised
    		for (int shallower = levelInt - 1; shallower >= 0; shallower--) {
    			ListLevel l = getLevel(shallower);
    			if (l == null || l.counter(state).isEncounteredAlready()) break;
    			if (log.isDebugEnabled()) log.debug("Increment lower level " + shallower);
    			l.incrementCounter(state);
    		}
    	}

    	if (log.isDebugEnabled()) log.debug("Increment level " + level);
        thisLevel.incrementCounter(state);

        // Now set the deeper levels back to their start - each unless its
        // w:lvlRestart says this level does not restart it (ECMA-376 17.9.11;
        // @since 17.1.1, CR-014 phase 2: unread before, so every deeper level
        // restarted)
        for (int deeperIlvl = levelInt + 1; ; deeperIlvl++) {
        	ListLevel deeper = getLevel(deeperIlvl);
        	if (deeper == null) break;
        	if (deeper.restartsAfter(levelInt)) {
        		if (log.isDebugEnabled()) log.debug("Reset level " + deeperIlvl);
        		deeper.resetCounter(state);
        	} else if (log.isDebugEnabled()) {
        		log.debug("Level " + deeperIlvl + " keeps counting (w:lvlRestart " + deeper.getLvlRestart() + ")");
        	}
        }
    }

    private String listNumberId;

    /** The {@code w:numId}. */
    public String getListNumberId() 
        {
            return this.listNumberId;
        }

    /**
     * The label of the given level from the counters of the default state: its
     * {@code w:lvlText} with every level's count filled in ("1.2.", "(c)").
     */
    public String getCurrentNumberString(String level)
    {
    	return getCurrentNumberString(level, defaultState());
    }

    /** The label of the given level from the counters of the given state.  @since 17.1.1 */
    public String getCurrentNumberString(String level, NumberingState state)
    {
        ListLevel controllingLvl = this.levels.get( level ); 
        
        /* w:isLgl at *any* level, and applied to every level the format string
         * displays *above* this one - Word's "Legal style numbering": the levels this
         * number inherits are shown in decimal whatever their own w:numFmt, while this
         * level keeps its own (Word's built-in Article/Section numbering, whose ilvl 1
         * is decimalZero with w:isLgl, prints "Section 1.01" - IsLglTest).  Until
         * 17.1.0 it was honoured at ilvl 1 alone: measured on a document whose
         * abstractNum carries <w:isLgl/> at ilvl 1, 2 and 3 with w:lvlText "%1.%2." and
         * "%1.%2.%3." over an upperRoman ilvl 0, Word prints "3.6.2." where docx4j
         * printed "III.6.2.".  The instance's own level definition (w:lvlOverride/w:lvl)
         * carries it too, where there is one.  @since 17.1.0 */
        Lvl controllingDefinition = (controllingLvl.getJaxbOverrideLvl()!=null)
        					? controllingLvl.getJaxbOverrideLvl() : controllingLvl.getJaxbAbstractLvl();
        boolean isLegal = controllingDefinition!=null
        					&& controllingDefinition.getIsLgl() !=null
        					&& controllingDefinition.getIsLgl().isVal();
        /*
         * Explanation of <w:isLgl/>
         * 
         * Consider Word (2010)'s built-in legal numbering:
         * 
         *   <w:abstractNum w:abstractNumId="2">
			    <w:nsid w:val="78220137"/>
			    <w:multiLevelType w:val="multilevel"/>
			    <w:tmpl w:val="0DE0BCC2"/>
			    <w:lvl w:ilvl="0">
			      <w:start w:val="1"/>
			      <w:numFmt w:val="upperRoman"/>
			      <w:pStyle w:val="Heading1"/>
			      <w:lvlText w:val="Article %1."/>
			      <w:lvlJc w:val="left"/>
			      <w:pPr>
			        <w:ind w:left="0" w:firstLine="0"/>
			      </w:pPr>
			    </w:lvl>
			    <w:lvl w:ilvl="1">
			      <w:start w:val="1"/>
			      <w:numFmt w:val="decimalZero"/>
			      <w:pStyle w:val="Heading2"/>
			      <w:isLgl/>                        <----------------
			      <w:lvlText w:val="Section %1.%2"/>
			      <w:lvlJc w:val="left"/>
			      <w:pPr>
			        <w:ind w:left="0" w:firstLine="0"/>
			      </w:pPr>
			    </w:lvl>
			    <w:lvl w:ilvl="2">
			      <w:start w:val="1"/>
			      <w:numFmt w:val="lowerLetter"/>
			      <w:pStyle w:val="Heading3"/>
			      <w:lvlText w:val="(%3)"/>
			      <w:lvlJc w:val="left"/>
			      <w:pPr>
			        <w:ind w:left="720" w:hanging="432"/>
			      </w:pPr>
			    </w:lvl>
			    			  
         * Notice <w:isLgl/> at @w:ilvl="1".  This produces:
         * 
         *   Article I.
         *   Section 1.01
         *   
         * Without it, you'd get:
         * 
         *   Article I.
         *   Section I.01
         *  
         * In other words, the default numbering behaviour is to 
         * format a level using the w:numFmt specified in that level.
         * w:isLgl overrides that behaviour for the levels this number
         * *inherits*: each of them is shown in decimal, whatever its own
         * w:numFmt, while the level carrying w:isLgl keeps its own (which
         * is why the example prints "Section 1.01" and not "Section 1.1").
         * Measured at ilvl 2 and 3 as well as at ilvl 1 (@since 17.1.0;
         * before, it was read at ilvl 1 only).
         */
    	
        int thisLevel = -1;
        try {
        	thisLevel = Integer.parseInt(level);
        } catch (NumberFormatException e) {
        	// not a level number: no level is "above" it, so isLgl changes nothing
        }

        String formatString = controllingLvl.getLevelText();
        if (log.isDebugEnabled()) log.debug("levelText: " + formatString );
        StringBuilder result = new StringBuilder();
        String temp = "";

        for (int i = 0; i < formatString.length(); i++)
        {
        	temp = formatString.substring(i, i+1);
            if (temp.equals("%") )
            {
                if (i < formatString.length() - 1)
                {
                    String formatStringLevel = formatString.substring(i + 1, i+2);
                    // as it turns out, in the format String, the level is 1-based
                    int levelId =  Integer.parseInt(formatStringLevel) - 1;
                    ListLevel lvl = this.levels.get( Integer.toString(levelId) );
                    if (isLegal && levelId < thisLevel) {
                    	// Special case: Use normal decimal numbering, for every level
                    	// above this one (@since 17.1.0; ilvl 0 only before)
                    	result.append(lvl.getCurrentValueUnformatted(state) );

                    } else {
                    	// Usual case
                    	result.append(lvl.getCurrentValueFormatted(state, "numId " + listNumberId) );
                    }
                    i++;
                }
            }
            else
            {
                result.append(temp);
            }
        }

        return result.toString();
    }

    /**
     * The {@code w:hAnsi} font the level's rPr names, or null.
     * @deprecated use the level's rPr ({@link Emulator.NumberingResult#getLabelRPr()})
     */
    @Deprecated
    public String getFont(String level)
    {
        return this.levels.get(level).getFont();
    }

    /** Whether the level's {@code w:numFmt} is bullet. */
    public boolean isBullet(String level)
    {
        return this.levels.get(level).isBullet();
    }

    /**
     * Whether the level exists: Word has been seen to write a {@code w:num} whose
     * abstract definition is missing, or lacks the level, so callers check.
     */
    public boolean levelExists(String level)
    {
    	if (this.levels==null) {
    		log.info("No levels present in abstractNumId");
    		if (getAbstractListDefinition()==null) {
    			log.info("[missing]");
    		} else {
    			log.info(XmlUtils.marshaltoString(getAbstractListDefinition().getAbstractNumNode()));
    		}
    		log.debug("referenced from ");
    		log.debug(XmlUtils.marshaltoString(numNode));
    		
    		return false;
    	}
        return this.levels.containsKey(level);
    }


    /** Whether the level with this ilvl exists.  @since 17.1.1 */
    public boolean levelExists(int ilvl) {
    	return levelExists(Integer.toString(ilvl));
    }

    // ---- the names before 17.1.1 (from the C# original this was translated from);
    //      removal no earlier than 17.2 (CR-014 phase 5)

    /** @deprecated since 17.1.1, use {@link #incrementCounter(String)} */
    @Deprecated
    public void IncrementCounter(String level) { incrementCounter(level); }
    /** @deprecated since 17.1.1, use {@link #incrementCounter(String, NumberingState)} */
    @Deprecated
    public void IncrementCounter(String level, NumberingState state) { incrementCounter(level, state); }
    /** @deprecated since 17.1.1, use {@link #getCurrentNumberString(String)} */
    @Deprecated
    public String GetCurrentNumberString(String level) { return getCurrentNumberString(level); }
    /** @deprecated since 17.1.1, use {@link #getCurrentNumberString(String, NumberingState)} */
    @Deprecated
    public String GetCurrentNumberString(String level, NumberingState state) { return getCurrentNumberString(level, state); }
    /** @deprecated since 17.1.1, use {@link #getFont(String)} */
    @Deprecated
    public String GetFont(String level) { return getFont(level); }
    /** @deprecated since 17.1.1, use {@link #isBullet(String)} */
    @Deprecated
    public boolean IsBullet(String level) { return isBullet(level); }
    /** @deprecated since 17.1.1, use {@link #levelExists(String)} */
    @Deprecated
    public boolean LevelExists(String level) { return levelExists(level); }
}
