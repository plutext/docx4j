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
 * Represents:
 * 
	  <w:num w:numId="1">
	    <w:abstractNumId w:val="0"/>
	  </w:num>
	  
	  or 
	  
	  <w:num w:numId="2">
	    <w:abstractNumId w:val="0"/>
	    <w:lvlOverride w:ilvl="0">
	      <w:startOverride w:val="10"/>
	    </w:lvlOverride>
	  </w:num>
	  	    
	    (layered on top of the JAXB object representing same)
    
 */
public class ListNumberingDefinition {
		
	// The underlying JAXB object 
	private Numbering.Num numNode;
	public Numbering.Num getNumNode() {
		return numNode;
	}
	
	protected static Logger log = LoggerFactory.getLogger(ListNumberingDefinition.class);
	
    /**
     * @param numNode
     * @param abstractListDefinitions
     */
    public ListNumberingDefinition(Numbering.Num numNode, 
    		HashMap<String, AbstractListNumberingDefinition> abstractListDefinitions)
    {
    	this.numNode = numNode;
    	
        this.listNumberId =  numNode.getNumId().toString(); //getAttributeValue(numNode, "w:numId");
//        if (log.isDebugEnabled()) {
//	    	log.debug("Constructing model for numId=" + listNumberId);
//	    	log.debug(XmlUtils.marshaltoString(numNode));
//        }

        //XmlNode abstractNumNode = numNode.SelectSingleNode("./w:abstractNumId", nsm);
        Numbering.Num.AbstractNumId abstractNumNode = numNode.getAbstractNumId();
        if (abstractNumNode == null) {
        	log.warn("No abstractNumId on w:numId=" + listNumberId);
        } else {
        	log.debug("concrete " + listNumberId + " points to abstract list " + abstractNumNode.getVal().toString());
            this.abstractListDefinition = abstractListDefinitions.get(abstractNumNode.getVal().toString() ); //[getAttributeValue(abstractNumNode, ValAttrName)];
            if (abstractListDefinition==null) {
            	log.warn("No abstractListDefinition for w:numId=" + listNumberId);  
            	return;
            }
//            if (log.isDebugEnabled()) {
//            	log.debug(XmlUtils.marshaltoString(abstractListDefinition.getAbstractNumNode()));
//            }

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
            }

            this.levels = new HashMap<String, ListLevel>(this.abstractListDefinition.getLevelCount() );

            // initialize the levels to the same as the template ("abstract") list level
    		Iterator listLevelIterator = this.abstractListDefinition.getListLevels().entrySet().iterator();
    	    while (listLevelIterator.hasNext()) {
    	        Map.Entry pairs = (Map.Entry)listLevelIterator.next();
    	        this.levels.put( (String)pairs.getKey(), new ListLevel( (ListLevel)pairs.getValue() ) ); 
    	        //log.debug("init'd level " + pairs.getKey());
    	    }

            // propagate the level overrides into the current list number level definition
            // XmlNodeList levelOverrideNodes = numNode.SelectNodes("./w:lvlOverride", nsm);

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
									throw new RuntimeException(overrideLevelId + " level missing for abstractListDefinition " + abstractListDefinition.getID());
								}
								this.levels.get(overrideLevelId).setStartValue(
										startOverride.getVal().subtract(BigInteger.ONE));
								log.debug("level " + overrideLevelId + "starts at " + startOverride.getVal());
							}
						}

						Lvl lvl = overrideNode.getLvl();
						if (lvl != null && this.levels.get(overrideLevelId) != null) {
							this.levels.get(overrideLevelId).SetOverrides(lvl);
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
		return levels.get(ilvl);
	}
    

    /// <summary>
    /// increment the occurrence count of the specified level, reset the occurrence count of derived levels
    /// </summary>
    /// <param name="level"></param>
    public void IncrementCounter(String level)
    {
        int otherLevelInt;
        String otherLevelStr;
    	
    	if (!this.levels.get(level).getCounter().isEncounteredAlready()) {
    		// We haven't encountered this level before,
    		// so check that the lower levels have been initialised
            otherLevelInt = Integer.parseInt(level)-1;
            otherLevelStr =  Integer.toString(otherLevelInt);
            
            while (this.levels.containsKey(otherLevelStr) // will fail once negative
            		&& !this.levels.get(otherLevelStr).getCounter().isEncounteredAlready()) 
            {
            	log.debug("Increment lower level " + otherLevelStr);
                this.levels.get(otherLevelStr).IncrementCounter();
                otherLevelInt--;
                otherLevelStr = Integer.toString(otherLevelInt);
            }
    	}    		
    		
    	
    	log.debug("Increment level " + level);
        this.levels.get(level).IncrementCounter();

        // Now set all higher levels back to 1.
        
        // here's a bit where the decision to use Strings as level IDs was bad 
        // - I need to loop through the derived levels and reset their counters
        otherLevelInt = Integer.parseInt(level)+1;
        otherLevelStr =  Integer.toString(otherLevelInt);

        while (this.levels.containsKey(otherLevelStr))
        {
        	log.debug("Reset level " + otherLevelInt);
            this.levels.get(otherLevelStr).ResetCounter();
            otherLevelInt++;
            otherLevelStr = Integer.toString(otherLevelInt);
        }
    }

    private String listNumberId;

    /// <summary>
    /// numId of this list numbering schema
    /// </summary>
    public String getListNumberId() 
        {
            return this.listNumberId;
        }

    /**
     * returns a String containing the current state of the counters, up to the indicated level
     * 
     * @param level
     * @return
     */
    public String GetCurrentNumberString(String level)
    {
        ListLevel controllingLvl = this.levels.get( level ); 
        
        boolean isLegal = level.equals("1")
        					&& controllingLvl.getJaxbAbstractLvl().getIsLgl() !=null 
        					&& controllingLvl.getJaxbAbstractLvl().getIsLgl().isVal();
        /*
         * Explanation of <w:isLgl/>
         * 
         * COnsider Word (2010)'s built-in legal numbering:
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
         * w:isLgl overrides that behaviour, and uses decimal
         * numbering for ilvl 0.  w:isLgl only seems to have 
         * any effect in Word 2010 when specified at ilvl 1
         * (I tried it at a deeper level, and is behaved as if
         *  not present at all).
         */
    	
        String formatString = controllingLvl.getLevelText();
        log.debug("levelText: " + formatString );
        StringBuilder result = new StringBuilder();
        String temp = ""; //String.Empty;

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
                    if (levelId==0  && isLegal ) {
                    	// Special case: Use normal decimal numbering
                    	result.append(lvl.getCurrentValueUnformatted() );
                    	
                    } else {
                    	// Usual case
                    	result.append(lvl.getCurrentValueFormatted() );
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

    /// <summary>
    /// retrieve the font name that was specified for the list String
    /// </summary>
    /// <param name="level"></param>
    /// <returns></returns>
    public String GetFont(String level)
    {
        return this.levels.get(level).getFont();
    }

    /// <summary>
    /// retrieve whether the level was a bullet list type
    /// </summary>
    /// <param name="level"></param>
    /// <returns></returns>
    public boolean IsBullet(String level)
    {
        return this.levels.get(level).IsBullet();
    }

    /// <summary>
    /// returns whether the specific level ID exists - in testing we've seen some referential integrity issues due to Word bugs
    /// </summary>
    /// <param name="level">
    /// </param>
    /// <returns>
    /// </returns>
    /// <id guid="b94c13b8-7273-4f6a-927b-178d685fbe0f" />
    /// <owner alias="ROrleth" />
    public boolean LevelExists(String level)
    {
    	if (this.levels==null) {
    		log.info("No levels present in abstractNumId");
    		if (getAbstractListDefinition()==null) {
    			log.info("[missing]");
    		} else {
    			log.info(XmlUtils.marshaltoString(getAbstractListDefinition()));
    		}
    		log.debug("referenced from ");
    		log.debug(XmlUtils.marshaltoString(numNode));
    		
    		return false;
    	}
        return this.levels.containsKey(level);
    }

}


