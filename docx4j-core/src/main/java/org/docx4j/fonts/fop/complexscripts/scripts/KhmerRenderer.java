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
package org.docx4j.fonts.fop.complexscripts.scripts;

/**
 * Integrating existing rendering of Android for Khmer Unicode to iText
 *    The class from the rendering of Mobile Project, Android from Nokor Group (AKA: Nokor-IT)
 *    The understanding also taking from the Khmum Browser that would lead to build this helper
 *    (Comment above by Pongsametrey S. <metrey@osify.com>)
 *    Thanks for Nokor Group & Mr. Pengleng HUOT
 *
 * author sok.pongsametrey
 * @version 1.0
 */

/**
 * UnicodeRender Class.
 * author huot.pengleng
 *
 * simple classes, they are used in the state table (in this file) to control the length of a syllable
 * they are also used to know where a character should be placed (location in reference to the base character)
 * and also to know if a character, when independently displayed, should be displayed with a dotted-circle to
 * indicate error in syllable construction
 * Character class tables
 * xx character does not combine into syllable, such as numbers, puntuation marks, non-Khmer signs...
 * sa Sign placed above the base
 * sp Sign placed after the base
 * c1 Consonant of type 1 or independent vowel (independent vowels behave as type 1 consonants)
 * c2 Consonant of type 2 (only RO)
 * c3 Consonant of type 3
 * rb Khmer sign robat u17CC. combining mark for subscript consonants
 * cd Consonant-shifter
 * dl Dependent vowel placed before the base (left of the base)
 * db Dependent vowel placed below the base
 * da Dependent vowel placed above the base
 * dr Dependent vowel placed behind the base (right of the base)
 * co Khmer combining mark COENG u17D2, combines with the consonant or independent vowel following
 *     it to create a subscript consonant or independent vowel
 * va Khmer split vowel in wich the first part is before the base and the second one above the base
 * vr Khmer split vowel in wich the first part is before the base and the second one behind (right of) the base
 *
 */
public class KhmerRenderer {

    private static final int XX = 0;
    private static final int CC_COENG = 7; // Subscript consonant combining character
    private static final int CC_CONSONANT = 1; // Consonant of type 1 or independent vowel
    private static final int CC_CONSONANT_SHIFTER = 5;
    private static final int CC_CONSONANT2 = 2; // Consonant of type 2
    private static final int CC_CONSONANT3 = 3; // Consonant of type 3
    private static final int CC_DEPENDENT_VOWEL = 8;
    private static final int CC_ROBAT = 6; // Khmer special diacritic accent -treated differently in state table
    private static final int CC_SIGN_ABOVE = 9;
    private static final int CC_SIGN_AFTER = 10;
    private static final int CF_ABOVE_VOWEL = 536870912; // flag to speed up comparing
    private static final int CF_CLASS_MASK = 65535;
    private static final int CF_COENG = 134217728; // flag to speed up comparing
    private static final int CF_CONSONANT = 16777216; // flag to speed up comparing
    private static final int CF_DOTTED_CIRCLE = 67108864;

    // add a dotted circle if a character with this flag is the first in a syllable
    private static final int CF_POS_ABOVE = 131072;
    private static final int CF_POS_AFTER = 65536;
    private static final int CF_POS_BEFORE = 524288;
    private static final int CF_POS_BELOW = 262144;
    private static final int CF_SHIFTER = 268435456; // flag to speed up comparing
    private static final int CF_SPLIT_VOWEL = 33554432;
    private static final int C1 = CC_CONSONANT + CF_CONSONANT;
    private static final int C2 = CC_CONSONANT2 + CF_CONSONANT;
    private static final int C3 = CC_CONSONANT3 + CF_CONSONANT;
    private static final int CO = CC_COENG + CF_COENG + CF_DOTTED_CIRCLE;
    private static final int CS = CC_CONSONANT_SHIFTER + CF_DOTTED_CIRCLE + CF_SHIFTER;
    private static final int DA = CC_DEPENDENT_VOWEL + CF_POS_ABOVE + CF_DOTTED_CIRCLE + CF_ABOVE_VOWEL;
    private static final int DB = CC_DEPENDENT_VOWEL + CF_POS_BELOW + CF_DOTTED_CIRCLE;
    private static final int DL = CC_DEPENDENT_VOWEL + CF_POS_BEFORE + CF_DOTTED_CIRCLE;
    private static final int DR = CC_DEPENDENT_VOWEL + CF_POS_AFTER + CF_DOTTED_CIRCLE;
    private static final int RB = CC_ROBAT + CF_POS_ABOVE + CF_DOTTED_CIRCLE;
    private static final int SA = CC_SIGN_ABOVE + CF_DOTTED_CIRCLE + CF_POS_ABOVE;
    private static final int SP = CC_SIGN_AFTER + CF_DOTTED_CIRCLE + CF_POS_AFTER;
    private static final int VA = DA + CF_SPLIT_VOWEL;
    private static final int VR = DR + CF_SPLIT_VOWEL;
    // flag for a split vowel -> the first part is added in front of the syllable
    private static final char BA = '\u1794';
    private static final char COENG = '\u17D2';
    private static final String CONYO = Character.toString('\u17D2').concat(Character.toString('\u1789'));
    private static final String CORO = Character.toString('\u17D2').concat(Character.toString('\u179A'));

    private int[] khmerCharClasses = new int[] {
            C1, C1, C1, C3, C1, C1, C1, C1, C3, C1, C1, C1, C1, C3, C1, C1, C1, C1, C1, C1, C3,
            C1, C1, C1, C1, C3, C2, C1, C1, C1, C3, C3, C1, C3, C1, C1, C1, C1, C1, C1, C1, C1,
            C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, DR, DR, DR, DA, DA, DA, DA, DB, DB, DB, VA,
            VR, VR, DL, DL, DL, VR, VR, SA, SP, SP, CS, CS, SA, RB, SA, SA, SA, SA, SA, CO, SA,
            XX, XX, XX, XX, XX, XX, XX, XX, XX, SA, XX, XX
    };
    private short[][] khmerStateTable = new short[][] {
            {
                    1, 2, 2, 2, 1, 1, 1, 6, 1, 1, 1, 2
            }, {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    }, {
            -1, -1, -1, -1, 3, 4, 5, 6, 16, 17, 1, -1
    }, {
            -1, -1, -1, -1, -1, 4, -1, -1, 16, -1, -1, -1
    }, {
            -1, -1, -1, -1, 15, -1, -1, 6, 16, 17, 1, 14
    }, {
            -1, -1, -1, -1, -1, -1, -1, -1, 20, -1, 1, -1
    }, {
            -1, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, -1
    }, {
            -1, -1, -1, -1, 12, 13, -1, 10, 16, 17, 1, 14
    }, {
            -1, -1, -1, -1, 12, 13, -1, -1, 16, 17, 1, 14
    }, {
            -1, -1, -1, -1, 12, 13, -1, 10, 16, 17, 1, 14
    }, {
            -1, 11, 11, 11, -1, -1, -1, -1, -1, -1, -1, -1
    }, {
            -1, -1, -1, -1, 15, -1, -1, -1, 16, 17, 1, 14
    }, {
            -1, -1, -1, -1, -1, 13, -1, -1, 16, -1, -1, -1
    }, {
            -1, -1, -1, -1, 15, -1, -1, -1, 16, 17, 1, 14
    }, {
            -1, -1, -1, -1, -1, -1, -1, -1, 16, -1, -1, -1
    }, {
            -1, -1, -1, -1, -1, -1, -1, -1, 16, -1, -1, -1
    }, {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 17, 1, 18
    }, {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 18
    }, {
            -1, -1, -1, -1, -1, -1, -1, 19, -1, -1, -1, -1
    }, {
            -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1
    }, {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1
    }
    };
    private static final char MARK = '\u17EA';
    private static final char NYO = '\u1789';
    private static final char SA_C = '\u179F';
    private static final char SRAAA = '\u17B6';
    private static final char SRAAU = '\u17C5';
    private static final char SRAE = '\u17C1';
    private static final char SRAIE = '\u17C0';
    private static final char SRAII = '\u17B8';
    private static final char SRAOE = '\u17BE';
    private static final char SRAOO = '\u17C4';
    private static final char SRAU = '\u17BB';
    private static final char SRAYA = '\u17BF';
    private static final char TRIISAP = '\u17CA';
    private static final char YO = '\u1799';

    private char strEcombining(final char chrInput) {
        char retChar = ' ';
        if (chrInput == SRAOE) {
            retChar = SRAII;
        } else if (chrInput == SRAYA) {
            retChar = SRAYA;
        } else if (chrInput == SRAIE) {
            retChar = SRAIE;
        } else if (chrInput == SRAOO) {
            retChar = SRAAA;
        } else if (chrInput == SRAAU) {
            retChar = SRAAU;
        }

        return retChar;
    }

    // Gets the charactor class.
    private int getCharClass(final char uniChar) {
        int retValue = 0;
        int ch;
        ch = uniChar;
        if (ch > 255) {
            if (ch >= '\u1780') {
                ch -= '\u1780';
                if (ch < khmerCharClasses.length) {
                    retValue = khmerCharClasses[ch];
                }
            }
        }
        return retValue;
    }

    /**
     * Re-order Khmer unicode for display with Khmer.ttf file on Android.
     * @param strInput Khmer unicode string.
     * @return String after render.
     */
    public String render(final String strInput) {
        //Given an input String of unicode cluster to reorder.
        //The return is the visual based cluster (legacy style) String.

        int cursor = 0;
        short state = 0;
        int charCount = strInput.length();
        StringBuilder result = new StringBuilder();

        while (cursor < charCount) {
            String reserved = "";
            String signAbove = "";
            String signAfter = "";
            String base = "";
            String robat = "";
            String shifter = "";
            String vowelBefore = "";
            String vowelBelow = "";
            String vowelAbove = "";
            String vowelAfter = "";
            boolean coeng = false;
            String cluster;

            String coeng1 = "";
            String coeng2 = "";

            boolean shifterAfterCoeng = false;

            while (cursor < charCount) {
                char curChar = strInput.charAt(cursor);
                int kChar = getCharClass(curChar);
                int charClass = kChar & CF_CLASS_MASK;
                try {
                    state = khmerStateTable[state][charClass];
                } catch (Exception ex) {
                    state = -1;
                }

                if (state < 0) {
                    break;
                }

                //collect variable for cluster here

                if (kChar == XX) {
                    reserved = Character.toString(curChar);
                } else if (kChar == SA) { //Sign placed above the base
                    signAbove = Character.toString(curChar);
                } else if (kChar == SP) { //Sign placed after the base
                    signAfter = Character.toString(curChar);
                } else if (kChar == C1 || kChar == C2 || kChar == C3) { //Consonant
                    if (coeng) {
                        if ("".equalsIgnoreCase(coeng1)) {
                            coeng1 = Character.toString(COENG).concat(Character.toString(curChar));
                        } else {
                            coeng2 = Character.toString(COENG).concat(Character.toString(curChar));
                        }
                        coeng = false;
                    } else {
                        base = Character.toString(curChar);
                    }
                } else if (kChar == RB) { //Khmer sign robat u17CC
                    robat = Character.toString(curChar);
                } else if (kChar == CS) { //Consonant-shifter
                    if (!"".equalsIgnoreCase(coeng1)) {
                        shifterAfterCoeng = true;
                    }

                    shifter = Character.toString(curChar);
                } else if (kChar == DL) { //Dependent vowel placed before the base
                    vowelBefore = Character.toString(curChar);
                } else if (kChar == DB) { //Dependent vowel placed below the base
                    vowelBelow = Character.toString(curChar);
                } else if (kChar == DA) { //Dependent vowel placed above the base
                    vowelAbove = Character.toString(curChar);
                } else if (kChar == DR) { //Dependent vowel placed behind the base
                    vowelAfter = Character.toString(curChar);
                } else if (kChar == CO) { //Khmer combining mark COENG
                    coeng = true;
                } else if (kChar == VA) { //Khmer split vowel, see da
                    vowelBefore = Character.toString(SRAE);
                    vowelAbove = Character.toString(strEcombining(curChar));
                } else if (kChar == VR) { //Khmer split vowel, see dr
                    vowelBefore = Character.toString(SRAE);
                    vowelAfter = Character.toString(strEcombining(curChar));
                }

                cursor += 1;
            }
            // end of while (a cluster has found)

            // logic when cluster has coeng
            // should coeng be located on left side
            String coengBefore = "";
            if (CORO.equalsIgnoreCase(coeng1)) {
                coengBefore = coeng1;
                coeng1 = "";
            } else if (CORO.equalsIgnoreCase(coeng2)) {
                coengBefore = coeng2;
                coeng2 = "";
            }

            //logic of shifter with base character
            if (!"".equalsIgnoreCase(base) && !"".equalsIgnoreCase(shifter)) {
                if (!"".equalsIgnoreCase(vowelAbove)) {
                    shifter = "";
                    vowelBelow = Character.toString(SRAU);
                }
            }

            // uncomplete coeng
            if (coeng && "".equalsIgnoreCase(coeng1)) {
                coeng1 = Character.toString(COENG);
            } else if (coeng && "".equalsIgnoreCase(coeng2)) {
                coeng2 = Character.toString(MARK).concat(Character.toString(COENG));
            }

            //place of shifter
            String shifter1 = "";
            String shifter2 = "";

            if (shifterAfterCoeng) {
                shifter2 = shifter;
            } else {
                shifter1 = shifter;
            }

            boolean specialCaseBA = false;
            String strMARKSRAAA = Character.toString(MARK).concat(Character.toString(SRAAA));
            String strMARKSRAAU = Character.toString(MARK).concat(Character.toString(SRAAU));

            if (Character.toString(BA).equalsIgnoreCase(base)
                    && (Character.toString(SRAAA).equalsIgnoreCase(vowelAfter)
                    || Character.toString(SRAAU).equalsIgnoreCase(vowelAfter)
                    || strMARKSRAAA.equalsIgnoreCase(vowelAfter) || strMARKSRAAU.equalsIgnoreCase(vowelAfter))) {
                specialCaseBA = true;

                if (!"".equalsIgnoreCase(coeng1)) {
                    String coeng1Complete = coeng1.substring(0, coeng1.length() - 1);
                    if (Character.toString(BA).equalsIgnoreCase(coeng1Complete)
                            || Character.toString(YO).equalsIgnoreCase(coeng1Complete)
                            || Character.toString(SA_C).equalsIgnoreCase(coeng1Complete)) {
                        specialCaseBA = false;

                    }
                }
            }

            // cluster formation
            if (specialCaseBA) {
                cluster = vowelBefore + coengBefore + base + vowelAfter + robat + shifter1 + coeng1 + coeng2
                        + shifter2 + vowelBelow + vowelAbove + signAbove + signAfter;
            } else {
                cluster = vowelBefore + coengBefore + base + robat + shifter1 + coeng1 + coeng2 + shifter2
                        + vowelBelow + vowelAbove + vowelAfter + signAbove + signAfter;
            }
            result.append(cluster + reserved);
            state = 0;
            //end of while
        }

        return result.toString();
    }
}
