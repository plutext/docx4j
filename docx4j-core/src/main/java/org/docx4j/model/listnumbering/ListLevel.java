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

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.NumberFormat;

public class ListLevel {
		
	/* TODO 2011 02 23: in addition to having numbering in the 
	 * Main document part, you can have numbering in other 
	 * stories:
	 *   - headers/footers
	 *   - comments
	 *   - footnotes/endnotes
	 * This means that ListLevel should have independent counters
	 * for each story, or the there should be a ListLevel defined
	 * for each story! 
	 * 
	 * 2012 01 10: numbering set up on a per-part basis
	 * seems the most sensible approach.
	 * 
	 */
	
	protected static Logger log = LoggerFactory.getLogger(ListLevel.class);
	
	private Lvl jaxbAbstractLvl;
	public Lvl getJaxbAbstractLvl() {
		return jaxbAbstractLvl;
	}

	private Lvl jaxbOverrideLvl;
	public Lvl getJaxbOverrideLvl() {
		return jaxbOverrideLvl;
	}
	
	/*
	 * Since 17.1.1 (CR-014 phase 4) a level holds no counter: the counters live in
	 * a NumberingState, keyed by the referencing abstract list and the level - which
	 * is the sharing every instance definition of one abstract level had through the
	 * single Counter they used to reference - and a traversal passes its state in.
	 * The no-state overloads use the NumberingDefinitionsPart's default state, as
	 * they always did, through the supplier the part installs.
	 */

	/** The referencing {@code w:abstractNum} this level belongs to (its counter's key). */
	private String abstractNumId;

	void setAbstractNumId(String abstractNumId) {
		this.abstractNumId = abstractNumId;
	}

	/** The id of the referencing {@code w:abstractNum} this level was read for.  @since 17.1.1 */
	public String getAbstractNumId() {
		return abstractNumId;
	}

	/** The {@code w:num} this instance level belongs to; null for an abstract level. */
	private String ownerNumId;

	void setOwnerNumId(String numId) {
		this.ownerNumId = numId;
	}

	private java.util.function.Supplier<NumberingState> defaultStateSupplier;
	private NumberingState orphanState;

	void setDefaultStateSupplier(java.util.function.Supplier<NumberingState> supplier) {
		this.defaultStateSupplier = supplier;
	}

	/** The state the no-state overloads use: the numbering part's, or - for a level
	 *  built outside any part - one of its own. */
	NumberingState defaultState() {
		if (defaultStateSupplier != null) {
			NumberingState s = defaultStateSupplier.get();
			if (s != null) return s;
		}
		if (orphanState == null) orphanState = new NumberingState();
		return orphanState;
	}

	/** This level's counter in the given state. */
	Counter counter(NumberingState state) {
		return state.counter(abstractNumId, id, startValue);
	}

	/** This level's counter in the default state.
	 *  @deprecated since 17.1.1: counters belong to a {@link NumberingState}; use {@link #counter}. */
	@Deprecated
	protected Counter getCounter() {
		return counter(defaultState());
	}
	

	/**
     * Constructor for a ListLevel in an abstract definition.
     */
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
        }

        if (levelNode.getLvlRestart() != null && levelNode.getLvlRestart().getVal() != null) {
        	this.lvlRestart = levelNode.getLvlRestart().getVal().intValue();
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

    /** 
     * Constructor for a ListLevel in an instance definition.
     */
    public ListLevel(ListLevel masterCopy)
    {
    	this.jaxbAbstractLvl = masterCopy.jaxbAbstractLvl;
    	
        this.id = masterCopy.id;
        this.levelText = masterCopy.levelText;
        this.startValue = masterCopy.startValue;
        this.abstractNumId = masterCopy.abstractNumId;  // the counter is shared through the state, by this key
        this.defaultStateSupplier = masterCopy.defaultStateSupplier;
        this.font = masterCopy.font;
        this.isBullet = masterCopy.isBullet;
        this.numFmt = masterCopy.numFmt;
        this.lvlRestart = masterCopy.lvlRestart;
    }

    /**
     * Get overridden values
     * @param levelNode
     */
    public void SetOverrides(Lvl levelNode)
    {
    	this.jaxbOverrideLvl = levelNode;
    	
        Lvl.Start startValueNode = levelNode.getStart();
        if (startValueNode != null)
        {
        	this.startValue = startValueNode.getVal().subtract(BigInteger.ONE);
    		// Start value is one less than the user set it to,
    		// since whenever we fetch the number, we first increment it.
        }

        if (levelNode.getLvlRestart() != null && levelNode.getLvlRestart().getVal() != null) {
        	this.lvlRestart = levelNode.getLvlRestart().getVal().intValue();
        }

        Lvl.LvlText levelTextNode = levelNode.getLvlText();
        if (levelTextNode != null)
        {
            this.levelText = levelTextNode.getVal(); 
        }

        //XmlNode fontNode = levelNode.SelectSingleNode(".//w:rFonts", nsm);
        org.docx4j.wml.RFonts fontNode =null;
        if (levelNode.getRPr() != null) {
        	fontNode = levelNode.getRPr().getRFonts();
        }
        
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

    /**
     * returns the ID of the level
     * @return
     */
    public String getID()
    {
            return this.id;
    }

    private BigInteger startValue = BigInteger.ZERO;

    public void setStartValue(BigInteger startValue) {
		this.startValue = startValue;
    	startAtUsed = false;
    	hasStartOverride = true;
	}

    /** This instance level carries a {@code w:startOverride}, to be applied to the
     *  shared counter the first time its {@code w:num} is met at this level in a story. */
    private boolean hasStartOverride = false;

	/**
     * start value of that level
     * @return
     */
    public BigInteger getStartValue()
    {
            return this.startValue;
    }


    /**
     * The current number, formatted using numFmt.
     */
    public String getCurrentValueFormatted()
    {    	
    	return getCurrentValueFormatted(defaultState(), null);
    }    
    /**
     * The current number, formatted using numFmt; {@code where} (the numId this
     * level is being formatted for) is named in the one-time warning if the
     * format cannot express the value and the decimal label is used instead.
     * @since 17.1.1
     */
    public String getCurrentValueFormatted(String where)
    {
    	return getCurrentValueFormatted(defaultState(), where);
    }
    /** The current number in the given state, formatted using numFmt.  @since 17.1.1 */
    public String getCurrentValueFormatted(NumberingState state)
    {
    	return getCurrentValueFormatted(state, null);
    }
    /** The current number in the given state, formatted using numFmt; {@code where}
     *  as for {@link #getCurrentValueFormatted(String)}.  @since 17.1.1 */
    public String getCurrentValueFormatted(NumberingState state, String where)
    {
    	return NumberFormatter.getCurrentValueFormatted(numFmt, counter(state).getCurrentValue().intValue(),
    			where == null ? null : where + " ilvl " + id);
    }
    public String getCurrentValueUnformatted()
    {        	
        return getCurrentValueUnformatted(defaultState());
    }    
    /** The current number in the given state, as a decimal.  @since 17.1.1 */
    public String getCurrentValueUnformatted(NumberingState state)
    {
        return counter(state).getCurrentValue().toString();
    }
    
    /**
     * increments the current count of list items of that level 
     */
    public void IncrementCounter()
    {
    	IncrementCounter(defaultState());
    }

    /**
     * Increments the count of list items at this level in the given state.  The
     * shared counter takes this level's start value the first time it is met in the
     * state, and again the first time this instance level's {@code w:num} is met
     * there when the {@code w:num} overrides the start (deferred until then, since
     * otherwise earlier numbering over the same abstract list would use it).
     *
     * @since 17.1.1
     */
    public void IncrementCounter(NumberingState state)
    {
    	Counter counter = counter(state);
    	boolean overridePending = hasStartOverride && ownerNumId != null
    			&& !state.startOverrideApplied(ownerNumId, id);
    	if (overridePending || !counter.encounteredAlready) {
        	counter.setCurrentValue(this.startValue); 
        	log.debug("not encounteredAlready; set to startValue " + startValue);
        	counter.encounteredAlready = true;
        	counter.resetPending = false;
        	if (ownerNumId != null) state.markStartOverrideApplied(ownerNumId, id);
    	}
    	if (counter.resetPending) {
    		// the reset already placed the counter at its start value (see ResetCounter)
    		counter.resetPending = false;
    		return;
    	}
        counter.IncrementCounter();
    }
    
	/** @deprecated since 17.1.1: unused; whether a start override has been applied is
	 *  per {@link NumberingState}. */
	@Deprecated
	protected boolean startAtUsed = true;
    

    /**
     * Resets the counter, a shallower level having been used: the level shows its
     * start value from now until it is next used, and that first use does not
     * increment it.
     *
     * <p>Before 17.1.1 the counter went to start-1 and a deeper label printed it so:
     * a level-2 item straight after a level-0 one read "2.0.1" where Word reads
     * "2.1.1" (CR-014 probe P8, measured).</p>
     */
    public void ResetCounter()
    {
        ResetCounter(defaultState());
    }

    /** {@link #ResetCounter()}, in the given state.  @since 17.1.1 */
    public void ResetCounter(NumberingState state)
    {
    	Counter counter = counter(state);
        counter.setCurrentValue(this.startValue.add(BigInteger.ONE));
        counter.resetPending = true;
    }

    private Integer lvlRestart;

    /**
     * The level's {@code w:lvlRestart} value (ECMA-376 17.9.11), or null where it
     * states none: the 1-based number of the shallowest level whose use restarts
     * this one; 0 means it never restarts.
     *
     * @since 17.1.1
     */
    public Integer getLvlRestart() {
    	return lvlRestart;
    }

    /**
     * Whether this level's counter restarts when the given shallower level
     * ({@code shallowerIlvl}, 0-based) is used.  Without {@code w:lvlRestart} any
     * shallower level restarts it; {@code w:lvlRestart w:val="0"} means none does;
     * {@code w:val="n"} means levels 1..n (1-based, so ilvl 0..n-1) do and deeper
     * ones do not.  Measured (CR-014 probe P8): with {@code w:val="1"} on level 2, a
     * level-1 item leaves it counting (1.2.3) and a level-0 item restarts it
     * (2.1.1); with {@code w:val="0"} neither does (1.2.3, 2.1.4).
     *
     * @since 17.1.1
     */
    public boolean restartsAfter(int shallowerIlvl) {
    	if (lvlRestart == null) return true;
    	if (lvlRestart.intValue() <= 0) return false;
    	return shallowerIlvl <= lvlRestart.intValue() - 1;
    }

    private String levelText;

    /**
     * returns the indicated lvlText value
     * @return
     */
    public String getLevelText()
    {
            return this.levelText;
    }

    private String font;

    /**
     * returns the font name
     * @return
     */
	@Deprecated
    public String getFont()
    {
            return this.font;
    }
    
    private NumberFormat numFmt; // TODO: alter schema 
    // w:numFmt = RTF's \levelnfcN

//		private void setNumFmt(STNumberFormat numFmt) {
//			this.numFmt = numFmt;
//		}
	public NumberFormat getNumFmt() {
		return numFmt;
	}

    private boolean isBullet;

    /**
     * returns whether the enumeration type is a bulleted list or not
     * 
     * @return
     */
    public boolean IsBullet()
    {
            return this.isBullet;
    }
    
    /** A level's count in one {@link NumberingState}.  Static since 17.1.1. */
    protected static class Counter {
    	
    	protected boolean encounteredAlready = false;

    	/** The counter was reset by a shallower level and holds its start value;
    	 *  the next use of the level takes that value rather than incrementing. */
    	protected boolean resetPending = false;
    	
        protected boolean isEncounteredAlready() {
    		return encounteredAlready;
    	}
    	
    	
        private BigInteger currentValue;
        
        Counter() {
        	currentValue = BigInteger.ZERO;
        }

        Counter copy() {
        	Counter c = new Counter();
        	c.currentValue = currentValue;
        	c.encounteredAlready = encounteredAlready;
        	c.resetPending = resetPending;
        	return c;
        }

        @Override
        public String toString() {
        	return currentValue + (encounteredAlready ? "" : " (unused)") + (resetPending ? " (reset)" : "");
        }

        public void setCurrentValue(BigInteger currentValue) {
			this.currentValue = currentValue;
		}

		/**
         * returns the current count of list items of that level
         * @return
         */
        public BigInteger getCurrentValue()
        {        	
        	log.debug("counter: " + currentValue.intValue() );
            return this.currentValue;
        }
    	
        /**
         * increments the current count of list items of that level 
         */
        public void IncrementCounter()
        {
        	setCurrentValue( currentValue.add(BigInteger.ONE)); 
            
            log.debug("counter now: " + currentValue.intValue() );
            
        }
    	
    }

}

