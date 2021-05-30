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

public class TimesRoman extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Times-Roman";
    private static final String fullName = "Times Roman";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 662;
    private static final int xHeight = 450;
    private static final int ascender = 683;
    private static final int descender = -217;
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
        boundingBoxes[0x41] = new Rectangle(15, 0, 691, 674);
        width[0xc6] = 889;
        boundingBoxes[0xc6] = new Rectangle(0, 0, 863, 662);
        width[0xc1] = 722;
        boundingBoxes[0xc1] = new Rectangle(15, 0, 691, 890);
        width[0xc2] = 722;
        boundingBoxes[0xc2] = new Rectangle(15, 0, 691, 886);
        width[0xc4] = 722;
        boundingBoxes[0xc4] = new Rectangle(15, 0, 691, 835);
        width[0xc0] = 722;
        boundingBoxes[0xc0] = new Rectangle(15, 0, 691, 890);
        width[0xc5] = 722;
        boundingBoxes[0xc5] = new Rectangle(15, 0, 691, 898);
        width[0xc3] = 722;
        boundingBoxes[0xc3] = new Rectangle(15, 0, 691, 850);
        width[0x42] = 667;
        boundingBoxes[0x42] = new Rectangle(17, 0, 576, 662);
        width[0x43] = 667;
        boundingBoxes[0x43] = new Rectangle(28, -14, 605, 690);
        width[0xc7] = 667;
        boundingBoxes[0xc7] = new Rectangle(28, -215, 605, 891);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(16, 0, 669, 662);
        width[0x45] = 611;
        boundingBoxes[0x45] = new Rectangle(12, 0, 585, 662);
        width[0xc9] = 611;
        boundingBoxes[0xc9] = new Rectangle(12, 0, 585, 890);
        width[0xca] = 611;
        boundingBoxes[0xca] = new Rectangle(12, 0, 585, 886);
        width[0xcb] = 611;
        boundingBoxes[0xcb] = new Rectangle(12, 0, 585, 835);
        width[0xc8] = 611;
        boundingBoxes[0xc8] = new Rectangle(12, 0, 585, 890);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(16, 0, 669, 662);
        width[0x80] = 500;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 556;
        boundingBoxes[0x46] = new Rectangle(12, 0, 534, 662);
        width[0x47] = 722;
        boundingBoxes[0x47] = new Rectangle(32, -14, 677, 690);
        width[0x48] = 722;
        boundingBoxes[0x48] = new Rectangle(19, 0, 683, 662);
        width[0x49] = 333;
        boundingBoxes[0x49] = new Rectangle(18, 0, 297, 662);
        width[0xcd] = 333;
        boundingBoxes[0xcd] = new Rectangle(18, 0, 299, 890);
        width[0xce] = 333;
        boundingBoxes[0xce] = new Rectangle(11, 0, 311, 886);
        width[0xcf] = 333;
        boundingBoxes[0xcf] = new Rectangle(18, 0, 297, 835);
        width[0xcc] = 333;
        boundingBoxes[0xcc] = new Rectangle(18, 0, 297, 890);
        width[0x4a] = 389;
        boundingBoxes[0x4a] = new Rectangle(10, -14, 360, 676);
        width[0x4b] = 722;
        boundingBoxes[0x4b] = new Rectangle(34, 0, 689, 662);
        width[0x4c] = 611;
        boundingBoxes[0x4c] = new Rectangle(12, 0, 586, 662);

        width[0x4d] = 889;
        boundingBoxes[0x4d] = new Rectangle(12, 0, 851, 662);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(12, -11, 695, 673);
        width[0xd1] = 722;
        boundingBoxes[0xd1] = new Rectangle(12, -11, 695, 861);
        width[0x4f] = 722;
        boundingBoxes[0x4f] = new Rectangle(34, -14, 654, 690);
        width[0x8c] = 889;
        boundingBoxes[0x8c] = new Rectangle(30, -6, 855, 674);
        width[0xd3] = 722;
        boundingBoxes[0xd3] = new Rectangle(34, -14, 654, 904);
        width[0xd4] = 722;
        boundingBoxes[0xd4] = new Rectangle(34, -14, 654, 900);
        width[0xd6] = 722;
        boundingBoxes[0xd6] = new Rectangle(34, -14, 654, 849);
        width[0xd2] = 722;
        boundingBoxes[0xd2] = new Rectangle(34, -14, 654, 904);
        width[0xd8] = 722;
        boundingBoxes[0xd8] = new Rectangle(34, -80, 654, 814);
        width[0xd5] = 722;
        boundingBoxes[0xd5] = new Rectangle(34, -14, 654, 864);
        width[0x50] = 556;
        boundingBoxes[0x50] = new Rectangle(16, 0, 526, 662);
        width[0x51] = 722;
        boundingBoxes[0x51] = new Rectangle(34, -178, 667, 854);
        width[0x52] = 667;
        boundingBoxes[0x52] = new Rectangle(17, 0, 642, 662);
        width[0x53] = 556;
        boundingBoxes[0x53] = new Rectangle(42, -14, 449, 690);
        width[0x8a] = 556;
        boundingBoxes[0x8a] = new Rectangle(42, -14, 449, 900);
        width[0x54] = 611;
        boundingBoxes[0x54] = new Rectangle(17, 0, 576, 662);
        width[0xde] = 556;
        boundingBoxes[0xde] = new Rectangle(16, 0, 526, 662);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(14, -14, 691, 676);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(14, -14, 691, 904);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(14, -14, 691, 900);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(14, -14, 691, 849);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(14, -14, 691, 904);
        width[0x56] = 722;
        boundingBoxes[0x56] = new Rectangle(16, -11, 681, 673);
        width[0x57] = 944;
        boundingBoxes[0x57] = new Rectangle(5, -11, 927, 673);
        width[0x58] = 722;
        boundingBoxes[0x58] = new Rectangle(10, 0, 694, 662);
        width[0x59] = 722;
        boundingBoxes[0x59] = new Rectangle(22, 0, 681, 662);
        width[0xdd] = 722;
        boundingBoxes[0xdd] = new Rectangle(22, 0, 681, 890);
        width[0x9f] = 722;
        boundingBoxes[0x9f] = new Rectangle(22, 0, 681, 835);
        width[0x5a] = 611;
        boundingBoxes[0x5a] = new Rectangle(9, 0, 588, 662);
        width[0x8e] = 611;
        boundingBoxes[0x8e] = new Rectangle(9, 0, 588, 886);
        width[0x61] = 444;
        boundingBoxes[0x61] = new Rectangle(37, -10, 405, 470);
        width[0xe1] = 444;
        boundingBoxes[0xe1] = new Rectangle(37, -10, 405, 688);
        width[0xe2] = 444;
        boundingBoxes[0xe2] = new Rectangle(37, -10, 405, 684);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(93, 507, 224, 171);
        width[0xe4] = 444;
        boundingBoxes[0xe4] = new Rectangle(37, -10, 405, 633);
        width[0xe6] = 667;
        boundingBoxes[0xe6] = new Rectangle(38, -10, 594, 470);
        width[0xe0] = 444;
        boundingBoxes[0xe0] = new Rectangle(37, -10, 405, 688);
        width[0x26] = 778;
        boundingBoxes[0x26] = new Rectangle(42, -13, 708, 689);
        width[0xe5] = 444;
        boundingBoxes[0xe5] = new Rectangle(37, -10, 405, 721);
        width[0x5e] = 469;
        boundingBoxes[0x5e] = new Rectangle(24, 297, 422, 365);
        width[0x7e] = 541;
        boundingBoxes[0x7e] = new Rectangle(40, 183, 462, 140);
        width[0x2a] = 500;
        boundingBoxes[0x2a] = new Rectangle(69, 265, 363, 411);
        width[0x40] = 921;
        boundingBoxes[0x40] = new Rectangle(116, -14, 693, 690);
        width[0xe3] = 444;
        boundingBoxes[0xe3] = new Rectangle(37, -10, 405, 648);
        width[0x62] = 500;
        boundingBoxes[0x62] = new Rectangle(3, -10, 465, 693);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(-9, -14, 296, 690);
        width[0x7c] = 200;
        boundingBoxes[0x7c] = new Rectangle(67, -218, 66, 1000);
        width[0x7b] = 480;
        boundingBoxes[0x7b] = new Rectangle(100, -181, 250, 861);
        width[0x7d] = 480;
        boundingBoxes[0x7d] = new Rectangle(130, -181, 250, 861);
        width[0x5b] = 333;
        boundingBoxes[0x5b] = new Rectangle(88, -156, 211, 818);
        width[0x5d] = 333;
        boundingBoxes[0x5d] = new Rectangle(34, -156, 211, 818);

        width[0xa6] = 200;
        boundingBoxes[0xa6] = new Rectangle(67, -143, 66, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(40, 196, 270, 270);
        width[0x63] = 444;
        boundingBoxes[0x63] = new Rectangle(25, -10, 387, 470);

        width[0xe7] = 444;
        boundingBoxes[0xe7] = new Rectangle(25, -215, 387, 675);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(52, -215, 209, 215);
        width[0xa2] = 500;
        boundingBoxes[0xa2] = new Rectangle(53, -138, 395, 717);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(11, 507, 311, 167);
        width[0x3a] = 278;
        boundingBoxes[0x3a] = new Rectangle(81, -11, 111, 470);
        width[0x2c] = 250;
        boundingBoxes[0x2c] = new Rectangle(56, -141, 139, 243);
        width[0xa9] = 760;
        boundingBoxes[0xa9] = new Rectangle(38, -14, 684, 690);
        width[0xa4] = 500;
        boundingBoxes[0xa4] = new Rectangle(-22, 58, 544, 544);
        width[0x64] = 500;
        boundingBoxes[0x64] = new Rectangle(27, -10, 464, 693);
        width[0x86] = 500;
        boundingBoxes[0x86] = new Rectangle(59, -149, 383, 825);
        width[0x87] = 500;
        boundingBoxes[0x87] = new Rectangle(58, -153, 384, 829);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(57, 390, 286, 286);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(18, 581, 297, 100);
        width[0xf7] = 564;
        boundingBoxes[0xf7] = new Rectangle(30, -10, 504, 526);
        width[0x24] = 500;
        boundingBoxes[0x24] = new Rectangle(44, -87, 413, 814);


        width[0x65] = 444;
        boundingBoxes[0x65] = new Rectangle(25, -10, 399, 470);
        width[0xe9] = 444;
        boundingBoxes[0xe9] = new Rectangle(25, -10, 399, 688);
        width[0xea] = 444;
        boundingBoxes[0xea] = new Rectangle(25, -10, 399, 684);
        width[0xeb] = 444;
        boundingBoxes[0xeb] = new Rectangle(25, -10, 399, 633);
        width[0xe8] = 444;
        boundingBoxes[0xe8] = new Rectangle(25, -10, 399, 688);
        width[0x38] = 500;
        boundingBoxes[0x38] = new Rectangle(56, -14, 389, 690);
        width[0x85] = 1000;
        boundingBoxes[0x85] = new Rectangle(111, -11, 777, 111);
        width[0x97] = 1000;
        boundingBoxes[0x97] = new Rectangle(0, 201, 1000, 49);
        width[0x96] = 500;
        boundingBoxes[0x96] = new Rectangle(0, 201, 500, 49);
        width[0x3d] = 564;
        boundingBoxes[0x3d] = new Rectangle(30, 120, 504, 266);
        width[0xf0] = 500;
        boundingBoxes[0xf0] = new Rectangle(29, -10, 442, 696);
        width[0x21] = 333;
        boundingBoxes[0x21] = new Rectangle(130, -9, 108, 685);
        width[0xa1] = 333;
        boundingBoxes[0xa1] = new Rectangle(97, -218, 108, 685);
        width[0x66] = 333;
        boundingBoxes[0x66] = new Rectangle(20, 0, 363, 683);

        width[0x35] = 500;
        boundingBoxes[0x35] = new Rectangle(32, -14, 406, 702);

        width[0x83] = 500;
        boundingBoxes[0x83] = new Rectangle(7, -189, 483, 865);
        width[0x34] = 500;
        boundingBoxes[0x34] = new Rectangle(12, 0, 460, 676);

        width[0x67] = 500;
        boundingBoxes[0x67] = new Rectangle(28, -218, 442, 678);
        width[0xdf] = 500;
        boundingBoxes[0xdf] = new Rectangle(12, -9, 456, 692);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(19, 507, 223, 171);
        width[0x3e] = 564;
        boundingBoxes[0x3e] = new Rectangle(28, -8, 508, 522);
        width[0xab] = 500;
        boundingBoxes[0xab] = new Rectangle(42, 33, 414, 383);
        width[0xbb] = 500;
        boundingBoxes[0xbb] = new Rectangle(44, 33, 414, 383);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(63, 33, 222, 383);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(48, 33, 222, 383);
        width[0x68] = 500;
        boundingBoxes[0x68] = new Rectangle(9, 0, 478, 683);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(39, 194, 246, 63);
        width[0x69] = 278;
        boundingBoxes[0x69] = new Rectangle(16, 0, 237, 683);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(16, 0, 274, 678);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(-16, 0, 311, 674);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(-9, 0, 297, 623);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(-8, 0, 261, 678);
        width[0x6a] = 278;
        boundingBoxes[0x6a] = new Rectangle(-70, -218, 264, 901);
        width[0x6b] = 500;
        boundingBoxes[0x6b] = new Rectangle(7, 0, 498, 683);
        width[0x6c] = 278;
        boundingBoxes[0x6c] = new Rectangle(19, 0, 238, 683);
        width[0x3c] = 564;
        boundingBoxes[0x3c] = new Rectangle(28, -8, 508, 522);
        width[0xac] = 564;
        boundingBoxes[0xac] = new Rectangle(30, 108, 504, 278);

        width[0x6d] = 778;
        boundingBoxes[0x6d] = new Rectangle(16, 0, 759, 460);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(11, 547, 311, 54);

        width[0xb5] = 500;
        boundingBoxes[0xb5] = new Rectangle(36, -218, 476, 668);
        width[0xd7] = 564;
        boundingBoxes[0xd7] = new Rectangle(38, 8, 489, 489);
        width[0x6e] = 500;
        boundingBoxes[0x6e] = new Rectangle(16, 0, 469, 460);
        width[0x39] = 500;
        boundingBoxes[0x39] = new Rectangle(30, -22, 429, 698);
        width[0xf1] = 500;
        boundingBoxes[0xf1] = new Rectangle(16, 0, 469, 638);
        width[0x23] = 500;
        boundingBoxes[0x23] = new Rectangle(5, 0, 491, 662);
        width[0x6f] = 500;
        boundingBoxes[0x6f] = new Rectangle(29, -10, 441, 470);
        width[0xf3] = 500;
        boundingBoxes[0xf3] = new Rectangle(29, -10, 441, 688);
        width[0xf4] = 500;
        boundingBoxes[0xf4] = new Rectangle(29, -10, 441, 684);
        width[0xf6] = 500;
        boundingBoxes[0xf6] = new Rectangle(29, -10, 441, 633);
        width[0x9c] = 722;
        boundingBoxes[0x9c] = new Rectangle(30, -10, 660, 470);

        width[0xf2] = 500;
        boundingBoxes[0xf2] = new Rectangle(29, -10, 441, 688);
        width[0x31] = 500;
        boundingBoxes[0x31] = new Rectangle(111, 0, 283, 676);
        width[0xbd] = 750;
        boundingBoxes[0xbd] = new Rectangle(31, -14, 715, 690);
        width[0xbc] = 750;
        boundingBoxes[0xbc] = new Rectangle(37, -14, 681, 690);
        width[0xb9] = 300;
        boundingBoxes[0xb9] = new Rectangle(57, 270, 191, 406);
        width[0xaa] = 276;
        boundingBoxes[0xaa] = new Rectangle(4, 394, 266, 282);
        width[0xba] = 310;
        boundingBoxes[0xba] = new Rectangle(6, 394, 298, 282);
        width[0xf8] = 500;
        boundingBoxes[0xf8] = new Rectangle(29, -112, 441, 663);
        width[0xf5] = 500;
        boundingBoxes[0xf5] = new Rectangle(29, -10, 441, 648);
        width[0x70] = 500;
        boundingBoxes[0x70] = new Rectangle(5, -217, 465, 677);
        width[0xb6] = 453;
        boundingBoxes[0xb6] = new Rectangle(-22, -154, 472, 816);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(48, -177, 256, 853);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(29, -177, 256, 853);
        width[0x25] = 833;
        boundingBoxes[0x25] = new Rectangle(61, -13, 711, 689);
        width[0x2e] = 250;
        boundingBoxes[0x2e] = new Rectangle(70, -11, 111, 111);
        width[0xb7] = 250;
        boundingBoxes[0xb7] = new Rectangle(70, 199, 111, 111);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(7, -19, 987, 725);
        width[0x2b] = 564;
        boundingBoxes[0x2b] = new Rectangle(30, 0, 504, 506);
        width[0xb1] = 564;
        boundingBoxes[0xb1] = new Rectangle(30, 0, 504, 506);
        width[0x71] = 500;
        boundingBoxes[0x71] = new Rectangle(24, -217, 464, 677);
        width[0x3f] = 444;
        boundingBoxes[0x3f] = new Rectangle(68, -8, 346, 684);
        width[0xbf] = 444;
        boundingBoxes[0xbf] = new Rectangle(30, -218, 346, 684);
        width[0x22] = 408;
        boundingBoxes[0x22] = new Rectangle(77, 431, 254, 245);
        width[0x84] = 444;
        boundingBoxes[0x84] = new Rectangle(45, -141, 371, 243);
        width[0x93] = 444;
        boundingBoxes[0x93] = new Rectangle(43, 433, 371, 243);
        width[0x94] = 444;
        boundingBoxes[0x94] = new Rectangle(30, 433, 371, 243);
        width[0x91] = 333;
        boundingBoxes[0x91] = new Rectangle(115, 433, 139, 243);
        width[0x92] = 333;
        boundingBoxes[0x92] = new Rectangle(79, 433, 139, 243);
        width[0x82] = 333;
        boundingBoxes[0x82] = new Rectangle(79, -141, 139, 243);
        width[0x27] = 180;
        boundingBoxes[0x27] = new Rectangle(48, 431, 85, 245);
        width[0x72] = 333;
        boundingBoxes[0x72] = new Rectangle(5, 0, 330, 460);
        width[0xae] = 760;
        boundingBoxes[0xae] = new Rectangle(38, -14, 684, 690);

        width[0x73] = 389;
        boundingBoxes[0x73] = new Rectangle(51, -10, 297, 470);
        width[0x9a] = 389;
        boundingBoxes[0x9a] = new Rectangle(39, -10, 311, 684);
        width[0xa7] = 500;
        boundingBoxes[0xa7] = new Rectangle(70, -148, 356, 824);
        width[0x3b] = 278;
        boundingBoxes[0x3b] = new Rectangle(80, -141, 139, 600);
        width[0x37] = 500;
        boundingBoxes[0x37] = new Rectangle(20, -8, 429, 670);
        width[0x36] = 500;
        boundingBoxes[0x36] = new Rectangle(34, -14, 434, 698);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-9, -14, 296, 690);
        width[0x20] = 250;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 500;
        boundingBoxes[0xa3] = new Rectangle(12, -8, 478, 684);
        width[0x74] = 278;
        boundingBoxes[0x74] = new Rectangle(13, -10, 266, 589);
        width[0xfe] = 500;
        boundingBoxes[0xfe] = new Rectangle(5, -217, 465, 900);
        width[0x33] = 500;
        boundingBoxes[0x33] = new Rectangle(43, -14, 388, 690);
        width[0xbe] = 750;
        boundingBoxes[0xbe] = new Rectangle(15, -14, 703, 690);
        width[0xb3] = 300;
        boundingBoxes[0xb3] = new Rectangle(15, 262, 276, 414);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(1, 532, 330, 106);
        width[0x99] = 980;
        boundingBoxes[0x99] = new Rectangle(30, 256, 927, 406);
        width[0x32] = 500;
        boundingBoxes[0x32] = new Rectangle(30, 0, 445, 676);
        width[0xb2] = 300;
        boundingBoxes[0xb2] = new Rectangle(1, 270, 295, 406);
        width[0x75] = 500;
        boundingBoxes[0x75] = new Rectangle(9, -10, 470, 460);
        width[0xfa] = 500;
        boundingBoxes[0xfa] = new Rectangle(9, -10, 470, 688);
        width[0xfb] = 500;
        boundingBoxes[0xfb] = new Rectangle(9, -10, 470, 684);
        width[0xfc] = 500;
        boundingBoxes[0xfc] = new Rectangle(9, -10, 470, 633);
        width[0xf9] = 500;
        boundingBoxes[0xf9] = new Rectangle(9, -10, 470, 688);
        width[0x5f] = 500;
        boundingBoxes[0x5f] = new Rectangle(0, -125, 500, 50);
        width[0x76] = 500;
        boundingBoxes[0x76] = new Rectangle(19, -14, 458, 464);
        width[0x77] = 722;
        boundingBoxes[0x77] = new Rectangle(21, -14, 673, 464);
        width[0x78] = 500;
        boundingBoxes[0x78] = new Rectangle(17, 0, 462, 450);
        width[0x79] = 500;
        boundingBoxes[0x79] = new Rectangle(14, -218, 461, 668);
        width[0xfd] = 500;
        boundingBoxes[0xfd] = new Rectangle(14, -218, 461, 896);
        width[0xff] = 500;
        boundingBoxes[0xff] = new Rectangle(14, -218, 461, 841);
        width[0xa5] = 500;
        boundingBoxes[0xa5] = new Rectangle(-53, 0, 565, 662);
        width[0x7a] = 444;
        boundingBoxes[0x7a] = new Rectangle(27, 0, 391, 450);
        width[0x9e] = 444;
        boundingBoxes[0x9e] = new Rectangle(27, 0, 391, 674);
        width[0x30] = 500;
        boundingBoxes[0x30] = new Rectangle(24, -14, 452, 690);

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
        pairs.put(second, -35);

        second = 87;
        pairs.put(second, -35);

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
        pairs.put(second, -10);

        first = 80;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, -15);

        second = 65;
        pairs.put(second, -92);

        second = 46;
        pairs.put(second, -111);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -111);

        first = 86;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -129);

        second = 79;
        pairs.put(second, -40);

        second = 58;
        pairs.put(second, -74);

        second = 71;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, -129);

        second = 59;
        pairs.put(second, -74);

        second = 45;
        pairs.put(second, -100);

        second = 105;
        pairs.put(second, -60);

        second = 65;
        pairs.put(second, -135);

        second = 97;
        pairs.put(second, -111);

        second = 117;
        pairs.put(second, -75);

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
        pairs.put(second, -20);

        second = 97;
        pairs.put(second, -25);

        second = 46;
        pairs.put(second, -65);

        second = 101;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, -65);

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
        pairs.put(second, -90);

        second = 84;
        pairs.put(second, -18);

        second = 145;
        pairs.put(second, 0);

        second = 86;
        pairs.put(second, -50);

        first = 97;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -15);

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
        pairs.put(second, -20);

        first = 70;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -15);

        second = 105;
        pairs.put(second, 0);

        second = 114;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, -15);

        second = 65;
        pairs.put(second, -74);

        second = 46;
        pairs.put(second, -80);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -80);

        first = 85;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -40);

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
        pairs.put(second, -40);

        second = 87;
        pairs.put(second, -30);

        second = 89;
        pairs.put(second, -55);

        second = 46;
        pairs.put(second, 0);

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
        pairs.put(second, -50);

        second = 32;
        pairs.put(second, -74);

        second = 146;
        pairs.put(second, -74);

        second = 114;
        pairs.put(second, -50);

        second = 116;
        pairs.put(second, -18);

        second = 108;
        pairs.put(second, -10);

        second = 115;
        pairs.put(second, -55);

        second = 118;
        pairs.put(second, -50);

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
        pairs.put(second, -10);

        second = 104;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -65);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -65);

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
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -25);

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
        pairs.put(second, -55);

        second = 85;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -65);

        second = 84;
        pairs.put(second, -60);

        second = 86;
        pairs.put(second, -80);

        first = 145;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -80);

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
        pairs.put(second, -5);

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
        pairs.put(second, -35);

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
        pairs.put(second, 0);

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
        pairs.put(second, -70);

        second = 32;
        pairs.put(second, 0);

        second = 146;
        pairs.put(second, -70);

        first = 102;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, 0);

        second = 111;
        pairs.put(second, 0);

        second = 105;
        pairs.put(second, -20);

        second = 146;
        pairs.put(second, 55);

        second = 97;
        pairs.put(second, -10);

        second = 102;
        pairs.put(second, -25);

        second = 46;
        pairs.put(second, 0);

        second = 108;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 84;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -80);

        second = 79;
        pairs.put(second, -18);

        second = 119;
        pairs.put(second, -80);

        second = 58;
        pairs.put(second, -50);

        second = 114;
        pairs.put(second, -35);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -74);

        second = 59;
        pairs.put(second, -55);

        second = 45;
        pairs.put(second, -92);

        second = 105;
        pairs.put(second, -35);

        second = 65;
        pairs.put(second, -93);

        second = 97;
        pairs.put(second, -80);

        second = 117;
        pairs.put(second, -45);

        second = 121;
        pairs.put(second, -80);

        second = 46;
        pairs.put(second, -74);

        second = 101;
        pairs.put(second, -70);

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
        pairs.put(second, -65);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -65);

        first = 120;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 101;
        pairs.put(second, -15);

        first = 101;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 119;
        pairs.put(second, -25);

        second = 121;
        pairs.put(second, -15);

        second = 112;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, -15);

        second = 98;
        pairs.put(second, 0);

        second = 120;
        pairs.put(second, -15);

        second = 118;
        pairs.put(second, -25);

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
        pairs.put(second, -15);

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
        pairs.put(second, -10);

        second = 58;
        pairs.put(second, -37);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -92);

        second = 59;
        pairs.put(second, -37);

        second = 45;
        pairs.put(second, -65);

        second = 105;
        pairs.put(second, -40);

        second = 65;
        pairs.put(second, -120);

        second = 97;
        pairs.put(second, -80);

        second = 117;
        pairs.put(second, -50);

        second = 121;
        pairs.put(second, -73);

        second = 46;
        pairs.put(second, -92);

        second = 101;
        pairs.put(second, -80);

        first = 104;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 121;
        pairs.put(second, -5);

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
        pairs.put(second, -25);

        first = 65;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 79;
        pairs.put(second, -55);

        second = 146;
        pairs.put(second, -111);

        second = 119;
        pairs.put(second, -92);

        second = 87;
        pairs.put(second, -90);

        second = 67;
        pairs.put(second, -40);

        second = 112;
        pairs.put(second, 0);

        second = 81;
        pairs.put(second, -55);

        second = 71;
        pairs.put(second, -40);

        second = 86;
        pairs.put(second, -135);

        second = 118;
        pairs.put(second, -74);

        second = 148;
        pairs.put(second, 0);

        second = 85;
        pairs.put(second, -55);

        second = 117;
        pairs.put(second, 0);

        second = 89;
        pairs.put(second, -105);

        second = 121;
        pairs.put(second, -92);

        second = 84;
        pairs.put(second, -111);

        first = 147;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -80);

        second = 145;
        pairs.put(second, 0);

        first = 78;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -35);

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
        pairs.put(second, 0);

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
        pairs.put(second, -18);

        second = 108;
        pairs.put(second, 0);

        second = 113;
        pairs.put(second, 0);

        second = 118;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -40);

        second = 45;
        pairs.put(second, -20);

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
        pairs.put(second, -55);

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
        pairs.put(second, -10);

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
        pairs.put(second, -92);

        second = 87;
        pairs.put(second, -74);

        second = 89;
        pairs.put(second, -100);

        second = 121;
        pairs.put(second, -55);

        second = 84;
        pairs.put(second, -92);

        second = 86;
        pairs.put(second, -100);

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
        pairs.put(second, -110);

        second = 45;
        pairs.put(second, -111);

        second = 105;
        pairs.put(second, -55);

        second = 79;
        pairs.put(second, -30);

        second = 58;
        pairs.put(second, -92);

        second = 97;
        pairs.put(second, -100);

        second = 65;
        pairs.put(second, -120);

        second = 117;
        pairs.put(second, -111);

        second = 46;
        pairs.put(second, -129);

        second = 101;
        pairs.put(second, -100);

        second = 59;
        pairs.put(second, -92);

        second = 44;
        pairs.put(second, -129);

        first = 74;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, 0);

        second = 65;
        pairs.put(second, -60);

        second = 117;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, 0);

        first = 46;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -70);

        second = 146;
        pairs.put(second, -70);

        first = 110;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 117;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, -15);

        second = 118;
        pairs.put(second, -40);

    }

    public TimesRoman() {
        this(false);
    }

    public TimesRoman(boolean enableKerning) {
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
