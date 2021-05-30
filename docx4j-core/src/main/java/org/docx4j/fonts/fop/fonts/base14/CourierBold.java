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

import java.util.Set;

import org.docx4j.fonts.fop.fonts.Base14Font;
import org.docx4j.fonts.fop.fonts.CodePointMapping;
import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.Typeface;

// CSOFF: ConstantNameCheck

public class CourierBold extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Courier-Bold";
    private static final String fullName = "Courier Bold";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 562;
    private static final int xHeight = 439;
    private static final int ascender = 626;
    private static final int descender = -142;
    private static final int firstChar = 32;
    private static final int lastChar = 255;
    private static final int[] width;
    private static final Rectangle[] boundingBoxes;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("WinAnsiEncoding");


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

        width[0x41] = 600;
        boundingBoxes[0x41] = new Rectangle(-9, 0, 618, 562);
        width[0xc6] = 600;
        boundingBoxes[0xc6] = new Rectangle(-29, 0, 631, 562);
        width[0xc1] = 600;
        boundingBoxes[0xc1] = new Rectangle(-9, 0, 618, 784);
        width[0xc2] = 600;
        boundingBoxes[0xc2] = new Rectangle(-9, 0, 618, 780);
        width[0xc4] = 600;
        boundingBoxes[0xc4] = new Rectangle(-9, 0, 618, 761);
        width[0xc0] = 600;
        boundingBoxes[0xc0] = new Rectangle(-9, 0, 618, 784);
        width[0xc5] = 600;
        boundingBoxes[0xc5] = new Rectangle(-9, 0, 618, 801);
        width[0xc3] = 600;
        boundingBoxes[0xc3] = new Rectangle(-9, 0, 618, 759);
        width[0x42] = 600;
        boundingBoxes[0x42] = new Rectangle(30, 0, 543, 562);
        width[0x43] = 600;
        boundingBoxes[0x43] = new Rectangle(22, -18, 538, 598);
        width[0xc7] = 600;
        boundingBoxes[0xc7] = new Rectangle(22, -206, 538, 786);
        width[0x44] = 600;
        boundingBoxes[0x44] = new Rectangle(30, 0, 564, 562);
        width[0x45] = 600;
        boundingBoxes[0x45] = new Rectangle(25, 0, 535, 562);
        width[0xc9] = 600;
        boundingBoxes[0xc9] = new Rectangle(25, 0, 535, 784);
        width[0xca] = 600;
        boundingBoxes[0xca] = new Rectangle(25, 0, 535, 780);
        width[0xcb] = 600;
        boundingBoxes[0xcb] = new Rectangle(25, 0, 535, 761);
        width[0xc8] = 600;
        boundingBoxes[0xc8] = new Rectangle(25, 0, 535, 784);
        width[0xd0] = 600;
        boundingBoxes[0xd0] = new Rectangle(30, 0, 564, 562);
        width[0x80] = 600;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 600;
        boundingBoxes[0x46] = new Rectangle(39, 0, 531, 562);
        width[0x47] = 600;
        boundingBoxes[0x47] = new Rectangle(22, -18, 572, 598);
        width[0x48] = 600;
        boundingBoxes[0x48] = new Rectangle(20, 0, 560, 562);
        width[0x49] = 600;
        boundingBoxes[0x49] = new Rectangle(77, 0, 446, 562);
        width[0xcd] = 600;
        boundingBoxes[0xcd] = new Rectangle(77, 0, 446, 784);
        width[0xce] = 600;
        boundingBoxes[0xce] = new Rectangle(77, 0, 446, 780);
        width[0xcf] = 600;
        boundingBoxes[0xcf] = new Rectangle(77, 0, 446, 761);
        width[0xcc] = 600;
        boundingBoxes[0xcc] = new Rectangle(77, 0, 446, 784);
        width[0x4a] = 600;
        boundingBoxes[0x4a] = new Rectangle(37, -18, 564, 580);
        width[0x4b] = 600;
        boundingBoxes[0x4b] = new Rectangle(21, 0, 578, 562);
        width[0x4c] = 600;
        boundingBoxes[0x4c] = new Rectangle(39, 0, 539, 562);

        width[0x4d] = 600;
        boundingBoxes[0x4d] = new Rectangle(-2, 0, 604, 562);
        width[0x4e] = 600;
        boundingBoxes[0x4e] = new Rectangle(8, -12, 602, 574);
        width[0xd1] = 600;
        boundingBoxes[0xd1] = new Rectangle(8, -12, 602, 771);
        width[0x4f] = 600;
        boundingBoxes[0x4f] = new Rectangle(22, -18, 556, 598);
        width[0x8c] = 600;
        boundingBoxes[0x8c] = new Rectangle(-25, 0, 620, 562);
        width[0xd3] = 600;
        boundingBoxes[0xd3] = new Rectangle(22, -18, 556, 802);
        width[0xd4] = 600;
        boundingBoxes[0xd4] = new Rectangle(22, -18, 556, 798);
        width[0xd6] = 600;
        boundingBoxes[0xd6] = new Rectangle(22, -18, 556, 779);
        width[0xd2] = 600;
        boundingBoxes[0xd2] = new Rectangle(22, -18, 556, 802);
        width[0xd8] = 600;
        boundingBoxes[0xd8] = new Rectangle(22, -22, 556, 606);
        width[0xd5] = 600;
        boundingBoxes[0xd5] = new Rectangle(22, -18, 556, 777);
        width[0x50] = 600;
        boundingBoxes[0x50] = new Rectangle(48, 0, 511, 562);
        width[0x51] = 600;
        boundingBoxes[0x51] = new Rectangle(32, -138, 546, 718);
        width[0x52] = 600;
        boundingBoxes[0x52] = new Rectangle(24, 0, 575, 562);
        width[0x53] = 600;
        boundingBoxes[0x53] = new Rectangle(47, -22, 506, 604);
        width[0x8a] = 600;
        boundingBoxes[0x8a] = new Rectangle(47, -22, 506, 812);

        width[0x54] = 600;
        boundingBoxes[0x54] = new Rectangle(21, 0, 558, 562);
        width[0xde] = 600;
        boundingBoxes[0xde] = new Rectangle(48, 0, 509, 562);
        width[0x55] = 600;
        boundingBoxes[0x55] = new Rectangle(4, -18, 592, 580);
        width[0xda] = 600;
        boundingBoxes[0xda] = new Rectangle(4, -18, 592, 802);
        width[0xdb] = 600;
        boundingBoxes[0xdb] = new Rectangle(4, -18, 592, 798);
        width[0xdc] = 600;
        boundingBoxes[0xdc] = new Rectangle(4, -18, 592, 779);
        width[0xd9] = 600;
        boundingBoxes[0xd9] = new Rectangle(4, -18, 592, 802);
        width[0x56] = 600;
        boundingBoxes[0x56] = new Rectangle(-13, 0, 626, 562);
        width[0x57] = 600;
        boundingBoxes[0x57] = new Rectangle(-18, 0, 636, 562);
        width[0x58] = 600;
        boundingBoxes[0x58] = new Rectangle(12, 0, 576, 562);
        width[0x59] = 600;
        boundingBoxes[0x59] = new Rectangle(12, 0, 577, 562);
        width[0xdd] = 600;
        boundingBoxes[0xdd] = new Rectangle(12, 0, 577, 784);
        width[0x9f] = 600;
        boundingBoxes[0x9f] = new Rectangle(12, 0, 577, 761);
        width[0x5a] = 600;
        boundingBoxes[0x5a] = new Rectangle(62, 0, 477, 562);
        width[0x8e] = 600;
        boundingBoxes[0x8e] = new Rectangle(62, 0, 477, 790);
        width[0x61] = 600;
        boundingBoxes[0x61] = new Rectangle(35, -15, 535, 469);
        width[0xe1] = 600;
        boundingBoxes[0xe1] = new Rectangle(35, -15, 535, 676);
        width[0xe2] = 600;
        boundingBoxes[0xe2] = new Rectangle(35, -15, 535, 672);
        width[0xb4] = 600;
        boundingBoxes[0xb4] = new Rectangle(205, 508, 263, 153);
        width[0xe4] = 600;
        boundingBoxes[0xe4] = new Rectangle(35, -15, 535, 653);
        width[0xe6] = 600;
        boundingBoxes[0xe6] = new Rectangle(-4, -15, 605, 469);
        width[0xe0] = 600;
        boundingBoxes[0xe0] = new Rectangle(35, -15, 535, 676);
        width[0x26] = 600;
        boundingBoxes[0x26] = new Rectangle(36, -15, 510, 558);
        width[0xe5] = 600;
        boundingBoxes[0xe5] = new Rectangle(35, -15, 535, 693);
        width[0x5e] = 600;
        boundingBoxes[0x5e] = new Rectangle(108, 250, 384, 366);
        width[0x7e] = 600;
        boundingBoxes[0x7e] = new Rectangle(71, 153, 459, 203);
        width[0x2a] = 600;
        boundingBoxes[0x2a] = new Rectangle(91, 219, 418, 382);
        width[0x40] = 600;
        boundingBoxes[0x40] = new Rectangle(16, -15, 568, 631);
        width[0xe3] = 600;
        boundingBoxes[0xe3] = new Rectangle(35, -15, 535, 651);
        width[0x62] = 600;
        boundingBoxes[0x62] = new Rectangle(0, -15, 584, 641);
        width[0x5c] = 600;
        boundingBoxes[0x5c] = new Rectangle(99, -77, 404, 703);
        width[0x7c] = 600;
        boundingBoxes[0x7c] = new Rectangle(255, -250, 90, 1000);
        width[0x7b] = 600;
        boundingBoxes[0x7b] = new Rectangle(160, -102, 304, 718);
        width[0x7d] = 600;
        boundingBoxes[0x7d] = new Rectangle(136, -102, 304, 718);
        width[0x5b] = 600;
        boundingBoxes[0x5b] = new Rectangle(245, -102, 230, 718);
        width[0x5d] = 600;
        boundingBoxes[0x5d] = new Rectangle(125, -102, 230, 718);

        width[0xa6] = 600;
        boundingBoxes[0xa6] = new Rectangle(255, -175, 90, 850);
        width[0x95] = 600;
        boundingBoxes[0x95] = new Rectangle(140, 132, 320, 298);
        width[0x63] = 600;
        boundingBoxes[0x63] = new Rectangle(40, -15, 505, 474);

        width[0xe7] = 600;
        boundingBoxes[0xe7] = new Rectangle(40, -206, 505, 665);
        width[0xb8] = 600;
        boundingBoxes[0xb8] = new Rectangle(205, -206, 182, 206);
        width[0xa2] = 600;
        boundingBoxes[0xa2] = new Rectangle(66, -49, 452, 663);
        width[0x88] = 600;
        boundingBoxes[0x88] = new Rectangle(103, 483, 394, 174);
        width[0x3a] = 600;
        boundingBoxes[0x3a] = new Rectangle(191, -15, 216, 440);
        width[0x2c] = 600;
        boundingBoxes[0x2c] = new Rectangle(123, -111, 270, 285);
        width[0xa9] = 600;
        boundingBoxes[0xa9] = new Rectangle(0, -18, 600, 598);
        width[0xa4] = 600;
        boundingBoxes[0xa4] = new Rectangle(54, 49, 492, 468);
        width[0x64] = 600;
        boundingBoxes[0x64] = new Rectangle(20, -15, 571, 641);
        width[0x86] = 600;
        boundingBoxes[0x86] = new Rectangle(106, -70, 388, 650);
        width[0x87] = 600;
        boundingBoxes[0x87] = new Rectangle(106, -70, 388, 650);
        width[0xb0] = 600;
        boundingBoxes[0xb0] = new Rectangle(86, 243, 388, 373);
        width[0xa8] = 600;
        boundingBoxes[0xa8] = new Rectangle(128, 498, 344, 140);
        width[0xf7] = 600;
        boundingBoxes[0xf7] = new Rectangle(71, 16, 458, 484);
        width[0x24] = 600;
        boundingBoxes[0x24] = new Rectangle(82, -126, 437, 792);


        width[0x65] = 600;
        boundingBoxes[0x65] = new Rectangle(40, -15, 523, 469);
        width[0xe9] = 600;
        boundingBoxes[0xe9] = new Rectangle(40, -15, 523, 676);
        width[0xea] = 600;
        boundingBoxes[0xea] = new Rectangle(40, -15, 523, 672);
        width[0xeb] = 600;
        boundingBoxes[0xeb] = new Rectangle(40, -15, 523, 653);
        width[0xe8] = 600;
        boundingBoxes[0xe8] = new Rectangle(40, -15, 523, 676);
        width[0x38] = 600;
        boundingBoxes[0x38] = new Rectangle(83, -15, 434, 631);
        width[0x85] = 600;
        boundingBoxes[0x85] = new Rectangle(26, -15, 548, 131);
        width[0x97] = 600;
        boundingBoxes[0x97] = new Rectangle(-10, 203, 620, 110);
        width[0x96] = 600;
        boundingBoxes[0x96] = new Rectangle(65, 203, 470, 110);
        width[0x3d] = 600;
        boundingBoxes[0x3d] = new Rectangle(71, 118, 458, 280);
        width[0xf0] = 600;
        boundingBoxes[0xf0] = new Rectangle(58, -27, 485, 653);
        width[0x21] = 600;
        boundingBoxes[0x21] = new Rectangle(202, -15, 196, 587);
        width[0xa1] = 600;
        boundingBoxes[0xa1] = new Rectangle(202, -146, 196, 595);
        width[0x66] = 600;
        boundingBoxes[0x66] = new Rectangle(83, 0, 464, 626);

        width[0x35] = 600;
        boundingBoxes[0x35] = new Rectangle(70, -15, 451, 616);

        width[0x83] = 600;
        boundingBoxes[0x83] = new Rectangle(-30, -131, 602, 747);
        width[0x34] = 600;
        boundingBoxes[0x34] = new Rectangle(53, 0, 454, 616);

        width[0x67] = 600;
        boundingBoxes[0x67] = new Rectangle(30, -146, 550, 600);
        width[0xdf] = 600;
        boundingBoxes[0xdf] = new Rectangle(22, -15, 574, 641);
        width[0x60] = 600;
        boundingBoxes[0x60] = new Rectangle(132, 508, 263, 153);
        width[0x3e] = 600;
        boundingBoxes[0x3e] = new Rectangle(77, 15, 457, 486);
        width[0xab] = 600;
        boundingBoxes[0xab] = new Rectangle(8, 70, 545, 376);
        width[0xbb] = 600;
        boundingBoxes[0xbb] = new Rectangle(47, 70, 545, 376);
        width[0x8b] = 600;
        boundingBoxes[0x8b] = new Rectangle(141, 70, 318, 376);
        width[0x9b] = 600;
        boundingBoxes[0x9b] = new Rectangle(141, 70, 318, 376);
        width[0x68] = 600;
        boundingBoxes[0x68] = new Rectangle(5, 0, 587, 626);

        width[0x2d] = 600;
        boundingBoxes[0x2d] = new Rectangle(100, 203, 400, 110);
        width[0x69] = 600;
        boundingBoxes[0x69] = new Rectangle(77, 0, 446, 658);
        width[0xed] = 600;
        boundingBoxes[0xed] = new Rectangle(77, 0, 446, 661);
        width[0xee] = 600;
        boundingBoxes[0xee] = new Rectangle(73, 0, 450, 657);
        width[0xef] = 600;
        boundingBoxes[0xef] = new Rectangle(77, 0, 446, 618);
        width[0xec] = 600;
        boundingBoxes[0xec] = new Rectangle(77, 0, 446, 661);
        width[0x6a] = 600;
        boundingBoxes[0x6a] = new Rectangle(63, -146, 377, 804);
        width[0x6b] = 600;
        boundingBoxes[0x6b] = new Rectangle(20, 0, 565, 626);
        width[0x6c] = 600;
        boundingBoxes[0x6c] = new Rectangle(77, 0, 446, 626);
        width[0x3c] = 600;
        boundingBoxes[0x3c] = new Rectangle(66, 15, 457, 486);
        width[0xac] = 600;
        boundingBoxes[0xac] = new Rectangle(71, 103, 458, 310);

        width[0x6d] = 600;
        boundingBoxes[0x6d] = new Rectangle(-22, 0, 648, 454);
        width[0xaf] = 600;
        boundingBoxes[0xaf] = new Rectangle(88, 505, 424, 80);

        width[0xb5] = 600;
        boundingBoxes[0xb5] = new Rectangle(-1, -142, 570, 581);
        width[0xd7] = 600;
        boundingBoxes[0xd7] = new Rectangle(81, 39, 439, 439);
        width[0x6e] = 600;
        boundingBoxes[0x6e] = new Rectangle(18, 0, 574, 454);
        width[0x39] = 600;
        boundingBoxes[0x39] = new Rectangle(79, -15, 431, 631);
        width[0xf1] = 600;
        boundingBoxes[0xf1] = new Rectangle(18, 0, 574, 636);
        width[0x23] = 600;
        boundingBoxes[0x23] = new Rectangle(56, -45, 488, 696);
        width[0x6f] = 600;
        boundingBoxes[0x6f] = new Rectangle(30, -15, 540, 469);
        width[0xf3] = 600;
        boundingBoxes[0xf3] = new Rectangle(30, -15, 540, 676);
        width[0xf4] = 600;
        boundingBoxes[0xf4] = new Rectangle(30, -15, 540, 672);
        width[0xf6] = 600;
        boundingBoxes[0xf6] = new Rectangle(30, -15, 540, 653);
        width[0x9c] = 600;
        boundingBoxes[0x9c] = new Rectangle(-18, -15, 629, 469);

        width[0xf2] = 600;
        boundingBoxes[0xf2] = new Rectangle(30, -15, 540, 676);
        width[0x31] = 600;
        boundingBoxes[0x31] = new Rectangle(81, 0, 458, 616);
        width[0xbd] = 600;
        boundingBoxes[0xbd] = new Rectangle(-47, -60, 695, 721);
        width[0xbc] = 600;
        boundingBoxes[0xbc] = new Rectangle(-56, -60, 712, 721);
        width[0xb9] = 600;
        boundingBoxes[0xb9] = new Rectangle(153, 230, 294, 386);
        width[0xaa] = 600;
        boundingBoxes[0xaa] = new Rectangle(147, 196, 306, 384);
        width[0xba] = 600;
        boundingBoxes[0xba] = new Rectangle(147, 196, 306, 384);
        width[0xf8] = 600;
        boundingBoxes[0xf8] = new Rectangle(30, -24, 540, 487);
        width[0xf5] = 600;
        boundingBoxes[0xf5] = new Rectangle(30, -15, 540, 651);
        width[0x70] = 600;
        boundingBoxes[0x70] = new Rectangle(-1, -142, 571, 596);
        width[0xb6] = 600;
        boundingBoxes[0xb6] = new Rectangle(6, -70, 570, 650);
        width[0x28] = 600;
        boundingBoxes[0x28] = new Rectangle(219, -102, 242, 718);
        width[0x29] = 600;
        boundingBoxes[0x29] = new Rectangle(139, -102, 242, 718);
        width[0x25] = 600;
        boundingBoxes[0x25] = new Rectangle(5, -15, 590, 631);
        width[0x2e] = 600;
        boundingBoxes[0x2e] = new Rectangle(192, -15, 216, 186);
        width[0xb7] = 600;
        boundingBoxes[0xb7] = new Rectangle(196, 165, 208, 186);
        width[0x89] = 600;
        boundingBoxes[0x89] = new Rectangle(-113, -15, 826, 631);
        width[0x2b] = 600;
        boundingBoxes[0x2b] = new Rectangle(71, 39, 458, 439);
        width[0xb1] = 600;
        boundingBoxes[0xb1] = new Rectangle(71, 24, 458, 491);
        width[0x71] = 600;
        boundingBoxes[0x71] = new Rectangle(20, -142, 571, 596);
        width[0x3f] = 600;
        boundingBoxes[0x3f] = new Rectangle(98, -14, 403, 594);
        width[0xbf] = 600;
        boundingBoxes[0xbf] = new Rectangle(99, -146, 403, 595);
        width[0x22] = 600;
        boundingBoxes[0x22] = new Rectangle(135, 277, 330, 285);
        width[0x84] = 600;
        boundingBoxes[0x84] = new Rectangle(65, -142, 464, 285);
        width[0x93] = 600;
        boundingBoxes[0x93] = new Rectangle(71, 277, 464, 285);
        width[0x94] = 600;
        boundingBoxes[0x94] = new Rectangle(61, 277, 464, 285);
        width[0x91] = 600;
        boundingBoxes[0x91] = new Rectangle(178, 277, 250, 285);
        width[0x92] = 600;
        boundingBoxes[0x92] = new Rectangle(171, 277, 252, 285);
        width[0x82] = 600;
        boundingBoxes[0x82] = new Rectangle(175, -142, 252, 285);
        width[0x27] = 600;
        boundingBoxes[0x27] = new Rectangle(227, 277, 146, 285);
        width[0x72] = 600;
        boundingBoxes[0x72] = new Rectangle(47, 0, 533, 454);
        width[0xae] = 600;
        boundingBoxes[0xae] = new Rectangle(0, -18, 600, 598);

        width[0x73] = 600;
        boundingBoxes[0x73] = new Rectangle(68, -17, 467, 476);
        width[0x9a] = 600;
        boundingBoxes[0x9a] = new Rectangle(68, -17, 467, 684);

        width[0xa7] = 600;
        boundingBoxes[0xa7] = new Rectangle(83, -70, 434, 650);
        width[0x3b] = 600;
        boundingBoxes[0x3b] = new Rectangle(123, -111, 285, 536);
        width[0x37] = 600;
        boundingBoxes[0x37] = new Rectangle(55, 0, 439, 601);
        width[0x36] = 600;
        boundingBoxes[0x36] = new Rectangle(90, -15, 431, 631);
        width[0x2f] = 600;
        boundingBoxes[0x2f] = new Rectangle(98, -77, 404, 703);
        width[0x20] = 600;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 600;
        boundingBoxes[0xa3] = new Rectangle(72, -28, 486, 639);
        width[0x74] = 600;
        boundingBoxes[0x74] = new Rectangle(47, -15, 485, 577);
        width[0xfe] = 600;
        boundingBoxes[0xfe] = new Rectangle(-14, -142, 584, 768);
        width[0x33] = 600;
        boundingBoxes[0x33] = new Rectangle(63, -15, 438, 631);
        width[0xbe] = 600;
        boundingBoxes[0xbe] = new Rectangle(-47, -60, 695, 721);
        width[0xb3] = 600;
        boundingBoxes[0xb3] = new Rectangle(138, 222, 295, 394);
        width[0x98] = 600;
        boundingBoxes[0x98] = new Rectangle(89, 493, 423, 143);
        width[0x99] = 600;
        boundingBoxes[0x99] = new Rectangle(-9, 230, 758, 332);
        width[0x32] = 600;
        boundingBoxes[0x32] = new Rectangle(61, 0, 438, 616);
        width[0xb2] = 600;
        boundingBoxes[0xb2] = new Rectangle(143, 230, 293, 386);
        width[0x75] = 600;
        boundingBoxes[0x75] = new Rectangle(-1, -15, 570, 454);
        width[0xfa] = 600;
        boundingBoxes[0xfa] = new Rectangle(-1, -15, 570, 676);
        width[0xfb] = 600;
        boundingBoxes[0xfb] = new Rectangle(-1, -15, 570, 672);
        width[0xfc] = 600;
        boundingBoxes[0xfc] = new Rectangle(-1, -15, 570, 653);
        width[0xf9] = 600;
        boundingBoxes[0xf9] = new Rectangle(-1, -15, 570, 676);
        width[0x5f] = 600;
        boundingBoxes[0x5f] = new Rectangle(0, -125, 600, 50);
        width[0x76] = 600;
        boundingBoxes[0x76] = new Rectangle(-1, 0, 602, 439);
        width[0x77] = 600;
        boundingBoxes[0x77] = new Rectangle(-18, 0, 636, 439);
        width[0x78] = 600;
        boundingBoxes[0x78] = new Rectangle(6, 0, 588, 439);
        width[0x79] = 600;
        boundingBoxes[0x79] = new Rectangle(-4, -142, 605, 581);
        width[0xfd] = 600;
        boundingBoxes[0xfd] = new Rectangle(-4, -142, 605, 803);
        width[0xff] = 600;
        boundingBoxes[0xff] = new Rectangle(-4, -142, 605, 780);
        width[0xa5] = 600;
        boundingBoxes[0xa5] = new Rectangle(10, 0, 580, 562);
        width[0x7a] = 600;
        boundingBoxes[0x7a] = new Rectangle(81, 0, 439, 439);
        width[0x9e] = 600;
        boundingBoxes[0x9e] = new Rectangle(81, 0, 439, 667);
        width[0x30] = 600;
        boundingBoxes[0x30] = new Rectangle(87, -15, 426, 631);

        familyNames = new java.util.HashSet();
        familyNames.add("Courier");

    }

    public CourierBold() {
        this(false);
    }

    public CourierBold(boolean enableKerning) {
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
        return false;
    }

    public java.util.Map getKerningInfo() {
        return java.util.Collections.EMPTY_MAP;
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
