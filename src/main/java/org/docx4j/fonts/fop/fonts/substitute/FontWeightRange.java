/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: FontWeightRange.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.substitute;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a range of font weight values
 */
public class FontWeightRange {

    /** logging instance */
    protected static Logger log = LoggerFactory.getLogger("org.apache.fop.render.fonts");

    /**
     * Returns an <code>FontWeightRange</code> object holding the
     * range values of the specified <code>String</code>.
     *
     * @param weightRangeString the value range string
     * @return an <code>FontWeightRange</code> object holding the value ranges
     */
    public static FontWeightRange valueOf(String weightRangeString) {
        StringTokenizer rangeToken = new StringTokenizer(weightRangeString, "..");
        FontWeightRange weightRange = null;
        if (rangeToken.countTokens() == 2) {
            String weightString = rangeToken.nextToken().trim();
            try {
                int start = Integer.parseInt(weightString);
                if (start % 100 != 0) {
                    log.error("font-weight start range is not a multiple of 100");
                }
                int end = Integer.parseInt(rangeToken.nextToken());
                if (end % 100 != 0) {
                    log.error("font-weight end range is not a multiple of 100");
                }
                if (start <= end) {
                    weightRange = new FontWeightRange(start, end);
                } else {
                    log.error("font-weight start range is greater than end range");
                }
            } catch (NumberFormatException e) {
                log.error("invalid font-weight value " + weightString);
            }
        }
        return weightRange;
    }

    /** the start range */
    private int start;

    /** the end range */
    private int end;

    /**
     * Main constructor
     * @param start the start value range
     * @param end the end value range
     */
    public FontWeightRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns true if the given integer value is within this integer range
     * @param value the integer value
     * @return true if the given integer value is within this integer range
     */
    public boolean isWithinRange(int value) {
        return (value >= start && value <= end);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return start + ".." + end;
    }

    /**
     * @return an integer array containing the weight ranges
     */
    public int[] toArray() {
        int cnt = 0;
        for (int i = start; i <= end; i += 100) {
            cnt++;
        }
        int[] range = new int[cnt];
        for (int i = 0; i < cnt; i++) {
            range[i] = start + (i * 100);
        }
        return range;
    }
}
