/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.rangedifferencer;

/**
 * Description of a change between two or three ranges of comparable entities.
 * <p>
 * <code>RangeDifference</code> objects are the elements of a compare result
 * returned from the <code>RangeDifferencer</code> <code>find* </code>
 * methods. Clients use these objects as they are returned from the differencer.
 * This class is not intended to be instantiated or subclassed outside of the
 * Compare framework.
 * <p>
 * Note: A range in the <code>RangeDifference</code> object is given as a
 * start index and length in terms of comparable entities. However, these entity
 * indices and counts are not necessarily character positions. For example, if
 * an entity represents a line in a document, the start index would be a line
 * number and the count would be in lines.
 * </p>
 * 
 * @see RangeDifferencer
 */
public class RangeDifference {

    /** Two-way change constant indicating no change. */
    public final static int NOCHANGE = 0;

    /**
     * Two-way change constant indicating two-way change (same as
     * <code>RIGHT</code>)
     */
    public final static int CHANGE = 2;

    /** Three-way change constant indicating a change in both right and left. */
    public final static int CONFLICT = 1;

    /** Three-way change constant indicating a change in right. */
    public final static int RIGHT = 2;

    /** Three-way change constant indicating a change in left. */
    public final static int LEFT = 3;

    /**
     * Three-way change constant indicating the same change in both right and
     * left, that is only the ancestor is different.
     */
    public final static int ANCESTOR = 4;

    /** Constant indicating an unknown change kind. */
    public final static int ERROR = 5;

    /**
     * the kind of change: NOCHANGE, CHANGE, LEFT, RIGHT, ANCESTOR, CONFLICT,
     * ERROR
     */
    int fKind;

    int fLeftStart;

    int fLeftLength;

    int fRightStart;

    int fRightLength;

    int lAncestorStart;

    int lAncestorLength;

    /**
     * Creates a new range difference with the given change kind.
     * 
     * @param changeKind
     *                the kind of change
     */
    /* package */RangeDifference(int changeKind) {
        fKind = changeKind;
    }

    /**
     * Creates a new <code>RangeDifference</code> with the given change kind
     * and left and right ranges.
     * 
     * @param kind
     *                the kind of change
     * @param rightStart
     *                start index of entity on right side
     * @param rightLength
     *                number of entities on right side
     * @param leftStart
     *                start index of entity on left side
     * @param leftLength
     *                number of entities on left side
     */
    public RangeDifference(int kind, int rightStart, int rightLength,
            int leftStart, int leftLength) {
        fKind = kind;
        fRightStart = rightStart;
        fRightLength = rightLength;
        fLeftStart = leftStart;
        fLeftLength = leftLength;
    }

    /**
     * Creates a new <code>RangeDifference</code> with the given change kind
     * and left, right, and ancestor ranges.
     * 
     * @param kind
     *                the kind of change
     * @param rightStart
     *                start index of entity on right side
     * @param rightLength
     *                number of entities on right side
     * @param leftStart
     *                start index of entity on left side
     * @param leftLength
     *                number of entities on left side
     * @param ancestorStart
     *                start index of entity on ancestor side
     * @param ancestorLength
     *                number of entities on ancestor side
     */
    /* package */RangeDifference(int kind, int rightStart, int rightLength,
            int leftStart, int leftLength, int ancestorStart, int ancestorLength) {
        this(kind, rightStart, rightLength, leftStart, leftLength);
        lAncestorStart = ancestorStart;
        lAncestorLength = ancestorLength;
    }

    /**
     * Returns the kind of difference.
     * 
     * @return the kind of difference, one of <code>NOCHANGE</code>,
     *         <code>CHANGE</code>, <code>LEFT</code>, <code>RIGHT</code>,
     *         <code>ANCESTOR</code>, <code>CONFLICT</code>,
     *         <code>ERROR</code>
     */
    public int kind() {
        return fKind;
    }

    public String kindString() {
        
		if (fKind==NOCHANGE) {
			return "NOCHANGE";
		} else if (fKind==CHANGE) {
			return "CHANGE";
		} else if (fKind==LEFT) {
			return "LEFT";
		} else if (fKind==RIGHT) {
			return "RIGHT";
		} else if (fKind==ANCESTOR) {
			return "ANCESTOR";
		} else if (fKind==CONFLICT) {
			return "CONFLICT";
		} else if (fKind==ERROR) {
			return "ERROR";
		} else return "???";
    }
    
    /**
     * Returns the start index of the entity range on the ancestor side.
     * 
     * @return the start index of the entity range on the ancestor side
     */
    public int ancestorStart() {
        return lAncestorStart;
    }

    /**
     * Returns the number of entities on the ancestor side.
     * 
     * @return the number of entities on the ancestor side
     */
    public int ancestorLength() {
        return lAncestorLength;
    }

    /**
     * Returns the end index of the entity range on the ancestor side.
     * 
     * @return the end index of the entity range on the ancestor side
     */
    public int ancestorEnd() {
        return lAncestorStart + lAncestorLength;
    }

    /**
     * Returns the start index of the entity range on the right side.
     * 
     * @return the start index of the entity range on the right side
     */
    public int rightStart() {
        return fRightStart;
    }

    /**
     * Returns the number of entities on the right side.
     * 
     * @return the number of entities on the right side
     */
    public int rightLength() {
        return fRightLength;
    }

    /**
     * Returns the end index of the entity range on the right side.
     * 
     * @return the end index of the entity range on the right side
     */
    public int rightEnd() {
        return fRightStart + fRightLength;
    }

    /**
     * Returns the start index of the entity range on the left side.
     * 
     * @return the start index of the entity range on the left side
     */
    public int leftStart() {
        return fLeftStart;
    }

    /**
     * Returns the number of entities on the left side.
     * 
     * @return the number of entities on the left side
     */
    public int leftLength() {
        return fLeftLength;
    }

    /**
     * Returns the end index of the entity range on the left side.
     * 
     * @return the end index of the entity range on the left side
     */
    public int leftEnd() {
        return fLeftStart + fLeftLength;
    }

    /**
     * Returns the maximum number of entities in the left, right, and ancestor
     * sides of this range.
     * 
     * @return the maximum number of entities in the left, right, and ancestor
     *         sides of this range
     */
    public int maxLength() {
        return Math.max(fRightLength, Math.max(fLeftLength, lAncestorLength));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RangeDifference) {
            RangeDifference other = (RangeDifference) obj;
            return fKind == other.fKind && fLeftStart == other.fLeftStart
                    && fLeftLength == other.fLeftLength
                    && fRightStart == other.fRightStart
                    && fRightLength == other.fRightLength
                    && lAncestorStart == other.lAncestorStart
                    && lAncestorLength == other.lAncestorLength;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        String string = "Left: " + toRangeString(fLeftStart, fLeftLength) + " Right: " + toRangeString(fRightStart, fRightLength); //$NON-NLS-1$ //$NON-NLS-2$
        if (lAncestorLength > 0 || lAncestorStart > 0)
            string += " Ancestor: " + toRangeString(lAncestorStart, lAncestorLength); //$NON-NLS-1$
        return string;
    }

    private String toRangeString(int start, int length) {
        return "(" + start + ", " + length + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
