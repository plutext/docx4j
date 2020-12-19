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

public class TimesItalic extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Times-Italic";
    private static final String fullName = "Times Italic";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 653;
    private static final int xHeight = 441;
    private static final int ascender = 683;
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

        width[0x41] = 611;
        boundingBoxes[0x41] = new Rectangle(-51, 0, 615, 668);
        width[0xc6] = 889;
        boundingBoxes[0xc6] = new Rectangle(-27, 0, 938, 653);
        width[0xc1] = 611;
        boundingBoxes[0xc1] = new Rectangle(-51, 0, 615, 876);
        width[0xc2] = 611;
        boundingBoxes[0xc2] = new Rectangle(-51, 0, 615, 873);
        width[0xc4] = 611;
        boundingBoxes[0xc4] = new Rectangle(-51, 0, 615, 818);
        width[0xc0] = 611;
        boundingBoxes[0xc0] = new Rectangle(-51, 0, 615, 876);
        width[0xc5] = 611;
        boundingBoxes[0xc5] = new Rectangle(-51, 0, 615, 883);
        width[0xc3] = 611;
        boundingBoxes[0xc3] = new Rectangle(-51, 0, 617, 836);
        width[0x42] = 611;
        boundingBoxes[0x42] = new Rectangle(-8, 0, 596, 653);
        width[0x43] = 667;
        boundingBoxes[0x43] = new Rectangle(66, -18, 623, 684);
        width[0xc7] = 667;
        boundingBoxes[0xc7] = new Rectangle(66, -217, 623, 883);
        width[0x44] = 722;
        boundingBoxes[0x44] = new Rectangle(-8, 0, 708, 653);
        width[0x45] = 611;
        boundingBoxes[0x45] = new Rectangle(-1, 0, 635, 653);
        width[0xc9] = 611;
        boundingBoxes[0xc9] = new Rectangle(-1, 0, 635, 876);
        width[0xca] = 611;
        boundingBoxes[0xca] = new Rectangle(-1, 0, 635, 873);
        width[0xcb] = 611;
        boundingBoxes[0xcb] = new Rectangle(-1, 0, 635, 818);
        width[0xc8] = 611;
        boundingBoxes[0xc8] = new Rectangle(-1, 0, 635, 876);
        width[0xd0] = 722;
        boundingBoxes[0xd0] = new Rectangle(-8, 0, 708, 653);
        width[0x80] = 500;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 611;
        boundingBoxes[0x46] = new Rectangle(8, 0, 637, 653);
        width[0x47] = 722;
        boundingBoxes[0x47] = new Rectangle(52, -18, 670, 684);
        width[0x48] = 722;
        boundingBoxes[0x48] = new Rectangle(-8, 0, 775, 653);
        width[0x49] = 333;
        boundingBoxes[0x49] = new Rectangle(-8, 0, 392, 653);
        width[0xcd] = 333;
        boundingBoxes[0xcd] = new Rectangle(-8, 0, 441, 876);
        width[0xce] = 333;
        boundingBoxes[0xce] = new Rectangle(-8, 0, 433, 873);
        width[0xcf] = 333;
        boundingBoxes[0xcf] = new Rectangle(-8, 0, 443, 818);
        width[0xcc] = 333;
        boundingBoxes[0xcc] = new Rectangle(-8, 0, 392, 876);
        width[0x4a] = 444;
        boundingBoxes[0x4a] = new Rectangle(-6, -18, 497, 671);
        width[0x4b] = 667;
        boundingBoxes[0x4b] = new Rectangle(7, 0, 715, 653);
        width[0x4c] = 556;
        boundingBoxes[0x4c] = new Rectangle(-8, 0, 567, 653);

        width[0x4d] = 833;
        boundingBoxes[0x4d] = new Rectangle(-18, 0, 891, 653);
        width[0x4e] = 667;
        boundingBoxes[0x4e] = new Rectangle(-20, -15, 747, 668);
        width[0xd1] = 667;
        boundingBoxes[0xd1] = new Rectangle(-20, -15, 747, 851);
        width[0x4f] = 722;
        boundingBoxes[0x4f] = new Rectangle(60, -18, 639, 684);
        width[0x8c] = 944;
        boundingBoxes[0x8c] = new Rectangle(49, -8, 915, 674);
        width[0xd3] = 722;
        boundingBoxes[0xd3] = new Rectangle(60, -18, 639, 894);
        width[0xd4] = 722;
        boundingBoxes[0xd4] = new Rectangle(60, -18, 639, 891);
        width[0xd6] = 722;
        boundingBoxes[0xd6] = new Rectangle(60, -18, 639, 836);
        width[0xd2] = 722;
        boundingBoxes[0xd2] = new Rectangle(60, -18, 639, 894);
        width[0xd8] = 722;
        boundingBoxes[0xd8] = new Rectangle(60, -105, 639, 827);
        width[0xd5] = 722;
        boundingBoxes[0xd5] = new Rectangle(60, -18, 639, 854);
        width[0x50] = 611;
        boundingBoxes[0x50] = new Rectangle(0, 0, 605, 653);
        width[0x51] = 722;
        boundingBoxes[0x51] = new Rectangle(59, -182, 640, 848);
        width[0x52] = 611;
        boundingBoxes[0x52] = new Rectangle(-13, 0, 601, 653);
        width[0x53] = 500;
        boundingBoxes[0x53] = new Rectangle(17, -18, 491, 685);
        width[0x8a] = 500;
        boundingBoxes[0x8a] = new Rectangle(17, -18, 503, 891);
        width[0x54] = 556;
        boundingBoxes[0x54] = new Rectangle(59, 0, 574, 653);
        width[0xde] = 611;
        boundingBoxes[0xde] = new Rectangle(0, 0, 569, 653);
        width[0x55] = 722;
        boundingBoxes[0x55] = new Rectangle(102, -18, 663, 671);
        width[0xda] = 722;
        boundingBoxes[0xda] = new Rectangle(102, -18, 663, 894);
        width[0xdb] = 722;
        boundingBoxes[0xdb] = new Rectangle(102, -18, 663, 891);
        width[0xdc] = 722;
        boundingBoxes[0xdc] = new Rectangle(102, -18, 663, 836);
        width[0xd9] = 722;
        boundingBoxes[0xd9] = new Rectangle(102, -18, 663, 894);
        width[0x56] = 611;
        boundingBoxes[0x56] = new Rectangle(76, -18, 612, 671);
        width[0x57] = 833;
        boundingBoxes[0x57] = new Rectangle(71, -18, 835, 671);
        width[0x58] = 611;
        boundingBoxes[0x58] = new Rectangle(-29, 0, 684, 653);
        width[0x59] = 556;
        boundingBoxes[0x59] = new Rectangle(78, 0, 555, 653);
        width[0xdd] = 556;
        boundingBoxes[0xdd] = new Rectangle(78, 0, 555, 876);
        width[0x9f] = 556;
        boundingBoxes[0x9f] = new Rectangle(78, 0, 555, 818);
        width[0x5a] = 556;
        boundingBoxes[0x5a] = new Rectangle(-6, 0, 612, 653);
        width[0x8e] = 556;
        boundingBoxes[0x8e] = new Rectangle(-6, 0, 612, 873);
        width[0x61] = 500;
        boundingBoxes[0x61] = new Rectangle(17, -11, 459, 452);
        width[0xe1] = 500;
        boundingBoxes[0xe1] = new Rectangle(17, -11, 470, 675);
        width[0xe2] = 500;
        boundingBoxes[0xe2] = new Rectangle(17, -11, 459, 672);
        width[0xb4] = 333;
        boundingBoxes[0xb4] = new Rectangle(180, 494, 223, 170);
        width[0xe4] = 500;
        boundingBoxes[0xe4] = new Rectangle(17, -11, 472, 617);
        width[0xe6] = 667;
        boundingBoxes[0xe6] = new Rectangle(23, -11, 617, 452);
        width[0xe0] = 500;
        boundingBoxes[0xe0] = new Rectangle(17, -11, 459, 675);
        width[0x26] = 778;
        boundingBoxes[0x26] = new Rectangle(76, -18, 647, 684);
        width[0xe5] = 500;
        boundingBoxes[0xe5] = new Rectangle(17, -11, 459, 702);
        width[0x5e] = 422;
        boundingBoxes[0x5e] = new Rectangle(0, 301, 422, 365);
        width[0x7e] = 541;
        boundingBoxes[0x7e] = new Rectangle(40, 183, 462, 140);
        width[0x2a] = 500;
        boundingBoxes[0x2a] = new Rectangle(128, 255, 364, 411);
        width[0x40] = 920;
        boundingBoxes[0x40] = new Rectangle(118, -18, 688, 684);
        width[0xe3] = 500;
        boundingBoxes[0xe3] = new Rectangle(17, -11, 494, 635);
        width[0x62] = 500;
        boundingBoxes[0x62] = new Rectangle(23, -11, 450, 694);
        width[0x5c] = 278;
        boundingBoxes[0x5c] = new Rectangle(-41, -18, 360, 684);
        width[0x7c] = 275;
        boundingBoxes[0x7c] = new Rectangle(105, -217, 66, 1000);
        width[0x7b] = 400;
        boundingBoxes[0x7b] = new Rectangle(51, -177, 356, 864);
        width[0x7d] = 400;
        boundingBoxes[0x7d] = new Rectangle(-7, -177, 356, 864);
        width[0x5b] = 389;
        boundingBoxes[0x5b] = new Rectangle(21, -153, 370, 816);
        width[0x5d] = 389;
        boundingBoxes[0x5d] = new Rectangle(12, -153, 370, 816);

        width[0xa6] = 275;
        boundingBoxes[0xa6] = new Rectangle(105, -142, 66, 850);
        width[0x95] = 350;
        boundingBoxes[0x95] = new Rectangle(40, 191, 270, 270);
        width[0x63] = 444;
        boundingBoxes[0x63] = new Rectangle(30, -11, 395, 452);

        width[0xe7] = 444;
        boundingBoxes[0xe7] = new Rectangle(30, -217, 395, 658);
        width[0xb8] = 333;
        boundingBoxes[0xb8] = new Rectangle(-30, -217, 212, 217);
        width[0xa2] = 500;
        boundingBoxes[0xa2] = new Rectangle(77, -143, 395, 703);
        width[0x88] = 333;
        boundingBoxes[0x88] = new Rectangle(91, 492, 294, 169);
        width[0x3a] = 333;
        boundingBoxes[0x3a] = new Rectangle(50, -11, 211, 452);
        width[0x2c] = 250;
        boundingBoxes[0x2c] = new Rectangle(-4, -129, 139, 230);
        width[0xa9] = 760;
        boundingBoxes[0xa9] = new Rectangle(41, -18, 678, 684);
        width[0xa4] = 500;
        boundingBoxes[0xa4] = new Rectangle(-22, 53, 544, 544);
        width[0x64] = 500;
        boundingBoxes[0x64] = new Rectangle(15, -13, 512, 696);
        width[0x86] = 500;
        boundingBoxes[0x86] = new Rectangle(101, -159, 387, 825);
        width[0x87] = 500;
        boundingBoxes[0x87] = new Rectangle(22, -143, 469, 809);
        width[0xb0] = 400;
        boundingBoxes[0xb0] = new Rectangle(101, 390, 286, 286);
        width[0xa8] = 333;
        boundingBoxes[0xa8] = new Rectangle(107, 548, 298, 98);
        width[0xf7] = 675;
        boundingBoxes[0xf7] = new Rectangle(86, -11, 504, 528);
        width[0x24] = 500;
        boundingBoxes[0x24] = new Rectangle(31, -89, 466, 820);


        width[0x65] = 444;
        boundingBoxes[0x65] = new Rectangle(31, -11, 381, 452);
        width[0xe9] = 444;
        boundingBoxes[0xe9] = new Rectangle(31, -11, 428, 675);
        width[0xea] = 444;
        boundingBoxes[0xea] = new Rectangle(31, -11, 410, 672);
        width[0xeb] = 444;
        boundingBoxes[0xeb] = new Rectangle(31, -11, 420, 617);
        width[0xe8] = 444;
        boundingBoxes[0xe8] = new Rectangle(31, -11, 381, 675);
        width[0x38] = 500;
        boundingBoxes[0x38] = new Rectangle(30, -7, 463, 683);
        width[0x85] = 889;
        boundingBoxes[0x85] = new Rectangle(57, -11, 705, 111);
        width[0x97] = 889;
        boundingBoxes[0x97] = new Rectangle(-6, 197, 900, 46);
        width[0x96] = 500;
        boundingBoxes[0x96] = new Rectangle(-6, 197, 511, 46);
        width[0x3d] = 675;
        boundingBoxes[0x3d] = new Rectangle(86, 120, 504, 266);
        width[0xf0] = 500;
        boundingBoxes[0xf0] = new Rectangle(27, -11, 455, 694);
        width[0x21] = 333;
        boundingBoxes[0x21] = new Rectangle(39, -11, 263, 678);
        width[0xa1] = 389;
        boundingBoxes[0xa1] = new Rectangle(59, -205, 263, 678);
        width[0x66] = 278;
        boundingBoxes[0x66] = new Rectangle(-147, -207, 571, 885);

        width[0x35] = 500;
        boundingBoxes[0x35] = new Rectangle(15, -7, 476, 673);

        width[0x83] = 500;
        boundingBoxes[0x83] = new Rectangle(25, -182, 482, 864);
        width[0x34] = 500;
        boundingBoxes[0x34] = new Rectangle(1, 0, 478, 676);

        width[0x67] = 500;
        boundingBoxes[0x67] = new Rectangle(8, -206, 464, 647);
        width[0xdf] = 500;
        boundingBoxes[0xdf] = new Rectangle(-168, -207, 661, 886);
        width[0x60] = 333;
        boundingBoxes[0x60] = new Rectangle(121, 492, 190, 172);
        width[0x3e] = 675;
        boundingBoxes[0x3e] = new Rectangle(84, -8, 508, 522);
        width[0xab] = 500;
        boundingBoxes[0xab] = new Rectangle(53, 37, 392, 366);
        width[0xbb] = 500;
        boundingBoxes[0xbb] = new Rectangle(55, 37, 392, 366);
        width[0x8b] = 333;
        boundingBoxes[0x8b] = new Rectangle(51, 37, 230, 366);
        width[0x9b] = 333;
        boundingBoxes[0x9b] = new Rectangle(52, 37, 230, 366);
        width[0x68] = 500;
        boundingBoxes[0x68] = new Rectangle(19, -9, 459, 692);

        width[0x2d] = 333;
        boundingBoxes[0x2d] = new Rectangle(49, 192, 233, 63);
        width[0x69] = 278;
        boundingBoxes[0x69] = new Rectangle(49, -11, 215, 665);
        width[0xed] = 278;
        boundingBoxes[0xed] = new Rectangle(49, -11, 306, 675);
        width[0xee] = 278;
        boundingBoxes[0xee] = new Rectangle(33, -11, 294, 672);
        width[0xef] = 278;
        boundingBoxes[0xef] = new Rectangle(49, -11, 303, 617);
        width[0xec] = 278;
        boundingBoxes[0xec] = new Rectangle(49, -11, 235, 675);
        width[0x6a] = 278;
        boundingBoxes[0x6a] = new Rectangle(-124, -207, 400, 861);
        width[0x6b] = 444;
        boundingBoxes[0x6b] = new Rectangle(14, -11, 447, 694);
        width[0x6c] = 278;
        boundingBoxes[0x6c] = new Rectangle(41, -11, 238, 694);
        width[0x3c] = 675;
        boundingBoxes[0x3c] = new Rectangle(84, -8, 508, 522);
        width[0xac] = 675;
        boundingBoxes[0xac] = new Rectangle(86, 108, 504, 278);

        width[0x6d] = 722;
        boundingBoxes[0x6d] = new Rectangle(12, -9, 692, 450);
        width[0xaf] = 333;
        boundingBoxes[0xaf] = new Rectangle(99, 532, 312, 51);

        width[0xb5] = 500;
        boundingBoxes[0xb5] = new Rectangle(-30, -209, 527, 637);
        width[0xd7] = 675;
        boundingBoxes[0xd7] = new Rectangle(93, 8, 489, 489);
        width[0x6e] = 500;
        boundingBoxes[0x6e] = new Rectangle(14, -9, 460, 450);
        width[0x39] = 500;
        boundingBoxes[0x39] = new Rectangle(23, -17, 469, 693);
        width[0xf1] = 500;
        boundingBoxes[0xf1] = new Rectangle(14, -9, 462, 633);
        width[0x23] = 500;
        boundingBoxes[0x23] = new Rectangle(2, 0, 538, 676);
        width[0x6f] = 500;
        boundingBoxes[0x6f] = new Rectangle(27, -11, 441, 452);
        width[0xf3] = 500;
        boundingBoxes[0xf3] = new Rectangle(27, -11, 460, 675);
        width[0xf4] = 500;
        boundingBoxes[0xf4] = new Rectangle(27, -11, 441, 672);
        width[0xf6] = 500;
        boundingBoxes[0xf6] = new Rectangle(27, -11, 462, 617);
        width[0x9c] = 667;
        boundingBoxes[0x9c] = new Rectangle(20, -12, 626, 453);

        width[0xf2] = 500;
        boundingBoxes[0xf2] = new Rectangle(27, -11, 441, 675);
        width[0x31] = 500;
        boundingBoxes[0x31] = new Rectangle(49, 0, 360, 676);
        width[0xbd] = 750;
        boundingBoxes[0xbd] = new Rectangle(34, -10, 715, 686);
        width[0xbc] = 750;
        boundingBoxes[0xbc] = new Rectangle(33, -10, 703, 686);
        width[0xb9] = 300;
        boundingBoxes[0xb9] = new Rectangle(43, 271, 241, 405);
        width[0xaa] = 276;
        boundingBoxes[0xaa] = new Rectangle(42, 406, 310, 270);
        width[0xba] = 310;
        boundingBoxes[0xba] = new Rectangle(67, 406, 295, 270);
        width[0xf8] = 500;
        boundingBoxes[0xf8] = new Rectangle(28, -135, 441, 689);
        width[0xf5] = 500;
        boundingBoxes[0xf5] = new Rectangle(27, -11, 469, 635);
        width[0x70] = 500;
        boundingBoxes[0x70] = new Rectangle(-75, -205, 544, 646);
        width[0xb6] = 523;
        boundingBoxes[0xb6] = new Rectangle(55, -123, 561, 776);
        width[0x28] = 333;
        boundingBoxes[0x28] = new Rectangle(42, -181, 273, 850);
        width[0x29] = 333;
        boundingBoxes[0x29] = new Rectangle(16, -180, 273, 849);
        width[0x25] = 833;
        boundingBoxes[0x25] = new Rectangle(79, -13, 711, 689);
        width[0x2e] = 250;
        boundingBoxes[0x2e] = new Rectangle(27, -11, 111, 111);
        width[0xb7] = 250;
        boundingBoxes[0xb7] = new Rectangle(70, 199, 111, 111);
        width[0x89] = 1000;
        boundingBoxes[0x89] = new Rectangle(25, -19, 985, 725);
        width[0x2b] = 675;
        boundingBoxes[0x2b] = new Rectangle(86, 0, 504, 506);
        width[0xb1] = 675;
        boundingBoxes[0xb1] = new Rectangle(86, 0, 504, 506);
        width[0x71] = 500;
        boundingBoxes[0x71] = new Rectangle(25, -209, 458, 650);
        width[0x3f] = 500;
        boundingBoxes[0x3f] = new Rectangle(132, -12, 340, 676);
        width[0xbf] = 500;
        boundingBoxes[0xbf] = new Rectangle(28, -205, 340, 676);
        width[0x22] = 420;
        boundingBoxes[0x22] = new Rectangle(144, 421, 288, 245);
        width[0x84] = 556;
        boundingBoxes[0x84] = new Rectangle(57, -129, 348, 230);
        width[0x93] = 556;
        boundingBoxes[0x93] = new Rectangle(166, 436, 348, 230);
        width[0x94] = 556;
        boundingBoxes[0x94] = new Rectangle(151, 436, 348, 230);
        width[0x91] = 333;
        boundingBoxes[0x91] = new Rectangle(171, 436, 139, 230);
        width[0x92] = 333;
        boundingBoxes[0x92] = new Rectangle(151, 436, 139, 230);
        width[0x82] = 333;
        boundingBoxes[0x82] = new Rectangle(44, -129, 139, 230);
        width[0x27] = 214;
        boundingBoxes[0x27] = new Rectangle(132, 421, 109, 245);
        width[0x72] = 389;
        boundingBoxes[0x72] = new Rectangle(45, 0, 367, 441);
        width[0xae] = 760;
        boundingBoxes[0xae] = new Rectangle(41, -18, 678, 684);

        width[0x73] = 389;
        boundingBoxes[0x73] = new Rectangle(16, -13, 350, 455);
        width[0x9a] = 389;
        boundingBoxes[0x9a] = new Rectangle(16, -13, 438, 674);
        width[0xa7] = 500;
        boundingBoxes[0xa7] = new Rectangle(53, -162, 408, 828);
        width[0x3b] = 333;
        boundingBoxes[0x3b] = new Rectangle(27, -129, 234, 570);
        width[0x37] = 500;
        boundingBoxes[0x37] = new Rectangle(75, -8, 462, 674);
        width[0x36] = 500;
        boundingBoxes[0x36] = new Rectangle(30, -7, 491, 693);
        width[0x2f] = 278;
        boundingBoxes[0x2f] = new Rectangle(-65, -18, 451, 684);
        width[0x20] = 250;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 500;
        boundingBoxes[0xa3] = new Rectangle(10, -6, 507, 676);
        width[0x74] = 278;
        boundingBoxes[0x74] = new Rectangle(37, -11, 259, 557);
        width[0xfe] = 500;
        boundingBoxes[0xfe] = new Rectangle(-75, -205, 544, 888);
        width[0x33] = 500;
        boundingBoxes[0x33] = new Rectangle(15, -7, 450, 683);
        width[0xbe] = 750;
        boundingBoxes[0xbe] = new Rectangle(23, -10, 713, 686);
        width[0xb3] = 300;
        boundingBoxes[0xb3] = new Rectangle(43, 268, 296, 408);
        width[0x98] = 333;
        boundingBoxes[0x98] = new Rectangle(100, 517, 327, 107);
        width[0x99] = 980;
        boundingBoxes[0x99] = new Rectangle(30, 247, 927, 406);
        width[0x32] = 500;
        boundingBoxes[0x32] = new Rectangle(12, 0, 440, 676);
        width[0xb2] = 300;
        boundingBoxes[0xb2] = new Rectangle(33, 271, 291, 405);
        width[0x75] = 500;
        boundingBoxes[0x75] = new Rectangle(42, -11, 433, 452);
        width[0xfa] = 500;
        boundingBoxes[0xfa] = new Rectangle(42, -11, 435, 675);
        width[0xfb] = 500;
        boundingBoxes[0xfb] = new Rectangle(42, -11, 433, 672);
        width[0xfc] = 500;
        boundingBoxes[0xfc] = new Rectangle(42, -11, 437, 617);
        width[0xf9] = 500;
        boundingBoxes[0xf9] = new Rectangle(42, -11, 433, 675);
        width[0x5f] = 500;
        boundingBoxes[0x5f] = new Rectangle(0, -125, 500, 50);
        width[0x76] = 444;
        boundingBoxes[0x76] = new Rectangle(21, -18, 405, 459);
        width[0x77] = 667;
        boundingBoxes[0x77] = new Rectangle(16, -18, 632, 459);
        width[0x78] = 444;
        boundingBoxes[0x78] = new Rectangle(-27, -11, 474, 452);
        width[0x79] = 444;
        boundingBoxes[0x79] = new Rectangle(-24, -206, 450, 647);
        width[0xfd] = 444;
        boundingBoxes[0xfd] = new Rectangle(-24, -206, 483, 870);
        width[0xff] = 444;
        boundingBoxes[0xff] = new Rectangle(-24, -206, 465, 812);
        width[0xa5] = 500;
        boundingBoxes[0xa5] = new Rectangle(27, 0, 576, 653);
        width[0x7a] = 389;
        boundingBoxes[0x7a] = new Rectangle(-2, -81, 382, 509);
        width[0x9e] = 389;
        boundingBoxes[0x9e] = new Rectangle(-2, -81, 436, 742);
        width[0x30] = 500;
        boundingBoxes[0x30] = new Rectangle(32, -7, 465, 683);

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
        pairs.put(second, -55);

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
        pairs.put(second, -10);

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
        pairs.put(second, -80);

        second = 97;
        pairs.put(second, -80);

        second = 65;
        pairs.put(second, -90);

        second = 46;
        pairs.put(second, -135);

        second = 101;
        pairs.put(second, -80);

        second = 44;
        pairs.put(second, -135);

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
        pairs.put(second, -65);

        second = 71;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -129);

        second = 59;
        pairs.put(second, -74);

        second = 45;
        pairs.put(second, -55);

        second = 105;
        pairs.put(second, -74);

        second = 65;
        pairs.put(second, -60);

        second = 97;
        pairs.put(second, -111);

        second = 117;
        pairs.put(second, -74);

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
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -74);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -74);

        first = 32;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -18);

        second = 87;
        pairs.put(second, -40);

        second = 147;
        pairs.put(second, 0);

        second = 89;
        pairs.put(second, -75);

        second = 84;
        pairs.put(second, -18);

        second = 145;
        pairs.put(second, 0);

        second = 86;
        pairs.put(second, -35);

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
        pairs.put(second, -10);

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
        pairs.put(second, -105);

        second = 105;
        pairs.put(second, -45);

        second = 114;
        pairs.put(second, -55);

        second = 97;
        pairs.put(second, -75);

        second = 65;
        pairs.put(second, -115);

        second = 46;
        pairs.put(second, -135);

        second = 101;
        pairs.put(second, -75);

        second = 44;
        pairs.put(second, -135);

        first = 85;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 65;
        pairs.put(second, -40);

        second = 46;
        pairs.put(second, -25);

        second = 44;
        pairs.put(second, -25);

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
        pairs.put(second, -35);

        second = 87;
        pairs.put(second, -40);

        second = 89;
        pairs.put(second, -40);

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
        pairs.put(second, -25);

        second = 32;
        pairs.put(second, -111);

        second = 146;
        pairs.put(second, -111);

        second = 114;
        pairs.put(second, -25);

        second = 116;
        pairs.put(second, -30);

        second = 108;
        pairs.put(second, 0);

        second = 115;
        pairs.put(second, -40);

        second = 118;
        pairs.put(second, -10);

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
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, 0);

        second = 104;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -74);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -74);

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
        pairs.put(second, -40);

        second = 121;
        pairs.put(second, -40);

        second = 101;
        pairs.put(second, -35);

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
        pairs.put(second, 0);

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
        pairs.put(second, -111);

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
        pairs.put(second, -10);

        second = 101;
        pairs.put(second, -10);

        second = 44;
        pairs.put(second, -10);

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
        pairs.put(second, 0);

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
        pairs.put(second, -140);

        second = 32;
        pairs.put(second, 0);

        second = 146;
        pairs.put(second, -140);

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
        pairs.put(second, 92);

        second = 97;
        pairs.put(second, 0);

        second = 102;
        pairs.put(second, -18);

        second = 46;
        pairs.put(second, -15);

        second = 108;
        pairs.put(second, 0);

        second = 101;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -10);

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
        pairs.put(second, -55);

        second = 114;
        pairs.put(second, -55);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -74);

        second = 59;
        pairs.put(second, -65);

        second = 45;
        pairs.put(second, -74);

        second = 105;
        pairs.put(second, -55);

        second = 65;
        pairs.put(second, -50);

        second = 97;
        pairs.put(second, -92);

        second = 117;
        pairs.put(second, -55);

        second = 121;
        pairs.put(second, -74);

        second = 46;
        pairs.put(second, -74);

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
        pairs.put(second, -55);

        second = 101;
        pairs.put(second, 0);

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
        pairs.put(second, -15);

        second = 121;
        pairs.put(second, -30);

        second = 112;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -15);

        second = 103;
        pairs.put(second, -40);

        second = 98;
        pairs.put(second, 0);

        second = 120;
        pairs.put(second, -20);

        second = 118;
        pairs.put(second, -15);

        second = 44;
        pairs.put(second, -10);

        first = 99;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 107;
        pairs.put(second, -20);

        second = 104;
        pairs.put(second, -15);

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
        pairs.put(second, -92);

        second = 79;
        pairs.put(second, -25);

        second = 58;
        pairs.put(second, -65);

        second = 104;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -92);

        second = 59;
        pairs.put(second, -65);

        second = 45;
        pairs.put(second, -37);

        second = 105;
        pairs.put(second, -55);

        second = 65;
        pairs.put(second, -60);

        second = 97;
        pairs.put(second, -92);

        second = 117;
        pairs.put(second, -55);

        second = 121;
        pairs.put(second, -70);

        second = 46;
        pairs.put(second, -92);

        second = 101;
        pairs.put(second, -92);

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
        pairs.put(second, -40);

        second = 146;
        pairs.put(second, -37);

        second = 119;
        pairs.put(second, -55);

        second = 87;
        pairs.put(second, -95);

        second = 67;
        pairs.put(second, -30);

        second = 112;
        pairs.put(second, 0);

        second = 81;
        pairs.put(second, -40);

        second = 71;
        pairs.put(second, -35);

        second = 86;
        pairs.put(second, -105);

        second = 118;
        pairs.put(second, -55);

        second = 148;
        pairs.put(second, 0);

        second = 85;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -20);

        second = 89;
        pairs.put(second, -55);

        second = 121;
        pairs.put(second, -55);

        second = 84;
        pairs.put(second, -37);

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
        pairs.put(second, -27);

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
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, -10);

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
        pairs.put(second, -45);

        second = 100;
        pairs.put(second, -37);

        second = 107;
        pairs.put(second, 0);

        second = 114;
        pairs.put(second, 0);

        second = 99;
        pairs.put(second, -37);

        second = 112;
        pairs.put(second, 0);

        second = 103;
        pairs.put(second, -37);

        second = 108;
        pairs.put(second, 0);

        second = 113;
        pairs.put(second, -37);

        second = 118;
        pairs.put(second, 0);

        second = 44;
        pairs.put(second, -111);

        second = 45;
        pairs.put(second, -20);

        second = 105;
        pairs.put(second, 0);

        second = 109;
        pairs.put(second, 0);

        second = 97;
        pairs.put(second, -15);

        second = 117;
        pairs.put(second, 0);

        second = 116;
        pairs.put(second, 0);

        second = 121;
        pairs.put(second, 0);

        second = 46;
        pairs.put(second, -111);

        second = 110;
        pairs.put(second, 0);

        second = 115;
        pairs.put(second, -10);

        second = 101;
        pairs.put(second, -37);

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
        pairs.put(second, -37);

        second = 87;
        pairs.put(second, -55);

        second = 89;
        pairs.put(second, -20);

        second = 121;
        pairs.put(second, -30);

        second = 84;
        pairs.put(second, -20);

        second = 86;
        pairs.put(second, -55);

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
        pairs.put(second, -92);

        second = 45;
        pairs.put(second, -74);

        second = 105;
        pairs.put(second, -74);

        second = 79;
        pairs.put(second, -15);

        second = 58;
        pairs.put(second, -65);

        second = 97;
        pairs.put(second, -92);

        second = 65;
        pairs.put(second, -50);

        second = 117;
        pairs.put(second, -92);

        second = 46;
        pairs.put(second, -92);

        second = 101;
        pairs.put(second, -92);

        second = 59;
        pairs.put(second, -65);

        second = 44;
        pairs.put(second, -92);

        first = 74;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 111;
        pairs.put(second, -25);

        second = 97;
        pairs.put(second, -35);

        second = 65;
        pairs.put(second, -40);

        second = 117;
        pairs.put(second, -35);

        second = 46;
        pairs.put(second, -25);

        second = 101;
        pairs.put(second, -25);

        second = 44;
        pairs.put(second, -25);

        first = 46;
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }

        second = 148;
        pairs.put(second, -140);

        second = 146;
        pairs.put(second, -140);

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

    public TimesItalic() {
        this(false);
    }

    public TimesItalic(boolean enableKerning) {
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
