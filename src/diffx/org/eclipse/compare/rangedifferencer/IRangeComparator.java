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
 * For breaking an object to compare into a sequence of comparable entities.
 * <p>
 * It is used by <code>RangeDifferencer</code> to find longest sequences of
 * matching and non-matching ranges.
 * <p>
 * For example, to compare two text documents and find longest common sequences
 * of matching and non-matching lines, the implementation must break the
 * document into lines. <code>getRangeCount</code> would return the number of
 * lines in the document, and <code>rangesEqual</code> would compare a
 * specified line given with one in another <code>IRangeComparator</code>.
 * </p>
 * <p>
 * Clients should implement this interface; there is no standard implementation.
 * </p>
 */
public interface IRangeComparator {

    /**
     * Returns the number of comparable entities.
     * 
     * @return the number of comparable entities
     */
    int getRangeCount();

    /**
     * Returns whether the comparable entity given by the first index matches an
     * entity specified by the other <code>IRangeComparator</code> and index.
     * 
     * @param thisIndex
     *                the index of the comparable entity within this
     *                <code>IRangeComparator</code>
     * @param other
     *                the IRangeComparator to compare this with
     * @param otherIndex
     *                the index of the comparable entity within the other
     *                <code>IRangeComparator</code>
     * @return <code>true</code> if the comparable entities are equal
     */
    boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex);

    /**
     * Returns whether a comparison should be skipped because it would be too
     * costly (or lengthy).
     * 
     * @param length
     *                a number on which to base the decision whether to return
     *                <code>true</code> or <code>false</code>
     * @param maxLength
     *                another number on which to base the decision whether to
     *                return <code>true</code> or <code>false</code>
     * @param other
     *                the other <code>IRangeComparator</code> to compare with
     * @return <code>true</code> to avoid a too lengthy range comparison
     */
    boolean skipRangeComparison(int length, int maxLength,
            IRangeComparator other);
}
