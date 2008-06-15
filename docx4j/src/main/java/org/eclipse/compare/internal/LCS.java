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
package org.eclipse.compare.internal;


/* Used to determine the change set responsible for each line */
public abstract class LCS {

    private int max_differences; // the maximum number of differences

    // from

    // each end to consider

    private int length;

    /**
     * Myers' algorithm for longest common subsequence. O((M + N)D) worst case
     * time, O(M + N + D^2) expected time, O(M + N) space
     * (http://citeseer.ist.psu.edu/myers86ond.html)
     * 
     * Note: Beyond implementing the algorithm as described in the paper I have
     * added diagonal range compression which helps when finding the LCS of a
     * very long and a very short sequence, also bound the running time to (N +
     * M)^1.5 when both sequences are very long.
     * 
     * After this method is called, the longest common subsequence is available
     * by calling getResult() where result[0] is composed of entries from l1 and
     * result[1] is composed of entries from l2
     * 
     * @param subMonitor
     */
    public void longestCommonSubsequence(LCSSettings settings) {
        int length1 = getLength1();
        int length2 = getLength2();
        if (length1 == 0 || length2 == 0) {
            length = 0;
            return;
        }

        max_differences = (length1 + length2 + 1) / 2; // ceil((N+M)/2)
        if ((double) length1 * (double) length2 > settings.getTooLong()) {
            // limit complexity to D^POW_LIMIT for long sequences
            max_differences = (int) Math.pow(max_differences, settings
                    .getPowLimit() - 1.0);
        }

        initializeLcs(length1);

        /*
         * The common prefixes and suffixes are always part of some LCS, include
         * them now to reduce our search space
         */
        int forwardBound;
        int max = Math.min(length1, length2);
        for (forwardBound = 0; forwardBound < max
                && isRangeEqual(forwardBound, forwardBound); forwardBound++) {
            setLcs(forwardBound, forwardBound);
        }

        int backBoundL1 = length1 - 1;
        int backBoundL2 = length2 - 1;

        while (backBoundL1 >= forwardBound && backBoundL2 >= forwardBound
                && isRangeEqual(backBoundL1, backBoundL2)) {
            setLcs(backBoundL1, backBoundL2);
            backBoundL1--;
            backBoundL2--;
        }

        length = forwardBound
                + length1
                - backBoundL1
                - 1
                + lcs_rec(forwardBound, backBoundL1, forwardBound, backBoundL2,
                        new int[2][length1 + length2 + 1], new int[3]);

    }

    /**
     * The recursive helper function for Myers' LCS. Computes the LCS of
     * l1[bottoml1 .. topl1] and l2[bottoml2 .. topl2] fills in the appropriate
     * location in lcs and returns the length
     * 
     * @param l1
     *                The 1st sequence
     * @param bottoml1
     *                Index in the 1st sequence to start from (inclusive)
     * @param topl1
     *                Index in the 1st sequence to end on (inclusive)
     * @param l2
     *                The 2nd sequence
     * @param bottoml2
     *                Index in the 2nd sequence to start from (inclusive)
     * @param topl2
     *                Index in the 2nd sequence to end on (inclusive)
     * @param V
     *                should be allocated as int[2][l1.length + l2.length + 1],
     *                used to store furthest reaching D-paths
     * @param snake
     *                should be allocated as int[3], used to store the beginning
     *                x, y coordinates and the length of the latest snake
     *                traversed
     * @param subMonitor
     * @param lcs
     *                should be allocated as TextLine[2][l1.length], used to
     *                store the common points found to be part of the LCS where
     *                lcs[0] references lines of l1 and lcs[1] references lines
     *                of l2.
     * 
     * @return the length of the LCS
     */
    private int lcs_rec(int bottoml1, int topl1, int bottoml2, int topl2,
            int[][] V, int[] snake) {

        // check that both sequences are non-empty
        if (bottoml1 > topl1 || bottoml2 > topl2) {
            return 0;
        }

        int d = find_middle_snake(bottoml1, topl1, bottoml2, topl2, V, snake);
        // System.out.println(snake[0] + " " + snake[1] + " " + snake[2]);

        // need to store these so we don't lose them when they're overwritten by
        // the recursion
        int len = snake[2];
        int startx = snake[0];
        int starty = snake[1];

        // the middle snake is part of the LCS, store it
        for (int i = 0; i < len; i++) {
            setLcs(startx + i, starty + i);
        }

        if (d > 1) {
            return len
                    + lcs_rec(bottoml1, startx - 1, bottoml2, starty - 1, V,
                            snake)
                    + lcs_rec(startx + len, topl1, starty + len, topl2, V,
                            snake);
        } else if (d == 1) {
            /*
             * In this case the sequences differ by exactly 1 line. We have
             * already saved all the lines after the difference in the for loop
             * above, now we need to save all the lines before the difference.
             */
            int max = Math.min(startx - bottoml1, starty - bottoml2);
            for (int i = 0; i < max; i++) {
                setLcs(bottoml1 + i, bottoml2 + i);
            }
            return max + len;
        }

        return len;
    }

    /**
     * Helper function for Myers' LCS algorithm to find the middle snake for
     * l1[bottoml1..topl1] and l2[bottoml2..topl2] The x, y coodrdinates of the
     * start of the middle snake are saved in snake[0], snake[1] respectively
     * and the length of the snake is saved in s[2].
     * 
     * @param l1
     *                The 1st sequence
     * @param bottoml1
     *                Index in the 1st sequence to start from (inclusive)
     * @param topl1
     *                Index in the 1st sequence to end on (inclusive)
     * @param l2
     *                The 2nd sequence
     * @param bottoml2
     *                Index in the 2nd sequence to start from (inclusive)
     * @param topl2
     *                Index in the 2nd sequence to end on (inclusive)
     * @param V
     *                should be allocated as int[2][l1.length + l2.length + 1],
     *                used to store furthest reaching D-paths
     * @param snake
     *                should be allocated as int[3], used to store the beginning
     *                x, y coordinates and the length of the middle snake
     * 
     * @return The number of differences (SES) between l1[bottoml1..topl1] and
     *         l2[bottoml2..topl2]
     */
    private int find_middle_snake(int bottoml1, int topl1, int bottoml2,
            int topl2, int[][] V, int[] snake) {
        int N = topl1 - bottoml1 + 1;
        int M = topl2 - bottoml2 + 1;
        // System.out.println("N: " + N + " M: " + M + " bottom: " + bottoml1 +
        // ", " +
        // bottoml2 + " top: " + topl1 + ", " + topl2);
        int delta = N - M;
        boolean isEven;
        if ((delta & 1) == 1) {
            isEven = false;
        } else {
            isEven = true;
        }

        int limit = Math.min(max_differences, (N + M + 1) / 2); // ceil((N+M)/2)

        int value_to_add_forward; // a 0 or 1 that we add to the start
        // offset
        // to make it odd/even
        if ((M & 1) == 1) {
            value_to_add_forward = 1;
        } else {
            value_to_add_forward = 0;
        }

        int value_to_add_backward;
        if ((N & 1) == 1) {
            value_to_add_backward = 1;
        } else {
            value_to_add_backward = 0;
        }

        int start_forward = -M;
        int end_forward = N;
        int start_backward = -N;
        int end_backward = M;

        V[0][limit + 1] = 0;
        V[1][limit - 1] = N;
        for (int d = 0; d <= limit; d++) {

            int start_diag = Math.max(value_to_add_forward + start_forward, -d);
            int end_diag = Math.min(end_forward, d);
            value_to_add_forward = 1 - value_to_add_forward;

            // compute forward furthest reaching paths
            for (int k = start_diag; k <= end_diag; k += 2) {
                int x;
                if (k == -d
                        || (k < d && V[0][limit + k - 1] < V[0][limit + k + 1])) {
                    x = V[0][limit + k + 1];
                } else {
                    x = V[0][limit + k - 1] + 1;
                }

                int y = x - k;

                snake[0] = x + bottoml1;
                snake[1] = y + bottoml2;
                snake[2] = 0;
                // System.out.println("1 x: " + x + " y: " + y + " k: " + k + "
                // d: " + d );
                while (x < N && y < M
                        && isRangeEqual(x + bottoml1, y + bottoml2)) {
                    x++;
                    y++;
                    snake[2]++;
                }
                V[0][limit + k] = x;
                // System.out.println(x + " " + V[1][limit+k -delta] + " " + k +
                // " " + delta);
                if (!isEven && k >= delta - d + 1 && k <= delta + d - 1
                        && x >= V[1][limit + k - delta]) {
                    // System.out.println("Returning: " + (2*d-1));
                    return 2 * d - 1;
                }

                // check to see if we can cut down the diagonal range
                if (x >= N && end_forward > k - 1) {
                    end_forward = k - 1;
                } else if (y >= M) {
                    start_forward = k + 1;
                    value_to_add_forward = 0;
                }
            }

            start_diag = Math.max(value_to_add_backward + start_backward, -d);
            end_diag = Math.min(end_backward, d);
            value_to_add_backward = 1 - value_to_add_backward;

            // compute backward furthest reaching paths
            for (int k = start_diag; k <= end_diag; k += 2) {
                int x;
                if (k == d
                        || (k != -d && V[1][limit + k - 1] < V[1][limit + k + 1])) {
                    x = V[1][limit + k - 1];
                } else {
                    x = V[1][limit + k + 1] - 1;
                }

                int y = x - k - delta;

                snake[2] = 0;
                // System.out.println("2 x: " + x + " y: " + y + " k: " + k + "
                // d: " + d);
                while (x > 0 && y > 0
                        && isRangeEqual(x - 1 + bottoml1, y - 1 + bottoml2)) {
                    x--;
                    y--;
                    snake[2]++;
                }
                V[1][limit + k] = x;

                if (isEven && k >= -delta - d && k <= d - delta
                        && x <= V[0][limit + k + delta]) {
                    // System.out.println("Returning: " + 2*d);
                    snake[0] = bottoml1 + x;
                    snake[1] = bottoml2 + y;

                    return 2 * d;
                }

                // check to see if we can cut down our diagonal range
                if (x <= 0) {
                    start_backward = k + 1;
                    value_to_add_backward = 0;
                } else if (y <= 0 && end_backward > k - 1) {
                    end_backward = k - 1;
                }
            }
        }

        /*
         * computing the true LCS is too expensive, instead find the diagonal
         * with the most progress and pretend a midle snake of length 0 occurs
         * there.
         */

        int[] most_progress = findMostProgress(M, N, limit, V);

        snake[0] = bottoml1 + most_progress[0];
        snake[1] = bottoml2 + most_progress[1];
        snake[2] = 0;
        return 5; /*
                     * HACK: since we didn't really finish the LCS computation
                     * we don't really know the length of the SES. We don't do
                     * anything with the result anyway, unless it's <=1. We know
                     * for a fact SES > 1 so 5 is as good a number as any to
                     * return here
                     */
    }

    /**
     * Takes the array with furthest reaching D-paths from an LCS computation
     * and returns the x,y coordinates and progress made in the middle diagonal
     * among those with maximum progress, both from the front and from the back.
     * 
     * @param M
     *                the length of the 1st sequence for which LCS is being
     *                computed
     * @param N
     *                the length of the 2nd sequence for which LCS is being
     *                computed
     * @param limit
     *                the number of steps made in an attempt to find the LCS
     *                from the front and back
     * @param V
     *                the array storing the furthest reaching D-paths for the
     *                LCS computation
     * @return The result as an array of 3 integers where result[0] is the x
     *         coordinate of the current location in the diagonal with the most
     *         progress, result[1] is the y coordinate of the current location
     *         in the diagonal with the most progress and result[2] is the
     *         amount of progress made in that diagonal
     */
    private static int[] findMostProgress(int M, int N, int limit, int[][] V) {
        int delta = N - M;

        int forward_start_diag;
        if ((M & 1) == (limit & 1)) {
            forward_start_diag = Math.max(-M, -limit);
        } else {
            forward_start_diag = Math.max(1 - M, -limit);
        }

        int forward_end_diag = Math.min(N, limit);

        int backward_start_diag;
        if ((N & 1) == (limit & 1)) {
            backward_start_diag = Math.max(-N, -limit);
        } else {
            backward_start_diag = Math.max(1 - N, -limit);
        }

        int backward_end_diag = Math.min(M, limit);

        int[][] max_progress = new int[Math.max(forward_end_diag
                - forward_start_diag, backward_end_diag - backward_start_diag) / 2 + 1][3];
        int num_progress = 0; // the 1st entry is current, it is initialized
        // with 0s

        // first search the forward diagonals
        for (int k = forward_start_diag; k <= forward_end_diag; k += 2) {
            int x = V[0][limit + k];
            int y = x - k;
            if (x > N || y > M) {
                continue;
            }

            int progress = x + y;
            if (progress > max_progress[0][2]) {
                num_progress = 0;
                max_progress[0][0] = x;
                max_progress[0][1] = y;
                max_progress[0][2] = progress;
            } else if (progress == max_progress[0][2]) {
                num_progress++;
                max_progress[num_progress][0] = x;
                max_progress[num_progress][1] = y;
                max_progress[num_progress][2] = progress;
            }
        }

        boolean max_progress_forward = true; // initially the maximum
        // progress is in the forward
        // direction

        // now search the backward diagonals
        for (int k = backward_start_diag; k <= backward_end_diag; k += 2) {
            int x = V[1][limit + k];
            int y = x - k - delta;
            if (x < 0 || y < 0) {
                continue;
            }

            int progress = N - x + M - y;
            if (progress > max_progress[0][2]) {
                num_progress = 0;
                max_progress_forward = false;
                max_progress[0][0] = x;
                max_progress[0][1] = y;
                max_progress[0][2] = progress;
            } else if (progress == max_progress[0][2] && !max_progress_forward) {
                num_progress++;
                max_progress[num_progress][0] = x;
                max_progress[num_progress][1] = y;
                max_progress[num_progress][2] = progress;
            }
        }

        // return the middle diagonal with maximal progress.
        return max_progress[num_progress / 2];
    }

    protected abstract int getLength2();

    protected abstract int getLength1();

    protected abstract boolean isRangeEqual(int i1, int i2);

    protected abstract void setLcs(int sl1, int sl2);

    protected abstract void initializeLcs(int lcsLength);

    public int getLength() {
        return length;
    }
}
