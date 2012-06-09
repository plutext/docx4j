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
 
package org.docx4j.model.listnumbering;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Numbering.Num.LvlOverride.StartOverride;

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
	
	protected static Logger log = Logger.getLogger(ListNumberingDefinition.class);
	
    /**
     * @param numNode
     * @param abstractListDefinitions
     */
    public ListNumberingDefinition(Numbering.Num numNode, 
    		HashMap<String, AbstractListNumberingDefinition> abstractListDefinitions)
    {
    	this.numNode = numNode;
    	
        this.listNumberId =  numNode.getNumId().toString(); //getAttributeValue(numNode, "w:numId");
    	log.debug("Constructing model for numId=" + listNumberId);

        //XmlNode abstractNumNode = numNode.SelectSingleNode("./w:abstractNumId", nsm);
        Numbering.Num.AbstractNumId abstractNumNode = numNode.getAbstractNumId();
        if (abstractNumNode == null) {
        	log.warn("No abstractNumId on w:numId=" + listNumberId);
        } else {
            this.abstractListDefinition = abstractListDefinitions.get(abstractNumNode.getVal().toString() ); //[getAttributeValue(abstractNumNode, ValAttrName)];
            if (abstractListDefinition==null) {
            	log.warn("No abstractListDefinition for w:numId=" + listNumberId);            	
            }

            this.levels = new HashMap<String, ListLevel>(this.abstractListDefinition.getLevelCount() );

            // initialize the levels to the same as the template ("abstract") list level
    		Iterator listLevelIterator = this.abstractListDefinition.getListLevels().entrySet().iterator();
    	    while (listLevelIterator.hasNext()) {
    	        Map.Entry pairs = (Map.Entry)listLevelIterator.next();
    	        this.levels.put( (String)pairs.getKey(), new ListLevel( (ListLevel)pairs.getValue() ) );        	        
    	    }

            // propagate the level overrides into the current list number level definition
            // XmlNodeList levelOverrideNodes = numNode.SelectNodes("./w:lvlOverride", nsm);

            List<Numbering.Num.LvlOverride> levelOverrideNodes = numNode.getLvlOverride(); 
			if (levelOverrideNodes != null) {
				/*
				 * <w:lvlOverride w:ilvl="0"> <w:startOverride w:val="10"/>
				 * </w:lvlOverride>
				 */
				for (Numbering.Num.LvlOverride overrideNode : levelOverrideNodes) {
					log.debug("found LvlOverride "
							+ XmlUtils.marshaltoString(overrideNode, true));
					
					if (overrideNode.getIlvl() == null) {
						log.warn("Missing @w:ilvl! " + XmlUtils.marshaltoString(overrideNode, true));
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
								
								this.levels.get(overrideLevelId).setStartValue(
										startOverride.getVal().subtract(BigInteger.ONE));
								log.debug("level " + overrideLevelId + "starts at " + startOverride.getVal());
							}
						}

						Lvl lvl = overrideNode.getLvl();
						if (lvl != null) {
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
    	
    	log.debug("Increment level " + level);
        this.levels.get(level).IncrementCounter();

        // Now set all lower levels back to 1.
        
        // here's a bit where the decision to use Strings as level IDs was bad 
        // - I need to loop through the derived levels and reset their counters
        //UInt32 levelNumber = System.Convert.ToUInt32(level, CultureInfo.InvariantCulture) + 1;
        int levelNumber = Integer.parseInt(level)+1;
        String levelString =  Integer.toString(levelNumber);

        while (this.levels.containsKey(levelString))
        {
        	log.debug("Reset level " + levelNumber);
            this.levels.get(levelString).ResetCounter();
            levelNumber++;
            levelString = Integer.toString(levelNumber);
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
            //temp = formatString.SubString(i, 1);
        	// C# pos i, length 1            	
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
        return this.levels.containsKey(level);
    }

}

//    static String getAttributeValue(XmlNode node, String name)
//    {
//        String value = String.Empty;
//
//        XmlAttribute attribute = node.Attributes[name];
//        if (attribute != null && attribute.Value != null)
//        {
//            value = attribute.Value;
//        }
//
//        return value;
//    }

