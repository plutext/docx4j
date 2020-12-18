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

public class CourierBoldOblique extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Courier-BoldOblique";
    private static final String fullName = "Courier Bold Oblique";
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
        boundingBoxes[0x41] = new Rectangle(-9, 0, 641, 562);
        width[0xc6] = 600;
        boundingBoxes[0xc6] = new Rectangle(-29, 0, 737, 562);
        width[0xc1] = 600;
        boundingBoxes[0xc1] = new Rectangle(-9, 0, 664, 784);
        width[0xc2] = 600;
        boundingBoxes[0xc2] = new Rectangle(-9, 0, 641, 780);
        width[0xc4] = 600;
        boundingBoxes[0xc4] = new Rectangle(-9, 0, 641, 761);
        width[0xc0] = 600;
        boundingBoxes[0xc0] = new Rectangle(-9, 0, 641, 784);
        width[0xc5] = 600;
        boundingBoxes[0xc5] = new Rectangle(-9, 0, 641, 801);
        width[0xc3] = 600;
        boundingBoxes[0xc3] = new Rectangle(-9, 0, 678, 759);
        width[0x42] = 600;
        boundingBoxes[0x42] = new Rectangle(30, 0, 600, 562);
        width[0x43] = 600;
        boundingBoxes[0x43] = new Rectangle(74, -18, 601, 598);
        width[0xc7] = 600;
        boundingBoxes[0xc7] = new Rectangle(74, -206, 601, 786);
        width[0x44] = 600;
        boundingBoxes[0x44] = new Rectangle(30, 0, 634, 562);
        width[0x45] = 600;
        boundingBoxes[0x45] = new Rectangle(25, 0, 645, 562);
        width[0xc9] = 600;
        boundingBoxes[0xc9] = new Rectangle(25, 0, 645, 784);
        width[0xca] = 600;
        boundingBoxes[0xca] = new Rectangle(25, 0, 645, 780);
        width[0xcb] = 600;
        boundingBoxes[0xcb] = new Rectangle(25, 0, 645, 761);
        width[0xc8] = 600;
        boundingBoxes[0xc8] = new Rectangle(25, 0, 645, 784);
        width[0xd0] = 600;
        boundingBoxes[0xd0] = new Rectangle(30, 0, 634, 562);
        width[0x80] = 600;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 600;
        boundingBoxes[0x46] = new Rectangle(39, 0, 645, 562);
        width[0x47] = 600;
        boundingBoxes[0x47] = new Rectangle(74, -18, 601, 598);
        width[0x48] = 600;
        boundingBoxes[0x48] = new Rectangle(20, 0, 680, 562);
        width[0x49] = 600;
        boundingBoxes[0x49] = new Rectangle(77, 0, 566, 562);
        width[0xcd] = 600;
        boundingBoxes[0xcd] = new Rectangle(77, 0, 566, 784);
        width[0xce] = 600;
        boundingBoxes[0xce] = new Rectangle(77, 0, 566, 780);
        width[0xcf] = 600;
        boundingBoxes[0xcf] = new Rectangle(77, 0, 566, 761);
        width[0xcc] = 600;
        boundingBoxes[0xcc] = new Rectangle(77, 0, 566, 784);
        width[0x4a] = 600;
        boundingBoxes[0x4a] = new Rectangle(58, -18, 663, 580);
        width[0x4b] = 600;
        boundingBoxes[0x4b] = new Rectangle(21, 0, 671, 562);
        width[0x4c] = 600;
        boundingBoxes[0x4c] = new Rectangle(39, 0, 597, 562);

        width[0x4d] = 600;
        boundingBoxes[0x4d] = new Rectangle(-2, 0, 724, 562);
        width[0x4e] = 600;
        boundingBoxes[0x4e] = new Rectangle(8, -12, 722, 574);
        width[0xd1] = 600;
        boundingBoxes[0xd1] = new Rectangle(8, -12, 722, 771);
        width[0x4f] = 600;
        boundingBoxes[0x4f] = new Rectangle(74, -18, 571, 598);
        width[0x8c] = 600;
        boundingBoxes[0x8c] = new Rectangle(26, 0, 675, 562);
        width[0xd3] = 600;
        boundingBoxes[0xd3] = new Rectangle(74, -18, 571, 802);
        width[0xd4] = 600;
        boundingBoxes[0xd4] = new Rectangle(74, -18, 571, 798);
        width[0xd6] = 600;
        boundingBoxes[0xd6] = new Rectangle(74, -18, 571, 779);
        width[0xd2] = 600;
        boundingBoxes[0xd2] = new Rectangle(74, -18, 571, 802);
        width[0xd8] = 600;
        boundingBoxes[0xd8] = new Rectangle(48, -22, 625, 606);
        width[0xd5] = 600;
        boundingBoxes[0xd5] = new Rectangle(74, -18, 595, 777);
        width[0x50] = 600;
        boundingBoxes[0x50] = new Rectangle(48, 0, 595, 562);
        width[0x51] = 600;
        boundingBoxes[0x51] = new Rectangle(83, -138, 553, 718);
        width[0x52] = 600;
        boundingBoxes[0x52] = new Rectangle(24, 0, 593, 562);
        width[0x53] = 600;
        boundingBoxes[0x53] = new Rectangle(54, -22, 619, 604);
        width[0x8a] = 600;
        boundingBoxes[0x8a] = new Rectangle(54, -22, 635, 812);

        width[0x54] = 600;
        boundingBoxes[0x54] = new Rectangle(86, 0, 593, 562);
        width[0xde] = 600;
        boundingBoxes[0xde] = new Rectangle(48, 0, 572, 562);
        width[0x55] = 600;
        boundingBoxes[0x55] = new Rectangle(101, -18, 615, 580);
        width[0xda] = 600;
        boundingBoxes[0xda] = new Rectangle(101, -18, 615, 802);
        width[0xdb] = 600;
        boundingBoxes[0xdb] = new Rectangle(101, -18, 615, 798);
        width[0xdc] = 600;
        boundingBoxes[0xdc] = new Rectangle(101, -18, 615, 779);
        width[0xd9] = 600;
        boundingBoxes[0xd9] = new Rectangle(101, -18, 615, 802);
        width[0x56] = 600;
        boundingBoxes[0x56] = new Rectangle(84, 0, 649, 562);
        width[0x57] = 600;
        boundingBoxes[0x57] = new Rectangle(79, 0, 659, 562);
        width[0x58] = 600;
        boundingBoxes[0x58] = new Rectangle(12, 0, 678, 562);
        width[0x59] = 600;
        boundingBoxes[0x59] = new Rectangle(109, 0, 600, 562);
        width[0xdd] = 600;
        boundingBoxes[0xdd] = new Rectangle(109, 0, 600, 784);
        width[0x9f] = 600;
        boundingBoxes[0x9f] = new Rectangle(109, 0, 600, 761);
        width[0x5a] = 600;
        boundingBoxes[0x5a] = new Rectangle(62, 0, 575, 562);
        width[0x8e] = 600;
        boundingBoxes[0x8e] = new Rectangle(62, 0, 597, 790);
        width[0x61] = 600;
        boundingBoxes[0x61] = new Rectangle(61, -15, 532, 469);
        width[0xe1] = 600;
        boundingBoxes[0xe1] = new Rectangle(61, -15, 548, 676);
        width[0xe2] = 600;
        boundingBoxes[0xe2] = new Rectangle(61, -15, 546, 672);
        width[0xb4] = 600;
        boundingBoxes[0xb4] = new Rectangle(312, 508, 297, 153);
        width[0xe4] = 600;
        boundingBoxes[0xe4] = new Rectangle(61, -15, 534, 653);
        width[0xe6] = 600;
        boundingBoxes[0xe6] = new Rectangle(21, -15, 631, 469);
        width[0xe0] = 600;
        boundingBoxes[0xe0] = new Rectangle(61, -15, 532, 676);
        width[0x26] = 600;
        boundingBoxes[0x26] = new Rectangle(61, -15, 534, 558);
        width[0xe5] = 600;
        boundingBoxes[0xe5] = new Rectangle(61, -15, 532, 693);
        width[0x5e] = 600;
        boundingBoxes[0x5e] = new Rectangle(171, 250, 385, 366);
        width[0x7e] = 600;
        boundingBoxes[0x7e] = new Rectangle(120, 153, 470, 203);
        width[0x2a] = 600;
        boundingBoxes[0x2a] = new Rectangle(179, 219, 419, 382);
        width[0x40] = 600;
        boundingBoxes[0x40] = new Rectangle(65, -15, 577, 631);
        width[0xe3] = 600;
        boundingBoxes[0xe3] = new Rectangle(61, -15, 582, 651);
        width[0x62] = 600;
        boundingBoxes[0x62] = new Rectangle(13, -15, 623, 641);
        width[0x5c] = 600;
        boundingBoxes[0x5c] = new Rectangle(222, -77, 274, 703);
        width[0x7c] = 600;
        boundingBoxes[0x7c] = new Rectangle(201, -250, 304, 1000);
        width[0x7b] = 600;
        boundingBoxes[0x7b] = new Rectangle(203, -102, 392, 718);
        width[0x7d] = 600;
        boundingBoxes[0x7d] = new Rectangle(114, -102, 392, 718);
        width[0x5b] = 600;
        boundingBoxes[0x5b] = new Rectangle(223, -102, 383, 718);
        width[0x5d] = 600;
        boundingBoxes[0x5d] = new Rectangle(103, -102, 383, 718);

        width[0xa6] = 600;
        boundingBoxes[0xa6] = new Rectangle(217, -175, 272, 850);
        width[0x95] = 600;
        boundingBoxes[0x95] = new Rectangle(196, 132, 327, 298);
        width[0x63] = 600;
        boundingBoxes[0x63] = new Rectangle(81, -15, 550, 474);

        width[0xe7] = 600;
        boundingBoxes[0xe7] = new Rectangle(81, -206, 550, 665);
        width[0xb8] = 600;
        boundingBoxes[0xb8] = new Rectangle(168, -206, 200, 206);
        width[0xa2] = 600;
        boundingBoxes[0xa2] = new Rectangle(121, -49, 484, 663);
        width[0x88] = 600;
        boundingBoxes[0x88] = new Rectangle(212, 483, 395, 174);
        width[0x3a] = 600;
        boundingBoxes[0x3a] = new Rectangle(205, -15, 275, 440);
        width[0x2c] = 600;
        boundingBoxes[0x2c] = new Rectangle(99, -111, 331, 285);
        width[0xa9] = 600;
        boundingBoxes[0xa9] = new Rectangle(53, -18, 614, 598);
        width[0xa4] = 600;
        boundingBoxes[0xa4] = new Rectangle(77, 49, 567, 468);
        width[0x64] = 600;
        boundingBoxes[0x64] = new Rectangle(60, -15, 585, 641);
        width[0x86] = 600;
        boundingBoxes[0x86] = new Rectangle(175, -70, 411, 650);
        width[0x87] = 600;
        boundingBoxes[0x87] = new Rectangle(121, -70, 466, 650);
        width[0xb0] = 600;
        boundingBoxes[0xb0] = new Rectangle(173, 243, 397, 373);
        width[0xa8] = 600;
        boundingBoxes[0xa8] = new Rectangle(246, 498, 349, 140);
        width[0xf7] = 600;
        boundingBoxes[0xf7] = new Rectangle(114, 16, 482, 484);
        width[0x24] = 600;
        boundingBoxes[0x24] = new Rectangle(87, -126, 543, 792);


        width[0x65] = 600;
        boundingBoxes[0x65] = new Rectangle(81, -15, 524, 469);
        width[0xe9] = 600;
        boundingBoxes[0xe9] = new Rectangle(81, -15, 528, 676);
        width[0xea] = 600;
        boundingBoxes[0xea] = new Rectangle(81, -15, 526, 672);
        width[0xeb] = 600;
        boundingBoxes[0xeb] = new Rectangle(81, -15, 524, 653);
        width[0xe8] = 600;
        boundingBoxes[0xe8] = new Rectangle(81, -15, 524, 676);
        width[0x38] = 600;
        boundingBoxes[0x38] = new Rectangle(115, -15, 489, 631);
        width[0x85] = 600;
        boundingBoxes[0x85] = new Rectangle(35, -15, 552, 131);
        width[0x97] = 600;
        boundingBoxes[0x97] = new Rectangle(33, 203, 644, 110);
        width[0x96] = 600;
        boundingBoxes[0x96] = new Rectangle(108, 203, 494, 110);
        width[0x3d] = 600;
        boundingBoxes[0x3d] = new Rectangle(96, 118, 518, 280);
        width[0xf0] = 600;
        boundingBoxes[0xf0] = new Rectangle(93, -27, 568, 653);
        width[0x21] = 600;
        boundingBoxes[0x21] = new Rectangle(215, -15, 280, 587);
        width[0xa1] = 600;
        boundingBoxes[0xa1] = new Rectangle(196, -146, 281, 595);
        width[0x66] = 600;
        boundingBoxes[0x66] = new Rectangle(83, 0, 594, 626);

        width[0x35] = 600;
        boundingBoxes[0x35] = new Rectangle(77, -15, 544, 616);

        width[0x83] = 600;
        boundingBoxes[0x83] = new Rectangle(-57, -131, 759, 747);
        width[0x34] = 600;
        boundingBoxes[0x34] = new Rectangle(81, 0, 478, 616);

        width[0x67] = 600;
        boundingBoxes[0x67] = new Rectangle(40, -146, 634, 600);
        width[0xdf] = 600;
        boundingBoxes[0xdf] = new Rectangle(22, -15, 607, 641);
        width[0x60] = 600;
        boundingBoxes[0x60] = new Rectangle(272, 508, 231, 153);
        width[0x3e] = 600;
        boundingBoxes[0x3e] = new Rectangle(97, 15, 492, 486);
        width[0xab] = 600;
        boundingBoxes[0xab] = new Rectangle(62, 70, 577, 376);
        width[0xbb] = 600;
        boundingBoxes[0xbb] = new Rectangle(71, 70, 576, 376);
        width[0x8b] = 600;
        boundingBoxes[0x8b] = new Rectangle(195, 70, 350, 376);
        width[0x9b] = 600;
        boundingBoxes[0x9b] = new Rectangle(165, 70, 349, 376);
        width[0x68] = 600;
        boundingBoxes[0x68] = new Rectangle(18, 0, 597, 626);

        width[0x2d] = 600;
        boundingBoxes[0x2d] = new Rectangle(143, 203, 424, 110);
        width[0x69] = 600;
        boundingBoxes[0x69] = new Rectangle(77, 0, 469, 658);
        width[0xed] = 600;
        boundingBoxes[0xed] = new Rectangle(77, 0, 532, 661);
        width[0xee] = 600;
        boundingBoxes[0xee] = new Rectangle(77, 0, 500, 657);
        width[0xef] = 600;
        boundingBoxes[0xef] = new Rectangle(77, 0, 484, 618);
        width[0xec] = 600;
        boundingBoxes[0xec] = new Rectangle(77, 0, 469, 661);
        width[0x6a] = 600;
        boundingBoxes[0x6a] = new Rectangle(36, -146, 544, 804);
        width[0x6b] = 600;
        boundingBoxes[0x6b] = new Rectangle(33, 0, 610, 626);
        width[0x6c] = 600;
        boundingBoxes[0x6c] = new Rectangle(77, 0, 469, 626);
        width[0x3c] = 600;
        boundingBoxes[0x3c] = new Rectangle(120, 15, 493, 486);
        width[0xac] = 600;
        boundingBoxes[0xac] = new Rectangle(135, 103, 482, 310);

        width[0x6d] = 600;
        boundingBoxes[0x6d] = new Rectangle(-22, 0, 671, 454);
        width[0xaf] = 600;
        boundingBoxes[0xaf] = new Rectangle(195, 505, 442, 80);

        width[0xb5] = 600;
        boundingBoxes[0xb5] = new Rectangle(49, -142, 543, 581);
        width[0xd7] = 600;
        boundingBoxes[0xd7] = new Rectangle(104, 39, 502, 439);
        width[0x6e] = 600;
        boundingBoxes[0x6e] = new Rectangle(18, 0, 597, 454);
        width[0x39] = 600;
        boundingBoxes[0x39] = new Rectangle(75, -15, 517, 631);
        width[0xf1] = 600;
        boundingBoxes[0xf1] = new Rectangle(18, 0, 625, 636);
        width[0x23] = 600;
        boundingBoxes[0x23] = new Rectangle(88, -45, 553, 696);
        width[0x6f] = 600;
        boundingBoxes[0x6f] = new Rectangle(71, -15, 551, 469);
        width[0xf3] = 600;
        boundingBoxes[0xf3] = new Rectangle(71, -15, 578, 676);
        width[0xf4] = 600;
        boundingBoxes[0xf4] = new Rectangle(71, -15, 551, 672);
        width[0xf6] = 600;
        boundingBoxes[0xf6] = new Rectangle(71, -15, 551, 653);
        width[0x9c] = 600;
        boundingBoxes[0x9c] = new Rectangle(18, -15, 644, 469);

        width[0xf2] = 600;
        boundingBoxes[0xf2] = new Rectangle(71, -15, 551, 676);
        width[0x31] = 600;
        boundingBoxes[0x31] = new Rectangle(93, 0, 469, 616);
        width[0xbd] = 600;
        boundingBoxes[0xbd] = new Rectangle(22, -60, 694, 721);
        width[0xbc] = 600;
        boundingBoxes[0xbc] = new Rectangle(13, -60, 694, 721);
        width[0xb9] = 600;
        boundingBoxes[0xb9] = new Rectangle(212, 230, 302, 386);
        width[0xaa] = 600;
        boundingBoxes[0xaa] = new Rectangle(188, 196, 338, 384);
        width[0xba] = 600;
        boundingBoxes[0xba] = new Rectangle(188, 196, 355, 384);
        width[0xf8] = 600;
        boundingBoxes[0xf8] = new Rectangle(54, -24, 584, 487);
        width[0xf5] = 600;
        boundingBoxes[0xf5] = new Rectangle(71, -15, 572, 651);
        width[0x70] = 600;
        boundingBoxes[0x70] = new Rectangle(-32, -142, 654, 596);
        width[0xb6] = 600;
        boundingBoxes[0xb6] = new Rectangle(61, -70, 639, 650);
        width[0x28] = 600;
        boundingBoxes[0x28] = new Rectangle(265, -102, 327, 718);
        width[0x29] = 600;
        boundingBoxes[0x29] = new Rectangle(117, -102, 327, 718);
        width[0x25] = 600;
        boundingBoxes[0x25] = new Rectangle(101, -15, 524, 631);
        width[0x2e] = 600;
        boundingBoxes[0x2e] = new Rectangle(206, -15, 221, 186);
        width[0xb7] = 600;
        boundingBoxes[0xb7] = new Rectangle(248, 165, 213, 186);
        width[0x89] = 600;
        boundingBoxes[0x89] = new Rectangle(-45, -15, 788, 631);
        width[0x2b] = 600;
        boundingBoxes[0x2b] = new Rectangle(114, 39, 482, 439);
        width[0xb1] = 600;
        boundingBoxes[0xb1] = new Rectangle(76, 24, 538, 491);
        width[0x71] = 600;
        boundingBoxes[0x71] = new Rectangle(60, -142, 625, 596);
        width[0x3f] = 600;
        boundingBoxes[0x3f] = new Rectangle(183, -14, 409, 594);
        width[0xbf] = 600;
        boundingBoxes[0xbf] = new Rectangle(100, -146, 409, 595);
        width[0x22] = 600;
        boundingBoxes[0x22] = new Rectangle(211, 277, 374, 285);
        width[0x84] = 600;
        boundingBoxes[0x84] = new Rectangle(34, -142, 526, 285);
        width[0x93] = 600;
        boundingBoxes[0x93] = new Rectangle(190, 277, 404, 285);
        width[0x94] = 600;
        boundingBoxes[0x94] = new Rectangle(119, 277, 526, 285);
        width[0x91] = 600;
        boundingBoxes[0x91] = new Rectangle(297, 277, 190, 285);
        width[0x92] = 600;
        boundingBoxes[0x92] = new Rectangle(229, 277, 314, 285);
        width[0x82] = 600;
        boundingBoxes[0x82] = new Rectangle(144, -142, 314, 285);
        width[0x27] = 600;
        boundingBoxes[0x27] = new Rectangle(303, 277, 190, 285);
        width[0x72] = 600;
        boundingBoxes[0x72] = new Rectangle(47, 0, 608, 454);
        width[0xae] = 600;
        boundingBoxes[0xae] = new Rectangle(53, -18, 614, 598);

        width[0x73] = 600;
        boundingBoxes[0x73] = new Rectangle(66, -17, 542, 476);
        width[0x9a] = 600;
        boundingBoxes[0x9a] = new Rectangle(66, -17, 567, 684);

        width[0xa7] = 600;
        boundingBoxes[0xa7] = new Rectangle(74, -70, 546, 650);
        width[0x3b] = 600;
        boundingBoxes[0x3b] = new Rectangle(99, -111, 382, 536);
        width[0x37] = 600;
        boundingBoxes[0x37] = new Rectangle(147, 0, 475, 601);
        width[0x36] = 600;
        boundingBoxes[0x36] = new Rectangle(135, -15, 517, 631);
        width[0x2f] = 600;
        boundingBoxes[0x2f] = new Rectangle(90, -77, 536, 703);
        width[0x20] = 600;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 600;
        boundingBoxes[0xa3] = new Rectangle(106, -28, 544, 639);
        width[0x74] = 600;
        boundingBoxes[0x74] = new Rectangle(118, -15, 449, 577);
        width[0xfe] = 600;
        boundingBoxes[0xfe] = new Rectangle(-32, -142, 654, 768);
        width[0x33] = 600;
        boundingBoxes[0x33] = new Rectangle(71, -15, 500, 631);
        width[0xbe] = 600;
        boundingBoxes[0xbe] = new Rectangle(8, -60, 691, 721);
        width[0xb3] = 600;
        boundingBoxes[0xb3] = new Rectangle(193, 222, 333, 394);
        width[0x98] = 600;
        boundingBoxes[0x98] = new Rectangle(199, 493, 444, 143);
        width[0x99] = 600;
        boundingBoxes[0x99] = new Rectangle(86, 230, 783, 332);
        width[0x32] = 600;
        boundingBoxes[0x32] = new Rectangle(61, 0, 533, 616);
        width[0xb2] = 600;
        boundingBoxes[0xb2] = new Rectangle(191, 230, 351, 386);
        width[0x75] = 600;
        boundingBoxes[0x75] = new Rectangle(70, -15, 522, 454);
        width[0xfa] = 600;
        boundingBoxes[0xfa] = new Rectangle(70, -15, 529, 676);
        width[0xfb] = 600;
        boundingBoxes[0xfb] = new Rectangle(70, -15, 527, 672);
        width[0xfc] = 600;
        boundingBoxes[0xfc] = new Rectangle(70, -15, 525, 653);
        width[0xf9] = 600;
        boundingBoxes[0xf9] = new Rectangle(70, -15, 522, 676);
        width[0x5f] = 600;
        boundingBoxes[0x5f] = new Rectangle(-27, -125, 612, 50);
        width[0x76] = 600;
        boundingBoxes[0x76] = new Rectangle(70, 0, 625, 439);
        width[0x77] = 600;
        boundingBoxes[0x77] = new Rectangle(53, 0, 659, 439);
        width[0x78] = 600;
        boundingBoxes[0x78] = new Rectangle(6, 0, 665, 439);
        width[0x79] = 600;
        boundingBoxes[0x79] = new Rectangle(-21, -142, 716, 581);
        width[0xfd] = 600;
        boundingBoxes[0xfd] = new Rectangle(-21, -142, 716, 803);
        width[0xff] = 600;
        boundingBoxes[0xff] = new Rectangle(-21, -142, 716, 780);
        width[0xa5] = 600;
        boundingBoxes[0xa5] = new Rectangle(98, 0, 612, 562);
        width[0x7a] = 600;
        boundingBoxes[0x7a] = new Rectangle(81, 0, 533, 439);
        width[0x9e] = 600;
        boundingBoxes[0x9e] = new Rectangle(81, 0, 562, 667);
        width[0x30] = 600;
        boundingBoxes[0x30] = new Rectangle(135, -15, 458, 631);

        familyNames = new java.util.HashSet();
        familyNames.add("Courier");

    }

    public CourierBoldOblique() {
        this(false);
    }

    public CourierBoldOblique(boolean enableKerning) {
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
