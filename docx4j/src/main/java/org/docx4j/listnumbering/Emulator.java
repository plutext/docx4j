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
package org.docx4j.listnumbering;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.STNumberFormat;
import org.w3c.dom.DocumentFragment;

public class Emulator {
	
	/* There should be only one Emulator object per 
	 * WordprocessingML package.
	 */
	
    HashMap<String, AbstractListNumberingDefinition> abstractListDefinitions; 
    HashMap<String, ListNumberingDefinition> instanceListDefinitions; 
	
    public Emulator(Numbering numbering)
    {
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
    

    /* Get the computed list number for the given list at this point in the
     * document.
     */
    public static ResultTriple getNumber(Emulator em, String levelId, String numId) {
    	
        // Looking at nodes //w:numPr/w:ilvl
    	
    	ResultTriple triple = em.new ResultTriple();
    	
		if (levelId != null && !levelId.equals("")) {
			// String numId = getAttributeValue(numIdNode, ValAttrName);

			if (numId != null
					&& !numId.equals("")
					&& em.instanceListDefinitions.containsKey(numId)
					&& em.instanceListDefinitions.get(numId).LevelExists(
							levelId)) {
				// XmlAttribute counterAttr =
				// mainDoc.CreateAttribute("numString");

				em.instanceListDefinitions.get(numId).IncrementCounter(levelId);
				triple.numString = em.instanceListDefinitions.get(numId)
						.GetCurrentNumberString(levelId);

				String font = em.instanceListDefinitions.get(numId).GetFont(
						levelId);

				if (font != null && !font.equals("")) {
					triple.numFont = font;
				}

				if (em.instanceListDefinitions.get(numId).IsBullet(levelId)) {
					triple.isBullet = true;
				}
			}
		}
		return triple;

    }

    /**
	 * The method used by the XSLT extension function during HTML export.
	 * 
	 * @param em
	 * @param levelId
	 * @param numId
	 * @return
	 */
    public static DocumentFragment getNumberXmlNode(Emulator em, String levelId, String numId) {

    	// TODO
    	
    	return null;
    	
    }
    
    private class ResultTriple {
    	
    	String numString;
    	
    	String numFont;
    	
    	boolean isBullet = false;
    }


    private class ListLevel
    {
        /// <summary>
        /// constructor used when abstract list levels are instantiated
        /// </summary>
        public ListLevel(Lvl levelNode)
        {
            this.id = levelNode.getIlvl().toString(); 

            Lvl.Start startValueNode = levelNode.getStart();
            if (startValueNode != null)
            {
            	this.startValue = startValueNode.getVal(); 
                this.counter = this.startValue;
            }

            Lvl.LvlText levelTextNode = levelNode.getLvlText();
            if (levelTextNode != null)
            {
                this.levelText = levelTextNode.getVal(); 
            }

            //XmlNode fontNode = levelNode.SelectSingleNode(".//w:rFonts", nsm);
            org.docx4j.wml.RFonts fontNode = levelNode.getRPr().getRFonts();
            if (fontNode != null)
            {
                this.font = fontNode.getHAnsi(); //getAttributeValue(fontNode, "w:hAnsi");
            }

            //XmlNode enumTypeNode = levelNode.SelectSingleNode("w:numFmt", nsm);
            
            NumFmt enumTypeNode = levelNode.getNumFmt();
            if (enumTypeNode != null)
            {
            	STNumberFormat type =  enumTypeNode.getVal(); // getAttributeValue(enumTypeNode, ValAttrName);
            		// TODO: alter schema 

                // w:numFmt="bullet" indicates a bulleted list
            	this.isBullet = type.equals( STNumberFormat.BULLET ); 
            	
                // this.isBullet = String.Compare(type, "bullet", StringComparison.OrdinalIgnoreCase) == 0;
            }

        }

        /// <summary>
        /// copy constructor
        /// </summary>
        /// <param name="masterCopy"></param>
        public ListLevel(ListLevel masterCopy)
        {
            this.id = masterCopy.id;
            this.levelText = masterCopy.levelText;
            this.startValue = masterCopy.startValue;
            this.counter = this.startValue;
            this.font = masterCopy.font;
            this.isBullet = masterCopy.isBullet;
        }

        /// <summary>
        /// Get overridden values
        /// </summary>
        /// <param name="levelNode"></param>
        /// <param name="nsm"></param>
        public void SetOverrides(Lvl levelNode)
        {
            Lvl.Start startValueNode = levelNode.getStart();
            if (startValueNode != null)
            {
            	this.startValue = startValueNode.getVal(); 
            }
            this.counter = this.startValue;

            Lvl.LvlText levelTextNode = levelNode.getLvlText();
            if (levelTextNode != null)
            {
                this.levelText = levelTextNode.getVal(); 
            }

            //XmlNode fontNode = levelNode.SelectSingleNode(".//w:rFonts", nsm);
            org.docx4j.wml.RFonts fontNode = levelNode.getRPr().getRFonts();
            if (fontNode != null)
            {
                this.font = fontNode.getHAnsi(); 
            }

            //XmlNode enumTypeNode = levelNode.SelectSingleNode("w:numFmt", nsm);            
            NumFmt enumTypeNode = levelNode.getNumFmt();
            if (enumTypeNode != null)
            {
            	STNumberFormat type =  enumTypeNode.getVal(); 
            		// TODO: alter schema 

                // w:numFmt="bullet" indicates a bulleted list
            	this.isBullet = type.equals( STNumberFormat.BULLET ); 
            }
        }

        private String id;

        /// <summary>
        /// returns the ID of the level
        /// </summary>
        public String getID()
        {
                return this.id;
        }

        private BigInteger startValue;

        /// <summary>
        /// start value of that level
        /// </summary>
        public BigInteger getStartValue()
        {
                return this.startValue;
        }

        private BigInteger counter;

        /// <summary>
        /// returns the current count of list items of that level
        /// </summary>
        public BigInteger getCurrentValue()
        {        	
                return this.counter;
        }

        /// <summary>
        /// increments the current count of list items of that level
        /// </summary>
        public void IncrementCounter()
        {
            this.counter = this.counter.add(BigInteger.ONE);            
        }

        /// <summary>
        /// resets the counter to the start value
        /// </summary>
        /// <id guid="823b5a3c-7501-4746-8dc4-7b098de5947a" />
        /// <owner alias="ROrleth" />
        public void ResetCounter()
        {
            this.counter = this.startValue;
        }

        private String levelText;

        /// <summary>
        /// returns the indicated lvlText value
        /// </summary>
        public String getLevelText()
        {
                return this.levelText;
        }

        private String font;

        /// <summary>
        /// returns the font name
        /// </summary>
        public String getFont()
        {
                return this.font;
        }

        private boolean isBullet;

        /// <summary>
        /// returns whether the enumeration type is a bulleted list or not
        /// </summary>
        public boolean IsBullet()
        {
                return this.isBullet;
        }
    }

    /// <summary>
    /// private helper class to deal with abstract number lists
    /// </summary>
    private class AbstractListNumberingDefinition
    {
        private HashMap<String, ListLevel> listLevels;

        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="abstractNumNode"></param>
        /// <param name="nsm"></param>
        public AbstractListNumberingDefinition(Numbering.AbstractNum abstractNumNode)
        {
//            String abstractNumString = getAttributeValue(abstractNumNode, "w:abstractNumId");
            String abstractNumString = abstractNumNode.getAbstractNumId().toString();

            if (abstractNumString!=null && !abstractNumString.equals("") )
            {
                this.abstractNumDefId = abstractNumString;

                this.readListLevelsFromAbsNode(abstractNumNode);

                // find out whether there is a linked abstractNum definition that this needs to be populated from later on
                //XmlNode linkedStyleNode = abstractNumNode.SelectSingleNode("./w:numStyleLink", nsm);                
                Numbering.AbstractNum.NumStyleLink linkedStyleNode = abstractNumNode.getNumStyleLink();

                if (linkedStyleNode != null)
                {
                    this.linkedStyleId = linkedStyleNode.getVal(); // getAttributeValue(linkedStyleNode, ValAttrName);
                }
            }
        }

        /// <summary>
        /// update the level definitions from a linked abstractNum node
        /// </summary>
        /// <param name="linkedNode">
        /// </param>
        /// <param name="nsm">
        /// </param>
        /// <id guid="36473168-7947-41ea-8210-839bf07eded7" />
        /// <owner alias="ROrleth" />
        public void UpdateDefinitionFromLinkedStyle(Numbering.AbstractNum linkedNode)
        {
        	// TODO - review this; it looks wrong!
        	
            if (!this.hasLinkedStyle() )
                return;

            this.readListLevelsFromAbsNode(linkedNode);
        }

        /// <id guid="0e05c34c-f257-4c76-8916-3059af84e333" />
        /// <owner alias="ROrleth" />
        private void readListLevelsFromAbsNode(Numbering.AbstractNum abstractNumNode)
        {
            //XmlNodeList levelNodes = absNumNode.SelectNodes("./w:lvl", nsm);

        	List<Lvl> levelNodes = abstractNumNode.getLvl(); 
            if (this.listLevels == null)
            {
                this.listLevels = new HashMap<String, ListLevel>(levelNodes.size());
            }

            // loop through the levels it defines and instantiate those
            //foreach (XmlNode levelNode in levelNodes)
            for ( Lvl levelNode : levelNodes )
            {
                ListLevel level = new ListLevel(levelNode);

                this.listLevels.put(level.id, level);
            }
        }

        private String linkedStyleId;

        /// <summary>
        /// returnts the ID of the linked style
        /// </summary>
        /// <id guid="ae2caeec-2d86-4e5f-b816-d508f6f2c893" />
        /// <owner alias="ROrleth" />
        public String getLinkedStyleId()
        {
                return this.linkedStyleId;
        }

        /// <summary>
        /// indicates whether there is a linked style
        /// </summary>
        /// <id guid="75d74788-9839-448e-ae23-02d40e013d98" />
        /// <owner alias="ROrleth" />
        public boolean hasLinkedStyle()
        {
        	if (this.linkedStyleId!=null && !this.linkedStyleId.equals("")  ) {
        		return true;
        	} else {
        		return false;
        	}
                //return !String.IsNullOrEmpty(this.linkedStyleId);
        }


        private String abstractNumDefId;

        /// <summary>
        /// returns the ID of this abstract number list definition
        /// </summary>
        public String getID() 
        {
                return this.abstractNumDefId;
        }

        public HashMap<String, ListLevel> getListLevels()
        {
                return this.listLevels;
        }

        public int getLevelCount() 
        {
                if (this.listLevels != null)
                    return this.listLevels.size();
                else
                    return 0;
        }
    }

    /// <summary>
    /// private helper class to deal with number lists
    /// </summary>
    private class ListNumberingDefinition
    {
        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="numNode"></param>
        /// <param name="nsm"></param>
        /// <param name="abstractListDefinitions"></param>
        public ListNumberingDefinition(Numbering.Num numNode, 
        		HashMap<String, AbstractListNumberingDefinition> abstractListDefinitions)
        {
            this.listNumberId =  numNode.getNumId().toString(); //getAttributeValue(numNode, "w:numId");

            //XmlNode abstractNumNode = numNode.SelectSingleNode("./w:abstractNumId", nsm);
            Numbering.Num.AbstractNumId abstractNumNode = numNode.getAbstractNumId();
            if (abstractNumNode != null)
            {
                this.abstractListDefinition = abstractListDefinitions.get(abstractNumNode.getVal().toString() ); //[getAttributeValue(abstractNumNode, ValAttrName)];

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
                if (levelOverrideNodes != null)
                {
                    for (Numbering.Num.LvlOverride overrideNode : levelOverrideNodes)
                    {
                        //XmlNode node = overrideNode.SelectSingleNode("./w:lvl", nsm);
                    	Lvl node = overrideNode.getLvl();
                        if (node != null)
                        {
                            String overrideLevelId = node.getIlvl().toString(); //getAttributeValue(node, "w:ilvl");

                            if (overrideLevelId!=null && !overrideLevelId.equals("") )
                            {
                                this.levels.get(overrideLevelId).SetOverrides(node);
                            }
                        }
                    }
                }
            }
        }

        private AbstractListNumberingDefinition abstractListDefinition;
        private HashMap<String, ListLevel> levels;

        /// <summary>
        /// increment the occurrence count of the specified level, reset the occurrence count of derived levels
        /// </summary>
        /// <param name="level"></param>
        public void IncrementCounter(String level)
        {
            this.levels.get(level).IncrementCounter();

            // here's a bit where the decision to use Strings as level IDs was bad 
            // - I need to loop through the derived levels and reset their counters
            //UInt32 levelNumber = System.Convert.ToUInt32(level, CultureInfo.InvariantCulture) + 1;
            int levelNumber = Integer.parseInt(level)+1;
            String levelString =  Integer.toString(levelNumber);

            while (this.levels.containsKey(levelString))
            {
                this.levels.get(level).ResetCounter();
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

        /// <summary>
        /// returns a String containing the current state of the counters, up to the indicated level
        /// </summary>
        /// <param name="level"></param>
        /// <returns></returns>
        public String GetCurrentNumberString(String level)
        {
            String formatString = this.levels.get(level).levelText;
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
                        result.append(this.levels.get( Integer.toString(levelId) ).toString());
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
            return this.levels.get(level).font;
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


}
