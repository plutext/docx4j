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

package org.docx4j.fonts.fop.fonts.base14;

import java.awt.Rectangle;
import java.net.URI;

import java.util.Map;

import java.util.Set;

import org.docx4j.fonts.fop.fonts.Base14Font;
import org.docx4j.fonts.fop.fonts.CodePointMapping;
import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.Typeface;

// CSOFF: ConstantNameCheck

public class HelveticaBold extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Helvetica-Bold";
    private static final String fullName = "Helvetica Bold";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 718;
    private static final int xHeight = 532;
    private static final int ascender = 718;
    private static final int descender = -207;
    private static final int firstChar = 32;
    private static final int lastChar = 255;
    private static final int[] width;
    private static final Rectangle[] boundingBoxes;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("WinAnsiEncoding");

    private static final Map kerning;


    private boolean enableKerning;

    static {
        URI uri = null;
        try {
            uri = new URI("base14:" + fontName.toLowerCase());
        } catch (java.net.URISyntaxException e) {
          throw new RuntimeException(e);
        }
        fontFileURI = uri;
        width = new int[256];
        boundingBoxes = new Rectangle[256];

        width[0x41] = 722;
        boundingBoxes[0x41] = new Rectangle(20, 0, 682, 718);
        width[0xc6] = 1000;
        boundingBoxes[0xc6] = new Rectangle(5, 0, 949, 718);
        width[0xc1] = 722;
        boundingBoxes[0xc1] = new Rectangle(20, 0, 682, 936);
        width[0xc2] = 722;
        boundingBoxes[0xc2] = new Rectangle(20, 0, 682, 936);
        width[0xc4] = 722;
        boundingBoxes[0xc4] = new Rectangle(20, 0, 682, 915);
        width[0xc0] = 722;
        boundingBoxes[0xc0] = new Rectangle(20, 0, 682, 936);
        width[0xc5] = 722;
        boundingBoxes[0xc5] = new Rectangle(20, 0, 682, 962);
        width[0xc3] = 722;
        boundingBoxes[0xc3] = new Rectangle(20, 0, 682, 923);
        width[0x42] = 722;
        boundingBoxes[0x42] = new Rectangle(76, 0, 593, 718);
        width[0x43] = 722;
        boundingBoxes[0x43] = new Rectangle(44, -19, 640, 756);
        width[0xc7] = 722;
        boundingBoxes[0xc7] = new Rectangle(44, -228, 640, 965);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(76, 0, 609, 718);
        width[0x45] = 667;
        boundingBoxes[0x45] = new Rectangle(76, 0, 545, 718);
        width[0xc9] = 667;
        boundingBoxes[0xc9] = new Rectangle(76, 0, 545, 936);
        width[0xca] = 667;
        boundingBoxes[0xca] = new Rectangle(76, 0, 545, 936);
        width[0xcb] = 667;
        boundingBoxes[0xcb] = new Rectangle(76, 0, 545, 915);
        width[0xc8] = 667;
        boundingBoxes[0xc8] = new Rectangle(76, 0, 545, 936);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(-5, 0, 690, 718);
        width[0x80] = 556;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 611;
        boundingBoxes[0x46] = new Rectangle(76, 0, 511, 718);
        width[0x47] = 778;
        boundingBoxes[0x47] = new Rectangle(44, -19, 669, 756);
        width[0x48] = 722;
        boundingBoxes[0x48] = new Rectangle(71, 0, 580, 718);
        width[0x49] = 278;
        boundingBoxes[0x49] = new Rectangle(64, 0, 150, 718);
        width[0xcd] = 278;
        boundingBoxes[0xcd] = new Rectangle(64, 0, 265, 936);
        width[0xce] = 278;
        boundingBoxes[0xce] = new Rectangle(-37, 0, 353, 936);
        width[0xcf] = 278;
        boundingBoxes[0xcf] = new Rectangle(-21, 0, 321, 915);
        width[0xcc] = 278;
        boundingBoxes[0xcc] = new Rectangle(-50, 0, 264, 936);
        width[0x4a] = 556;
        boundingBoxes[0x4a] = new Rectangle(22, -18, 462, 736);
        width[0x4b] = 722;
        boundingBoxes[0x4b] = new Rectangle(87, 0, 635, 718);
        width[0x4c] = 611;
        boundingBoxes[0x4c] = new Rectangle(76, 0, 507, 718);

        width[0x4d] = 833;
        boundingBoxes[0x4d] = new Rectangle(69, 0, 696, 718);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(69, 0, 585, 718);
        width[0xd1] = 722;
        boundingBoxes[0xd1] = new Rectangle(69, 0, 585, 923);
        width[0x4f] = 778;
        boundingBoxes[0x4f] = new Rectangle(44, -19, 690, 756);
        width[0x8c] = 1000;
        boundingBoxes[0x8c] = new Rectangle(37, -19, 924, 756);
        width[0xd3] = 778;
        boundingBoxes[0xd3] = new Rectangle(44, -19, 690, 955);
        width[0xd4] = 778;
        boundingBoxes[0xd4] = new Rectangle(44, -19, 690, 955);
        width[0xd6] = 778;
        boundingBoxes[0xd6] = new Rectangle(44, -19, 690, 934);
        width[0xd2] = 778;
        boundingBoxes[0xd2] = new Rectangle(44, -19, 690, 955);
        width[0xd8] = 778;
        boundingBoxes[0xd8] = new Rectangle(33, -27, 711, 772);
        width[0xd5] = 778;
        boundingBoxes[0xd5] = new Rectangle(44, -19, 690, 942);
        width[0x50] = 667;
        boundingBoxes[0x50] = new Rectangle(76, 0, 551, 718);
        width[0x51] = 778;
        boundingBoxes[0x51] = new Rectangle(44, -52, 693, 789);
        width[0x52] = 722;
        boundingBoxes[0x52] = new Rectangle(76, 0, 601, 718);
        width[0x53] = 667;
        boundingBoxes[0x53] = new Rectangle(39, -19, 590, 756);
        width[0x8a] = 667;
        boundingBoxes[0x8a] = new Rectangle(39, -19, 590, 955);
        width[0x54] = 611;
        boundingBoxes[0x54] = new Rectangle(14, 0, 584, 718);
        width[0xde] = 667;
        boundingBoxes[0xde] = new Rectangle(76, 0, 551, 718);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(72, -19, 579, 737);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(72, -19, 579, 955);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(72, -19, 579, 955);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(72, -19, 579, 934);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(72, -19, 579, 955);
        width[0x56] = 667;
        boundingBoxes[0x56] = new Rectangle(19, 0, 629, 718);
        width[0x57] = 944;
        boundingBoxes[0x57] = new Rectangle(16, 0, 913, 718);
        width[0x58] = 667;
        boundingBoxes[0x58] = new Rectangle(14, 0, 639, 718);
        width[0x59] = 667;
        boundingBoxes[0x59] = new Rectangle(15, 0, 638, 718);
        width[0xdd] = 667;
        boundingBoxes[0xdd] = new Rectangle(15, 0, 638, 936);
        width[0x9f] = 667;
        boundingBoxes[0x9f] = new Rectangle(15, 0, 638, 915);
        width[0x5a] = 611;
        boundingBoxes[0x5a] = new Rectangle(25, 0, 561, 718);
        width[0x8e] = 611;
        boundingBoxes[0x8e] = new Rectangle(25, 0, 561, 936);
        width[0x61] = 556;
        boundingBoxes[0x61] = new Rectangle(29, -14, 498, 560);
        width[0xe1] = 556;
        boundingBoxes[0xe1] = new Rectangle(29, -14, 498, 764);
        width[0xe2] = 556;
        boundingBoxes[0xe2] = new Rectangle(29, -14, 498, 764);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(108, 604, 248, 146);
        width[0xe4] = 556;
        boundingBoxes[0xe4] = new Rectangle(29, -14, 498, 743);
        width[0xe6] = 889;
        boundingBoxes[0xe6] = new Rectangle(29, -14, 829, 560);
        width[0xe0] = 556;
        boundingBoxes[0xe0] = new Rectangle(29, -14, 498, 764);
        width[0x26] = 722;
        boundingBoxes[0x26] = new Rectangle(54, -19, 647, 737);
        width[0xe5] = 556;
        boundingBoxes[0xe5] = new Rectangle(29, -14, 498, 790);
        width[0x5e] = 584;
        boundingBoxes[0x5e] = new Rectangle(62, 323, 460, 375);
        width[0x7e] = 584;
        boundingBoxes[0x7e] = new Rectangle(61, 163, 462, 180);
        width[0x2a] = 389;
        boundingBoxes[0x2a] = new Rectangle(27, 387, 335, 331);
        width[0x40] = 975;
        boundingBoxes[0x40] = new Rectangle(118, -19, 738, 756);
        width[0xe3] = 556;
        boundingBoxes[0xe3] = new Rectangle(29, -14, 498, 751);
        width[0x62] = 611;
        boundingBoxes[0x62] = new Rectangle(61, -14, 517, 732);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(-33, -19, 344, 756);
        width[0x7c] = 280;
        boundingBoxes[0x7c] = new Rectangle(84, -225, 112, 1000);
        width[0x7b] = 389;
        boundingBoxes[0x7b] = new Rectangle(48, -196, 317, 918);
        width[0x7d] = 389;
        boundingBoxes[0x7d] = new Rectangle(24, -196, 317, 918);
        width[0x5b] = 333;
        boundingBoxes[0x5b] = new Rectangle(63, -196, 246, 918);
        width[0x5d] = 333;
        boundingBoxes[0x5d] = new Rectangle(24, -196, 246, 918);

        width[0xa6] = 280;
        boundingBoxes[0xa6] = new Rectangle(84, -150, 112, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(10, 194, 330, 330);
        width[0x63] = 556;
        boundingBoxes[0x63] = new Rectangle(34, -14, 490, 560);

        width[0xe7] = 556;
        boundingBoxes[0xe7] = new Rectangle(34, -228, 490, 774);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(6, -228, 239, 228);
        width[0xa2] = 556;
        boundingBoxes[0xa2] = new Rectangle(34, -118, 490, 746);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(-10, 604, 353, 146);
        width[0x3a] = 333;
        boundingBoxes[0x3a] = new Rectangle(92, 0, 150, 512);
        width[0x2c] = 278;
        boundingBoxes[0x2c] = new Rectangle(64, -168, 150, 314);
        width[0xa9] = 737;
        boundingBoxes[0xa9] = new Rectangle(-11, -19, 760, 756);
        width[0xa4] = 556;
        boundingBoxes[0xa4] = new Rectangle(-3, 76, 562, 560);
        width[0x64] = 611;
        boundingBoxes[0x64] = new Rectangle(34, -14, 517, 732);
        width[0x86] = 556;
        boundingBoxes[0x86] = new Rectangle(36, -171, 484, 889);
        width[0x87] = 556;
        boundingBoxes[0x87] = new Rectangle(36, -171, 484, 889);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(57, 426, 286, 286);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(6, 614, 321, 115);
        width[0xf7] = 584;
        boundingBoxes[0xf7] = new Rectangle(40, -42, 504, 590);
        width[0x24] = 556;
        boundingBoxes[0x24] = new Rectangle(30, -115, 493, 890);


        width[0x65] = 556;
        boundingBoxes[0x65] = new Rectangle(23, -14, 505, 560);
        width[0xe9] = 556;
        boundingBoxes[0xe9] = new Rectangle(23, -14, 505, 764);
        width[0xea] = 556;
        boundingBoxes[0xea] = new Rectangle(23, -14, 505, 764);
        width[0xeb] = 556;
        boundingBoxes[0xeb] = new Rectangle(23, -14, 505, 743);
        width[0xe8] = 556;
        boundingBoxes[0xe8] = new Rectangle(23, -14, 505, 764);
        width[0x38] = 556;
        boundingBoxes[0x38] = new Rectangle(32, -19, 492, 729);
        width[0x85] = 1000;
        boundingBoxes[0x85] = new Rectangle(92, 0, 816, 146);
        width[0x97] = 1000;
        boundingBoxes[0x97] = new Rectangle(0, 227, 1000, 106);
        width[0x96] = 556;
        boundingBoxes[0x96] = new Rectangle(0, 227, 556, 106);
        width[0x3d] = 584;
        boundingBoxes[0x3d] = new Rectangle(40, 87, 504, 332);
        width[0xf0] = 611;
        boundingBoxes[0xf0] = new Rectangle(34, -14, 544, 751);
        width[0x21] = 333;
        boundingBoxes[0x21] = new Rectangle(90, 0, 154, 718);
        width[0xa1] = 333;
        boundingBoxes[0xa1] = new Rectangle(90, -186, 154, 718);
        width[0x66] = 333;
        boundingBoxes[0x66] = new Rectangle(10, 0, 308, 727);

        width[0x35] = 556;
        boundingBoxes[0x35] = new Rectangle(27, -19, 489, 717);

        width[0x83] = 556;
        boundingBoxes[0x83] = new Rectangle(-10, -210, 526, 947);
        width[0x34] = 556;
        boundingBoxes[0x34] = new Rectangle(27, 0, 499, 710);

        width[0x67] = 611;
        boundingBoxes[0x67] = new Rectangle(40, -217, 513, 763);
        width[0xdf] = 611;
        boundingBoxes[0xdf] = new Rectangle(69, -14, 510, 745);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(-23, 604, 248, 146);
        width[0x3e] = 584;
        boundingBoxes[0x3e] = new Rectangle(38, -8, 508, 522);
        width[0xab] = 556;
        boundingBoxes[0xab] = new Rectangle(88, 76, 380, 408);
        width[0xbb] = 556;
        boundingBoxes[0xbb] = new Rectangle(88, 76, 380, 408);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(83, 76, 167, 408);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(83, 76, 167, 408);
        width[0x68] = 611;
        boundingBoxes[0x68] = new Rectangle(65, 0, 481, 718);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(27, 215, 279, 130);
        width[0x69] = 278;
        boundingBoxes[0x69] = new Rectangle(69, 0, 140, 725);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(69, 0, 260, 750);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(-37, 0, 353, 750);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(-21, 0, 321, 729);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(-50, 0, 259, 750);
        width[0x6a] = 278;
        boundingBoxes[0x6a] = new Rectangle(3, -214, 206, 939);
        width[0x6b] = 556;
        boundingBoxes[0x6b] = new Rectangle(69, 0, 493, 718);
        width[0x6c] = 278;
        boundingBoxes[0x6c] = new Rectangle(69, 0, 140, 718);
        width[0x3c] = 584;
        boundingBoxes[0x3c] = new Rectangle(38, -8, 508, 522);
        width[0xac] = 584;
        boundingBoxes[0xac] = new Rectangle(40, 108, 504, 311);

        width[0x6d] = 889;
        boundingBoxes[0x6d] = new Rectangle(64, 0, 762, 546);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(-6, 604, 345, 74);

        width[0xb5] = 611;
        boundingBoxes[0xb5] = new Rectangle(66, -207, 479, 739);
        width[0xd7] = 584;
        boundingBoxes[0xd7] = new Rectangle(40, 1, 505, 504);
        width[0x6e] = 611;
        boundingBoxes[0x6e] = new Rectangle(65, 0, 481, 546);
        width[0x39] = 556;
        boundingBoxes[0x39] = new Rectangle(30, -19, 492, 729);
        width[0xf1] = 611;
        boundingBoxes[0xf1] = new Rectangle(65, 0, 481, 737);
        width[0x23] = 556;
        boundingBoxes[0x23] = new Rectangle(18, 0, 520, 698);
        width[0x6f] = 611;
        boundingBoxes[0x6f] = new Rectangle(34, -14, 544, 560);
        width[0xf3] = 611;
        boundingBoxes[0xf3] = new Rectangle(34, -14, 544, 764);
        width[0xf4] = 611;
        boundingBoxes[0xf4] = new Rectangle(34, -14, 544, 764);
        width[0xf6] = 611;
        boundingBoxes[0xf6] = new Rectangle(34, -14, 544, 743);
        width[0x9c] = 944;
        boundingBoxes[0x9c] = new Rectangle(34, -14, 878, 560);

        width[0xf2] = 611;
        boundingBoxes[0xf2] = new Rectangle(34, -14, 544, 764);
        width[0x31] = 556;
        boundingBoxes[0x31] = new Rectangle(69, 0, 309, 710);
        width[0xbd] = 834;
        boundingBoxes[0xbd] = new Rectangle(26, -19, 768, 729);
        width[0xbc] = 834;
        boundingBoxes[0xbc] = new Rectangle(26, -19, 740, 729);
        width[0xb9] = 333;
        boundingBoxes[0xb9] = new Rectangle(26, 283, 211, 427);
        width[0xaa] = 370;
        boundingBoxes[0xaa] = new Rectangle(22, 401, 325, 336);
        width[0xba] = 365;
        boundingBoxes[0xba] = new Rectangle(6, 401, 354, 336);
        width[0xf8] = 611;
        boundingBoxes[0xf8] = new Rectangle(22, -29, 567, 589);
        width[0xf5] = 611;
        boundingBoxes[0xf5] = new Rectangle(34, -14, 544, 751);
        width[0x70] = 611;
        boundingBoxes[0x70] = new Rectangle(62, -207, 516, 753);
        width[0xb6] = 556;
        boundingBoxes[0xb6] = new Rectangle(-8, -191, 547, 891);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(35, -208, 279, 942);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(19, -208, 279, 942);
        width[0x25] = 889;
        boundingBoxes[0x25] = new Rectangle(28, -19, 833, 729);
        width[0x2e] = 278;
        boundingBoxes[0x2e] = new Rectangle(64, 0, 150, 146);
        width[0xb7] = 278;
        boundingBoxes[0xb7] = new Rectangle(58, 172, 162, 162);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(-3, -19, 1006, 729);
        width[0x2b] = 584;
        boundingBoxes[0x2b] = new Rectangle(40, 0, 504, 506);
        width[0xb1] = 584;
        boundingBoxes[0xb1] = new Rectangle(40, 0, 504, 506);
        width[0x71] = 611;
        boundingBoxes[0x71] = new Rectangle(34, -207, 518, 753);
        width[0x3f] = 611;
        boundingBoxes[0x3f] = new Rectangle(60, 0, 496, 727);
        width[0xbf] = 611;
        boundingBoxes[0xbf] = new Rectangle(55, -195, 496, 727);
        width[0x22] = 474;
        boundingBoxes[0x22] = new Rectangle(98, 447, 278, 271);
        width[0x84] = 500;
        boundingBoxes[0x84] = new Rectangle(64, -146, 372, 273);
        width[0x93] = 500;
        boundingBoxes[0x93] = new Rectangle(64, 454, 372, 273);
        width[0x94] = 500;
        boundingBoxes[0x94] = new Rectangle(64, 445, 372, 273);
        width[0x91] = 278;
        boundingBoxes[0x91] = new Rectangle(69, 454, 140, 273);
        width[0x92] = 278;
        boundingBoxes[0x92] = new Rectangle(69, 445, 140, 273);
        width[0x82] = 278;
        boundingBoxes[0x82] = new Rectangle(69, -146, 140, 273);
        width[0x27] = 238;
        boundingBoxes[0x27] = new Rectangle(70, 447, 98, 271);
        width[0x72] = 389;
        boundingBoxes[0x72] = new Rectangle(64, 0, 309, 546);
        width[0xae] = 737;
        boundingBoxes[0xae] = new Rectangle(-11, -19, 759, 756);

        width[0x73] = 556;
        boundingBoxes[0x73] = new Rectangle(30, -14, 489, 560);
        width[0x9a] = 556;
        boundingBoxes[0x9a] = new Rectangle(30, -14, 489, 764);
        width[0xa7] = 556;
        boundingBoxes[0xa7] = new Rectangle(34, -184, 488, 911);
        width[0x3b] = 333;
        boundingBoxes[0x3b] = new Rectangle(92, -168, 150, 680);
        width[0x37] = 556;
        boundingBoxes[0x37] = new Rectangle(25, 0, 503, 698);
        width[0x36] = 556;
        boundingBoxes[0x36] = new Rectangle(31, -19, 489, 729);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-33, -19, 344, 756);
        width[0x20] = 278;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 556;
        boundingBoxes[0xa3] = new Rectangle(28, -16, 513, 734);
        width[0x74] = 333;
        boundingBoxes[0x74] = new Rectangle(10, -6, 299, 682);
        width[0xfe] = 611;
        boundingBoxes[0xfe] = new Rectangle(62, -208, 516, 926);
        width[0x33] = 556;
        boundingBoxes[0x33] = new Rectangle(27, -19, 489, 729);
        width[0xbe] = 834;
        boundingBoxes[0xbe] = new Rectangle(16, -19, 783, 729);
        width[0xb3] = 333;
        boundingBoxes[0xb3] = new Rectangle(8, 271, 318, 439);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(-17, 610, 367, 127);
        width[0x99] = 1000;
        boundingBoxes[0x99] = new Rectangle(44, 306, 912, 412);
        width[0x32] = 556;
        boundingBoxes[0x32] = new Rectangle(26, 0, 485, 710);
        width[0xb2] = 333;
        boundingBoxes[0xb2] = new Rectangle(9, 283, 315, 427);
        width[0x75] = 611;
        boundingBoxes[0x75] = new Rectangle(66, -14, 479, 546);
        width[0xfa] = 611;
        boundingBoxes[0xfa] = new Rectangle(66, -14, 479, 764);
        width[0xfb] = 611;
        boundingBoxes[0xfb] = new Rectangle(66, -14, 479, 764);
        width[0xfc] = 611;
        boundingBoxes[0xfc] = new Rectangle(66, -14, 479, 743);
        width[0xf9] = 611;
        boundingBoxes[0xf9] = new Rectangle(66, -14, 479, 764);
        width[0x5f] = 556;
        boundingBoxes[0x5f] = new Rectangle(0, -125, 556, 50);
        width[0x76] = 556;
        boundingBoxes[0x76] = new Rectangle(13, 0, 530, 532);
        width[0x77] = 778;
        boundingBoxes[0x77] = new Rectangle(10, 0, 759, 532);
        width[0x78] = 556;
        boundingBoxes[0x78] = new Rectangle(15, 0, 526, 532);
        width[0x79] = 556;
        boundingBoxes[0x79] = new Rectangle(10, -214, 529, 746);
        width[0xfd] = 556;
        boundingBoxes[0xfd] = new Rectangle(10, -214, 529, 964);
        width[0xff] = 556;
        boundingBoxes[0xff] = new Rectangle(10, -214, 529, 943);
        width[0xa5] = 556;
        boundingBoxes[0xa5] = new Rectangle(-9, 0, 574, 698);
        width[0x7a] = 500;
        boundingBoxes[0x7a] = new Rectangle(20, 0, 460, 532);
        width[0x9e] = 500;
        boundingBoxes[0x9e] = new Rectangle(20, 0, 460, 750);
        width[0x30] = 556;
        boundingBoxes[0x30] = new Rectangle(32, -19, 492, 729);

        familyNames = new java.util.HashSet();
        familyNames.add("Helvetica");

        kerning = new java.util.HashMap();
        Integer first;
        Integer second;
        Map pairs;

        first = 107;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -15);

        first = 79;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -50);

        second = 87;
        pairs.put(second, -50);

        second = 89;
        pairs.put(second, -70);

        second = 84;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, -40);

        second = 86;
        pairs.put(second, -50);

        second = 88;
        pairs.put(second, -50);

        second = 44;
        pairs.put(second, -40);

        first = 104;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, -20);

        first = 99;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, -20);

        second = 104;
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, -10);

        second = 108;
        pairs.put(second, -20);

        first = 87;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -60);

        second = 45;
        pairs.put(second, -40);

        second = 79;
        pairs.put(second, -20);

        second = 58;
        pairs.put(second, -10);

        second = 97;
        pairs.put(second, -40);

        second = 65;
        pairs.put(second, -60);

        second = 117;
        pairs.put(second, -45);

        second = 121;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -80);

        second = 101;
        pairs.put(second, -35);

        second = 59;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -80);

        first = 112;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, -15);

        first = 80;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -40);

        second = 97;
        pairs.put(second, -30);

        second = 65;
        pairs.put(second, -100);

        second = 46;
        pairs.put(second, -120);

        second = 101;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -120);

        first = 86;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -90);

        second = 45;
        pairs.put(second, -80);

        second = 79;
        pairs.put(second, -50);

        second = 58;
        pairs.put(second, -40);

        second = 97;
        pairs.put(second, -60);

        second = 65;
        pairs.put(second, -80);

        second = 117;
        pairs.put(second, -60);

        second = 46;
        pairs.put(second, -120);

        second = 71;
        pairs.put(second, -50);

        second = 101;
        pairs.put(second, -50);

        second = 59;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, -120);

        first = 59;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, -40);

        first = 118;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -30);

        second = 97;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -80);

        second = 44;
        pairs.put(second, -80);

        first = 32;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 87;
        pairs.put(second, -80);

        second = 147;
        pairs.put(second, -80);

        second = 89;
        pairs.put(second, -120);

        second = 84;
        pairs.put(second, -100);

        second = 145;
        pairs.put(second, -60);

        second = 86;
        pairs.put(second, -80);

        first = 97;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -20);

        second = 103;
        pairs.put(second, -10);

        second = 118;
        pairs.put(second, -15);

        first = 65;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -40);

        second = 119;
        pairs.put(second, -30);

        second = 87;
        pairs.put(second, -60);

        second = 67;
        pairs.put(second, -40);

        second = 81;
        pairs.put(second, -40);

        second = 71;
        pairs.put(second, -50);

        second = 86;
        pairs.put(second, -80);

        second = 118;
        pairs.put(second, -40);

        second = 85;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -110);

        second = 84;
        pairs.put(second, -90);

        second = 121;
        pairs.put(second, -30);

        first = 70;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 97;
        pairs.put(second, -20);

        second = 65;
        pairs.put(second, -80);

        second = 46;
        pairs.put(second, -100);

        second = 44;
        pairs.put(second, -100);

        first = 85;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -50);

        second = 46;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -30);

        first = 115;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

        first = 111;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -20);

        second = 120;
        pairs.put(second, -30);

        second = 118;
        pairs.put(second, -20);

        first = 122;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 101;
        pairs.put(second, 10);

        first = 100;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 100;
        pairs.put(second, -10);

        second = 119;
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -15);

        second = 118;
        pairs.put(second, -15);

        first = 68;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -40);

        second = 87;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -70);

        second = 46;
        pairs.put(second, -30);

        second = 86;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, -30);

        first = 146;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 100;
        pairs.put(second, -80);

        second = 32;
        pairs.put(second, -80);

        second = 146;
        pairs.put(second, -46);

        second = 114;
        pairs.put(second, -40);

        second = 108;
        pairs.put(second, -20);

        second = 115;
        pairs.put(second, -60);

        second = 118;
        pairs.put(second, -20);

        first = 82;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -20);

        second = 87;
        pairs.put(second, -40);

        second = 85;
        pairs.put(second, -20);

        second = 89;
        pairs.put(second, -50);

        second = 84;
        pairs.put(second, -20);

        second = 86;
        pairs.put(second, -50);

        first = 75;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -35);

        second = 79;
        pairs.put(second, -30);

        second = 117;
        pairs.put(second, -30);

        second = 121;
        pairs.put(second, -40);

        second = 101;
        pairs.put(second, -15);

        first = 58;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, -40);

        first = 119;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, -40);

        first = 114;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -20);

        second = 100;
        pairs.put(second, -20);

        second = 45;
        pairs.put(second, -20);

        second = 99;
        pairs.put(second, -20);

        second = 116;
        pairs.put(second, 20);

        second = 121;
        pairs.put(second, 10);

        second = 46;
        pairs.put(second, -60);

        second = 103;
        pairs.put(second, -15);

        second = 113;
        pairs.put(second, -20);

        second = 115;
        pairs.put(second, -15);

        second = 118;
        pairs.put(second, 10);

        second = 44;
        pairs.put(second, -60);

        first = 145;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 145;
        pairs.put(second, -46);

        first = 108;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -15);

        first = 103;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 103;
        pairs.put(second, -10);

        second = 101;
        pairs.put(second, 10);

        first = 66;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -30);

        second = 85;
        pairs.put(second, -10);

        first = 98;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, -20);

        second = 108;
        pairs.put(second, -10);

        second = 118;
        pairs.put(second, -20);

        first = 76;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -140);

        second = 146;
        pairs.put(second, -140);

        second = 87;
        pairs.put(second, -80);

        second = 89;
        pairs.put(second, -120);

        second = 121;
        pairs.put(second, -30);

        second = 84;
        pairs.put(second, -90);

        second = 86;
        pairs.put(second, -110);

        first = 81;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 85;
        pairs.put(second, -10);

        second = 46;
        pairs.put(second, 20);

        second = 44;
        pairs.put(second, 20);

        first = 44;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -120);

        second = 32;
        pairs.put(second, -40);

        second = 146;
        pairs.put(second, -120);

        first = 148;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, -80);

        first = 109;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, -30);

        first = 102;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 30);

        second = 111;
        pairs.put(second, -20);

        second = 146;
        pairs.put(second, 30);

        second = 46;
        pairs.put(second, -10);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -10);

        first = 74;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -20);

        second = 117;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -20);

        first = 89;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -100);

        second = 79;
        pairs.put(second, -70);

        second = 58;
        pairs.put(second, -50);

        second = 97;
        pairs.put(second, -90);

        second = 65;
        pairs.put(second, -110);

        second = 117;
        pairs.put(second, -100);

        second = 46;
        pairs.put(second, -100);

        second = 101;
        pairs.put(second, -80);

        second = 59;
        pairs.put(second, -50);

        second = 44;
        pairs.put(second, -100);

        first = 84;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -80);

        second = 79;
        pairs.put(second, -40);

        second = 58;
        pairs.put(second, -40);

        second = 119;
        pairs.put(second, -60);

        second = 114;
        pairs.put(second, -80);

        second = 44;
        pairs.put(second, -80);

        second = 59;
        pairs.put(second, -40);

        second = 45;
        pairs.put(second, -120);

        second = 65;
        pairs.put(second, -90);

        second = 97;
        pairs.put(second, -80);

        second = 117;
        pairs.put(second, -90);

        second = 121;
        pairs.put(second, -60);

        second = 46;
        pairs.put(second, -80);

        second = 101;
        pairs.put(second, -60);

        first = 121;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -25);

        second = 97;
        pairs.put(second, -30);

        second = 46;
        pairs.put(second, -80);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -80);

        first = 46;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -120);

        second = 32;
        pairs.put(second, -40);

        second = 146;
        pairs.put(second, -120);

        first = 110;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, -20);

        second = 118;
        pairs.put(second, -40);

        first = 120;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 101;
        pairs.put(second, -10);

        first = 101;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -15);

        second = 46;
        pairs.put(second, 20);

        second = 120;
        pairs.put(second, -15);

        second = 118;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, 10);

    }

    public HelveticaBold() {
        this(false);
    }

    public HelveticaBold(boolean enableKerning) {
        this.enableKerning = enableKerning;
    }

    public String getEncodingName() {
        return encoding;
    }

    public URI getFontURI() {
        return fontFileURI;
    }

    public String getFontName() {
        return fontName;
    }

    public String getEmbedFontName() {
        return getFontName();
    }

    public String getFullName() {
        return fullName;
    }

    public Set getFamilyNames() {
        return familyNames;
    }

    public FontType getFontType() {
        return FontType.TYPE1;
    }

    public int getAscender(int size) {
        return size * ascender;
    }

    public int getCapHeight(int size) {
        return size * capHeight;
    }

    public int getDescender(int size) {
        return size * descender;
    }

    public int getXHeight(int size) {
        return size * xHeight;
    }

    public int getUnderlinePosition(int size) {
        return size * underlinePosition;
    }

    public int getUnderlineThickness(int size) {
        return size * underlineThickness;
    }

    public int getFirstChar() {
        return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }

    public int getWidth(int i, int size) {
        return size * width[i];
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        Rectangle bbox = boundingBoxes[glyphIndex];
        return new Rectangle(bbox.x * size, bbox.y * size, bbox.width * size, bbox.height * size);
    }

    public int[] getWidths() {
        int[] arr = new int[getLastChar() - getFirstChar() + 1];
        System.arraycopy(width, getFirstChar(), arr, 0, getLastChar() - getFirstChar() + 1);
        return arr;
    }

    public boolean hasKerningInfo() {
        return enableKerning;
    }

    public java.util.Map getKerningInfo() {
        return kerning;
    }

    public char mapChar(char c) {
        notifyMapOperation();
        char d = mapping.mapChar(c);
        if (d != 0) {
            return d;
        } else {
            this.warnMissingGlyph(c);
            return Typeface.NOT_FOUND;
        }
    }

    public boolean hasChar(char c) {
        return (mapping.mapChar(c) > 0);
    }

}
