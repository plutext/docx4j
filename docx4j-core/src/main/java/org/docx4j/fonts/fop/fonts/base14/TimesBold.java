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

public class TimesBold extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Times-Bold";
    private static final String fullName = "Times Bold";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 676;
    private static final int xHeight = 461;
    private static final int ascender = 676;
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

        width[0x41] = 722;
        boundingBoxes[0x41] = new Rectangle(9, 0, 680, 690);
        width[0xc6] = 1000;
        boundingBoxes[0xc6] = new Rectangle(4, 0, 947, 676);
        width[0xc1] = 722;
        boundingBoxes[0xc1] = new Rectangle(9, 0, 680, 923);
        width[0xc2] = 722;
        boundingBoxes[0xc2] = new Rectangle(9, 0, 680, 914);
        width[0xc4] = 722;
        boundingBoxes[0xc4] = new Rectangle(9, 0, 680, 877);
        width[0xc0] = 722;
        boundingBoxes[0xc0] = new Rectangle(9, 0, 680, 923);
        width[0xc5] = 722;
        boundingBoxes[0xc5] = new Rectangle(9, 0, 680, 935);
        width[0xc3] = 722;
        boundingBoxes[0xc3] = new Rectangle(9, 0, 680, 884);
        width[0x42] = 667;
        boundingBoxes[0x42] = new Rectangle(16, 0, 603, 676);
        width[0x43] = 722;
        boundingBoxes[0x43] = new Rectangle(49, -19, 638, 710);
        width[0xc7] = 722;
        boundingBoxes[0xc7] = new Rectangle(49, -218, 638, 909);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(14, 0, 676, 676);
        width[0x45] = 667;
        boundingBoxes[0x45] = new Rectangle(16, 0, 625, 676);
        width[0xc9] = 667;
        boundingBoxes[0xc9] = new Rectangle(16, 0, 625, 923);
        width[0xca] = 667;
        boundingBoxes[0xca] = new Rectangle(16, 0, 625, 914);
        width[0xcb] = 667;
        boundingBoxes[0xcb] = new Rectangle(16, 0, 625, 877);
        width[0xc8] = 667;
        boundingBoxes[0xc8] = new Rectangle(16, 0, 625, 923);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(6, 0, 684, 676);
        width[0x80] = 500;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 611;
        boundingBoxes[0x46] = new Rectangle(16, 0, 567, 676);
        width[0x47] = 778;
        boundingBoxes[0x47] = new Rectangle(37, -19, 718, 710);
        width[0x48] = 778;
        boundingBoxes[0x48] = new Rectangle(21, 0, 738, 676);
        width[0x49] = 389;
        boundingBoxes[0x49] = new Rectangle(20, 0, 350, 676);
        width[0xcd] = 389;
        boundingBoxes[0xcd] = new Rectangle(20, 0, 350, 923);
        width[0xce] = 389;
        boundingBoxes[0xce] = new Rectangle(20, 0, 350, 914);
        width[0xcf] = 389;
        boundingBoxes[0xcf] = new Rectangle(20, 0, 350, 877);
        width[0xcc] = 389;
        boundingBoxes[0xcc] = new Rectangle(20, 0, 350, 923);
        width[0x4a] = 500;
        boundingBoxes[0x4a] = new Rectangle(3, -96, 476, 772);
        width[0x4b] = 778;
        boundingBoxes[0x4b] = new Rectangle(30, 0, 739, 676);
        width[0x4c] = 667;
        boundingBoxes[0x4c] = new Rectangle(19, 0, 619, 676);

        width[0x4d] = 944;
        boundingBoxes[0x4d] = new Rectangle(14, 0, 907, 676);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(16, -18, 685, 694);
        width[0xd1] = 722;
        boundingBoxes[0xd1] = new Rectangle(16, -18, 685, 902);
        width[0x4f] = 778;
        boundingBoxes[0x4f] = new Rectangle(35, -19, 708, 710);
        width[0x8c] = 1000;
        boundingBoxes[0x8c] = new Rectangle(22, -5, 959, 689);
        width[0xd3] = 778;
        boundingBoxes[0xd3] = new Rectangle(35, -19, 708, 942);
        width[0xd4] = 778;
        boundingBoxes[0xd4] = new Rectangle(35, -19, 708, 933);
        width[0xd6] = 778;
        boundingBoxes[0xd6] = new Rectangle(35, -19, 708, 896);
        width[0xd2] = 778;
        boundingBoxes[0xd2] = new Rectangle(35, -19, 708, 942);
        width[0xd8] = 778;
        boundingBoxes[0xd8] = new Rectangle(35, -74, 708, 811);
        width[0xd5] = 778;
        boundingBoxes[0xd5] = new Rectangle(35, -19, 708, 903);
        width[0x50] = 611;
        boundingBoxes[0x50] = new Rectangle(16, 0, 584, 676);
        width[0x51] = 778;
        boundingBoxes[0x51] = new Rectangle(35, -176, 708, 867);
        width[0x52] = 722;
        boundingBoxes[0x52] = new Rectangle(26, 0, 689, 676);
        width[0x53] = 556;
        boundingBoxes[0x53] = new Rectangle(35, -19, 478, 711);
        width[0x8a] = 556;
        boundingBoxes[0x8a] = new Rectangle(35, -19, 478, 933);
        width[0x54] = 667;
        boundingBoxes[0x54] = new Rectangle(31, 0, 605, 676);
        width[0xde] = 611;
        boundingBoxes[0xde] = new Rectangle(16, 0, 584, 676);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(16, -19, 685, 695);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(16, -19, 685, 942);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(16, -19, 685, 933);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(16, -19, 685, 896);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(16, -19, 685, 942);
        width[0x56] = 722;
        boundingBoxes[0x56] = new Rectangle(16, -18, 685, 694);
        width[0x57] = 1000;
        boundingBoxes[0x57] = new Rectangle(19, -15, 962, 691);
        width[0x58] = 722;
        boundingBoxes[0x58] = new Rectangle(16, 0, 683, 676);
        width[0x59] = 722;
        boundingBoxes[0x59] = new Rectangle(15, 0, 684, 676);
        width[0xdd] = 722;
        boundingBoxes[0xdd] = new Rectangle(15, 0, 684, 923);
        width[0x9f] = 722;
        boundingBoxes[0x9f] = new Rectangle(15, 0, 684, 877);
        width[0x5a] = 667;
        boundingBoxes[0x5a] = new Rectangle(28, 0, 606, 676);
        width[0x8e] = 667;
        boundingBoxes[0x8e] = new Rectangle(28, 0, 606, 914);
        width[0x61] = 500;
        boundingBoxes[0x61] = new Rectangle(25, -14, 463, 487);
        width[0xe1] = 500;
        boundingBoxes[0xe1] = new Rectangle(25, -14, 463, 727);
        width[0xe2] = 500;
        boundingBoxes[0xe2] = new Rectangle(25, -14, 463, 718);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(86, 528, 238, 185);
        width[0xe4] = 500;
        boundingBoxes[0xe4] = new Rectangle(25, -14, 463, 681);
        width[0xe6] = 722;
        boundingBoxes[0xe6] = new Rectangle(33, -14, 660, 487);
        width[0xe0] = 500;
        boundingBoxes[0xe0] = new Rectangle(25, -14, 463, 727);
        width[0x26] = 833;
        boundingBoxes[0x26] = new Rectangle(62, -16, 725, 707);
        width[0xe5] = 500;
        boundingBoxes[0xe5] = new Rectangle(25, -14, 463, 754);
        width[0x5e] = 581;
        boundingBoxes[0x5e] = new Rectangle(73, 311, 436, 365);
        width[0x7e] = 520;
        boundingBoxes[0x7e] = new Rectangle(29, 173, 462, 160);
        width[0x2a] = 500;
        boundingBoxes[0x2a] = new Rectangle(56, 255, 391, 436);
        width[0x40] = 930;
        boundingBoxes[0x40] = new Rectangle(108, -19, 714, 710);
        width[0xe3] = 500;
        boundingBoxes[0xe3] = new Rectangle(25, -14, 463, 688);
        width[0x62] = 556;
        boundingBoxes[0x62] = new Rectangle(17, -14, 504, 690);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(-25, -19, 328, 710);
        width[0x7c] = 220;
        boundingBoxes[0x7c] = new Rectangle(66, -218, 88, 1000);
        width[0x7b] = 394;
        boundingBoxes[0x7b] = new Rectangle(22, -175, 318, 873);
        width[0x7d] = 394;
        boundingBoxes[0x7d] = new Rectangle(54, -175, 318, 873);
        width[0x5b] = 333;
        boundingBoxes[0x5b] = new Rectangle(67, -149, 234, 827);
        width[0x5d] = 333;
        boundingBoxes[0x5d] = new Rectangle(32, -149, 234, 827);

        width[0xa6] = 220;
        boundingBoxes[0xa6] = new Rectangle(66, -143, 88, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(35, 198, 280, 280);
        width[0x63] = 444;
        boundingBoxes[0x63] = new Rectangle(25, -14, 405, 487);

        width[0xe7] = 444;
        boundingBoxes[0xe7] = new Rectangle(25, -218, 405, 691);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(68, -218, 226, 218);
        width[0xa2] = 500;
        boundingBoxes[0xa2] = new Rectangle(53, -140, 405, 728);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(-2, 528, 337, 176);
        width[0x3a] = 333;
        boundingBoxes[0x3a] = new Rectangle(82, -13, 169, 485);
        width[0x2c] = 250;
        boundingBoxes[0x2c] = new Rectangle(39, -180, 184, 335);
        width[0xa9] = 747;
        boundingBoxes[0xa9] = new Rectangle(26, -19, 695, 710);
        width[0xa4] = 500;
        boundingBoxes[0xa4] = new Rectangle(-26, 61, 552, 552);
        width[0x64] = 556;
        boundingBoxes[0x64] = new Rectangle(25, -14, 509, 690);
        width[0x86] = 500;
        boundingBoxes[0x86] = new Rectangle(47, -134, 406, 825);
        width[0x87] = 500;
        boundingBoxes[0x87] = new Rectangle(45, -132, 411, 823);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(57, 402, 286, 286);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(-2, 537, 337, 130);
        width[0xf7] = 570;
        boundingBoxes[0xf7] = new Rectangle(33, -31, 504, 568);
        width[0x24] = 500;
        boundingBoxes[0x24] = new Rectangle(29, -99, 443, 849);


        width[0x65] = 444;
        boundingBoxes[0x65] = new Rectangle(25, -14, 401, 487);
        width[0xe9] = 444;
        boundingBoxes[0xe9] = new Rectangle(25, -14, 401, 727);
        width[0xea] = 444;
        boundingBoxes[0xea] = new Rectangle(25, -14, 401, 718);
        width[0xeb] = 444;
        boundingBoxes[0xeb] = new Rectangle(25, -14, 401, 681);
        width[0xe8] = 444;
        boundingBoxes[0xe8] = new Rectangle(25, -14, 401, 727);
        width[0x38] = 500;
        boundingBoxes[0x38] = new Rectangle(28, -13, 444, 701);
        width[0x85] = 1000;
        boundingBoxes[0x85] = new Rectangle(82, -13, 835, 169);
        width[0x97] = 1000;
        boundingBoxes[0x97] = new Rectangle(0, 181, 1000, 90);
        width[0x96] = 500;
        boundingBoxes[0x96] = new Rectangle(0, 181, 500, 90);
        width[0x3d] = 570;
        boundingBoxes[0x3d] = new Rectangle(33, 107, 504, 292);
        width[0xf0] = 500;
        boundingBoxes[0xf0] = new Rectangle(25, -14, 451, 705);
        width[0x21] = 333;
        boundingBoxes[0x21] = new Rectangle(81, -13, 170, 704);
        width[0xa1] = 333;
        boundingBoxes[0xa1] = new Rectangle(82, -203, 170, 704);
        width[0x66] = 333;
        boundingBoxes[0x66] = new Rectangle(14, 0, 375, 691);

        width[0x35] = 500;
        boundingBoxes[0x35] = new Rectangle(22, -8, 448, 684);

        width[0x83] = 500;
        boundingBoxes[0x83] = new Rectangle(0, -155, 498, 861);
        width[0x34] = 500;
        boundingBoxes[0x34] = new Rectangle(19, 0, 456, 688);

        width[0x67] = 500;
        boundingBoxes[0x67] = new Rectangle(28, -206, 455, 679);
        width[0xdf] = 556;
        boundingBoxes[0xdf] = new Rectangle(19, -12, 498, 703);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(8, 528, 238, 185);
        width[0x3e] = 570;
        boundingBoxes[0x3e] = new Rectangle(31, -8, 508, 522);
        width[0xab] = 500;
        boundingBoxes[0xab] = new Rectangle(23, 36, 450, 379);
        width[0xbb] = 500;
        boundingBoxes[0xbb] = new Rectangle(27, 36, 450, 379);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(51, 36, 254, 379);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(28, 36, 254, 379);
        width[0x68] = 556;
        boundingBoxes[0x68] = new Rectangle(16, 0, 518, 676);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(44, 171, 243, 116);
        width[0x69] = 278;
        boundingBoxes[0x69] = new Rectangle(16, 0, 239, 691);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(16, 0, 273, 713);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(-37, 0, 337, 704);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(-37, 0, 337, 667);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(-27, 0, 282, 713);
        width[0x6a] = 333;
        boundingBoxes[0x6a] = new Rectangle(-57, -203, 320, 894);
        width[0x6b] = 556;
        boundingBoxes[0x6b] = new Rectangle(22, 0, 521, 676);
        width[0x6c] = 278;
        boundingBoxes[0x6c] = new Rectangle(16, 0, 239, 676);
        width[0x3c] = 570;
        boundingBoxes[0x3c] = new Rectangle(31, -8, 508, 522);
        width[0xac] = 570;
        boundingBoxes[0xac] = new Rectangle(33, 108, 504, 291);

        width[0x6d] = 833;
        boundingBoxes[0x6d] = new Rectangle(16, 0, 798, 473);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(1, 565, 330, 72);

        width[0xb5] = 556;
        boundingBoxes[0xb5] = new Rectangle(33, -206, 503, 667);
        width[0xd7] = 570;
        boundingBoxes[0xd7] = new Rectangle(48, 16, 474, 474);
        width[0x6e] = 556;
        boundingBoxes[0x6e] = new Rectangle(21, 0, 518, 473);
        width[0x39] = 500;
        boundingBoxes[0x39] = new Rectangle(26, -13, 447, 701);
        width[0xf1] = 556;
        boundingBoxes[0xf1] = new Rectangle(21, 0, 518, 674);
        width[0x23] = 500;
        boundingBoxes[0x23] = new Rectangle(4, 0, 492, 700);
        width[0x6f] = 500;
        boundingBoxes[0x6f] = new Rectangle(25, -14, 451, 487);
        width[0xf3] = 500;
        boundingBoxes[0xf3] = new Rectangle(25, -14, 451, 727);
        width[0xf4] = 500;
        boundingBoxes[0xf4] = new Rectangle(25, -14, 451, 718);
        width[0xf6] = 500;
        boundingBoxes[0xf6] = new Rectangle(25, -14, 451, 681);
        width[0x9c] = 722;
        boundingBoxes[0x9c] = new Rectangle(22, -14, 674, 487);

        width[0xf2] = 500;
        boundingBoxes[0xf2] = new Rectangle(25, -14, 451, 727);
        width[0x31] = 500;
        boundingBoxes[0x31] = new Rectangle(65, 0, 377, 688);
        width[0xbd] = 750;
        boundingBoxes[0xbd] = new Rectangle(-7, -12, 782, 700);
        width[0xbc] = 750;
        boundingBoxes[0xbc] = new Rectangle(28, -12, 715, 700);
        width[0xb9] = 300;
        boundingBoxes[0xb9] = new Rectangle(28, 275, 245, 413);
        width[0xaa] = 300;
        boundingBoxes[0xaa] = new Rectangle(-1, 397, 302, 291);
        width[0xba] = 330;
        boundingBoxes[0xba] = new Rectangle(18, 397, 294, 291);
        width[0xf8] = 500;
        boundingBoxes[0xf8] = new Rectangle(25, -92, 451, 641);
        width[0xf5] = 500;
        boundingBoxes[0xf5] = new Rectangle(25, -14, 451, 688);
        width[0x70] = 556;
        boundingBoxes[0x70] = new Rectangle(19, -205, 505, 678);
        width[0xb6] = 540;
        boundingBoxes[0xb6] = new Rectangle(0, -186, 519, 862);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(46, -168, 260, 862);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(27, -168, 260, 862);
        width[0x25] = 1000;
        boundingBoxes[0x25] = new Rectangle(124, -14, 753, 706);
        width[0x2e] = 250;
        boundingBoxes[0x2e] = new Rectangle(41, -13, 169, 169);
        width[0xb7] = 250;
        boundingBoxes[0xb7] = new Rectangle(41, 248, 169, 169);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(7, -29, 988, 735);
        width[0x2b] = 570;
        boundingBoxes[0x2b] = new Rectangle(33, 0, 504, 506);
        width[0xb1] = 570;
        boundingBoxes[0xb1] = new Rectangle(33, 0, 504, 506);
        width[0x71] = 556;
        boundingBoxes[0x71] = new Rectangle(34, -205, 502, 678);
        width[0x3f] = 500;
        boundingBoxes[0x3f] = new Rectangle(57, -13, 388, 702);
        width[0xbf] = 500;
        boundingBoxes[0xbf] = new Rectangle(55, -201, 388, 702);
        width[0x22] = 555;
        boundingBoxes[0x22] = new Rectangle(83, 404, 389, 287);
        width[0x84] = 500;
        boundingBoxes[0x84] = new Rectangle(14, -180, 454, 335);
        width[0x93] = 500;
        boundingBoxes[0x93] = new Rectangle(32, 356, 454, 335);
        width[0x94] = 500;
        boundingBoxes[0x94] = new Rectangle(14, 356, 454, 335);
        width[0x91] = 333;
        boundingBoxes[0x91] = new Rectangle(70, 356, 184, 335);
        width[0x92] = 333;
        boundingBoxes[0x92] = new Rectangle(79, 356, 184, 335);
        width[0x82] = 333;
        boundingBoxes[0x82] = new Rectangle(79, -180, 184, 335);
        width[0x27] = 278;
        boundingBoxes[0x27] = new Rectangle(75, 404, 129, 287);
        width[0x72] = 444;
        boundingBoxes[0x72] = new Rectangle(29, 0, 405, 473);
        width[0xae] = 747;
        boundingBoxes[0xae] = new Rectangle(26, -19, 695, 710);

        width[0x73] = 389;
        boundingBoxes[0x73] = new Rectangle(25, -14, 336, 487);
        width[0x9a] = 389;
        boundingBoxes[0x9a] = new Rectangle(25, -14, 338, 718);
        width[0xa7] = 500;
        boundingBoxes[0xa7] = new Rectangle(57, -132, 386, 823);
        width[0x3b] = 333;
        boundingBoxes[0x3b] = new Rectangle(82, -180, 184, 652);
        width[0x37] = 500;
        boundingBoxes[0x37] = new Rectangle(17, 0, 460, 676);
        width[0x36] = 500;
        boundingBoxes[0x36] = new Rectangle(28, -13, 447, 701);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-24, -19, 326, 710);
        width[0x20] = 250;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 500;
        boundingBoxes[0xa3] = new Rectangle(21, -14, 456, 698);
        width[0x74] = 333;
        boundingBoxes[0x74] = new Rectangle(20, -12, 312, 642);
        width[0xfe] = 556;
        boundingBoxes[0xfe] = new Rectangle(19, -205, 505, 881);
        width[0x33] = 500;
        boundingBoxes[0x33] = new Rectangle(16, -14, 452, 702);
        width[0xbe] = 750;
        boundingBoxes[0xbe] = new Rectangle(23, -12, 710, 700);
        width[0xb3] = 300;
        boundingBoxes[0xb3] = new Rectangle(3, 268, 294, 420);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(-16, 547, 365, 127);
        width[0x99] = 1000;
        boundingBoxes[0x99] = new Rectangle(24, 271, 953, 405);
        width[0x32] = 500;
        boundingBoxes[0x32] = new Rectangle(17, 0, 461, 688);
        width[0xb2] = 300;
        boundingBoxes[0xb2] = new Rectangle(0, 275, 300, 413);
        width[0x75] = 556;
        boundingBoxes[0x75] = new Rectangle(16, -14, 521, 475);
        width[0xfa] = 556;
        boundingBoxes[0xfa] = new Rectangle(16, -14, 521, 727);
        width[0xfb] = 556;
        boundingBoxes[0xfb] = new Rectangle(16, -14, 521, 718);
        width[0xfc] = 556;
        boundingBoxes[0xfc] = new Rectangle(16, -14, 521, 681);
        width[0xf9] = 556;
        boundingBoxes[0xf9] = new Rectangle(16, -14, 521, 727);
        width[0x5f] = 500;
        boundingBoxes[0x5f] = new Rectangle(0, -125, 500, 50);
        width[0x76] = 500;
        boundingBoxes[0x76] = new Rectangle(21, -14, 464, 475);
        width[0x77] = 722;
        boundingBoxes[0x77] = new Rectangle(23, -14, 684, 475);
        width[0x78] = 500;
        boundingBoxes[0x78] = new Rectangle(12, 0, 472, 461);
        width[0x79] = 500;
        boundingBoxes[0x79] = new Rectangle(16, -205, 464, 666);
        width[0xfd] = 500;
        boundingBoxes[0xfd] = new Rectangle(16, -205, 464, 918);
        width[0xff] = 500;
        boundingBoxes[0xff] = new Rectangle(16, -205, 464, 872);
        width[0xa5] = 500;
        boundingBoxes[0xa5] = new Rectangle(-64, 0, 611, 676);
        width[0x7a] = 444;
        boundingBoxes[0x7a] = new Rectangle(21, 0, 399, 461);
        width[0x9e] = 444;
        boundingBoxes[0x9e] = new Rectangle(21, 0, 399, 704);
        width[0x30] = 500;
        boundingBoxes[0x30] = new Rectangle(24, -13, 452, 701);

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
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -15);

        second = 101;
        pairs.put(second, -10);

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
        pairs.put(second, -20);

        second = 97;
        pairs.put(second, -10);

        second = 65;
        pairs.put(second, -74);

        second = 46;
        pairs.put(second, -110);

        second = 101;
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, -92);

        first = 86;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -100);

        second = 79;
        pairs.put(second, -45);

        second = 58;
        pairs.put(second, -92);

        second = 71;
        pairs.put(second, -30);

        second = 44;
        pairs.put(second, -129);

        second = 59;
        pairs.put(second, -92);

        second = 45;
        pairs.put(second, -74);

        second = 105;
        pairs.put(second, -37);

        second = 65;
        pairs.put(second, -135);

        second = 97;
        pairs.put(second, -92);

        second = 117;
        pairs.put(second, -92);

        second = 46;
        pairs.put(second, -145);

        second = 101;
        pairs.put(second, -100);

        first = 118;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -10);

        second = 97;
        pairs.put(second, -10);

        second = 46;
        pairs.put(second, -70);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -55);

        first = 32;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -55);

        second = 87;
        pairs.put(second, -30);

        second = 147;
        pairs.put(second, 0);

        second = 89;
        pairs.put(second, -55);

        second = 84;
        pairs.put(second, -30);

        second = 145;
        pairs.put(second, 0);

        second = 86;
        pairs.put(second, -45);

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
        pairs.put(second, -25);

        first = 70;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -25);

        second = 105;
        pairs.put(second, 0);

        second = 114;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, -25);

        second = 65;
        pairs.put(second, -90);

        second = 46;
        pairs.put(second, -110);

        second = 101;
        pairs.put(second, -25);

        second = 44;
        pairs.put(second, -92);

        first = 85;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -60);

        second = 46;
        pairs.put(second, -50);

        second = 44;
        pairs.put(second, -50);

        first = 100;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 100;
        pairs.put(second, 0);

        second = 119;
        pairs.put(second, -15);

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
        pairs.put(second, -35);

        second = 87;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, -20);

        second = 86;
        pairs.put(second, -40);

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
        pairs.put(second, -20);

        second = 32;
        pairs.put(second, -74);

        second = 146;
        pairs.put(second, -63);

        second = 114;
        pairs.put(second, -20);

        second = 116;
        pairs.put(second, 0);

        second = 108;
        pairs.put(second, 0);

        second = 115;
        pairs.put(second, -37);

        second = 118;
        pairs.put(second, -20);

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
        pairs.put(second, -10);

        second = 97;
        pairs.put(second, 0);

        second = 104;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -70);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -55);

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
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -45);

        second = 101;
        pairs.put(second, -25);

        first = 82;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -30);

        second = 87;
        pairs.put(second, -35);

        second = 85;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -35);

        second = 84;
        pairs.put(second, -40);

        second = 86;
        pairs.put(second, -55);

        first = 145;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -10);

        second = 145;
        pairs.put(second, -63);

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
        pairs.put(second, -15);

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
        pairs.put(second, -30);

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
        pairs.put(second, -15);

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
        pairs.put(second, -20);

        second = 44;
        pairs.put(second, 0);

        first = 44;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -45);

        second = 32;
        pairs.put(second, 0);

        second = 146;
        pairs.put(second, -55);

        first = 102;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 50);

        second = 111;
        pairs.put(second, -25);

        second = 105;
        pairs.put(second, -25);

        second = 146;
        pairs.put(second, 55);

        second = 97;
        pairs.put(second, 0);

        second = 102;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -15);

        second = 108;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -15);

        first = 84;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -92);

        second = 79;
        pairs.put(second, -18);

        second = 119;
        pairs.put(second, -74);

        second = 58;
        pairs.put(second, -74);

        second = 114;
        pairs.put(second, -74);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -74);

        second = 59;
        pairs.put(second, -74);

        second = 45;
        pairs.put(second, -92);

        second = 105;
        pairs.put(second, -18);

        second = 65;
        pairs.put(second, -90);

        second = 97;
        pairs.put(second, -92);

        second = 117;
        pairs.put(second, -92);

        second = 121;
        pairs.put(second, -74);

        second = 46;
        pairs.put(second, -90);

        second = 101;
        pairs.put(second, -92);

        first = 121;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -25);

        second = 97;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -70);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -55);

        first = 120;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 101;
        pairs.put(second, 0);

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
        pairs.put(second, 0);

        second = 120;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, 0);

        first = 99;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, 0);

        second = 104;
        pairs.put(second, 0);

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
        pairs.put(second, -75);

        second = 79;
        pairs.put(second, -10);

        second = 58;
        pairs.put(second, -55);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -92);

        second = 59;
        pairs.put(second, -55);

        second = 45;
        pairs.put(second, -37);

        second = 105;
        pairs.put(second, -18);

        second = 65;
        pairs.put(second, -120);

        second = 97;
        pairs.put(second, -65);

        second = 117;
        pairs.put(second, -50);

        second = 121;
        pairs.put(second, -60);

        second = 46;
        pairs.put(second, -92);

        second = 101;
        pairs.put(second, -65);

        first = 104;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, -15);

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
        pairs.put(second, -10);

        first = 65;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -45);

        second = 146;
        pairs.put(second, -74);

        second = 119;
        pairs.put(second, -90);

        second = 87;
        pairs.put(second, -130);

        second = 67;
        pairs.put(second, -55);

        second = 112;
        pairs.put(second, -25);

        second = 81;
        pairs.put(second, -45);

        second = 71;
        pairs.put(second, -55);

        second = 86;
        pairs.put(second, -145);

        second = 118;
        pairs.put(second, -100);

        second = 148;
        pairs.put(second, 0);

        second = 85;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -50);

        second = 89;
        pairs.put(second, -100);

        second = 121;
        pairs.put(second, -74);

        second = 84;
        pairs.put(second, -95);

        first = 147;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -10);

        second = 145;
        pairs.put(second, 0);

        first = 78;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -20);

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
        pairs.put(second, -10);

        second = 121;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, 0);

        second = 120;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, -10);

        first = 114;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -18);

        second = 100;
        pairs.put(second, 0);

        second = 107;
        pairs.put(second, 0);

        second = 114;
        pairs.put(second, 0);

        second = 99;
        pairs.put(second, -18);

        second = 112;
        pairs.put(second, -10);

        second = 103;
        pairs.put(second, -10);

        second = 108;
        pairs.put(second, 0);

        second = 113;
        pairs.put(second, -18);

        second = 118;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -92);

        second = 45;
        pairs.put(second, -37);

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
        pairs.put(second, -100);

        second = 110;
        pairs.put(second, -15);

        second = 115;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, -18);

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
        pairs.put(second, -20);

        second = 146;
        pairs.put(second, -110);

        second = 87;
        pairs.put(second, -92);

        second = 89;
        pairs.put(second, -92);

        second = 121;
        pairs.put(second, -55);

        second = 84;
        pairs.put(second, -92);

        second = 86;
        pairs.put(second, -92);

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
        pairs.put(second, -37);

        second = 79;
        pairs.put(second, -35);

        second = 58;
        pairs.put(second, -92);

        second = 97;
        pairs.put(second, -85);

        second = 65;
        pairs.put(second, -110);

        second = 117;
        pairs.put(second, -92);

        second = 46;
        pairs.put(second, -92);

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
        pairs.put(second, -15);

        second = 97;
        pairs.put(second, -15);

        second = 65;
        pairs.put(second, -30);

        second = 117;
        pairs.put(second, -15);

        second = 46;
        pairs.put(second, -20);

        second = 101;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, 0);

        first = 46;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -55);

        second = 146;
        pairs.put(second, -55);

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

    public TimesBold() {
        this(false);
    }

    public TimesBold(boolean enableKerning) {
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
