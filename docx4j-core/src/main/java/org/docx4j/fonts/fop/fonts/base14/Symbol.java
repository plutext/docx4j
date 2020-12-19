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

public class Symbol extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Symbol";
    private static final String fullName = "Symbol";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "SymbolEncoding";
    private static final int capHeight = 1010;
    private static final int xHeight = 520;
    private static final int ascender = 1010;
    private static final int descender = -293;
    private static final int firstChar = 32;
    private static final int lastChar = 255;
    private static final int[] width;
    private static final Rectangle[] boundingBoxes;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("SymbolEncoding");


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

        width[0x20] = 250;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);
        width[0x21] = 333;
        boundingBoxes[0x21] = new Rectangle(128, -17, 112, 689);
        width[0x22] = 713;
        boundingBoxes[0x22] = new Rectangle(31, 0, 650, 705);
        width[0x23] = 500;
        boundingBoxes[0x23] = new Rectangle(20, -16, 461, 689);
        width[0x24] = 549;
        boundingBoxes[0x24] = new Rectangle(25, 0, 453, 707);
        width[0x25] = 833;
        boundingBoxes[0x25] = new Rectangle(63, -36, 708, 691);
        width[0x26] = 778;
        boundingBoxes[0x26] = new Rectangle(41, -18, 709, 679);
        width[0x27] = 439;
        boundingBoxes[0x27] = new Rectangle(48, -17, 366, 517);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(53, -191, 247, 864);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(30, -191, 247, 864);
        width[0x2a] = 500;
        boundingBoxes[0x2a] = new Rectangle(65, 134, 362, 417);
        width[0x2b] = 549;
        boundingBoxes[0x2b] = new Rectangle(10, 0, 529, 533);
        width[0x2c] = 250;
        boundingBoxes[0x2c] = new Rectangle(56, -152, 138, 256);
        width[0x2d] = 549;
        boundingBoxes[0x2d] = new Rectangle(11, 233, 524, 55);
        width[0x2e] = 250;
        boundingBoxes[0x2e] = new Rectangle(69, -17, 112, 112);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(0, -18, 254, 664);
        width[0x30] = 500;
        boundingBoxes[0x30] = new Rectangle(24, -14, 452, 699);
        width[0x31] = 500;
        boundingBoxes[0x31] = new Rectangle(117, 0, 273, 673);
        width[0x32] = 500;
        boundingBoxes[0x32] = new Rectangle(25, 0, 450, 685);
        width[0x33] = 500;
        boundingBoxes[0x33] = new Rectangle(43, -14, 392, 699);
        width[0x34] = 500;
        boundingBoxes[0x34] = new Rectangle(15, 0, 454, 685);
        width[0x35] = 500;
        boundingBoxes[0x35] = new Rectangle(32, -14, 413, 704);
        width[0x36] = 500;
        boundingBoxes[0x36] = new Rectangle(34, -14, 434, 699);
        width[0x37] = 500;
        boundingBoxes[0x37] = new Rectangle(24, -16, 424, 689);
        width[0x38] = 500;
        boundingBoxes[0x38] = new Rectangle(56, -14, 389, 699);
        width[0x39] = 500;
        boundingBoxes[0x39] = new Rectangle(30, -18, 429, 703);
        width[0x3a] = 278;
        boundingBoxes[0x3a] = new Rectangle(81, -17, 112, 477);
        width[0x3b] = 278;
        boundingBoxes[0x3b] = new Rectangle(83, -152, 138, 612);
        width[0x3c] = 549;
        boundingBoxes[0x3c] = new Rectangle(26, 0, 497, 522);
        width[0x3d] = 549;
        boundingBoxes[0x3d] = new Rectangle(11, 141, 526, 249);
        width[0x3e] = 549;
        boundingBoxes[0x3e] = new Rectangle(26, 0, 497, 522);
        width[0x3f] = 444;
        boundingBoxes[0x3f] = new Rectangle(70, -17, 342, 703);
        width[0x40] = 549;
        boundingBoxes[0x40] = new Rectangle(11, 0, 526, 475);
        width[0x41] = 722;
        boundingBoxes[0x41] = new Rectangle(4, 0, 680, 673);
        width[0x42] = 667;
        boundingBoxes[0x42] = new Rectangle(29, 0, 563, 673);
        width[0x43] = 722;
        boundingBoxes[0x43] = new Rectangle(-9, 0, 713, 673);
        width[0x44] = 612;
        boundingBoxes[0x44] = new Rectangle(6, 0, 602, 688);
        width[0x45] = 611;
        boundingBoxes[0x45] = new Rectangle(32, 0, 585, 673);
        width[0x46] = 763;
        boundingBoxes[0x46] = new Rectangle(26, 0, 715, 673);
        width[0x47] = 603;
        boundingBoxes[0x47] = new Rectangle(24, 0, 585, 673);
        width[0x48] = 722;
        boundingBoxes[0x48] = new Rectangle(39, 0, 690, 673);
        width[0x49] = 333;
        boundingBoxes[0x49] = new Rectangle(32, 0, 284, 673);
        width[0x4a] = 631;
        boundingBoxes[0x4a] = new Rectangle(18, -18, 605, 707);
        width[0x4b] = 722;
        boundingBoxes[0x4b] = new Rectangle(35, 0, 687, 673);
        width[0x4c] = 686;
        boundingBoxes[0x4c] = new Rectangle(6, 0, 674, 688);
        width[0x4d] = 889;
        boundingBoxes[0x4d] = new Rectangle(28, 0, 859, 673);
        width[0x4e] = 722;
        boundingBoxes[0x4e] = new Rectangle(29, -8, 691, 681);
        width[0x4f] = 722;
        boundingBoxes[0x4f] = new Rectangle(41, -17, 674, 702);
        width[0x50] = 768;
        boundingBoxes[0x50] = new Rectangle(25, 0, 720, 673);
        width[0x51] = 741;
        boundingBoxes[0x51] = new Rectangle(41, -17, 674, 702);
        width[0x52] = 556;
        boundingBoxes[0x52] = new Rectangle(28, 0, 535, 673);
        width[0x53] = 592;
        boundingBoxes[0x53] = new Rectangle(5, 0, 584, 673);
        width[0x54] = 611;
        boundingBoxes[0x54] = new Rectangle(33, 0, 574, 673);
        width[0x55] = 690;
        boundingBoxes[0x55] = new Rectangle(-8, 0, 702, 673);
        width[0x56] = 439;
        boundingBoxes[0x56] = new Rectangle(40, -233, 396, 733);
        width[0x57] = 768;
        boundingBoxes[0x57] = new Rectangle(34, 0, 702, 688);
        width[0x58] = 645;
        boundingBoxes[0x58] = new Rectangle(40, 0, 559, 673);
        width[0x59] = 795;
        boundingBoxes[0x59] = new Rectangle(15, 0, 766, 684);
        width[0x5a] = 611;
        boundingBoxes[0x5a] = new Rectangle(44, 0, 592, 673);
        width[0x5b] = 333;
        boundingBoxes[0x5b] = new Rectangle(86, -155, 213, 829);
        width[0x5c] = 863;
        boundingBoxes[0x5c] = new Rectangle(163, 0, 538, 487);
        width[0x5d] = 333;
        boundingBoxes[0x5d] = new Rectangle(33, -155, 213, 829);
        width[0x5e] = 658;
        boundingBoxes[0x5e] = new Rectangle(15, 0, 637, 674);
        width[0x5f] = 500;
        boundingBoxes[0x5f] = new Rectangle(-2, -125, 504, 50);
        width[0x60] = 500;
        boundingBoxes[0x60] = new Rectangle(480, 881, 610, 36);
        width[0x61] = 631;
        boundingBoxes[0x61] = new Rectangle(41, -18, 581, 518);
        width[0x62] = 549;
        boundingBoxes[0x62] = new Rectangle(61, -223, 454, 964);
        width[0x63] = 549;
        boundingBoxes[0x63] = new Rectangle(12, -231, 510, 730);
        width[0x64] = 494;
        boundingBoxes[0x64] = new Rectangle(40, -19, 441, 759);
        width[0x65] = 439;
        boundingBoxes[0x65] = new Rectangle(22, -19, 405, 521);
        width[0x66] = 521;
        boundingBoxes[0x66] = new Rectangle(28, -224, 464, 897);
        width[0x67] = 411;
        boundingBoxes[0x67] = new Rectangle(5, -225, 479, 724);
        width[0x68] = 603;
        boundingBoxes[0x68] = new Rectangle(0, -202, 527, 716);
        width[0x69] = 329;
        boundingBoxes[0x69] = new Rectangle(0, -17, 301, 520);
        width[0x6a] = 603;
        boundingBoxes[0x6a] = new Rectangle(36, -224, 551, 723);
        width[0x6b] = 549;
        boundingBoxes[0x6b] = new Rectangle(33, 0, 525, 501);
        width[0x6c] = 549;
        boundingBoxes[0x6c] = new Rectangle(24, -17, 524, 756);
        width[0x6d] = 576;
        boundingBoxes[0x6d] = new Rectangle(33, -223, 534, 723);
        width[0x6e] = 521;
        boundingBoxes[0x6e] = new Rectangle(-9, -16, 484, 523);
        width[0x6f] = 549;
        boundingBoxes[0x6f] = new Rectangle(35, -19, 466, 518);
        width[0x70] = 549;
        boundingBoxes[0x70] = new Rectangle(10, -19, 520, 506);
        width[0x71] = 521;
        boundingBoxes[0x71] = new Rectangle(43, -17, 442, 707);
        width[0x72] = 549;
        boundingBoxes[0x72] = new Rectangle(50, -230, 440, 729);
        width[0x73] = 603;
        boundingBoxes[0x73] = new Rectangle(30, -21, 558, 521);
        width[0x74] = 439;
        boundingBoxes[0x74] = new Rectangle(10, -19, 408, 519);
        width[0x75] = 576;
        boundingBoxes[0x75] = new Rectangle(7, -18, 528, 525);
        width[0x76] = 713;
        boundingBoxes[0x76] = new Rectangle(12, -18, 659, 601);
        width[0x77] = 686;
        boundingBoxes[0x77] = new Rectangle(42, -17, 642, 517);
        width[0x78] = 493;
        boundingBoxes[0x78] = new Rectangle(27, -224, 442, 990);
        width[0x79] = 686;
        boundingBoxes[0x79] = new Rectangle(12, -228, 689, 728);
        width[0x7a] = 494;
        boundingBoxes[0x7a] = new Rectangle(60, -225, 407, 981);
        width[0x7b] = 480;
        boundingBoxes[0x7b] = new Rectangle(58, -183, 339, 856);
        width[0x7c] = 200;
        boundingBoxes[0x7c] = new Rectangle(65, -293, 70, 1000);
        width[0x7d] = 480;
        boundingBoxes[0x7d] = new Rectangle(79, -183, 339, 856);
        width[0x7e] = 549;
        boundingBoxes[0x7e] = new Rectangle(17, 203, 512, 104);
        width[0xa0] = 750;
        boundingBoxes[0xa0] = new Rectangle(20, -12, 694, 697);
        width[0xa1] = 620;
        boundingBoxes[0xa1] = new Rectangle(-2, 0, 612, 685);
        width[0xa2] = 247;
        boundingBoxes[0xa2] = new Rectangle(27, 459, 201, 276);
        width[0xa3] = 549;
        boundingBoxes[0xa3] = new Rectangle(29, 0, 497, 639);
        width[0xa4] = 167;
        boundingBoxes[0xa4] = new Rectangle(-180, -12, 520, 689);
        width[0xa5] = 713;
        boundingBoxes[0xa5] = new Rectangle(26, 124, 662, 280);
        width[0xa6] = 500;
        boundingBoxes[0xa6] = new Rectangle(2, -193, 492, 879);
        width[0xa7] = 753;
        boundingBoxes[0xa7] = new Rectangle(86, -26, 574, 559);
        width[0xa8] = 753;
        boundingBoxes[0xa8] = new Rectangle(142, -36, 458, 586);
        width[0xa9] = 753;
        boundingBoxes[0xa9] = new Rectangle(117, -33, 514, 565);
        width[0xaa] = 753;
        boundingBoxes[0xaa] = new Rectangle(113, -36, 516, 584);
        width[0xab] = 1042;
        boundingBoxes[0xab] = new Rectangle(24, -15, 1000, 526);
        width[0xac] = 987;
        boundingBoxes[0xac] = new Rectangle(32, -15, 910, 526);
        width[0xad] = 603;
        boundingBoxes[0xad] = new Rectangle(45, 0, 526, 910);
        width[0xae] = 987;
        boundingBoxes[0xae] = new Rectangle(49, -15, 910, 526);
        width[0xaf] = 603;
        boundingBoxes[0xaf] = new Rectangle(45, -22, 526, 910);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(50, 385, 300, 300);
        width[0xb1] = 549;
        boundingBoxes[0xb1] = new Rectangle(10, 0, 529, 645);
        width[0xb2] = 411;
        boundingBoxes[0xb2] = new Rectangle(20, 459, 393, 278);
        width[0xb3] = 549;
        boundingBoxes[0xb3] = new Rectangle(29, 0, 497, 639);
        width[0xb4] = 549;
        boundingBoxes[0xb4] = new Rectangle(17, 8, 516, 516);
        width[0xb5] = 713;
        boundingBoxes[0xb5] = new Rectangle(27, 123, 612, 281);
        width[0xb6] = 494;
        boundingBoxes[0xb6] = new Rectangle(26, -20, 436, 766);
        width[0xb7] = 460;
        boundingBoxes[0xb7] = new Rectangle(50, 113, 360, 360);
        width[0xb8] = 549;
        boundingBoxes[0xb8] = new Rectangle(10, 71, 526, 385);
        width[0xb9] = 549;
        boundingBoxes[0xb9] = new Rectangle(15, -25, 525, 574);
        width[0xba] = 549;
        boundingBoxes[0xba] = new Rectangle(14, 82, 524, 361);
        width[0xbb] = 549;
        boundingBoxes[0xbb] = new Rectangle(14, 135, 513, 259);
        width[0xbc] = 1000;
        boundingBoxes[0xbc] = new Rectangle(111, -17, 778, 112);
        width[0xbd] = 603;
        boundingBoxes[0xbd] = new Rectangle(280, -120, 56, 1130);
        width[0xbe] = 1000;
        boundingBoxes[0xbe] = new Rectangle(-60, 220, 1110, 56);
        width[0xbf] = 658;
        boundingBoxes[0xbf] = new Rectangle(15, -16, 587, 645);
        width[0xc0] = 823;
        boundingBoxes[0xc0] = new Rectangle(175, -18, 486, 676);
        width[0xc1] = 686;
        boundingBoxes[0xc1] = new Rectangle(10, -53, 568, 793);
        width[0xc2] = 795;
        boundingBoxes[0xc2] = new Rectangle(26, -15, 733, 749);
        width[0xc3] = 987;
        boundingBoxes[0xc3] = new Rectangle(159, -211, 711, 784);
        width[0xc4] = 768;
        boundingBoxes[0xc4] = new Rectangle(43, -17, 690, 690);
        width[0xc5] = 768;
        boundingBoxes[0xc5] = new Rectangle(43, -15, 690, 690);
        width[0xc6] = 823;
        boundingBoxes[0xc6] = new Rectangle(39, -24, 742, 743);
        width[0xc7] = 768;
        boundingBoxes[0xc7] = new Rectangle(40, 0, 692, 509);
        width[0xc8] = 768;
        boundingBoxes[0xc8] = new Rectangle(40, -17, 692, 509);
        width[0xc9] = 713;
        boundingBoxes[0xc9] = new Rectangle(20, 0, 653, 470);
        width[0xca] = 713;
        boundingBoxes[0xca] = new Rectangle(20, -125, 653, 595);
        width[0xcb] = 713;
        boundingBoxes[0xcb] = new Rectangle(36, -70, 654, 610);
        width[0xcc] = 713;
        boundingBoxes[0xcc] = new Rectangle(37, 0, 653, 470);
        width[0xcd] = 713;
        boundingBoxes[0xcd] = new Rectangle(37, -125, 653, 595);
        width[0xce] = 713;
        boundingBoxes[0xce] = new Rectangle(45, 0, 460, 468);
        width[0xcf] = 713;
        boundingBoxes[0xcf] = new Rectangle(45, -58, 460, 613);
        width[0xd0] = 768;
        boundingBoxes[0xd0] = new Rectangle(26, 0, 712, 673);
        width[0xd1] = 713;
        boundingBoxes[0xd1] = new Rectangle(36, -19, 645, 737);
        width[0xd2] = 790;
        boundingBoxes[0xd2] = new Rectangle(50, -17, 690, 690);
        width[0xd3] = 790;
        boundingBoxes[0xd3] = new Rectangle(51, -15, 690, 690);
        width[0xd4] = 890;
        boundingBoxes[0xd4] = new Rectangle(18, 293, 837, 380);
        width[0xd5] = 823;
        boundingBoxes[0xd5] = new Rectangle(25, -101, 778, 852);
        width[0xd6] = 549;
        boundingBoxes[0xd6] = new Rectangle(10, -38, 505, 955);
        width[0xd7] = 250;
        boundingBoxes[0xd7] = new Rectangle(69, 210, 100, 100);
        width[0xd8] = 713;
        boundingBoxes[0xd8] = new Rectangle(15, 0, 665, 288);
        width[0xd9] = 603;
        boundingBoxes[0xd9] = new Rectangle(23, 0, 560, 454);
        width[0xda] = 603;
        boundingBoxes[0xda] = new Rectangle(30, 0, 548, 477);
        width[0xdb] = 1042;
        boundingBoxes[0xdb] = new Rectangle(27, -20, 996, 530);
        width[0xdc] = 987;
        boundingBoxes[0xdc] = new Rectangle(30, -15, 909, 528);
        width[0xdd] = 603;
        boundingBoxes[0xdd] = new Rectangle(39, 2, 528, 909);
        width[0xde] = 987;
        boundingBoxes[0xde] = new Rectangle(45, -20, 909, 528);
        width[0xdf] = 603;
        boundingBoxes[0xdf] = new Rectangle(44, -19, 528, 909);
        width[0xe0] = 494;
        boundingBoxes[0xe0] = new Rectangle(18, 0, 448, 745);
        width[0xe1] = 329;
        boundingBoxes[0xe1] = new Rectangle(25, -198, 281, 944);
        width[0xe2] = 790;
        boundingBoxes[0xe2] = new Rectangle(50, -20, 690, 690);
        width[0xe3] = 790;
        boundingBoxes[0xe3] = new Rectangle(49, -15, 690, 690);
        width[0xe4] = 786;
        boundingBoxes[0xe4] = new Rectangle(5, 293, 720, 380);
        width[0xe5] = 713;
        boundingBoxes[0xe5] = new Rectangle(14, -108, 681, 860);
        width[0xe6] = 384;
        boundingBoxes[0xe6] = new Rectangle(24, -293, 412, 1219);
        width[0xe7] = 384;
        boundingBoxes[0xe7] = new Rectangle(24, -85, 84, 1010);
        width[0xe8] = 384;
        boundingBoxes[0xe8] = new Rectangle(24, -293, 412, 1219);
        width[0xe9] = 384;
        boundingBoxes[0xe9] = new Rectangle(0, -80, 349, 1006);
        width[0xea] = 384;
        boundingBoxes[0xea] = new Rectangle(0, -79, 77, 1004);
        width[0xeb] = 384;
        boundingBoxes[0xeb] = new Rectangle(0, -80, 349, 1006);
        width[0xec] = 494;
        boundingBoxes[0xec] = new Rectangle(209, -85, 236, 1010);
        width[0xed] = 494;
        boundingBoxes[0xed] = new Rectangle(20, -85, 264, 1020);
        width[0xee] = 494;
        boundingBoxes[0xee] = new Rectangle(209, -75, 236, 1010);
        width[0xef] = 494;
        boundingBoxes[0xef] = new Rectangle(209, -85, 75, 1020);
        width[0xf1] = 329;
        boundingBoxes[0xf1] = new Rectangle(21, -198, 281, 944);
        width[0xf2] = 274;
        boundingBoxes[0xf2] = new Rectangle(2, -107, 289, 1023);
        width[0xf3] = 686;
        boundingBoxes[0xf3] = new Rectangle(308, -88, 367, 1008);
        width[0xf4] = 686;
        boundingBoxes[0xf4] = new Rectangle(308, -88, 70, 1063);
        width[0xf5] = 686;
        boundingBoxes[0xf5] = new Rectangle(11, -87, 367, 1008);
        width[0xf6] = 384;
        boundingBoxes[0xf6] = new Rectangle(54, -293, 412, 1219);
        width[0xf7] = 384;
        boundingBoxes[0xf7] = new Rectangle(382, -85, 84, 1010);
        width[0xf8] = 384;
        boundingBoxes[0xf8] = new Rectangle(54, -293, 412, 1219);
        width[0xf9] = 384;
        boundingBoxes[0xf9] = new Rectangle(22, -80, 349, 1006);
        width[0xfa] = 384;
        boundingBoxes[0xfa] = new Rectangle(294, -79, 77, 1004);
        width[0xfb] = 384;
        boundingBoxes[0xfb] = new Rectangle(22, -80, 349, 1006);
        width[0xfc] = 494;
        boundingBoxes[0xfc] = new Rectangle(48, -85, 236, 1010);
        width[0xfd] = 494;
        boundingBoxes[0xfd] = new Rectangle(209, -85, 264, 1020);
        width[0xfe] = 494;
        boundingBoxes[0xfe] = new Rectangle(48, -75, 236, 1010);


        familyNames = new java.util.HashSet();
        familyNames.add("Symbol");

    }

    public Symbol() {
        this(false);
    }

    public Symbol(boolean enableKerning) {
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
