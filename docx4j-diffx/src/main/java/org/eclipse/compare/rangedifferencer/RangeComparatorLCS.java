/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.rangedifferencer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.internal.CompareMessages;
import org.eclipse.compare.internal.LCS;
import org.eclipse.compare.internal.LCSSettings;

/* package */class RangeComparatorLCS extends LCS {

    private final IRangeComparator comparator1, comparator2;

    private int[][] lcs;

    public static RangeDifference[] findDifferences(
            LCSSettings settings, IRangeComparator left, IRangeComparator right) {
        RangeComparatorLCS lcs = new RangeComparatorLCS(left, right);
        lcs.longestCommonSubsequence( settings);
        return lcs.getDifferences();
    }

    public RangeComparatorLCS(IRangeComparator comparator1,
            IRangeComparator comparator2) {
        this.comparator1 = comparator1;
        this.comparator2 = comparator2;
    }

    @Override
    protected int getLength1() {
        return comparator1.getRangeCount();
    }

    @Override
    protected int getLength2() {
        return comparator2.getRangeCount();
    }

    @Override
    protected void initializeLcs(int lcsLength) {
        lcs = new int[2][lcsLength];
    }

    @Override
    protected boolean isRangeEqual(int i1, int i2) {
        return comparator1.rangesEqual(i1, comparator2, i2);
    }

    @Override
    protected void setLcs(int sl1, int sl2) {
        // Add one to the values so that 0 can mean that the slot is empty
        lcs[0][sl1] = sl1 + 1;
        lcs[1][sl1] = sl2 + 1;
    }

    public RangeDifference[] getDifferences() {
    	
        List differences = new ArrayList();
        int length = getLength();
        if (length == 0) {
            differences.add(new RangeDifference(RangeDifference.CHANGE, 0,
                    comparator2.getRangeCount(), 0, comparator1
                            .getRangeCount()));
        } else {
            int index1, index2;
            index1 = index2 = 0;
            int l1, l2;
            int s1 = -1;
            int s2 = -1;
            while (index1 < lcs[0].length && index2 < lcs[1].length) {
                // Move both LCS lists to the next occupied slot
                while ((l1 = lcs[0][index1]) == 0) {
                    index1++;
                    if (index1 >= lcs[0].length)
                        break;
                }
                if (index1 >= lcs[0].length)
                    break;
                while ((l2 = lcs[1][index2]) == 0) {
                    index2++;
                    if (index2 >= lcs[1].length)
                        break;
                }
                if (index2 >= lcs[1].length)
                    break;
                // Convert the entry to an array index (see setLcs(int,
                // int))
                int end1 = l1 - 1;
                int end2 = l2 - 1;
                if (s1 == -1 && (end1 != 0 || end2 != 0)) {
                    // There is a diff at the beginning
                    // TODO: We need to conform that this is the proper
                    // order
                    differences.add(new RangeDifference(
                            RangeDifference.CHANGE, 0, end2, 0, end1));
                } else if (end1 != s1 + 1 || end2 != s2 + 1) {
                    // A diff was found on one of the sides
                    int leftStart = s1 + 1;
                    int leftLength = end1 - leftStart;
                    int rightStart = s2 + 1;
                    int rightLength = end2 - rightStart;
                    // TODO: We need to conform that this is the proper
                    // order
                    differences.add(new RangeDifference(
                            RangeDifference.CHANGE, rightStart,
                            rightLength, leftStart, leftLength));
                }
                s1 = end1;
                s2 = end2;
                index1++;
                index2++;
            }
            if (s1 != -1
                    && (s1 + 1 < comparator1.getRangeCount() || s2 + 1 < comparator2
                            .getRangeCount())) {
                // TODO: we need to find the proper way of representing an
                // append
                int leftStart = s1 < comparator1.getRangeCount() ? s1 + 1
                        : s1;
                int rightStart = s2 < comparator2.getRangeCount() ? s2 + 1
                        : s2;
                // TODO: We need to conform that this is the proper order
                differences.add(new RangeDifference(RangeDifference.CHANGE,
                        rightStart, comparator2.getRangeCount() - (s2 + 1),
                        leftStart, comparator1.getRangeCount() - (s1 + 1)));
            }

        }
        return (RangeDifference[]) differences
                .toArray(new RangeDifference[differences.size()]);
    }


}
