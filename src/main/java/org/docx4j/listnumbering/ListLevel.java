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

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.PPrBase.NumPr;

public class ListLevel {
	
	
	protected static Logger log = Logger.getLogger(ListLevel.class);
	
	private Lvl jaxbAbstractLvl;
	public Lvl getJaxbAbstractLvl() {
		return jaxbAbstractLvl;
	}

	private Lvl jaxbOverrideLvl;
	public Lvl getJaxbOverrideLvl() {
		return jaxbOverrideLvl;
	}
	
        public ListLevel(Lvl levelNode)
        {
        	this.jaxbAbstractLvl = levelNode;
        	
            this.id = levelNode.getIlvl().toString(); 

            Lvl.Start startValueNode = levelNode.getStart();
            if (startValueNode != null)
            {
            	this.startValue = startValueNode.getVal().subtract(BigInteger.ONE);
            		// Start value is one less than the user set it to,
            		// since whenever we fetch the number, we first increment it.
                this.counter = this.startValue;
            }

            Lvl.LvlText levelTextNode = levelNode.getLvlText();
            if (levelTextNode != null)
            {
                this.levelText = levelTextNode.getVal(); 
            }

            org.docx4j.wml.RFonts fontNode = null;
            if (levelNode.getRPr() != null) {
                //XmlNode fontNode = levelNode.SelectSingleNode(".//w:rFonts", nsm);
            	fontNode = levelNode.getRPr().getRFonts();
            }
            if (fontNode != null)
            {
                this.font = fontNode.getHAnsi(); //getAttributeValue(fontNode, "w:hAnsi");
            }

            //XmlNode enumTypeNode = levelNode.SelectSingleNode("w:numFmt", nsm);
            
            NumFmt enumTypeNode = levelNode.getNumFmt();
            if (enumTypeNode != null)
            {
            	this.numFmt =  enumTypeNode.getVal(); 

                // w:numFmt="bullet" indicates a bulleted list
            	this.isBullet = numFmt.equals( NumberFormat.BULLET ); 
            	
                // this.isBullet = String.Compare(type, "bullet", StringComparison.OrdinalIgnoreCase) == 0;
            }

        }

        /// <summary>
        /// copy constructor
        /// </summary>
        /// <param name="masterCopy"></param>
        public ListLevel(ListLevel masterCopy)
        {
        	this.jaxbAbstractLvl = masterCopy.jaxbAbstractLvl;
        	
            this.id = masterCopy.id;
            this.levelText = masterCopy.levelText;
            this.startValue = masterCopy.startValue;
            this.counter = this.startValue;
            this.font = masterCopy.font;
            this.isBullet = masterCopy.isBullet;
            this.numFmt = masterCopy.numFmt;
        }

        /// <summary>
        /// Get overridden values
        /// </summary>
        /// <param name="levelNode"></param>
        /// <param name="nsm"></param>
        public void SetOverrides(Lvl levelNode)
        {
        	this.jaxbOverrideLvl = levelNode;
        	
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
            	this.numFmt =  enumTypeNode.getVal(); 

                // w:numFmt="bullet" indicates a bulleted list
            	this.isBullet = numFmt.equals( NumberFormat.BULLET ); 
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

        private BigInteger startValue = BigInteger.ZERO;

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
        public BigInteger getCurrentValueRaw()
        {        	
                return this.counter;
        }

        /**
         * The current number, formatted using numFmt.
         */
        public String getCurrentValueFormatted()
        {
        	/*
        	 * If you look at the OpenXML spec or
        	 * STNumberFormat.java, you'll see there are some 60 number formats.
        	 * 
        	 * Of these, we currently aim to support:
        	 * 
			 *     decimal
			 *     upperRoman
			 *     lowerRoman
			 *     upperLetter
			 *     lowerLetter
			 *     bullet
			 *     none
			 *     
			 * What about?
			 *     
			 *     ordinal
			 *     cardinalText
			 *     ordinalText
        	 * 
        	 */
        	
        	if (numFmt.equals( NumberFormat.DECIMAL ) ) {
        		return this.counter.toString();
        	}
        	
        	if (numFmt.equals( NumberFormat.NONE ) ) {
        		return "";        		
        	}

        	if (numFmt.equals( NumberFormat.BULLET ) ) {
        		
        		// TODO - revisit how this is handled.
        		// The code elsewhere for handling bullets
        		// overlaps with this numFmt stuff.
        		return "*";        		
        	}
        	        	
        	int current = this.counter.intValue();
        	
        	if (numFmt.equals( NumberFormat.UPPER_ROMAN ) ) {        		
        		NumberFormatRomanUpper converter = new NumberFormatRomanUpper(); 
        		return converter.format(current);
        	}
        	if (numFmt.equals( NumberFormat.LOWER_ROMAN ) ) {        		
        		NumberFormatRomanLower converter = new NumberFormatRomanLower(); 
        		return converter.format(current);
        	}
        	if (numFmt.equals( NumberFormat.LOWER_LETTER ) ) {        		
        		NumberFormatLowerLetter converter = new NumberFormatLowerLetter(); 
        		return converter.format(current);
        	}
        	if (numFmt.equals( NumberFormat.UPPER_LETTER ) ) {        		
        		NumberFormatLowerLetter converter = new NumberFormatLowerLetter(); 
        		return converter.format(current).toUpperCase();
        	}        	
        	
        	log.error("Unhandled numFmt: " + numFmt.name() );
            return this.counter.toString();
        }
        
        
        /// <summary>
        /// increments the current count of list items of that level
        /// </summary>
        public void IncrementCounter()
        {
            this.counter = this.counter.add(BigInteger.ONE); 
            
            log.debug("counter now: " + this.counter.toString() );
            
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
        
        private NumberFormat numFmt; // TODO: alter schema 
        // w:numFmt = RTF's \levelnfcN

//		private void setNumFmt(STNumberFormat numFmt) {
//			this.numFmt = numFmt;
//		}
		private NumberFormat getNumFmt() {
			return numFmt;
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

