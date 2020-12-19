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

public class ZapfDingbats extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "ZapfDingbats";
    private static final String fullName = "ITC Zapf Dingbats";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "ZapfDingbatsEncoding";
    private static final int capHeight = 820;
    private static final int xHeight = 426;
    private static final int ascender = 820;
    private static final int descender = -143;
    private static final int firstChar = 32;
    private static final int lastChar = 255;
    private static final int[] width;
    private static final Rectangle[] boundingBoxes;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("ZapfDingbatsEncoding");


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

        width[0x20] = 278;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);
        width[0x21] = 974;
        boundingBoxes[0x21] = new Rectangle(35, 72, 904, 549);
        width[0x22] = 961;
        boundingBoxes[0x22] = new Rectangle(35, 81, 892, 530);
        width[0x23] = 974;
        boundingBoxes[0x23] = new Rectangle(35, 72, 904, 549);
        width[0x24] = 980;
        boundingBoxes[0x24] = new Rectangle(35, 0, 910, 692);
        width[0x25] = 719;
        boundingBoxes[0x25] = new Rectangle(34, 139, 651, 427);
        width[0x26] = 789;
        boundingBoxes[0x26] = new Rectangle(35, -14, 720, 719);
        width[0x27] = 790;
        boundingBoxes[0x27] = new Rectangle(35, -14, 720, 719);
        width[0x28] = 791;
        boundingBoxes[0x28] = new Rectangle(35, -13, 726, 718);
        width[0x29] = 690;
        boundingBoxes[0x29] = new Rectangle(34, 138, 621, 415);
        width[0x2A] = 960;
        boundingBoxes[0x2A] = new Rectangle(35, 123, 890, 445);
        width[0x2B] = 939;
        boundingBoxes[0x2B] = new Rectangle(35, 134, 869, 425);
        width[0x2C] = 549;
        boundingBoxes[0x2C] = new Rectangle(29, -11, 487, 716);
        width[0x2D] = 855;
        boundingBoxes[0x2D] = new Rectangle(34, 59, 786, 573);
        width[0x2E] = 911;
        boundingBoxes[0x2E] = new Rectangle(35, 50, 841, 592);
        width[0x2F] = 933;
        boundingBoxes[0x2F] = new Rectangle(35, 139, 864, 411);
        width[0x30] = 911;
        boundingBoxes[0x30] = new Rectangle(35, 50, 841, 592);
        width[0x31] = 945;
        boundingBoxes[0x31] = new Rectangle(35, 139, 874, 414);
        width[0x32] = 974;
        boundingBoxes[0x32] = new Rectangle(35, 104, 903, 483);
        width[0x33] = 755;
        boundingBoxes[0x33] = new Rectangle(34, -13, 687, 718);
        width[0x34] = 846;
        boundingBoxes[0x34] = new Rectangle(36, -14, 775, 719);
        width[0x35] = 762;
        boundingBoxes[0x35] = new Rectangle(35, 0, 692, 692);
        width[0x36] = 761;
        boundingBoxes[0x36] = new Rectangle(35, 0, 692, 692);
        width[0x37] = 571;
        boundingBoxes[0x37] = new Rectangle(-1, -68, 572, 729);
        width[0x38] = 677;
        boundingBoxes[0x38] = new Rectangle(36, -13, 606, 718);
        width[0x39] = 763;
        boundingBoxes[0x39] = new Rectangle(35, 0, 693, 692);
        width[0x3A] = 760;
        boundingBoxes[0x3A] = new Rectangle(35, 0, 691, 692);
        width[0x3B] = 759;
        boundingBoxes[0x3B] = new Rectangle(35, 0, 690, 692);
        width[0x3C] = 754;
        boundingBoxes[0x3C] = new Rectangle(35, 0, 685, 692);
        width[0x3D] = 494;
        boundingBoxes[0x3D] = new Rectangle(35, 0, 425, 692);
        width[0x3E] = 552;
        boundingBoxes[0x3E] = new Rectangle(35, 0, 482, 692);
        width[0x3F] = 537;
        boundingBoxes[0x3F] = new Rectangle(35, 0, 468, 692);
        width[0x40] = 577;
        boundingBoxes[0x40] = new Rectangle(35, 96, 507, 500);
        width[0x41] = 692;
        boundingBoxes[0x41] = new Rectangle(35, -14, 622, 719);
        width[0x42] = 786;
        boundingBoxes[0x42] = new Rectangle(35, -14, 716, 719);
        width[0x43] = 788;
        boundingBoxes[0x43] = new Rectangle(35, -14, 717, 719);
        width[0x44] = 788;
        boundingBoxes[0x44] = new Rectangle(35, -14, 718, 719);
        width[0x45] = 790;
        boundingBoxes[0x45] = new Rectangle(35, -14, 721, 719);
        width[0x46] = 793;
        boundingBoxes[0x46] = new Rectangle(35, -13, 724, 718);
        width[0x47] = 794;
        boundingBoxes[0x47] = new Rectangle(35, -13, 724, 718);
        width[0x48] = 816;
        boundingBoxes[0x48] = new Rectangle(35, -14, 747, 719);
        width[0x49] = 823;
        boundingBoxes[0x49] = new Rectangle(35, -14, 752, 719);
        width[0x4A] = 789;
        boundingBoxes[0x4A] = new Rectangle(35, -14, 719, 719);
        width[0x4B] = 841;
        boundingBoxes[0x4B] = new Rectangle(35, -14, 772, 719);
        width[0x4C] = 823;
        boundingBoxes[0x4C] = new Rectangle(35, -14, 754, 719);
        width[0x4D] = 833;
        boundingBoxes[0x4D] = new Rectangle(35, -14, 763, 719);
        width[0x4E] = 816;
        boundingBoxes[0x4E] = new Rectangle(35, -13, 747, 718);
        width[0x4F] = 831;
        boundingBoxes[0x4F] = new Rectangle(35, -14, 761, 719);
        width[0x50] = 923;
        boundingBoxes[0x50] = new Rectangle(35, -14, 853, 719);
        width[0x51] = 744;
        boundingBoxes[0x51] = new Rectangle(35, 0, 675, 692);
        width[0x52] = 723;
        boundingBoxes[0x52] = new Rectangle(35, 0, 653, 692);
        width[0x53] = 749;
        boundingBoxes[0x53] = new Rectangle(35, 0, 679, 692);
        width[0x54] = 790;
        boundingBoxes[0x54] = new Rectangle(34, -14, 722, 719);
        width[0x55] = 792;
        boundingBoxes[0x55] = new Rectangle(35, -14, 723, 719);
        width[0x56] = 695;
        boundingBoxes[0x56] = new Rectangle(35, -14, 626, 720);
        width[0x57] = 776;
        boundingBoxes[0x57] = new Rectangle(35, -6, 706, 705);
        width[0x58] = 768;
        boundingBoxes[0x58] = new Rectangle(35, -7, 699, 706);
        width[0x59] = 792;
        boundingBoxes[0x59] = new Rectangle(35, -14, 722, 719);
        width[0x5A] = 759;
        boundingBoxes[0x5A] = new Rectangle(35, 0, 690, 692);
        width[0x5B] = 707;
        boundingBoxes[0x5B] = new Rectangle(35, -13, 637, 717);
        width[0x5C] = 708;
        boundingBoxes[0x5C] = new Rectangle(35, -14, 637, 719);
        width[0x5D] = 682;
        boundingBoxes[0x5D] = new Rectangle(35, -14, 612, 719);
        width[0x5E] = 701;
        boundingBoxes[0x5E] = new Rectangle(35, -14, 631, 719);
        width[0x5F] = 826;
        boundingBoxes[0x5F] = new Rectangle(35, -14, 756, 719);
        width[0x60] = 815;
        boundingBoxes[0x60] = new Rectangle(35, -14, 745, 719);
        width[0x61] = 789;
        boundingBoxes[0x61] = new Rectangle(35, -14, 719, 719);
        width[0x62] = 789;
        boundingBoxes[0x62] = new Rectangle(35, -14, 719, 719);
        width[0x63] = 707;
        boundingBoxes[0x63] = new Rectangle(34, -14, 639, 719);
        width[0x64] = 687;
        boundingBoxes[0x64] = new Rectangle(36, 0, 615, 692);
        width[0x65] = 696;
        boundingBoxes[0x65] = new Rectangle(35, 0, 626, 691);
        width[0x66] = 689;
        boundingBoxes[0x66] = new Rectangle(35, 0, 620, 692);
        width[0x67] = 786;
        boundingBoxes[0x67] = new Rectangle(34, -14, 717, 719);
        width[0x68] = 787;
        boundingBoxes[0x68] = new Rectangle(35, -14, 717, 719);
        width[0x69] = 713;
        boundingBoxes[0x69] = new Rectangle(35, -14, 643, 719);
        width[0x6A] = 791;
        boundingBoxes[0x6A] = new Rectangle(35, -14, 721, 719);
        width[0x6B] = 785;
        boundingBoxes[0x6B] = new Rectangle(36, -14, 715, 719);
        width[0x6C] = 791;
        boundingBoxes[0x6C] = new Rectangle(35, -14, 722, 719);
        width[0x6D] = 873;
        boundingBoxes[0x6D] = new Rectangle(35, -14, 803, 719);
        width[0x6E] = 761;
        boundingBoxes[0x6E] = new Rectangle(35, 0, 691, 692);
        width[0x6F] = 762;
        boundingBoxes[0x6F] = new Rectangle(35, 0, 692, 692);
        width[0x70] = 762;
        boundingBoxes[0x70] = new Rectangle(35, 0, 692, 692);
        width[0x71] = 759;
        boundingBoxes[0x71] = new Rectangle(35, 0, 690, 692);
        width[0x72] = 759;
        boundingBoxes[0x72] = new Rectangle(35, 0, 690, 692);
        width[0x73] = 892;
        boundingBoxes[0x73] = new Rectangle(35, 0, 823, 705);
        width[0x74] = 892;
        boundingBoxes[0x74] = new Rectangle(35, -14, 823, 706);
        width[0x75] = 788;
        boundingBoxes[0x75] = new Rectangle(35, -14, 719, 719);
        width[0x76] = 784;
        boundingBoxes[0x76] = new Rectangle(35, -14, 714, 719);
        width[0x77] = 438;
        boundingBoxes[0x77] = new Rectangle(35, -14, 368, 719);
        width[0x78] = 138;
        boundingBoxes[0x78] = new Rectangle(35, 0, 69, 692);
        width[0x79] = 277;
        boundingBoxes[0x79] = new Rectangle(35, 0, 207, 692);
        width[0x7A] = 415;
        boundingBoxes[0x7A] = new Rectangle(35, 0, 345, 692);
        width[0x7B] = 392;
        boundingBoxes[0x7B] = new Rectangle(35, 263, 322, 442);
        width[0x7C] = 392;
        boundingBoxes[0x7C] = new Rectangle(34, 263, 323, 442);
        width[0x7D] = 668;
        boundingBoxes[0x7D] = new Rectangle(35, 263, 598, 442);
        width[0x7E] = 668;
        boundingBoxes[0x7E] = new Rectangle(36, 263, 598, 442);
        width[0xA1] = 732;
        boundingBoxes[0xA1] = new Rectangle(35, -143, 662, 949);
        width[0xA2] = 544;
        boundingBoxes[0xA2] = new Rectangle(56, -14, 432, 720);
        width[0xA3] = 544;
        boundingBoxes[0xA3] = new Rectangle(34, -14, 474, 719);
        width[0xA4] = 910;
        boundingBoxes[0xA4] = new Rectangle(35, 40, 840, 611);
        width[0xA5] = 667;
        boundingBoxes[0xA5] = new Rectangle(35, -14, 598, 719);
        width[0xA6] = 760;
        boundingBoxes[0xA6] = new Rectangle(35, -14, 691, 719);
        width[0xA7] = 760;
        boundingBoxes[0xA7] = new Rectangle(0, 121, 758, 448);
        width[0xA8] = 776;
        boundingBoxes[0xA8] = new Rectangle(35, 0, 706, 705);
        width[0xA9] = 595;
        boundingBoxes[0xA9] = new Rectangle(34, -14, 526, 719);
        width[0xAA] = 694;
        boundingBoxes[0xAA] = new Rectangle(35, -14, 624, 719);
        width[0xAB] = 626;
        boundingBoxes[0xAB] = new Rectangle(34, 0, 557, 705);
        width[0xAC] = 788;
        boundingBoxes[0xAC] = new Rectangle(35, -14, 719, 719);
        width[0xAD] = 788;
        boundingBoxes[0xAD] = new Rectangle(35, -14, 719, 719);
        width[0xAE] = 788;
        boundingBoxes[0xAE] = new Rectangle(35, -14, 719, 719);
        width[0xAF] = 788;
        boundingBoxes[0xAF] = new Rectangle(35, -14, 719, 719);
        width[0xB0] = 788;
        boundingBoxes[0xB0] = new Rectangle(35, -14, 719, 719);
        width[0xB1] = 788;
        boundingBoxes[0xB1] = new Rectangle(35, -14, 719, 719);
        width[0xB2] = 788;
        boundingBoxes[0xB2] = new Rectangle(35, -14, 719, 719);
        width[0xB3] = 788;
        boundingBoxes[0xB3] = new Rectangle(35, -14, 719, 719);
        width[0xB4] = 788;
        boundingBoxes[0xB4] = new Rectangle(35, -14, 719, 719);
        width[0xB5] = 788;
        boundingBoxes[0xB5] = new Rectangle(35, -14, 719, 719);
        width[0xB6] = 788;
        boundingBoxes[0xB6] = new Rectangle(35, -14, 719, 719);
        width[0xB7] = 788;
        boundingBoxes[0xB7] = new Rectangle(35, -14, 719, 719);
        width[0xB8] = 788;
        boundingBoxes[0xB8] = new Rectangle(35, -14, 719, 719);
        width[0xB9] = 788;
        boundingBoxes[0xB9] = new Rectangle(35, -14, 719, 719);
        width[0xBA] = 788;
        boundingBoxes[0xBA] = new Rectangle(35, -14, 719, 719);
        width[0xBB] = 788;
        boundingBoxes[0xBB] = new Rectangle(35, -14, 719, 719);
        width[0xBC] = 788;
        boundingBoxes[0xBC] = new Rectangle(35, -14, 719, 719);
        width[0xBD] = 788;
        boundingBoxes[0xBD] = new Rectangle(35, -14, 719, 719);
        width[0xBE] = 788;
        boundingBoxes[0xBE] = new Rectangle(35, -14, 719, 719);
        width[0xBF] = 788;
        boundingBoxes[0xBF] = new Rectangle(35, -14, 719, 719);
        width[0xC0] = 788;
        boundingBoxes[0xC0] = new Rectangle(35, -14, 719, 719);
        width[0xC1] = 788;
        boundingBoxes[0xC1] = new Rectangle(35, -14, 719, 719);
        width[0xC2] = 788;
        boundingBoxes[0xC2] = new Rectangle(35, -14, 719, 719);
        width[0xC3] = 788;
        boundingBoxes[0xC3] = new Rectangle(35, -14, 719, 719);
        width[0xC4] = 788;
        boundingBoxes[0xC4] = new Rectangle(35, -14, 719, 719);
        width[0xC5] = 788;
        boundingBoxes[0xC5] = new Rectangle(35, -14, 719, 719);
        width[0xC6] = 788;
        boundingBoxes[0xC6] = new Rectangle(35, -14, 719, 719);
        width[0xC7] = 788;
        boundingBoxes[0xC7] = new Rectangle(35, -14, 719, 719);
        width[0xC8] = 788;
        boundingBoxes[0xC8] = new Rectangle(35, -14, 719, 719);
        width[0xC9] = 788;
        boundingBoxes[0xC9] = new Rectangle(35, -14, 719, 719);
        width[0xCA] = 788;
        boundingBoxes[0xCA] = new Rectangle(35, -14, 719, 719);
        width[0xCB] = 788;
        boundingBoxes[0xCB] = new Rectangle(35, -14, 719, 719);
        width[0xCC] = 788;
        boundingBoxes[0xCC] = new Rectangle(35, -14, 719, 719);
        width[0xCD] = 788;
        boundingBoxes[0xCD] = new Rectangle(35, -14, 719, 719);
        width[0xCE] = 788;
        boundingBoxes[0xCE] = new Rectangle(35, -14, 719, 719);
        width[0xCF] = 788;
        boundingBoxes[0xCF] = new Rectangle(35, -14, 719, 719);
        width[0xD0] = 788;
        boundingBoxes[0xD0] = new Rectangle(35, -14, 719, 719);
        width[0xD1] = 788;
        boundingBoxes[0xD1] = new Rectangle(35, -14, 719, 719);
        width[0xD2] = 788;
        boundingBoxes[0xD2] = new Rectangle(35, -14, 719, 719);
        width[0xD3] = 788;
        boundingBoxes[0xD3] = new Rectangle(35, -14, 719, 719);
        width[0xD4] = 894;
        boundingBoxes[0xD4] = new Rectangle(35, 58, 825, 576);
        width[0xD5] = 838;
        boundingBoxes[0xD5] = new Rectangle(35, 152, 768, 388);
        width[0xD6] = 1016;
        boundingBoxes[0xD6] = new Rectangle(34, 152, 947, 388);
        width[0xD7] = 458;
        boundingBoxes[0xD7] = new Rectangle(35, -127, 387, 947);
        width[0xD8] = 748;
        boundingBoxes[0xD8] = new Rectangle(35, 94, 663, 503);
        width[0xD9] = 924;
        boundingBoxes[0xD9] = new Rectangle(35, 140, 855, 412);
        width[0xDA] = 748;
        boundingBoxes[0xDA] = new Rectangle(35, 94, 663, 503);
        width[0xDB] = 918;
        boundingBoxes[0xDB] = new Rectangle(35, 166, 849, 360);
        width[0xDC] = 927;
        boundingBoxes[0xDC] = new Rectangle(35, 32, 857, 628);
        width[0xDD] = 928;
        boundingBoxes[0xDD] = new Rectangle(35, 129, 856, 433);
        width[0xDE] = 928;
        boundingBoxes[0xDE] = new Rectangle(35, 128, 858, 435);
        width[0xDF] = 834;
        boundingBoxes[0xDF] = new Rectangle(35, 155, 764, 382);
        width[0xE0] = 873;
        boundingBoxes[0xE0] = new Rectangle(35, 93, 803, 506);
        width[0xE1] = 828;
        boundingBoxes[0xE1] = new Rectangle(35, 104, 756, 484);
        width[0xE2] = 924;
        boundingBoxes[0xE2] = new Rectangle(35, 98, 854, 496);
        width[0xE3] = 924;
        boundingBoxes[0xE3] = new Rectangle(35, 98, 854, 496);
        width[0xE4] = 917;
        boundingBoxes[0xE4] = new Rectangle(35, 0, 847, 692);
        width[0xE5] = 930;
        boundingBoxes[0xE5] = new Rectangle(35, 84, 861, 524);
        width[0xE6] = 931;
        boundingBoxes[0xE6] = new Rectangle(35, 84, 861, 524);
        width[0xE7] = 463;
        boundingBoxes[0xE7] = new Rectangle(35, -99, 394, 890);
        width[0xE8] = 883;
        boundingBoxes[0xE8] = new Rectangle(35, 71, 813, 552);
        width[0xE9] = 836;
        boundingBoxes[0xE9] = new Rectangle(35, 44, 767, 604);
        width[0xEA] = 836;
        boundingBoxes[0xEA] = new Rectangle(35, 44, 767, 604);
        width[0xEB] = 867;
        boundingBoxes[0xEB] = new Rectangle(35, 101, 797, 490);
        width[0xEC] = 867;
        boundingBoxes[0xEC] = new Rectangle(35, 101, 797, 490);
        width[0xED] = 696;
        boundingBoxes[0xED] = new Rectangle(35, 44, 626, 604);
        width[0xEE] = 696;
        boundingBoxes[0xEE] = new Rectangle(35, 44, 626, 604);
        width[0xEF] = 874;
        boundingBoxes[0xEF] = new Rectangle(35, 77, 805, 542);
        width[0xF1] = 874;
        boundingBoxes[0xF1] = new Rectangle(35, 73, 805, 542);
        width[0xF2] = 760;
        boundingBoxes[0xF2] = new Rectangle(35, 0, 690, 692);
        width[0xF3] = 946;
        boundingBoxes[0xF3] = new Rectangle(35, 160, 876, 373);
        width[0xF4] = 771;
        boundingBoxes[0xF4] = new Rectangle(34, 37, 702, 618);
        width[0xF5] = 865;
        boundingBoxes[0xF5] = new Rectangle(35, 207, 795, 274);
        width[0xF6] = 771;
        boundingBoxes[0xF6] = new Rectangle(34, 37, 702, 618);
        width[0xF7] = 888;
        boundingBoxes[0xF7] = new Rectangle(34, -19, 819, 731);
        width[0xF8] = 967;
        boundingBoxes[0xF8] = new Rectangle(35, 124, 897, 444);
        width[0xF9] = 888;
        boundingBoxes[0xF9] = new Rectangle(34, -19, 819, 731);
        width[0xFA] = 831;
        boundingBoxes[0xFA] = new Rectangle(35, 113, 761, 466);
        width[0xFB] = 873;
        boundingBoxes[0xFB] = new Rectangle(36, 118, 802, 460);
        width[0xFC] = 927;
        boundingBoxes[0xFC] = new Rectangle(35, 150, 856, 392);
        width[0xFD] = 970;
        boundingBoxes[0xFD] = new Rectangle(35, 76, 896, 540);
        width[0xFE] = 918;
        boundingBoxes[0xFE] = new Rectangle(34, 99, 850, 494);
        width[0x89] = 410;
        boundingBoxes[0x89] = new Rectangle(35, 0, 340, 692);
        width[0x87] = 509;
        boundingBoxes[0x87] = new Rectangle(35, 0, 440, 692);
        width[0x8C] = 334;
        boundingBoxes[0x8C] = new Rectangle(35, 0, 264, 692);
        width[0x86] = 509;
        boundingBoxes[0x86] = new Rectangle(35, 0, 440, 692);
        width[0x80] = 390;
        boundingBoxes[0x80] = new Rectangle(35, -14, 321, 719);
        width[0x8A] = 234;
        boundingBoxes[0x8A] = new Rectangle(35, -14, 164, 719);
        width[0x84] = 276;
        boundingBoxes[0x84] = new Rectangle(35, 0, 207, 692);
        width[0x81] = 390;
        boundingBoxes[0x81] = new Rectangle(35, -14, 320, 719);
        width[0x88] = 410;
        boundingBoxes[0x88] = new Rectangle(35, 0, 340, 692);
        width[0x83] = 317;
        boundingBoxes[0x83] = new Rectangle(35, 0, 248, 692);
        width[0x82] = 317;
        boundingBoxes[0x82] = new Rectangle(35, 0, 248, 692);
        width[0x85] = 276;
        boundingBoxes[0x85] = new Rectangle(35, 0, 207, 692);
        width[0x8D] = 334;
        boundingBoxes[0x8D] = new Rectangle(35, 0, 264, 692);
        width[0x8B] = 234;
        boundingBoxes[0x8B] = new Rectangle(35, -14, 164, 719);

        familyNames = new java.util.HashSet();
        familyNames.add("ZapfDingbats");

    }

    public ZapfDingbats() {
        this(false);
    }

    public ZapfDingbats(boolean enableKerning) {
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
