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

import java.util.HashMap;
import java.util.List;

import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	protected static Logger log = LoggerFactory.getLogger(AbstractListNumberingDefinition.class);
	
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
                // NDP.resolveLinkedAbstractNum
                Numbering.AbstractNum.NumStyleLink linkedStyleNode = abstractNumNode.getNumStyleLink();

                if (linkedStyleNode != null)
                {
                    this.linkedStyleId = linkedStyleNode.getVal(); // getAttributeValue(linkedStyleNode, ValAttrName);
                }
            }
        }

        public void updateDefinitionFromLinkedStyle(Numbering.AbstractNum linkedNode)
        {
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
            for ( Lvl levelNode : levelNodes )  {
            	readLevel(levelNode);
            }
        }
        
        /**
         * @param levelNode
         * @since 3.3.6
         */
        public void readLevel(Lvl levelNode) {

            ListLevel level = new ListLevel(levelNode);
            this.listLevels.put(level.getID(), level);
        	
        }

        private String linkedStyleId;
        public String getLinkedStyleId() {
            return this.linkedStyleId;
        }

        public boolean hasLinkedStyle()
        {
        	if (this.linkedStyleId!=null && !this.linkedStyleId.equals("")  ) {
        		return true;
        	} else {
        		return false;
        	}
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
