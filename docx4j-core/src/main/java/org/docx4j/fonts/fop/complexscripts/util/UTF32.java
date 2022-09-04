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

/* $Id$ */

package org.docx4j.fonts.fop.complexscripts.util;

import org.docx4j.fonts.fop.util.CharUtilities;


/**
 * <p>UTF32 related utilities.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public final class UTF32 {

    private UTF32() {
    }

    /**
     * Convert Java string (UTF-16) to a Unicode scalar array (UTF-32).
     * Note that if there are any non-BMP encoded characters present in the
     * input, then the number of entries in the output array will be less
     * than the number of elements in the input string. Any
     * @param s input string
     * @param substitution value to substitute for ill-formed surrogate
     * @param errorOnSubstitution throw runtime exception (IllegalArgumentException) in
     * case this argument is true and a substitution would be attempted
     * @return output scalar array
     * @throws IllegalArgumentException if substitution required and errorOnSubstitution
     *   is not false
     */
    public static Integer[] toUTF32(String s, int substitution, boolean errorOnSubstitution)
        throws IllegalArgumentException {
        int n;
        if ((n = s.length()) == 0) {
            return new Integer[0];
        } else {
            Integer[] sa = new Integer [ n ];
            int k = 0;
            for (int i = 0; i < n; i++) {
                int c = s.charAt(i);
                if ((c >= 0xD800) && (c < 0xE000)) {
                    int s1 = c;
                    int s2 = ((i + 1) < n) ? s.charAt(i + 1) : 0;
                    if (s1 < 0xDC00) {
                        if ((s2 >= 0xDC00) && (s2 < 0xE000)) {
                            c = ((s1 - 0xD800) << 10) + (s2 - 0xDC00) + 65536;
                            i++;
                        } else {
                            if (errorOnSubstitution) {
                                throw new IllegalArgumentException(
                                    "isolated high (leading) surrogate");
                            } else {
                                c = substitution;
                            }
                        }
                    } else {
                        if (errorOnSubstitution) {
                            throw new IllegalArgumentException(
                                "isolated low (trailing) surrogate");
                        } else {
                            c = substitution;
                        }
                    }
                }
                sa[k++] = c;
            }
            if (k == n) {
                return sa;
            } else {
                Integer[] na = new Integer [ k ];
                System.arraycopy(sa, 0, na, 0, k);
                return na;
            }
        }
    }

    /**
     * Convert a Unicode scalar array (UTF-32) a Java string (UTF-16).
     * @param sa input scalar array
     * @return output (UTF-16) string
     * @throws IllegalArgumentException if an input scalar value is illegal,
     *   e.g., a surrogate or out of range
     */
    public static String fromUTF32(Integer[] sa) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        for (int s : sa) {
            if (s < 65535) {
                if ((s < 0xD800) || (s > 0xDFFF)) {
                    sb.append((char) s);
                } else {
                    String ncr = CharUtilities.charToNCRef(s);
                    throw new IllegalArgumentException(
                        "illegal scalar value 0x" + ncr.substring(2, ncr.length() - 1)
                          + "; cannot be UTF-16 surrogate");
                }
            } else if (s < 1114112) {
                int s1 = (((s - 65536) >> 10) & 0x3FF) + 0xD800;
                int s2 = (((s - 65536) >>  0) & 0x3FF) + 0xDC00;
                sb.append((char) s1);
                sb.append((char) s2);
            } else {
                String ncr = CharUtilities.charToNCRef(s);
                throw new IllegalArgumentException(
                    "illegal scalar value 0x" + ncr.substring(2, ncr.length() - 1)
                      + "; out of range for UTF-16");
            }
        }
        return sb.toString();
    }

}
