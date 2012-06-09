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
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.PPrBase.NumPr;

/**
 * Represents:
 * 
	  <w:abstractNum w:abstractNumId="0">
	    <w:nsid w:val="2DD860C0"/>
	    <w:multiLevelType w:val="multilevel"/>
	    <w:tmpl w:val="0409001D"/>
	    <w:lvl w:ilvl="0">
	      <w:start w:val="1"/>
	      <w:numFmt w:val="decimal"/>
	      <w:lvlText w:val="%1)"/>
	      <w:lvlJc w:val="left"/>
	      <w:pPr>
	        <w:ind w:left="360" w:hanging="360"/>
	      </w:pPr>
	    </w:lvl>
	    <w:lvl w:ilvl="1">
	      <w:start w:val="1"/>
	      <w:numFmt w:val="lowerLetter"/>
	      <w:lvlText w:val="%2)"/>
	      <w:lvlJc w:val="left"/>
	      <w:pPr>
	        <w:ind w:left="720" w:hanging="360"/>
	      </w:pPr>
	    </w:lvl>
	    etc
	    
	    (layered on top of the JAXB object representing same)
    
 */
public class AbstractListNumberingDefinition {
	
	protected static Logger log = Logger.getLogger(AbstractListNumberingDefinition.class);
	
        private HashMap<String, ListLevel> listLevels;

        private Numbering.AbstractNum abstractNumNode;
        /**
         * The underling org.docx4j JAXB object
         * @return
         */
        public Numbering.AbstractNum getAbstractNumNode() {
        	return abstractNumNode;
        }
        
        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="abstractNumNode"></param>
        /// <param name="nsm"></param>
        public AbstractListNumberingDefinition(Numbering.AbstractNum abstractNumNode)
        {

        	this.abstractNumNode = abstractNumNode;
        	
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

                this.listLevels.put(level.getID(), level);
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
