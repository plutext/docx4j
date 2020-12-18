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

public class HelveticaBoldOblique extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Helvetica-BoldOblique";
    private static final String fullName = "Helvetica Bold Oblique";
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
        boundingBoxes[0xc6] = new Rectangle(5, 0, 1095, 718);
        width[0xc1] = 722;
        boundingBoxes[0xc1] = new Rectangle(20, 0, 730, 936);
        width[0xc2] = 722;
        boundingBoxes[0xc2] = new Rectangle(20, 0, 686, 936);
        width[0xc4] = 722;
        boundingBoxes[0xc4] = new Rectangle(20, 0, 696, 915);
        width[0xc0] = 722;
        boundingBoxes[0xc0] = new Rectangle(20, 0, 682, 936);
        width[0xc5] = 722;
        boundingBoxes[0xc5] = new Rectangle(20, 0, 682, 962);
        width[0xc3] = 722;
        boundingBoxes[0xc3] = new Rectangle(20, 0, 721, 923);
        width[0x42] = 722;
        boundingBoxes[0x42] = new Rectangle(76, 0, 688, 718);
        width[0x43] = 722;
        boundingBoxes[0x43] = new Rectangle(107, -19, 682, 756);
        width[0xc7] = 722;
        boundingBoxes[0xc7] = new Rectangle(107, -228, 682, 965);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(76, 0, 701, 718);
        width[0x45] = 667;
        boundingBoxes[0x45] = new Rectangle(76, 0, 681, 718);
        width[0xc9] = 667;
        boundingBoxes[0xc9] = new Rectangle(76, 0, 681, 936);
        width[0xca] = 667;
        boundingBoxes[0xca] = new Rectangle(76, 0, 681, 936);
        width[0xcb] = 667;
        boundingBoxes[0xcb] = new Rectangle(76, 0, 681, 915);
        width[0xc8] = 667;
        boundingBoxes[0xc8] = new Rectangle(76, 0, 681, 936);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(62, 0, 715, 718);
        width[0x80] = 556;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 611;
        boundingBoxes[0x46] = new Rectangle(76, 0, 664, 718);
        width[0x47] = 778;
        boundingBoxes[0x47] = new Rectangle(108, -19, 709, 756);
        width[0x48] = 722;
        boundingBoxes[0x48] = new Rectangle(71, 0, 733, 718);
        width[0x49] = 278;
        boundingBoxes[0x49] = new Rectangle(64, 0, 303, 718);
        width[0xcd] = 278;
        boundingBoxes[0xcd] = new Rectangle(64, 0, 464, 936);
        width[0xce] = 278;
        boundingBoxes[0xce] = new Rectangle(64, 0, 420, 936);
        width[0xcf] = 278;
        boundingBoxes[0xcf] = new Rectangle(64, 0, 430, 915);
        width[0xcc] = 278;
        boundingBoxes[0xcc] = new Rectangle(64, 0, 303, 936);
        width[0x4a] = 556;
        boundingBoxes[0x4a] = new Rectangle(60, -18, 577, 736);
        width[0x4b] = 722;
        boundingBoxes[0x4b] = new Rectangle(87, 0, 771, 718);
        width[0x4c] = 611;
        boundingBoxes[0x4c] = new Rectangle(76, 0, 535, 718);

        width[0x4d] = 833;
        boundingBoxes[0x4d] = new Rectangle(69, 0, 849, 718);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(69, 0, 738, 718);
        width[0xd1] = 722;
        boundingBoxes[0xd1] = new Rectangle(69, 0, 738, 923);
        width[0x4f] = 778;
        boundingBoxes[0x4f] = new Rectangle(107, -19, 716, 756);
        width[0x8c] = 1000;
        boundingBoxes[0x8c] = new Rectangle(99, -19, 1015, 756);
        width[0xd3] = 778;
        boundingBoxes[0xd3] = new Rectangle(107, -19, 716, 955);
        width[0xd4] = 778;
        boundingBoxes[0xd4] = new Rectangle(107, -19, 716, 955);
        width[0xd6] = 778;
        boundingBoxes[0xd6] = new Rectangle(107, -19, 716, 934);
        width[0xd2] = 778;
        boundingBoxes[0xd2] = new Rectangle(107, -19, 716, 955);
        width[0xd8] = 778;
        boundingBoxes[0xd8] = new Rectangle(35, -27, 859, 772);
        width[0xd5] = 778;
        boundingBoxes[0xd5] = new Rectangle(107, -19, 716, 942);
        width[0x50] = 667;
        boundingBoxes[0x50] = new Rectangle(76, 0, 662, 718);
        width[0x51] = 778;
        boundingBoxes[0x51] = new Rectangle(107, -52, 716, 789);
        width[0x52] = 722;
        boundingBoxes[0x52] = new Rectangle(76, 0, 702, 718);
        width[0x53] = 667;
        boundingBoxes[0x53] = new Rectangle(81, -19, 637, 756);
        width[0x8a] = 667;
        boundingBoxes[0x8a] = new Rectangle(81, -19, 637, 955);
        width[0x54] = 611;
        boundingBoxes[0x54] = new Rectangle(140, 0, 611, 718);
        width[0xde] = 667;
        boundingBoxes[0xde] = new Rectangle(76, 0, 640, 718);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(116, -19, 688, 737);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(116, -19, 688, 955);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(116, -19, 688, 955);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(116, -19, 688, 934);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(116, -19, 688, 955);
        width[0x56] = 667;
        boundingBoxes[0x56] = new Rectangle(172, 0, 629, 718);
        width[0x57] = 944;
        boundingBoxes[0x57] = new Rectangle(169, 0, 913, 718);
        width[0x58] = 667;
        boundingBoxes[0x58] = new Rectangle(14, 0, 777, 718);
        width[0x59] = 667;
        boundingBoxes[0x59] = new Rectangle(168, 0, 638, 718);
        width[0xdd] = 667;
        boundingBoxes[0xdd] = new Rectangle(168, 0, 638, 936);
        width[0x9f] = 667;
        boundingBoxes[0x9f] = new Rectangle(168, 0, 638, 915);
        width[0x5a] = 611;
        boundingBoxes[0x5a] = new Rectangle(25, 0, 712, 718);
        width[0x8e] = 611;
        boundingBoxes[0x8e] = new Rectangle(25, 0, 712, 936);
        width[0x61] = 556;
        boundingBoxes[0x61] = new Rectangle(55, -14, 528, 560);
        width[0xe1] = 556;
        boundingBoxes[0xe1] = new Rectangle(55, -14, 572, 764);
        width[0xe2] = 556;
        boundingBoxes[0xe2] = new Rectangle(55, -14, 528, 764);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(236, 604, 279, 146);
        width[0xe4] = 556;
        boundingBoxes[0xe4] = new Rectangle(55, -14, 539, 743);
        width[0xe6] = 889;
        boundingBoxes[0xe6] = new Rectangle(56, -14, 867, 560);
        width[0xe0] = 556;
        boundingBoxes[0xe0] = new Rectangle(55, -14, 528, 764);
        width[0x26] = 722;
        boundingBoxes[0x26] = new Rectangle(89, -19, 643, 737);
        width[0xe5] = 556;
        boundingBoxes[0xe5] = new Rectangle(55, -14, 528, 790);
        width[0x5e] = 584;
        boundingBoxes[0x5e] = new Rectangle(131, 323, 460, 375);
        width[0x7e] = 584;
        boundingBoxes[0x7e] = new Rectangle(115, 163, 462, 180);
        width[0x2a] = 389;
        boundingBoxes[0x2a] = new Rectangle(146, 387, 335, 331);
        width[0x40] = 975;
        boundingBoxes[0x40] = new Rectangle(186, -19, 768, 756);
        width[0xe3] = 556;
        boundingBoxes[0xe3] = new Rectangle(55, -14, 564, 751);
        width[0x62] = 611;
        boundingBoxes[0x62] = new Rectangle(61, -14, 584, 732);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(124, -19, 183, 756);
        width[0x7c] = 280;
        boundingBoxes[0x7c] = new Rectangle(36, -225, 325, 1000);
        width[0x7b] = 389;
        boundingBoxes[0x7b] = new Rectangle(94, -196, 424, 918);
        width[0x7d] = 389;
        boundingBoxes[0x7d] = new Rectangle(-18, -196, 425, 918);
        width[0x5b] = 333;
        boundingBoxes[0x5b] = new Rectangle(21, -196, 441, 918);
        width[0x5d] = 333;
        boundingBoxes[0x5d] = new Rectangle(-18, -196, 441, 918);

        width[0xa6] = 280;
        boundingBoxes[0xa6] = new Rectangle(52, -150, 293, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(83, 194, 337, 330);
        width[0x63] = 556;
        boundingBoxes[0x63] = new Rectangle(79, -14, 520, 560);

        width[0xe7] = 556;
        boundingBoxes[0xe7] = new Rectangle(79, -228, 520, 774);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(-37, -228, 257, 228);
        width[0xa2] = 556;
        boundingBoxes[0xa2] = new Rectangle(79, -118, 520, 746);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(118, 604, 353, 146);
        width[0x3a] = 333;
        boundingBoxes[0x3a] = new Rectangle(92, 0, 259, 512);
        width[0x2c] = 278;
        boundingBoxes[0x2c] = new Rectangle(28, -168, 217, 314);
        width[0xa9] = 737;
        boundingBoxes[0xa9] = new Rectangle(56, -19, 779, 756);
        width[0xa4] = 556;
        boundingBoxes[0xa4] = new Rectangle(27, 76, 653, 560);
        width[0x64] = 611;
        boundingBoxes[0x64] = new Rectangle(82, -14, 622, 732);
        width[0x86] = 556;
        boundingBoxes[0x86] = new Rectangle(118, -171, 508, 889);
        width[0x87] = 556;
        boundingBoxes[0x87] = new Rectangle(46, -171, 582, 889);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(175, 426, 292, 286);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(137, 614, 345, 115);
        width[0xf7] = 584;
        boundingBoxes[0xf7] = new Rectangle(82, -42, 528, 590);
        width[0x24] = 556;
        boundingBoxes[0x24] = new Rectangle(67, -115, 555, 890);


        width[0x65] = 556;
        boundingBoxes[0x65] = new Rectangle(70, -14, 523, 560);
        width[0xe9] = 556;
        boundingBoxes[0xe9] = new Rectangle(70, -14, 557, 764);
        width[0xea] = 556;
        boundingBoxes[0xea] = new Rectangle(70, -14, 523, 764);
        width[0xeb] = 556;
        boundingBoxes[0xeb] = new Rectangle(70, -14, 524, 743);
        width[0xe8] = 556;
        boundingBoxes[0xe8] = new Rectangle(70, -14, 523, 764);
        width[0x38] = 556;
        boundingBoxes[0x38] = new Rectangle(69, -19, 547, 729);
        width[0x85] = 1000;
        boundingBoxes[0x85] = new Rectangle(92, 0, 847, 146);
        width[0x97] = 1000;
        boundingBoxes[0x97] = new Rectangle(48, 227, 1023, 106);
        width[0x96] = 556;
        boundingBoxes[0x96] = new Rectangle(48, 227, 579, 106);
        width[0x3d] = 584;
        boundingBoxes[0x3d] = new Rectangle(58, 87, 575, 332);
        width[0xf0] = 611;
        boundingBoxes[0xf0] = new Rectangle(82, -14, 588, 751);
        width[0x21] = 333;
        boundingBoxes[0x21] = new Rectangle(94, 0, 303, 718);
        width[0xa1] = 333;
        boundingBoxes[0xa1] = new Rectangle(50, -186, 303, 718);
        width[0x66] = 333;
        boundingBoxes[0x66] = new Rectangle(87, 0, 382, 727);

        width[0x35] = 556;
        boundingBoxes[0x35] = new Rectangle(64, -19, 572, 717);

        width[0x83] = 556;
        boundingBoxes[0x83] = new Rectangle(-50, -210, 719, 947);
        width[0x34] = 556;
        boundingBoxes[0x34] = new Rectangle(60, 0, 538, 710);

        width[0x67] = 611;
        boundingBoxes[0x67] = new Rectangle(38, -217, 628, 763);
        width[0xdf] = 611;
        boundingBoxes[0xdf] = new Rectangle(69, -14, 588, 745);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(136, 604, 217, 146);
        width[0x3e] = 584;
        boundingBoxes[0x3e] = new Rectangle(36, -8, 573, 522);
        width[0xab] = 556;
        boundingBoxes[0xab] = new Rectangle(135, 76, 436, 408);
        width[0xbb] = 556;
        boundingBoxes[0xbb] = new Rectangle(104, 76, 436, 408);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(130, 76, 223, 408);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(99, 76, 223, 408);
        width[0x68] = 611;
        boundingBoxes[0x68] = new Rectangle(65, 0, 564, 718);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(73, 215, 306, 130);
        width[0x69] = 278;
        boundingBoxes[0x69] = new Rectangle(69, 0, 294, 725);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(69, 0, 419, 750);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(69, 0, 375, 750);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(69, 0, 386, 729);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(69, 0, 257, 750);
        width[0x6a] = 278;
        boundingBoxes[0x6a] = new Rectangle(-42, -214, 405, 939);
        width[0x6b] = 556;
        boundingBoxes[0x6b] = new Rectangle(69, 0, 601, 718);
        width[0x6c] = 278;
        boundingBoxes[0x6c] = new Rectangle(69, 0, 293, 718);
        width[0x3c] = 584;
        boundingBoxes[0x3c] = new Rectangle(82, -8, 573, 522);
        width[0xac] = 584;
        boundingBoxes[0xac] = new Rectangle(105, 108, 528, 311);

        width[0x6d] = 889;
        boundingBoxes[0x6d] = new Rectangle(64, 0, 845, 546);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(122, 604, 361, 74);

        width[0xb5] = 611;
        boundingBoxes[0xb5] = new Rectangle(22, -207, 636, 739);
        width[0xd7] = 584;
        boundingBoxes[0xd7] = new Rectangle(57, 1, 578, 504);
        width[0x6e] = 611;
        boundingBoxes[0x6e] = new Rectangle(65, 0, 564, 546);
        width[0x39] = 556;
        boundingBoxes[0x39] = new Rectangle(78, -19, 537, 729);
        width[0xf1] = 611;
        boundingBoxes[0xf1] = new Rectangle(65, 0, 581, 737);
        width[0x23] = 556;
        boundingBoxes[0x23] = new Rectangle(60, 0, 584, 698);
        width[0x6f] = 611;
        boundingBoxes[0x6f] = new Rectangle(82, -14, 561, 560);
        width[0xf3] = 611;
        boundingBoxes[0xf3] = new Rectangle(82, -14, 572, 764);
        width[0xf4] = 611;
        boundingBoxes[0xf4] = new Rectangle(82, -14, 561, 764);
        width[0xf6] = 611;
        boundingBoxes[0xf6] = new Rectangle(82, -14, 561, 743);
        width[0x9c] = 944;
        boundingBoxes[0x9c] = new Rectangle(82, -14, 895, 560);

        width[0xf2] = 611;
        boundingBoxes[0xf2] = new Rectangle(82, -14, 561, 764);
        width[0x31] = 556;
        boundingBoxes[0x31] = new Rectangle(173, 0, 356, 710);
        width[0xbd] = 834;
        boundingBoxes[0xbd] = new Rectangle(132, -19, 726, 729);
        width[0xbc] = 834;
        boundingBoxes[0xbc] = new Rectangle(132, -19, 674, 729);
        width[0xb9] = 333;
        boundingBoxes[0xb9] = new Rectangle(148, 283, 240, 427);
        width[0xaa] = 370;
        boundingBoxes[0xaa] = new Rectangle(125, 401, 340, 336);
        width[0xba] = 365;
        boundingBoxes[0xba] = new Rectangle(123, 401, 362, 336);
        width[0xf8] = 611;
        boundingBoxes[0xf8] = new Rectangle(22, -29, 679, 589);
        width[0xf5] = 611;
        boundingBoxes[0xf5] = new Rectangle(82, -14, 564, 751);
        width[0x70] = 611;
        boundingBoxes[0x70] = new Rectangle(18, -207, 627, 753);
        width[0xb6] = 556;
        boundingBoxes[0xb6] = new Rectangle(98, -191, 590, 891);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(76, -208, 394, 942);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(-25, -208, 394, 942);
        width[0x25] = 889;
        boundingBoxes[0x25] = new Rectangle(136, -19, 765, 729);
        width[0x2e] = 278;
        boundingBoxes[0x2e] = new Rectangle(64, 0, 181, 146);
        width[0xb7] = 278;
        boundingBoxes[0xb7] = new Rectangle(110, 172, 166, 162);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(76, -19, 962, 729);
        width[0x2b] = 584;
        boundingBoxes[0x2b] = new Rectangle(82, 0, 528, 506);
        width[0xb1] = 584;
        boundingBoxes[0xb1] = new Rectangle(40, 0, 585, 506);
        width[0x71] = 611;
        boundingBoxes[0x71] = new Rectangle(80, -207, 585, 753);
        width[0x3f] = 611;
        boundingBoxes[0x3f] = new Rectangle(165, 0, 506, 727);
        width[0xbf] = 611;
        boundingBoxes[0xbf] = new Rectangle(53, -195, 506, 727);
        width[0x22] = 474;
        boundingBoxes[0x22] = new Rectangle(193, 447, 336, 271);
        width[0x84] = 500;
        boundingBoxes[0x84] = new Rectangle(36, -146, 427, 273);
        width[0x93] = 500;
        boundingBoxes[0x93] = new Rectangle(160, 454, 428, 273);
        width[0x94] = 500;
        boundingBoxes[0x94] = new Rectangle(162, 445, 427, 273);
        width[0x91] = 278;
        boundingBoxes[0x91] = new Rectangle(165, 454, 196, 273);
        width[0x92] = 278;
        boundingBoxes[0x92] = new Rectangle(167, 445, 195, 273);
        width[0x82] = 278;
        boundingBoxes[0x82] = new Rectangle(41, -146, 195, 273);
        width[0x27] = 238;
        boundingBoxes[0x27] = new Rectangle(165, 447, 156, 271);
        width[0x72] = 389;
        boundingBoxes[0x72] = new Rectangle(64, 0, 425, 546);
        width[0xae] = 737;
        boundingBoxes[0xae] = new Rectangle(55, -19, 779, 756);

        width[0x73] = 556;
        boundingBoxes[0x73] = new Rectangle(63, -14, 521, 560);
        width[0x9a] = 556;
        boundingBoxes[0x9a] = new Rectangle(63, -14, 551, 764);
        width[0xa7] = 556;
        boundingBoxes[0xa7] = new Rectangle(61, -184, 537, 911);
        width[0x3b] = 333;
        boundingBoxes[0x3b] = new Rectangle(56, -168, 295, 680);
        width[0x37] = 556;
        boundingBoxes[0x37] = new Rectangle(125, 0, 551, 698);
        width[0x36] = 556;
        boundingBoxes[0x36] = new Rectangle(85, -19, 534, 729);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-37, -19, 505, 756);
        width[0x20] = 278;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 556;
        boundingBoxes[0xa3] = new Rectangle(50, -16, 585, 734);
        width[0x74] = 333;
        boundingBoxes[0x74] = new Rectangle(100, -6, 322, 682);
        width[0xfe] = 611;
        boundingBoxes[0xfe] = new Rectangle(18, -208, 627, 926);
        width[0x33] = 556;
        boundingBoxes[0x33] = new Rectangle(65, -19, 543, 729);
        width[0xbe] = 834;
        boundingBoxes[0xbe] = new Rectangle(99, -19, 740, 729);
        width[0xb3] = 333;
        boundingBoxes[0xb3] = new Rectangle(91, 271, 350, 439);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(113, 610, 394, 127);
        width[0x99] = 1000;
        boundingBoxes[0x99] = new Rectangle(179, 306, 930, 412);
        width[0x32] = 556;
        boundingBoxes[0x32] = new Rectangle(26, 0, 593, 710);
        width[0xb2] = 333;
        boundingBoxes[0xb2] = new Rectangle(69, 283, 380, 427);
        width[0x75] = 611;
        boundingBoxes[0x75] = new Rectangle(98, -14, 560, 546);
        width[0xfa] = 611;
        boundingBoxes[0xfa] = new Rectangle(98, -14, 560, 764);
        width[0xfb] = 611;
        boundingBoxes[0xfb] = new Rectangle(98, -14, 560, 764);
        width[0xfc] = 611;
        boundingBoxes[0xfc] = new Rectangle(98, -14, 560, 743);
        width[0xf9] = 611;
        boundingBoxes[0xf9] = new Rectangle(98, -14, 560, 764);
        width[0x5f] = 556;
        boundingBoxes[0x5f] = new Rectangle(-27, -125, 567, 50);
        width[0x76] = 556;
        boundingBoxes[0x76] = new Rectangle(126, 0, 530, 532);
        width[0x77] = 778;
        boundingBoxes[0x77] = new Rectangle(123, 0, 759, 532);
        width[0x78] = 556;
        boundingBoxes[0x78] = new Rectangle(15, 0, 633, 532);
        width[0x79] = 556;
        boundingBoxes[0x79] = new Rectangle(42, -214, 610, 746);
        width[0xfd] = 556;
        boundingBoxes[0xfd] = new Rectangle(42, -214, 610, 964);
        width[0xff] = 556;
        boundingBoxes[0xff] = new Rectangle(42, -214, 610, 943);
        width[0xa5] = 556;
        boundingBoxes[0xa5] = new Rectangle(60, 0, 653, 698);
        width[0x7a] = 500;
        boundingBoxes[0x7a] = new Rectangle(20, 0, 563, 532);
        width[0x9e] = 500;
        boundingBoxes[0x9e] = new Rectangle(20, 0, 566, 750);
        width[0x30] = 556;
        boundingBoxes[0x30] = new Rectangle(86, -19, 531, 729);

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

    public HelveticaBoldOblique() {
        this(false);
    }

    public HelveticaBoldOblique(boolean enableKerning) {
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
