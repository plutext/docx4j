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

public class TimesBoldItalic extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Times-BoldItalic";
    private static final String fullName = "Times Bold Italic";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 669;
    private static final int xHeight = 462;
    private static final int ascender = 699;
    private static final int descender = -205;
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
        boundingBoxes[0x41] = new Rectangle(-67, 0, 660, 683);
        width[0xc6] = 944;
        boundingBoxes[0xc6] = new Rectangle(-64, 0, 982, 669);
        width[0xc1] = 667;
        boundingBoxes[0xc1] = new Rectangle(-67, 0, 660, 904);
        width[0xc2] = 667;
        boundingBoxes[0xc2] = new Rectangle(-67, 0, 660, 897);
        width[0xc4] = 667;
        boundingBoxes[0xc4] = new Rectangle(-67, 0, 660, 862);
        width[0xc0] = 667;
        boundingBoxes[0xc0] = new Rectangle(-67, 0, 660, 904);
        width[0xc5] = 667;
        boundingBoxes[0xc5] = new Rectangle(-67, 0, 660, 921);
        width[0xc3] = 667;
        boundingBoxes[0xc3] = new Rectangle(-67, 0, 660, 862);
        width[0x42] = 667;
        boundingBoxes[0x42] = new Rectangle(-24, 0, 648, 669);
        width[0x43] = 667;
        boundingBoxes[0x43] = new Rectangle(32, -18, 645, 703);
        width[0xc7] = 667;
        boundingBoxes[0xc7] = new Rectangle(32, -218, 645, 903);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(-46, 0, 731, 669);
        width[0x45] = 667;
        boundingBoxes[0x45] = new Rectangle(-27, 0, 680, 669);
        width[0xc9] = 667;
        boundingBoxes[0xc9] = new Rectangle(-27, 0, 680, 904);
        width[0xca] = 667;
        boundingBoxes[0xca] = new Rectangle(-27, 0, 680, 897);
        width[0xcb] = 667;
        boundingBoxes[0xcb] = new Rectangle(-27, 0, 680, 862);
        width[0xc8] = 667;
        boundingBoxes[0xc8] = new Rectangle(-27, 0, 680, 904);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(-31, 0, 731, 669);
        width[0x80] = 500;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 667;
        boundingBoxes[0x46] = new Rectangle(-13, 0, 673, 669);
        width[0x47] = 722;
        boundingBoxes[0x47] = new Rectangle(21, -18, 685, 703);
        width[0x48] = 778;
        boundingBoxes[0x48] = new Rectangle(-24, 0, 823, 669);
        width[0x49] = 389;
        boundingBoxes[0x49] = new Rectangle(-32, 0, 438, 669);
        width[0xcd] = 389;
        boundingBoxes[0xcd] = new Rectangle(-32, 0, 464, 904);
        width[0xce] = 389;
        boundingBoxes[0xce] = new Rectangle(-32, 0, 482, 897);
        width[0xcf] = 389;
        boundingBoxes[0xcf] = new Rectangle(-32, 0, 482, 862);
        width[0xcc] = 389;
        boundingBoxes[0xcc] = new Rectangle(-32, 0, 438, 904);
        width[0x4a] = 500;
        boundingBoxes[0x4a] = new Rectangle(-46, -99, 570, 768);
        width[0x4b] = 667;
        boundingBoxes[0x4b] = new Rectangle(-21, 0, 723, 669);
        width[0x4c] = 611;
        boundingBoxes[0x4c] = new Rectangle(-22, 0, 612, 669);

        width[0x4d] = 889;
        boundingBoxes[0x4d] = new Rectangle(-29, -12, 946, 681);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(-27, -15, 775, 684);
        width[0xd1] = 722;
        boundingBoxes[0xd1] = new Rectangle(-27, -15, 775, 877);
        width[0x4f] = 722;
        boundingBoxes[0x4f] = new Rectangle(27, -18, 664, 703);
        width[0x8c] = 944;
        boundingBoxes[0x8c] = new Rectangle(23, -8, 923, 685);
        width[0xd3] = 722;
        boundingBoxes[0xd3] = new Rectangle(27, -18, 664, 922);
        width[0xd4] = 722;
        boundingBoxes[0xd4] = new Rectangle(27, -18, 664, 915);
        width[0xd6] = 722;
        boundingBoxes[0xd6] = new Rectangle(27, -18, 664, 880);
        width[0xd2] = 722;
        boundingBoxes[0xd2] = new Rectangle(27, -18, 664, 922);
        width[0xd8] = 722;
        boundingBoxes[0xd8] = new Rectangle(27, -125, 664, 889);
        width[0xd5] = 722;
        boundingBoxes[0xd5] = new Rectangle(27, -18, 664, 880);
        width[0x50] = 611;
        boundingBoxes[0x50] = new Rectangle(-27, 0, 640, 669);
        width[0x51] = 722;
        boundingBoxes[0x51] = new Rectangle(27, -208, 664, 893);
        width[0x52] = 667;
        boundingBoxes[0x52] = new Rectangle(-29, 0, 652, 669);
        width[0x53] = 556;
        boundingBoxes[0x53] = new Rectangle(2, -18, 524, 703);
        width[0x8a] = 556;
        boundingBoxes[0x8a] = new Rectangle(2, -18, 551, 915);
        width[0x54] = 611;
        boundingBoxes[0x54] = new Rectangle(50, 0, 600, 669);
        width[0xde] = 611;
        boundingBoxes[0xde] = new Rectangle(-27, 0, 600, 669);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(67, -18, 677, 687);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(67, -18, 677, 922);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(67, -18, 677, 915);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(67, -18, 677, 880);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(67, -18, 677, 922);
        width[0x56] = 667;
        boundingBoxes[0x56] = new Rectangle(65, -18, 650, 687);
        width[0x57] = 889;
        boundingBoxes[0x57] = new Rectangle(65, -18, 875, 687);
        width[0x58] = 667;
        boundingBoxes[0x58] = new Rectangle(-24, 0, 718, 669);
        width[0x59] = 611;
        boundingBoxes[0x59] = new Rectangle(73, 0, 586, 669);
        width[0xdd] = 611;
        boundingBoxes[0xdd] = new Rectangle(73, 0, 586, 904);
        width[0x9f] = 611;
        boundingBoxes[0x9f] = new Rectangle(73, 0, 586, 862);
        width[0x5a] = 611;
        boundingBoxes[0x5a] = new Rectangle(-11, 0, 601, 669);
        width[0x8e] = 611;
        boundingBoxes[0x8e] = new Rectangle(-11, 0, 601, 897);
        width[0x61] = 500;
        boundingBoxes[0x61] = new Rectangle(-21, -14, 476, 476);
        width[0xe1] = 500;
        boundingBoxes[0xe1] = new Rectangle(-21, -14, 484, 711);
        width[0xe2] = 500;
        boundingBoxes[0xe2] = new Rectangle(-21, -14, 476, 704);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(139, 516, 240, 181);
        width[0xe4] = 500;
        boundingBoxes[0xe4] = new Rectangle(-21, -14, 497, 669);
        width[0xe6] = 722;
        boundingBoxes[0xe6] = new Rectangle(-5, -13, 678, 475);
        width[0xe0] = 500;
        boundingBoxes[0xe0] = new Rectangle(-21, -14, 476, 711);
        width[0x26] = 778;
        boundingBoxes[0x26] = new Rectangle(5, -19, 694, 701);
        width[0xe5] = 500;
        boundingBoxes[0xe5] = new Rectangle(-21, -14, 476, 743);
        width[0x5e] = 570;
        boundingBoxes[0x5e] = new Rectangle(67, 304, 436, 365);
        width[0x7e] = 570;
        boundingBoxes[0x7e] = new Rectangle(54, 173, 462, 160);
        width[0x2a] = 500;
        boundingBoxes[0x2a] = new Rectangle(65, 249, 391, 436);
        width[0x40] = 832;
        boundingBoxes[0x40] = new Rectangle(63, -18, 707, 703);
        width[0xe3] = 500;
        boundingBoxes[0xe3] = new Rectangle(-21, -14, 512, 669);
        width[0x62] = 500;
        boundingBoxes[0x62] = new Rectangle(-14, -13, 458, 712);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(-1, -18, 280, 703);
        width[0x7c] = 220;
        boundingBoxes[0x7c] = new Rectangle(66, -218, 88, 1000);
        width[0x7b] = 348;
        boundingBoxes[0x7b] = new Rectangle(5, -187, 431, 873);
        width[0x7d] = 348;
        boundingBoxes[0x7d] = new Rectangle(-129, -187, 431, 873);
        width[0x5b] = 333;
        boundingBoxes[0x5b] = new Rectangle(-37, -159, 399, 833);
        width[0x5d] = 333;
        boundingBoxes[0x5d] = new Rectangle(-56, -157, 399, 831);

        width[0xa6] = 220;
        boundingBoxes[0xa6] = new Rectangle(66, -143, 88, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(0, 175, 350, 350);
        width[0x63] = 444;
        boundingBoxes[0x63] = new Rectangle(-5, -13, 397, 475);

        width[0xe7] = 444;
        boundingBoxes[0xe7] = new Rectangle(-5, -218, 397, 680);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(-80, -218, 236, 223);
        width[0xa2] = 500;
        boundingBoxes[0xa2] = new Rectangle(42, -143, 397, 719);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(40, 516, 327, 174);
        width[0x3a] = 333;
        boundingBoxes[0x3a] = new Rectangle(23, -13, 241, 472);
        width[0x2c] = 250;
        boundingBoxes[0x2c] = new Rectangle(-60, -182, 204, 316);
        width[0xa9] = 747;
        boundingBoxes[0xa9] = new Rectangle(30, -18, 688, 703);
        width[0xa4] = 500;
        boundingBoxes[0xa4] = new Rectangle(-26, 34, 552, 552);
        width[0x64] = 500;
        boundingBoxes[0x64] = new Rectangle(-21, -13, 538, 712);
        width[0x86] = 500;
        boundingBoxes[0x86] = new Rectangle(91, -145, 403, 830);
        width[0x87] = 500;
        boundingBoxes[0x87] = new Rectangle(10, -139, 483, 824);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(83, 397, 286, 286);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(55, 550, 347, 134);
        width[0xf7] = 570;
        boundingBoxes[0xf7] = new Rectangle(33, -29, 504, 564);
        width[0x24] = 500;
        boundingBoxes[0x24] = new Rectangle(-20, -100, 517, 833);


        width[0x65] = 444;
        boundingBoxes[0x65] = new Rectangle(5, -13, 393, 475);
        width[0xe9] = 444;
        boundingBoxes[0xe9] = new Rectangle(5, -13, 430, 710);
        width[0xea] = 444;
        boundingBoxes[0xea] = new Rectangle(5, -13, 418, 703);
        width[0xeb] = 444;
        boundingBoxes[0xeb] = new Rectangle(5, -13, 443, 668);
        width[0xe8] = 444;
        boundingBoxes[0xe8] = new Rectangle(5, -13, 393, 710);
        width[0x38] = 500;
        boundingBoxes[0x38] = new Rectangle(3, -13, 473, 696);
        width[0x85] = 1000;
        boundingBoxes[0x85] = new Rectangle(40, -13, 812, 148);
        width[0x97] = 1000;
        boundingBoxes[0x97] = new Rectangle(-40, 178, 1017, 91);
        width[0x96] = 500;
        boundingBoxes[0x96] = new Rectangle(-40, 178, 517, 91);
        width[0x3d] = 570;
        boundingBoxes[0x3d] = new Rectangle(33, 107, 504, 292);
        width[0xf0] = 500;
        boundingBoxes[0xf0] = new Rectangle(-3, -13, 457, 712);
        width[0x21] = 389;
        boundingBoxes[0x21] = new Rectangle(67, -13, 303, 697);
        width[0xa1] = 389;
        boundingBoxes[0xa1] = new Rectangle(19, -205, 303, 697);
        width[0x66] = 333;
        boundingBoxes[0x66] = new Rectangle(-169, -205, 615, 903);

        width[0x35] = 500;
        boundingBoxes[0x35] = new Rectangle(-11, -13, 498, 682);

        width[0x83] = 500;
        boundingBoxes[0x83] = new Rectangle(-87, -156, 624, 863);
        width[0x34] = 500;
        boundingBoxes[0x34] = new Rectangle(-15, 0, 518, 683);

        width[0x67] = 500;
        boundingBoxes[0x67] = new Rectangle(-52, -203, 530, 665);
        width[0xdf] = 500;
        boundingBoxes[0xdf] = new Rectangle(-200, -200, 673, 905);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(85, 516, 212, 181);
        width[0x3e] = 570;
        boundingBoxes[0x3e] = new Rectangle(31, -8, 508, 522);
        width[0xab] = 500;
        boundingBoxes[0xab] = new Rectangle(12, 32, 456, 383);
        width[0xbb] = 500;
        boundingBoxes[0xbb] = new Rectangle(12, 32, 456, 383);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(32, 32, 271, 383);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(10, 32, 271, 383);
        width[0x68] = 556;
        boundingBoxes[0x68] = new Rectangle(-13, -9, 511, 708);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(2, 166, 269, 116);
        width[0x69] = 278;
        boundingBoxes[0x69] = new Rectangle(2, -9, 261, 693);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(2, -9, 350, 706);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(-3, -9, 327, 699);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(2, -9, 362, 664);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(2, -9, 257, 706);
        width[0x6a] = 278;
        boundingBoxes[0x6a] = new Rectangle(-189, -207, 468, 891);
        width[0x6b] = 500;
        boundingBoxes[0x6b] = new Rectangle(-23, -8, 506, 707);
        width[0x6c] = 278;
        boundingBoxes[0x6c] = new Rectangle(2, -9, 288, 708);
        width[0x3c] = 570;
        boundingBoxes[0x3c] = new Rectangle(31, -8, 508, 522);
        width[0xac] = 606;
        boundingBoxes[0xac] = new Rectangle(51, 108, 504, 291);

        width[0x6d] = 778;
        boundingBoxes[0x6d] = new Rectangle(-14, -9, 736, 471);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(51, 553, 342, 70);

        width[0xb5] = 576;
        boundingBoxes[0xb5] = new Rectangle(-60, -207, 576, 656);
        width[0xd7] = 570;
        boundingBoxes[0xd7] = new Rectangle(48, 16, 474, 474);
        width[0x6e] = 556;
        boundingBoxes[0x6e] = new Rectangle(-6, -9, 499, 471);
        width[0x39] = 500;
        boundingBoxes[0x39] = new Rectangle(-12, -10, 487, 693);
        width[0xf1] = 556;
        boundingBoxes[0xf1] = new Rectangle(-6, -9, 510, 664);
        width[0x23] = 500;
        boundingBoxes[0x23] = new Rectangle(-33, 0, 566, 700);
        width[0x6f] = 500;
        boundingBoxes[0x6f] = new Rectangle(-3, -13, 444, 475);
        width[0xf3] = 500;
        boundingBoxes[0xf3] = new Rectangle(-3, -13, 466, 710);
        width[0xf4] = 500;
        boundingBoxes[0xf4] = new Rectangle(-3, -13, 454, 703);
        width[0xf6] = 500;
        boundingBoxes[0xf6] = new Rectangle(-3, -13, 474, 668);
        width[0x9c] = 722;
        boundingBoxes[0x9c] = new Rectangle(6, -13, 668, 475);

        width[0xf2] = 500;
        boundingBoxes[0xf2] = new Rectangle(-3, -13, 444, 710);
        width[0x31] = 500;
        boundingBoxes[0x31] = new Rectangle(5, 0, 414, 683);
        width[0xbd] = 750;
        boundingBoxes[0xbd] = new Rectangle(-9, -14, 732, 697);
        width[0xbc] = 750;
        boundingBoxes[0xbc] = new Rectangle(7, -14, 714, 697);
        width[0xb9] = 300;
        boundingBoxes[0xb9] = new Rectangle(30, 274, 271, 409);
        width[0xaa] = 266;
        boundingBoxes[0xaa] = new Rectangle(16, 399, 314, 286);
        width[0xba] = 300;
        boundingBoxes[0xba] = new Rectangle(56, 400, 291, 285);
        width[0xf8] = 500;
        boundingBoxes[0xf8] = new Rectangle(-3, -119, 444, 679);
        width[0xf5] = 500;
        boundingBoxes[0xf5] = new Rectangle(-3, -13, 494, 668);
        width[0x70] = 500;
        boundingBoxes[0x70] = new Rectangle(-120, -205, 566, 667);
        width[0xb6] = 500;
        boundingBoxes[0xb6] = new Rectangle(-57, -193, 619, 862);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(28, -179, 316, 864);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(-44, -179, 315, 864);
        width[0x25] = 833;
        boundingBoxes[0x25] = new Rectangle(39, -10, 754, 702);
        width[0x2e] = 250;
        boundingBoxes[0x2e] = new Rectangle(-9, -13, 148, 148);
        width[0xb7] = 250;
        boundingBoxes[0xb7] = new Rectangle(51, 257, 148, 148);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(7, -29, 989, 735);
        width[0x2b] = 570;
        boundingBoxes[0x2b] = new Rectangle(33, 0, 504, 506);
        width[0xb1] = 570;
        boundingBoxes[0xb1] = new Rectangle(33, 0, 504, 506);
        width[0x71] = 500;
        boundingBoxes[0x71] = new Rectangle(1, -205, 470, 667);
        width[0x3f] = 500;
        boundingBoxes[0x3f] = new Rectangle(79, -13, 391, 697);
        width[0xbf] = 500;
        boundingBoxes[0xbf] = new Rectangle(30, -205, 391, 697);
        width[0x22] = 555;
        boundingBoxes[0x22] = new Rectangle(136, 398, 400, 287);
        width[0x84] = 500;
        boundingBoxes[0x84] = new Rectangle(-57, -182, 460, 316);
        width[0x93] = 500;
        boundingBoxes[0x93] = new Rectangle(53, 369, 460, 316);
        width[0x94] = 500;
        boundingBoxes[0x94] = new Rectangle(53, 369, 460, 316);
        width[0x91] = 333;
        boundingBoxes[0x91] = new Rectangle(128, 369, 204, 316);
        width[0x92] = 333;
        boundingBoxes[0x92] = new Rectangle(98, 369, 204, 316);
        width[0x82] = 333;
        boundingBoxes[0x82] = new Rectangle(-5, -182, 204, 316);
        width[0x27] = 278;
        boundingBoxes[0x27] = new Rectangle(128, 398, 140, 287);
        width[0x72] = 389;
        boundingBoxes[0x72] = new Rectangle(-21, 0, 410, 462);
        width[0xae] = 747;
        boundingBoxes[0xae] = new Rectangle(30, -18, 688, 703);

        width[0x73] = 389;
        boundingBoxes[0x73] = new Rectangle(-19, -13, 352, 475);
        width[0x9a] = 389;
        boundingBoxes[0x9a] = new Rectangle(-19, -13, 443, 703);
        width[0xa7] = 500;
        boundingBoxes[0xa7] = new Rectangle(36, -143, 423, 828);
        width[0x3b] = 333;
        boundingBoxes[0x3b] = new Rectangle(-25, -183, 289, 642);
        width[0x37] = 500;
        boundingBoxes[0x37] = new Rectangle(52, 0, 473, 669);
        width[0x36] = 500;
        boundingBoxes[0x36] = new Rectangle(23, -15, 486, 694);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-64, -18, 406, 703);
        width[0x20] = 250;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 500;
        boundingBoxes[0xa3] = new Rectangle(-32, -12, 542, 695);
        width[0x74] = 278;
        boundingBoxes[0x74] = new Rectangle(-11, -9, 292, 603);
        width[0xfe] = 500;
        boundingBoxes[0xfe] = new Rectangle(-120, -205, 566, 904);
        width[0x33] = 500;
        boundingBoxes[0x33] = new Rectangle(-15, -13, 465, 696);
        width[0xbe] = 750;
        boundingBoxes[0xbe] = new Rectangle(7, -14, 719, 697);
        width[0xb3] = 300;
        boundingBoxes[0xb3] = new Rectangle(17, 265, 304, 418);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(48, 536, 359, 119);
        width[0x99] = 1000;
        boundingBoxes[0x99] = new Rectangle(32, 263, 936, 406);
        width[0x32] = 500;
        boundingBoxes[0x32] = new Rectangle(-27, 0, 473, 683);
        width[0xb2] = 300;
        boundingBoxes[0xb2] = new Rectangle(2, 274, 311, 409);
        width[0x75] = 556;
        boundingBoxes[0x75] = new Rectangle(15, -9, 477, 471);
        width[0xfa] = 556;
        boundingBoxes[0xfa] = new Rectangle(15, -9, 477, 706);
        width[0xfb] = 556;
        boundingBoxes[0xfb] = new Rectangle(15, -9, 477, 699);
        width[0xfc] = 556;
        boundingBoxes[0xfc] = new Rectangle(15, -9, 484, 664);
        width[0xf9] = 556;
        boundingBoxes[0xf9] = new Rectangle(15, -9, 477, 706);
        width[0x5f] = 500;
        boundingBoxes[0x5f] = new Rectangle(0, -125, 500, 50);
        width[0x76] = 444;
        boundingBoxes[0x76] = new Rectangle(16, -13, 385, 475);
        width[0x77] = 667;
        boundingBoxes[0x77] = new Rectangle(16, -13, 598, 475);
        width[0x78] = 500;
        boundingBoxes[0x78] = new Rectangle(-46, -13, 515, 475);
        width[0x79] = 444;
        boundingBoxes[0x79] = new Rectangle(-94, -205, 486, 667);
        width[0xfd] = 444;
        boundingBoxes[0xfd] = new Rectangle(-94, -205, 529, 902);
        width[0xff] = 444;
        boundingBoxes[0xff] = new Rectangle(-94, -205, 537, 860);
        width[0xa5] = 500;
        boundingBoxes[0xa5] = new Rectangle(33, 0, 595, 669);
        width[0x7a] = 389;
        boundingBoxes[0x7a] = new Rectangle(-43, -78, 411, 527);
        width[0x9e] = 389;
        boundingBoxes[0x9e] = new Rectangle(-43, -78, 467, 768);
        width[0x30] = 500;
        boundingBoxes[0x30] = new Rectangle(17, -14, 460, 697);

        familyNames = new java.util.HashSet();
        familyNames.add("Times");

        kerning = new java.util.HashMap();
        Integer first;
        Integer second;
        Map pairs;

        first = 79;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -40);

        second = 87;
        pairs.put(second, -50);

        second = 89;
        pairs.put(second, -50);

        second = 84;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, 0);

        second = 86;
        pairs.put(second, -50);

        second = 88;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, 0);

        first = 107;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, -30);

        first = 112;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, 0);

        first = 80;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -55);

        second = 97;
        pairs.put(second, -40);

        second = 65;
        pairs.put(second, -85);

        second = 46;
        pairs.put(second, -129);

        second = 101;
        pairs.put(second, -50);

        second = 44;
        pairs.put(second, -129);

        first = 86;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -111);

        second = 79;
        pairs.put(second, -30);

        second = 58;
        pairs.put(second, -74);

        second = 71;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -129);

        second = 59;
        pairs.put(second, -74);

        second = 45;
        pairs.put(second, -70);

        second = 105;
        pairs.put(second, -55);

        second = 65;
        pairs.put(second, -85);

        second = 97;
        pairs.put(second, -111);

        second = 117;
        pairs.put(second, -55);

        second = 46;
        pairs.put(second, -129);

        second = 101;
        pairs.put(second, -111);

        first = 118;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -15);

        second = 97;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -37);

        second = 101;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, -37);

        first = 32;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -37);

        second = 87;
        pairs.put(second, -70);

        second = 147;
        pairs.put(second, 0);

        second = 89;
        pairs.put(second, -70);

        second = 84;
        pairs.put(second, 0);

        second = 145;
        pairs.put(second, 0);

        second = 86;
        pairs.put(second, -70);

        first = 97;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, 0);

        second = 116;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 112;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, 0);

        second = 98;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, 0);

        first = 70;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -70);

        second = 105;
        pairs.put(second, -40);

        second = 114;
        pairs.put(second, -50);

        second = 97;
        pairs.put(second, -95);

        second = 65;
        pairs.put(second, -100);

        second = 46;
        pairs.put(second, -129);

        second = 101;
        pairs.put(second, -100);

        second = 44;
        pairs.put(second, -129);

        first = 85;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -45);

        second = 46;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 100;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 100;
        pairs.put(second, 0);

        second = 119;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 83;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 46;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 122;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        first = 68;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -25);

        second = 87;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -50);

        second = 46;
        pairs.put(second, 0);

        second = 86;
        pairs.put(second, -50);

        second = 44;
        pairs.put(second, 0);

        first = 146;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 0);

        second = 100;
        pairs.put(second, -15);

        second = 32;
        pairs.put(second, -74);

        second = 146;
        pairs.put(second, -74);

        second = 114;
        pairs.put(second, -15);

        second = 116;
        pairs.put(second, -37);

        second = 108;
        pairs.put(second, 0);

        second = 115;
        pairs.put(second, -74);

        second = 118;
        pairs.put(second, -15);

        first = 58;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, 0);

        first = 119;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -15);

        second = 97;
        pairs.put(second, -10);

        second = 104;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -37);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -37);

        first = 75;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -25);

        second = 79;
        pairs.put(second, -30);

        second = 117;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, -20);

        second = 101;
        pairs.put(second, -25);

        first = 82;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -40);

        second = 87;
        pairs.put(second, -18);

        second = 85;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -18);

        second = 84;
        pairs.put(second, -30);

        second = 86;
        pairs.put(second, -18);

        first = 145;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, 0);

        second = 145;
        pairs.put(second, -74);

        first = 103;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, 0);

        second = 105;
        pairs.put(second, 0);

        second = 114;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 66;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -25);

        second = 85;
        pairs.put(second, -10);

        second = 46;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 98;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -40);

        second = 108;
        pairs.put(second, 0);

        second = 98;
        pairs.put(second, -10);

        second = 118;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 81;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 85;
        pairs.put(second, -10);

        second = 46;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 44;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -95);

        second = 32;
        pairs.put(second, 0);

        second = 146;
        pairs.put(second, -95);

        first = 102;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 0);

        second = 111;
        pairs.put(second, -10);

        second = 105;
        pairs.put(second, 0);

        second = 146;
        pairs.put(second, 55);

        second = 97;
        pairs.put(second, 0);

        second = 102;
        pairs.put(second, -18);

        second = 46;
        pairs.put(second, -10);

        second = 108;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -10);

        first = 84;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -95);

        second = 79;
        pairs.put(second, -18);

        second = 119;
        pairs.put(second, -37);

        second = 58;
        pairs.put(second, -74);

        second = 114;
        pairs.put(second, -37);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -92);

        second = 59;
        pairs.put(second, -74);

        second = 45;
        pairs.put(second, -92);

        second = 105;
        pairs.put(second, -37);

        second = 65;
        pairs.put(second, -55);

        second = 97;
        pairs.put(second, -92);

        second = 117;
        pairs.put(second, -37);

        second = 121;
        pairs.put(second, -37);

        second = 46;
        pairs.put(second, -92);

        second = 101;
        pairs.put(second, -92);

        first = 121;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -37);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -37);

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
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 112;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, 0);

        second = 98;
        pairs.put(second, -10);

        second = 120;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 99;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, -10);

        second = 104;
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, 0);

        second = 108;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 87;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -80);

        second = 79;
        pairs.put(second, -15);

        second = 58;
        pairs.put(second, -55);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -74);

        second = 59;
        pairs.put(second, -55);

        second = 45;
        pairs.put(second, -50);

        second = 105;
        pairs.put(second, -37);

        second = 65;
        pairs.put(second, -74);

        second = 97;
        pairs.put(second, -85);

        second = 117;
        pairs.put(second, -55);

        second = 121;
        pairs.put(second, -55);

        second = 46;
        pairs.put(second, -74);

        second = 101;
        pairs.put(second, -90);

        first = 104;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, 0);

        first = 71;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 46;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 105;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 118;
        pairs.put(second, 0);

        first = 65;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -50);

        second = 146;
        pairs.put(second, -74);

        second = 119;
        pairs.put(second, -74);

        second = 87;
        pairs.put(second, -100);

        second = 67;
        pairs.put(second, -65);

        second = 112;
        pairs.put(second, 0);

        second = 81;
        pairs.put(second, -55);

        second = 71;
        pairs.put(second, -60);

        second = 86;
        pairs.put(second, -95);

        second = 118;
        pairs.put(second, -74);

        second = 148;
        pairs.put(second, 0);

        second = 85;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -70);

        second = 121;
        pairs.put(second, -74);

        second = 84;
        pairs.put(second, -55);

        first = 147;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, 0);

        second = 145;
        pairs.put(second, 0);

        first = 78;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -30);

        second = 46;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 115;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, 0);

        first = 111;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -25);

        second = 121;
        pairs.put(second, -10);

        second = 103;
        pairs.put(second, 0);

        second = 120;
        pairs.put(second, -10);

        second = 118;
        pairs.put(second, -15);

        first = 114;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, 0);

        second = 100;
        pairs.put(second, 0);

        second = 107;
        pairs.put(second, 0);

        second = 114;
        pairs.put(second, 0);

        second = 99;
        pairs.put(second, 0);

        second = 112;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, 0);

        second = 108;
        pairs.put(second, 0);

        second = 113;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -65);

        second = 45;
        pairs.put(second, 0);

        second = 105;
        pairs.put(second, 0);

        second = 109;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, 0);

        second = 117;
        pairs.put(second, 0);

        second = 116;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -65);

        second = 110;
        pairs.put(second, 0);

        second = 115;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        first = 108;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        first = 76;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 0);

        second = 146;
        pairs.put(second, -55);

        second = 87;
        pairs.put(second, -37);

        second = 89;
        pairs.put(second, -37);

        second = 121;
        pairs.put(second, -37);

        second = 84;
        pairs.put(second, -18);

        second = 86;
        pairs.put(second, -37);

        first = 148;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 32;
        pairs.put(second, 0);

        first = 109;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        first = 89;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -111);

        second = 45;
        pairs.put(second, -92);

        second = 105;
        pairs.put(second, -55);

        second = 79;
        pairs.put(second, -25);

        second = 58;
        pairs.put(second, -92);

        second = 97;
        pairs.put(second, -92);

        second = 65;
        pairs.put(second, -74);

        second = 117;
        pairs.put(second, -92);

        second = 46;
        pairs.put(second, -74);

        second = 101;
        pairs.put(second, -111);

        second = 59;
        pairs.put(second, -92);

        second = 44;
        pairs.put(second, -92);

        first = 74;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -40);

        second = 97;
        pairs.put(second, -40);

        second = 65;
        pairs.put(second, -25);

        second = 117;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, -10);

        second = 101;
        pairs.put(second, -40);

        second = 44;
        pairs.put(second, -10);

        first = 46;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -95);

        second = 146;
        pairs.put(second, -95);

        first = 110;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, -40);

    }

    public TimesBoldItalic() {
        this(false);
    }

    public TimesBoldItalic(boolean enableKerning) {
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
