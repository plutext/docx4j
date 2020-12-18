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

public class HelveticaOblique extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Helvetica-Oblique";
    private static final String fullName = "Helvetica Oblique";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 718;
    private static final int xHeight = 523;
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

        width[0x41] = 667;
        boundingBoxes[0x41] = new Rectangle(14, 0, 640, 718);
        width[0xc6] = 1000;
        boundingBoxes[0xc6] = new Rectangle(8, 0, 1089, 718);
        width[0xc1] = 667;
        boundingBoxes[0xc1] = new Rectangle(14, 0, 669, 929);
        width[0xc2] = 667;
        boundingBoxes[0xc2] = new Rectangle(14, 0, 640, 929);
        width[0xc4] = 667;
        boundingBoxes[0xc4] = new Rectangle(14, 0, 640, 901);
        width[0xc0] = 667;
        boundingBoxes[0xc0] = new Rectangle(14, 0, 640, 929);
        width[0xc5] = 667;
        boundingBoxes[0xc5] = new Rectangle(14, 0, 640, 931);
        width[0xc3] = 667;
        boundingBoxes[0xc3] = new Rectangle(14, 0, 685, 917);
        width[0x42] = 667;
        boundingBoxes[0x42] = new Rectangle(74, 0, 638, 718);
        width[0x43] = 722;
        boundingBoxes[0x43] = new Rectangle(108, -19, 674, 756);
        width[0xc7] = 722;
        boundingBoxes[0xc7] = new Rectangle(108, -225, 674, 962);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(81, 0, 683, 718);
        width[0x45] = 667;
        boundingBoxes[0x45] = new Rectangle(86, 0, 676, 718);
        width[0xc9] = 667;
        boundingBoxes[0xc9] = new Rectangle(86, 0, 676, 929);
        width[0xca] = 667;
        boundingBoxes[0xca] = new Rectangle(86, 0, 676, 929);
        width[0xcb] = 667;
        boundingBoxes[0xcb] = new Rectangle(86, 0, 676, 901);
        width[0xc8] = 667;
        boundingBoxes[0xc8] = new Rectangle(86, 0, 676, 929);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(69, 0, 695, 718);
        width[0x80] = 556;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 611;
        boundingBoxes[0x46] = new Rectangle(86, 0, 650, 718);
        width[0x47] = 778;
        boundingBoxes[0x47] = new Rectangle(111, -19, 688, 756);
        width[0x48] = 722;
        boundingBoxes[0x48] = new Rectangle(77, 0, 722, 718);
        width[0x49] = 278;
        boundingBoxes[0x49] = new Rectangle(91, 0, 250, 718);
        width[0xcd] = 278;
        boundingBoxes[0xcd] = new Rectangle(91, 0, 398, 929);
        width[0xce] = 278;
        boundingBoxes[0xce] = new Rectangle(91, 0, 361, 929);
        width[0xcf] = 278;
        boundingBoxes[0xcf] = new Rectangle(91, 0, 367, 901);
        width[0xcc] = 278;
        boundingBoxes[0xcc] = new Rectangle(91, 0, 260, 929);
        width[0x4a] = 500;
        boundingBoxes[0x4a] = new Rectangle(47, -19, 534, 737);
        width[0x4b] = 667;
        boundingBoxes[0x4b] = new Rectangle(76, 0, 732, 718);
        width[0x4c] = 556;
        boundingBoxes[0x4c] = new Rectangle(76, 0, 479, 718);

        width[0x4d] = 833;
        boundingBoxes[0x4d] = new Rectangle(73, 0, 841, 718);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(76, 0, 723, 718);
        width[0xd1] = 722;
        boundingBoxes[0xd1] = new Rectangle(76, 0, 723, 917);
        width[0x4f] = 778;
        boundingBoxes[0x4f] = new Rectangle(105, -19, 721, 756);
        width[0x8c] = 1000;
        boundingBoxes[0x8c] = new Rectangle(98, -19, 1018, 756);
        width[0xd3] = 778;
        boundingBoxes[0xd3] = new Rectangle(105, -19, 721, 948);
        width[0xd4] = 778;
        boundingBoxes[0xd4] = new Rectangle(105, -19, 721, 948);
        width[0xd6] = 778;
        boundingBoxes[0xd6] = new Rectangle(105, -19, 721, 920);
        width[0xd2] = 778;
        boundingBoxes[0xd2] = new Rectangle(105, -19, 721, 948);
        width[0xd8] = 778;
        boundingBoxes[0xd8] = new Rectangle(43, -19, 847, 756);
        width[0xd5] = 778;
        boundingBoxes[0xd5] = new Rectangle(105, -19, 721, 936);
        width[0x50] = 667;
        boundingBoxes[0x50] = new Rectangle(86, 0, 651, 718);
        width[0x51] = 778;
        boundingBoxes[0x51] = new Rectangle(105, -56, 721, 793);
        width[0x52] = 722;
        boundingBoxes[0x52] = new Rectangle(88, 0, 685, 718);
        width[0x53] = 667;
        boundingBoxes[0x53] = new Rectangle(90, -19, 623, 756);
        width[0x8a] = 667;
        boundingBoxes[0x8a] = new Rectangle(90, -19, 623, 948);
        width[0x54] = 611;
        boundingBoxes[0x54] = new Rectangle(148, 0, 602, 718);
        width[0xde] = 667;
        boundingBoxes[0xde] = new Rectangle(86, 0, 626, 718);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(123, -19, 674, 737);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(123, -19, 674, 948);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(123, -19, 674, 948);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(123, -19, 674, 920);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(123, -19, 674, 948);
        width[0x56] = 667;
        boundingBoxes[0x56] = new Rectangle(173, 0, 627, 718);
        width[0x57] = 944;
        boundingBoxes[0x57] = new Rectangle(169, 0, 912, 718);
        width[0x58] = 667;
        boundingBoxes[0x58] = new Rectangle(19, 0, 771, 718);
        width[0x59] = 667;
        boundingBoxes[0x59] = new Rectangle(167, 0, 639, 718);
        width[0xdd] = 667;
        boundingBoxes[0xdd] = new Rectangle(167, 0, 639, 929);
        width[0x9f] = 667;
        boundingBoxes[0x9f] = new Rectangle(167, 0, 639, 901);
        width[0x5a] = 611;
        boundingBoxes[0x5a] = new Rectangle(23, 0, 718, 718);
        width[0x8e] = 611;
        boundingBoxes[0x8e] = new Rectangle(23, 0, 718, 929);
        width[0x61] = 556;
        boundingBoxes[0x61] = new Rectangle(61, -15, 498, 553);
        width[0xe1] = 556;
        boundingBoxes[0xe1] = new Rectangle(61, -15, 526, 749);
        width[0xe2] = 556;
        boundingBoxes[0xe2] = new Rectangle(61, -15, 498, 749);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(248, 593, 227, 141);
        width[0xe4] = 556;
        boundingBoxes[0xe4] = new Rectangle(61, -15, 498, 721);
        width[0xe6] = 889;
        boundingBoxes[0xe6] = new Rectangle(61, -15, 848, 553);
        width[0xe0] = 556;
        boundingBoxes[0xe0] = new Rectangle(61, -15, 498, 749);
        width[0x26] = 667;
        boundingBoxes[0x26] = new Rectangle(77, -15, 570, 733);
        width[0xe5] = 556;
        boundingBoxes[0xe5] = new Rectangle(61, -15, 498, 771);
        width[0x5e] = 469;
        boundingBoxes[0x5e] = new Rectangle(42, 264, 497, 424);
        width[0x7e] = 584;
        boundingBoxes[0x7e] = new Rectangle(111, 180, 469, 146);
        width[0x2a] = 389;
        boundingBoxes[0x2a] = new Rectangle(165, 431, 310, 287);
        width[0x40] = 1015;
        boundingBoxes[0x40] = new Rectangle(215, -19, 750, 756);
        width[0xe3] = 556;
        boundingBoxes[0xe3] = new Rectangle(61, -15, 531, 737);
        width[0x62] = 556;
        boundingBoxes[0x62] = new Rectangle(58, -15, 526, 733);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(140, -19, 151, 756);
        width[0x7c] = 260;
        boundingBoxes[0x7c] = new Rectangle(46, -225, 286, 1000);
        width[0x7b] = 334;
        boundingBoxes[0x7b] = new Rectangle(92, -196, 353, 918);
        width[0x7d] = 334;
        boundingBoxes[0x7d] = new Rectangle(0, -196, 354, 918);
        width[0x5b] = 278;
        boundingBoxes[0x5b] = new Rectangle(21, -196, 382, 918);
        width[0x5d] = 278;
        boundingBoxes[0x5d] = new Rectangle(-14, -196, 382, 918);

        width[0xa6] = 260;
        boundingBoxes[0xa6] = new Rectangle(62, -150, 254, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(91, 202, 322, 315);
        width[0x63] = 500;
        boundingBoxes[0x63] = new Rectangle(74, -15, 479, 553);

        width[0xe7] = 500;
        boundingBoxes[0xe7] = new Rectangle(74, -225, 479, 763);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(2, -225, 230, 225);
        width[0xa2] = 556;
        boundingBoxes[0xa2] = new Rectangle(95, -115, 489, 738);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(147, 593, 291, 141);
        width[0x3a] = 278;
        boundingBoxes[0x3a] = new Rectangle(87, 0, 214, 516);
        width[0x2c] = 278;
        boundingBoxes[0x2c] = new Rectangle(56, -147, 158, 253);
        width[0xa9] = 737;
        boundingBoxes[0xa9] = new Rectangle(54, -19, 783, 756);
        width[0xa4] = 556;
        boundingBoxes[0xa4] = new Rectangle(60, 99, 586, 504);
        width[0x64] = 556;
        boundingBoxes[0x64] = new Rectangle(84, -15, 568, 733);
        width[0x86] = 556;
        boundingBoxes[0x86] = new Rectangle(135, -159, 487, 877);
        width[0x87] = 556;
        boundingBoxes[0x87] = new Rectangle(52, -159, 571, 877);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(169, 411, 299, 292);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(168, 604, 275, 102);
        width[0xf7] = 584;
        boundingBoxes[0xf7] = new Rectangle(85, -19, 521, 543);
        width[0x24] = 556;
        boundingBoxes[0x24] = new Rectangle(69, -115, 548, 890);


        width[0x65] = 556;
        boundingBoxes[0x65] = new Rectangle(84, -15, 494, 553);
        width[0xe9] = 556;
        boundingBoxes[0xe9] = new Rectangle(84, -15, 503, 749);
        width[0xea] = 556;
        boundingBoxes[0xea] = new Rectangle(84, -15, 494, 749);
        width[0xeb] = 556;
        boundingBoxes[0xeb] = new Rectangle(84, -15, 494, 721);
        width[0xe8] = 556;
        boundingBoxes[0xe8] = new Rectangle(84, -15, 494, 749);
        width[0x38] = 556;
        boundingBoxes[0x38] = new Rectangle(74, -19, 533, 722);
        width[0x85] = 1000;
        boundingBoxes[0x85] = new Rectangle(115, 0, 793, 106);
        width[0x97] = 1000;
        boundingBoxes[0x97] = new Rectangle(51, 240, 1016, 73);
        width[0x96] = 556;
        boundingBoxes[0x96] = new Rectangle(51, 240, 572, 73);
        width[0x3d] = 584;
        boundingBoxes[0x3d] = new Rectangle(63, 115, 565, 275);
        width[0xf0] = 556;
        boundingBoxes[0xf0] = new Rectangle(81, -15, 536, 752);
        width[0x21] = 278;
        boundingBoxes[0x21] = new Rectangle(90, 0, 250, 718);
        width[0xa1] = 333;
        boundingBoxes[0xa1] = new Rectangle(77, -195, 249, 718);
        width[0x66] = 278;
        boundingBoxes[0x66] = new Rectangle(86, 0, 330, 728);

        width[0x35] = 556;
        boundingBoxes[0x35] = new Rectangle(68, -19, 553, 707);

        width[0x83] = 556;
        boundingBoxes[0x83] = new Rectangle(-52, -207, 706, 944);
        width[0x34] = 556;
        boundingBoxes[0x34] = new Rectangle(61, 0, 515, 703);

        width[0x67] = 556;
        boundingBoxes[0x67] = new Rectangle(42, -220, 568, 758);
        width[0xdf] = 611;
        boundingBoxes[0xdf] = new Rectangle(67, -15, 591, 743);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(170, 593, 167, 141);
        width[0x3e] = 584;
        boundingBoxes[0x3e] = new Rectangle(50, 11, 547, 484);
        width[0xab] = 556;
        boundingBoxes[0xab] = new Rectangle(146, 108, 408, 338);
        width[0xbb] = 556;
        boundingBoxes[0xbb] = new Rectangle(120, 108, 408, 338);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(137, 108, 203, 338);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(111, 108, 203, 338);
        width[0x68] = 556;
        boundingBoxes[0x68] = new Rectangle(65, 0, 508, 718);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(93, 232, 264, 90);
        width[0x69] = 222;
        boundingBoxes[0x69] = new Rectangle(67, 0, 241, 718);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(95, 0, 353, 734);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(95, 0, 316, 734);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(95, 0, 321, 706);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(95, 0, 215, 734);
        width[0x6a] = 222;
        boundingBoxes[0x6a] = new Rectangle(-60, -210, 368, 928);
        width[0x6b] = 500;
        boundingBoxes[0x6b] = new Rectangle(67, 0, 533, 718);
        width[0x6c] = 222;
        boundingBoxes[0x6c] = new Rectangle(67, 0, 241, 718);
        width[0x3c] = 584;
        boundingBoxes[0x3c] = new Rectangle(94, 11, 547, 484);
        width[0xac] = 584;
        boundingBoxes[0xac] = new Rectangle(106, 108, 522, 282);

        width[0x6d] = 833;
        boundingBoxes[0x6d] = new Rectangle(65, 0, 787, 538);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(143, 627, 325, 57);

        width[0xb5] = 556;
        boundingBoxes[0xb5] = new Rectangle(24, -207, 576, 730);
        width[0xd7] = 584;
        boundingBoxes[0xd7] = new Rectangle(50, 0, 592, 506);
        width[0x6e] = 556;
        boundingBoxes[0x6e] = new Rectangle(65, 0, 508, 538);
        width[0x39] = 556;
        boundingBoxes[0x39] = new Rectangle(82, -19, 527, 722);
        width[0xf1] = 556;
        boundingBoxes[0xf1] = new Rectangle(65, 0, 527, 722);
        width[0x23] = 556;
        boundingBoxes[0x23] = new Rectangle(73, 0, 558, 688);
        width[0x6f] = 556;
        boundingBoxes[0x6f] = new Rectangle(83, -14, 502, 552);
        width[0xf3] = 556;
        boundingBoxes[0xf3] = new Rectangle(83, -14, 504, 748);
        width[0xf4] = 556;
        boundingBoxes[0xf4] = new Rectangle(83, -14, 502, 748);
        width[0xf6] = 556;
        boundingBoxes[0xf6] = new Rectangle(83, -14, 502, 720);
        width[0x9c] = 944;
        boundingBoxes[0x9c] = new Rectangle(83, -15, 881, 553);

        width[0xf2] = 556;
        boundingBoxes[0xf2] = new Rectangle(83, -14, 502, 748);
        width[0x31] = 556;
        boundingBoxes[0x31] = new Rectangle(207, 0, 301, 703);
        width[0xbd] = 834;
        boundingBoxes[0xbd] = new Rectangle(114, -19, 725, 722);
        width[0xbc] = 834;
        boundingBoxes[0xbc] = new Rectangle(150, -19, 652, 722);
        width[0xb9] = 333;
        boundingBoxes[0xb9] = new Rectangle(166, 281, 205, 422);
        width[0xaa] = 370;
        boundingBoxes[0xaa] = new Rectangle(127, 405, 322, 332);
        width[0xba] = 365;
        boundingBoxes[0xba] = new Rectangle(141, 405, 327, 332);
        width[0xf8] = 611;
        boundingBoxes[0xf8] = new Rectangle(29, -22, 618, 567);
        width[0xf5] = 556;
        boundingBoxes[0xf5] = new Rectangle(83, -14, 519, 736);
        width[0x70] = 556;
        boundingBoxes[0x70] = new Rectangle(14, -207, 570, 745);
        width[0xb6] = 537;
        boundingBoxes[0xb6] = new Rectangle(126, -173, 524, 891);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(108, -207, 346, 940);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(-9, -207, 346, 940);
        width[0x25] = 889;
        boundingBoxes[0x25] = new Rectangle(147, -19, 742, 722);
        width[0x2e] = 278;
        boundingBoxes[0x2e] = new Rectangle(87, 0, 127, 106);
        width[0xb7] = 278;
        boundingBoxes[0xb7] = new Rectangle(129, 190, 128, 125);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(88, -19, 941, 722);
        width[0x2b] = 584;
        boundingBoxes[0x2b] = new Rectangle(85, 0, 521, 505);
        width[0xb1] = 584;
        boundingBoxes[0xb1] = new Rectangle(39, 0, 579, 506);
        width[0x71] = 556;
        boundingBoxes[0x71] = new Rectangle(84, -207, 521, 745);
        width[0x3f] = 556;
        boundingBoxes[0x3f] = new Rectangle(161, 0, 449, 727);
        width[0xbf] = 611;
        boundingBoxes[0xbf] = new Rectangle(85, -201, 449, 726);
        width[0x22] = 355;
        boundingBoxes[0x22] = new Rectangle(168, 463, 270, 255);
        width[0x84] = 333;
        boundingBoxes[0x84] = new Rectangle(-6, -149, 324, 255);
        width[0x93] = 333;
        boundingBoxes[0x93] = new Rectangle(138, 470, 323, 255);
        width[0x94] = 333;
        boundingBoxes[0x94] = new Rectangle(124, 463, 324, 255);
        width[0x91] = 222;
        boundingBoxes[0x91] = new Rectangle(165, 470, 158, 255);
        width[0x92] = 222;
        boundingBoxes[0x92] = new Rectangle(151, 463, 159, 255);
        width[0x82] = 222;
        boundingBoxes[0x82] = new Rectangle(21, -149, 159, 255);
        width[0x27] = 191;
        boundingBoxes[0x27] = new Rectangle(157, 463, 128, 255);
        width[0x72] = 333;
        boundingBoxes[0x72] = new Rectangle(77, 0, 369, 538);
        width[0xae] = 737;
        boundingBoxes[0xae] = new Rectangle(54, -19, 783, 756);

        width[0x73] = 500;
        boundingBoxes[0x73] = new Rectangle(63, -15, 466, 553);
        width[0x9a] = 500;
        boundingBoxes[0x9a] = new Rectangle(63, -15, 489, 749);
        width[0xa7] = 556;
        boundingBoxes[0xa7] = new Rectangle(76, -191, 508, 928);
        width[0x3b] = 278;
        boundingBoxes[0x3b] = new Rectangle(56, -147, 245, 663);
        width[0x37] = 556;
        boundingBoxes[0x37] = new Rectangle(137, 0, 532, 688);
        width[0x36] = 556;
        boundingBoxes[0x36] = new Rectangle(91, -19, 524, 722);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-21, -19, 473, 756);
        width[0x20] = 278;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 556;
        boundingBoxes[0xa3] = new Rectangle(49, -16, 585, 734);
        width[0x74] = 278;
        boundingBoxes[0x74] = new Rectangle(102, -7, 266, 676);
        width[0xfe] = 556;
        boundingBoxes[0xfe] = new Rectangle(14, -207, 570, 925);
        width[0x33] = 556;
        boundingBoxes[0x33] = new Rectangle(75, -19, 535, 722);
        width[0xbe] = 834;
        boundingBoxes[0xbe] = new Rectangle(130, -19, 731, 722);
        width[0xb3] = 333;
        boundingBoxes[0xb3] = new Rectangle(90, 270, 346, 433);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(125, 606, 365, 116);
        width[0x99] = 1000;
        boundingBoxes[0x99] = new Rectangle(186, 306, 870, 412);
        width[0x32] = 556;
        boundingBoxes[0x32] = new Rectangle(26, 0, 591, 703);
        width[0xb2] = 333;
        boundingBoxes[0xb2] = new Rectangle(64, 281, 385, 422);
        width[0x75] = 556;
        boundingBoxes[0x75] = new Rectangle(94, -15, 506, 538);
        width[0xfa] = 556;
        boundingBoxes[0xfa] = new Rectangle(94, -15, 506, 749);
        width[0xfb] = 556;
        boundingBoxes[0xfb] = new Rectangle(94, -15, 506, 749);
        width[0xfc] = 556;
        boundingBoxes[0xfc] = new Rectangle(94, -15, 506, 721);
        width[0xf9] = 556;
        boundingBoxes[0xf9] = new Rectangle(94, -15, 506, 749);
        width[0x5f] = 556;
        boundingBoxes[0x5f] = new Rectangle(-27, -125, 567, 50);
        width[0x76] = 500;
        boundingBoxes[0x76] = new Rectangle(119, 0, 484, 523);
        width[0x77] = 722;
        boundingBoxes[0x77] = new Rectangle(125, 0, 695, 523);
        width[0x78] = 500;
        boundingBoxes[0x78] = new Rectangle(11, 0, 583, 523);
        width[0x79] = 500;
        boundingBoxes[0x79] = new Rectangle(15, -214, 585, 737);
        width[0xfd] = 500;
        boundingBoxes[0xfd] = new Rectangle(15, -214, 585, 948);
        width[0xff] = 500;
        boundingBoxes[0xff] = new Rectangle(15, -214, 585, 920);
        width[0xa5] = 556;
        boundingBoxes[0xa5] = new Rectangle(81, 0, 618, 688);
        width[0x7a] = 500;
        boundingBoxes[0x7a] = new Rectangle(31, 0, 540, 523);
        width[0x9e] = 500;
        boundingBoxes[0x9e] = new Rectangle(31, 0, 540, 734);
        width[0x30] = 556;
        boundingBoxes[0x30] = new Rectangle(93, -19, 515, 722);

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
        pairs.put(second, -20);

        second = 101;
        pairs.put(second, -20);

        first = 79;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -20);

        second = 87;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -70);

        second = 84;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, -40);

        second = 86;
        pairs.put(second, -50);

        second = 88;
        pairs.put(second, -60);

        second = 44;
        pairs.put(second, -40);

        first = 104;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, -30);

        first = 87;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -30);

        second = 45;
        pairs.put(second, -40);

        second = 79;
        pairs.put(second, -20);

        second = 97;
        pairs.put(second, -40);

        second = 65;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -30);

        second = 121;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -80);

        second = 101;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -80);

        first = 99;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -15);

        first = 112;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, -30);

        second = 46;
        pairs.put(second, -35);

        second = 44;
        pairs.put(second, -35);

        first = 80;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -50);

        second = 97;
        pairs.put(second, -40);

        second = 65;
        pairs.put(second, -120);

        second = 46;
        pairs.put(second, -180);

        second = 101;
        pairs.put(second, -50);

        second = 44;
        pairs.put(second, -180);

        first = 86;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -80);

        second = 45;
        pairs.put(second, -80);

        second = 79;
        pairs.put(second, -40);

        second = 58;
        pairs.put(second, -40);

        second = 97;
        pairs.put(second, -70);

        second = 65;
        pairs.put(second, -80);

        second = 117;
        pairs.put(second, -70);

        second = 46;
        pairs.put(second, -125);

        second = 71;
        pairs.put(second, -40);

        second = 101;
        pairs.put(second, -80);

        second = 59;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, -125);

        first = 118;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -25);

        second = 97;
        pairs.put(second, -25);

        second = 46;
        pairs.put(second, -80);

        second = 101;
        pairs.put(second, -25);

        second = 44;
        pairs.put(second, -80);

        first = 59;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, -50);

        first = 32;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 87;
        pairs.put(second, -40);

        second = 147;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -90);

        second = 84;
        pairs.put(second, -50);

        second = 145;
        pairs.put(second, -60);

        second = 86;
        pairs.put(second, -50);

        first = 97;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, -30);

        second = 118;
        pairs.put(second, -20);

        first = 65;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -30);

        second = 119;
        pairs.put(second, -40);

        second = 87;
        pairs.put(second, -50);

        second = 67;
        pairs.put(second, -30);

        second = 81;
        pairs.put(second, -30);

        second = 71;
        pairs.put(second, -30);

        second = 86;
        pairs.put(second, -70);

        second = 118;
        pairs.put(second, -40);

        second = 85;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -100);

        second = 84;
        pairs.put(second, -120);

        second = 121;
        pairs.put(second, -40);

        first = 70;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -30);

        second = 114;
        pairs.put(second, -45);

        second = 97;
        pairs.put(second, -50);

        second = 65;
        pairs.put(second, -80);

        second = 46;
        pairs.put(second, -150);

        second = 101;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -150);

        first = 85;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, -40);

        first = 115;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -30);

        second = 46;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, -15);

        first = 122;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -15);

        second = 101;
        pairs.put(second, -15);

        first = 83;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 46;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -20);

        first = 111;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -30);

        second = 46;
        pairs.put(second, -40);

        second = 120;
        pairs.put(second, -30);

        second = 118;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, -40);

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
        pairs.put(second, -90);

        second = 46;
        pairs.put(second, -70);

        second = 86;
        pairs.put(second, -70);

        second = 44;
        pairs.put(second, -70);

        first = 146;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 100;
        pairs.put(second, -50);

        second = 32;
        pairs.put(second, -70);

        second = 146;
        pairs.put(second, -57);

        second = 114;
        pairs.put(second, -50);

        second = 115;
        pairs.put(second, -50);

        first = 82;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -20);

        second = 87;
        pairs.put(second, -30);

        second = 85;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -50);

        second = 84;
        pairs.put(second, -30);

        second = 86;
        pairs.put(second, -50);

        first = 75;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -40);

        second = 79;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -30);

        second = 121;
        pairs.put(second, -50);

        second = 101;
        pairs.put(second, -40);

        first = 119;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -10);

        second = 97;
        pairs.put(second, -15);

        second = 46;
        pairs.put(second, -60);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -60);

        first = 58;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, -50);

        first = 114;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, 15);

        second = 58;
        pairs.put(second, 30);

        second = 112;
        pairs.put(second, 30);

        second = 108;
        pairs.put(second, 15);

        second = 118;
        pairs.put(second, 30);

        second = 44;
        pairs.put(second, -50);

        second = 59;
        pairs.put(second, 30);

        second = 105;
        pairs.put(second, 15);

        second = 109;
        pairs.put(second, 25);

        second = 97;
        pairs.put(second, -10);

        second = 117;
        pairs.put(second, 15);

        second = 116;
        pairs.put(second, 40);

        second = 121;
        pairs.put(second, 30);

        second = 46;
        pairs.put(second, -50);

        second = 110;
        pairs.put(second, 25);

        first = 67;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 46;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -30);

        first = 145;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 145;
        pairs.put(second, -57);

        first = 103;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 114;
        pairs.put(second, -10);

        first = 66;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 85;
        pairs.put(second, -10);

        second = 46;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -20);

        first = 81;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 85;
        pairs.put(second, -10);

        first = 76;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -140);

        second = 146;
        pairs.put(second, -160);

        second = 87;
        pairs.put(second, -70);

        second = 89;
        pairs.put(second, -140);

        second = 121;
        pairs.put(second, -30);

        second = 84;
        pairs.put(second, -110);

        second = 86;
        pairs.put(second, -110);

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

        second = 46;
        pairs.put(second, -40);

        second = 108;
        pairs.put(second, -20);

        second = 98;
        pairs.put(second, -10);

        second = 118;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -40);

        first = 44;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -100);

        second = 146;
        pairs.put(second, -100);

        first = 148;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, -40);

        first = 109;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, -15);

        first = 248;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, -55);

        second = 104;
        pairs.put(second, -55);

        second = 99;
        pairs.put(second, -55);

        second = 112;
        pairs.put(second, -55);

        second = 113;
        pairs.put(second, -55);

        second = 118;
        pairs.put(second, -70);

        second = 105;
        pairs.put(second, -55);

        second = 97;
        pairs.put(second, -55);

        second = 117;
        pairs.put(second, -55);

        second = 116;
        pairs.put(second, -55);

        second = 106;
        pairs.put(second, -55);

        second = 115;
        pairs.put(second, -55);

        second = 122;
        pairs.put(second, -55);

        second = 100;
        pairs.put(second, -55);

        second = 111;
        pairs.put(second, -55);

        second = 119;
        pairs.put(second, -70);

        second = 114;
        pairs.put(second, -55);

        second = 103;
        pairs.put(second, -55);

        second = 108;
        pairs.put(second, -55);

        second = 98;
        pairs.put(second, -55);

        second = 44;
        pairs.put(second, -95);

        second = 109;
        pairs.put(second, -55);

        second = 102;
        pairs.put(second, -55);

        second = 121;
        pairs.put(second, -70);

        second = 46;
        pairs.put(second, -95);

        second = 110;
        pairs.put(second, -55);

        second = 120;
        pairs.put(second, -85);

        second = 101;
        pairs.put(second, -55);

        first = 102;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 60);

        second = 111;
        pairs.put(second, -30);

        second = 146;
        pairs.put(second, 50);

        second = 97;
        pairs.put(second, -30);

        second = 46;
        pairs.put(second, -30);

        second = 101;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -30);

        first = 74;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 97;
        pairs.put(second, -20);

        second = 65;
        pairs.put(second, -20);

        second = 117;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -30);

        first = 89;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -140);

        second = 45;
        pairs.put(second, -140);

        second = 105;
        pairs.put(second, -20);

        second = 79;
        pairs.put(second, -85);

        second = 58;
        pairs.put(second, -60);

        second = 97;
        pairs.put(second, -140);

        second = 65;
        pairs.put(second, -110);

        second = 117;
        pairs.put(second, -110);

        second = 46;
        pairs.put(second, -140);

        second = 101;
        pairs.put(second, -140);

        second = 59;
        pairs.put(second, -60);

        second = 44;
        pairs.put(second, -140);

        first = 121;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -20);

        second = 97;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -100);

        second = 101;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -100);

        first = 84;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -120);

        second = 79;
        pairs.put(second, -40);

        second = 58;
        pairs.put(second, -20);

        second = 119;
        pairs.put(second, -120);

        second = 114;
        pairs.put(second, -120);

        second = 44;
        pairs.put(second, -120);

        second = 59;
        pairs.put(second, -20);

        second = 45;
        pairs.put(second, -140);

        second = 65;
        pairs.put(second, -120);

        second = 97;
        pairs.put(second, -120);

        second = 117;
        pairs.put(second, -120);

        second = 121;
        pairs.put(second, -120);

        second = 46;
        pairs.put(second, -120);

        second = 101;
        pairs.put(second, -120);

        first = 46;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -100);

        second = 32;
        pairs.put(second, -60);

        second = 146;
        pairs.put(second, -100);

        first = 110;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, -15);

        second = 118;
        pairs.put(second, -20);

        first = 120;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 101;
        pairs.put(second, -30);

        first = 101;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, -20);

        second = 46;
        pairs.put(second, -15);

        second = 120;
        pairs.put(second, -30);

        second = 118;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -15);

    }

    public HelveticaOblique() {
        this(false);
    }

    public HelveticaOblique(boolean enableKerning) {
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
