/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

/**
 * The algorithm used is an objectified version of one described in: <it>A File
 * Comparison Program,</it> by Webb Miller and Eugene W. Myers, Software
 * Practice and Experience, Vol. 15, Nov. 1985.
 */
/* package */class OldDifferencer {

    private static final RangeDifference[] EMPTY_RESULT = new RangeDifference[0];

    private static class LinkedRangeDifference extends RangeDifference {

        static final int INSERT = 0;

        static final int DELETE = 1;

        LinkedRangeDifference fNext;

        /*
         * Creates a LinkedRangeDifference an initializes it to the error state
         */
        LinkedRangeDifference() {
            super(ERROR);
            fNext = null;
        }

        /*
         * Constructs and links a LinkeRangeDifference to another
         * LinkedRangeDifference
         */
        LinkedRangeDifference(LinkedRangeDifference next, int operation) {
            super(operation);
            fNext = next;
        }

        /*
         * Follows the next link
         */
        LinkedRangeDifference getNext() {
            return fNext;
        }

        boolean isDelete() {
            return kind() == DELETE;
        }

        boolean isInsert() {
            return kind() == INSERT;
        }

        /*
         * Sets the next link of this LinkedRangeDifference
         */
        void setNext(LinkedRangeDifference next) {
            fNext = next;
        }
    }

    public static RangeDifference[] findDifferences(IRangeComparator left, IRangeComparator right) {

        // assert that both IRangeComparators are of the same class
//        Assert.isTrue(right.getClass().equals(left.getClass()));

        int rightSize = right.getRangeCount();
        int leftSize = left.getRangeCount();
        //
        // Differences matrix:
        // only the last d of each diagonal is stored, i.e., lastDiagonal[k] =
        // row of d
        //
        int diagLen = 2 * Math.max(rightSize, leftSize); // bound on the size
                                                            // of edit script
        int maxDiagonal = diagLen;
        int lastDiagonal[] = new int[diagLen + 1]; // the row containing the
                                                    // last d
        // on diagonal k (lastDiagonal[k] = row)
        int origin = diagLen / 2; // origin of diagonal 0

        // script corresponding to d[k]
        LinkedRangeDifference script[] = new LinkedRangeDifference[diagLen + 1];
        int row, col;

        // find common prefix
        for (row = 0; row < rightSize && row < leftSize
                && rangesEqual(right, row, left, row) == true;)
            row++;

        lastDiagonal[origin] = row;
        script[origin] = null;
        int lower = (row == rightSize) ? origin + 1 : origin - 1;
        int upper = (row == leftSize) ? origin - 1 : origin + 1;

        if (lower > upper)
            return EMPTY_RESULT;

        // System.out.println("findDifferences: " + maxDiagonal + " " + lower +
        // " " + upper);

        // for each value of the edit distance
        for (int d = 1; d <= maxDiagonal; ++d) { // d is the current edit
                                                    // distance

            if (right.skipRangeComparison(d, maxDiagonal, left))
                return EMPTY_RESULT; // should be something we already found

            // for each relevant diagonal (-d, -d+2 ..., d-2, d)
            for (int k = lower; k <= upper; k += 2) { // k is the current
                                                        // diagonal
                LinkedRangeDifference edit;

                if (k == origin - d || k != origin + d
                        && lastDiagonal[k + 1] >= lastDiagonal[k - 1]) {
                    //
                    // move down
                    //
                    row = lastDiagonal[k + 1] + 1;
                    edit = new LinkedRangeDifference(script[k + 1],
                            LinkedRangeDifference.DELETE);
                } else {
                    //
                    // move right
                    //
                    row = lastDiagonal[k - 1];
                    edit = new LinkedRangeDifference(script[k - 1],
                            LinkedRangeDifference.INSERT);
                }
                col = row + k - origin;
                edit.fRightStart = row;
                edit.fLeftStart = col;
//                Assert.isTrue(k >= 0 && k <= maxDiagonal);
                script[k] = edit;

                // slide down the diagonal as far as possible
                while (row < rightSize && col < leftSize
                        && rangesEqual(right, row, left, col) == true) {
                    ++row;
                    ++col;
                }

//                Assert.isTrue(k >= 0 && k <= maxDiagonal); // Unreasonable
                                                            // value for
                                                            // diagonal index
                lastDiagonal[k] = row;

                if (row == rightSize && col == leftSize) {
                    // showScript(script[k], right, left);
                    return createDifferencesRanges(script[k]);
                }
                if (row == rightSize)
                    lower = k + 2;
                if (col == leftSize)
                    upper = k - 2;
            }
            --lower;
            ++upper;
        }
        // too many differences
//        Assert.isTrue(false);
        return null;
    }

    /*
     * Tests if two ranges are equal
     */
    private static boolean rangesEqual(IRangeComparator a, int ai,
            IRangeComparator b, int bi) {
        return a.rangesEqual(ai, b, bi);
    }

    /*
     * Creates a Vector of DifferencesRanges out of the LinkedRangeDifference.
     * It coalesces adjacent changes. In addition, indices are changed such that
     * the ranges are 1) open, i.e, the end of the range is not included, and 2)
     * are zero based.
     */
    private static RangeDifference[] createDifferencesRanges(
            LinkedRangeDifference start) {

        LinkedRangeDifference ep = reverseDifferences(start);
        ArrayList result = new ArrayList();
        RangeDifference es = null;

        while (ep != null) {
            es = new RangeDifference(RangeDifference.CHANGE);

            if (ep.isInsert()) {
                es.fRightStart = ep.fRightStart + 1;
                es.fLeftStart = ep.fLeftStart;
                RangeDifference b = ep;
                do {
                    ep = ep.getNext();
                    es.fLeftLength++;
                } while (ep != null && ep.isInsert()
                        && ep.fRightStart == b.fRightStart);
            } else {
                es.fRightStart = ep.fRightStart;
                es.fLeftStart = ep.fLeftStart;

                RangeDifference a = ep;
                //
                // deleted lines
                //
                do {
                    a = ep;
                    ep = ep.getNext();
                    es.fRightLength++;
                } while (ep != null && ep.isDelete()
                        && ep.fRightStart == a.fRightStart + 1);

                boolean change = (ep != null && ep.isInsert() && ep.fRightStart == a.fRightStart);

                if (change) {
                    RangeDifference b = ep;
                    //
                    // replacement lines
                    //
                    do {
                        ep = ep.getNext();
                        es.fLeftLength++;
                    } while (ep != null && ep.isInsert()
                            && ep.fRightStart == b.fRightStart);
                } else {
                    es.fLeftLength = 0;
                }
                es.fLeftStart++; // meaning of range changes from "insert
                                    // after", to "replace with"

            }
            //
            // the script commands are 1 based, subtract one to make them zero
            // based
            //
            es.fRightStart--;
            es.fLeftStart--;
            result.add(es);
        }
        return (RangeDifference[]) result.toArray(EMPTY_RESULT);
    }

    /*
     * Reverses the range differences
     */
    private static LinkedRangeDifference reverseDifferences(
            LinkedRangeDifference start) {
        LinkedRangeDifference ep, behind, ahead;

        ahead = start;
        ep = null;
        while (ahead != null) {
            behind = ep;
            ep = ahead;
            ahead = ahead.getNext();
            ep.setNext(behind);
        }
        return ep;
    }
}
