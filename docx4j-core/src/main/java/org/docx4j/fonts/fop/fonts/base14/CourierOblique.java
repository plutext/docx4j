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

public class CourierOblique extends Base14Font {
    private static final URI fontFileURI;
    private static final String fontName = "Courier-Oblique";
    private static final String fullName = "Courier Oblique";
    private static final Set familyNames;
    private static final int underlinePosition = -100;
    private static final int underlineThickness = 50;
    private static final String encoding = "WinAnsiEncoding";
    private static final int capHeight = 562;
    private static final int xHeight = 426;
    private static final int ascender = 629;
    private static final int descender = -157;
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
        boundingBoxes[0x41] = new Rectangle(3, 0, 604, 562);
        width[0xc6] = 600;
        boundingBoxes[0xc6] = new Rectangle(3, 0, 652, 562);
        width[0xc1] = 600;
        boundingBoxes[0xc1] = new Rectangle(3, 0, 657, 805);
        width[0xc2] = 600;
        boundingBoxes[0xc2] = new Rectangle(3, 0, 604, 787);
        width[0xc4] = 600;
        boundingBoxes[0xc4] = new Rectangle(3, 0, 604, 753);
        width[0xc0] = 600;
        boundingBoxes[0xc0] = new Rectangle(3, 0, 604, 805);
        width[0xc5] = 600;
        boundingBoxes[0xc5] = new Rectangle(3, 0, 604, 750);
        width[0xc3] = 600;
        boundingBoxes[0xc3] = new Rectangle(3, 0, 652, 729);
        width[0x42] = 600;
        boundingBoxes[0x42] = new Rectangle(43, 0, 573, 562);
        width[0x43] = 600;
        boundingBoxes[0x43] = new Rectangle(93, -18, 562, 598);
        width[0xc7] = 600;
        boundingBoxes[0xc7] = new Rectangle(93, -151, 565, 731);
        width[0x44] = 600;
        boundingBoxes[0x44] = new Rectangle(43, 0, 602, 562);
        width[0x45] = 600;
        boundingBoxes[0x45] = new Rectangle(53, 0, 607, 562);
        width[0xc9] = 600;
        boundingBoxes[0xc9] = new Rectangle(53, 0, 617, 805);
        width[0xca] = 600;
        boundingBoxes[0xca] = new Rectangle(53, 0, 607, 787);
        width[0xcb] = 600;
        boundingBoxes[0xcb] = new Rectangle(53, 0, 607, 753);
        width[0xc8] = 600;
        boundingBoxes[0xc8] = new Rectangle(53, 0, 607, 805);
        width[0xd0] = 600;
        boundingBoxes[0xd0] = new Rectangle(43, 0, 602, 562);
        width[0x80] = 600;
        boundingBoxes[0x80] = new Rectangle(0, 0, 0, 0);
        width[0x46] = 600;
        boundingBoxes[0x46] = new Rectangle(53, 0, 607, 562);
        width[0x47] = 600;
        boundingBoxes[0x47] = new Rectangle(83, -18, 562, 598);
        width[0x48] = 600;
        boundingBoxes[0x48] = new Rectangle(32, 0, 655, 562);
        width[0x49] = 600;
        boundingBoxes[0x49] = new Rectangle(96, 0, 527, 562);
        width[0xcd] = 600;
        boundingBoxes[0xcd] = new Rectangle(96, 0, 544, 805);
        width[0xce] = 600;
        boundingBoxes[0xce] = new Rectangle(96, 0, 527, 787);
        width[0xcf] = 600;
        boundingBoxes[0xcf] = new Rectangle(96, 0, 527, 753);
        width[0xcc] = 600;
        boundingBoxes[0xcc] = new Rectangle(96, 0, 527, 805);
        width[0x4a] = 600;
        boundingBoxes[0x4a] = new Rectangle(52, -18, 633, 580);
        width[0x4b] = 600;
        boundingBoxes[0x4b] = new Rectangle(38, 0, 633, 562);
        width[0x4c] = 600;
        boundingBoxes[0x4c] = new Rectangle(47, 0, 560, 562);

        width[0x4d] = 600;
        boundingBoxes[0x4d] = new Rectangle(4, 0, 711, 562);
        width[0x4e] = 600;
        boundingBoxes[0x4e] = new Rectangle(7, -13, 705, 575);
        width[0xd1] = 600;
        boundingBoxes[0xd1] = new Rectangle(7, -13, 705, 742);
        width[0x4f] = 600;
        boundingBoxes[0x4f] = new Rectangle(94, -18, 531, 598);
        width[0x8c] = 600;
        boundingBoxes[0x8c] = new Rectangle(59, 0, 613, 562);
        width[0xd3] = 600;
        boundingBoxes[0xd3] = new Rectangle(94, -18, 546, 823);
        width[0xd4] = 600;
        boundingBoxes[0xd4] = new Rectangle(94, -18, 531, 805);
        width[0xd6] = 600;
        boundingBoxes[0xd6] = new Rectangle(94, -18, 531, 771);
        width[0xd2] = 600;
        boundingBoxes[0xd2] = new Rectangle(94, -18, 531, 823);
        width[0xd8] = 600;
        boundingBoxes[0xd8] = new Rectangle(94, -80, 531, 709);
        width[0xd5] = 600;
        boundingBoxes[0xd5] = new Rectangle(94, -18, 561, 747);
        width[0x50] = 600;
        boundingBoxes[0x50] = new Rectangle(79, 0, 565, 562);
        width[0x51] = 600;
        boundingBoxes[0x51] = new Rectangle(95, -138, 530, 718);
        width[0x52] = 600;
        boundingBoxes[0x52] = new Rectangle(38, 0, 560, 562);
        width[0x53] = 600;
        boundingBoxes[0x53] = new Rectangle(76, -20, 574, 600);
        width[0x8a] = 600;
        boundingBoxes[0x8a] = new Rectangle(76, -20, 596, 822);

        width[0x54] = 600;
        boundingBoxes[0x54] = new Rectangle(108, 0, 557, 562);
        width[0xde] = 600;
        boundingBoxes[0xde] = new Rectangle(79, 0, 527, 562);
        width[0x55] = 600;
        boundingBoxes[0x55] = new Rectangle(125, -18, 577, 580);
        width[0xda] = 600;
        boundingBoxes[0xda] = new Rectangle(125, -18, 577, 823);
        width[0xdb] = 600;
        boundingBoxes[0xdb] = new Rectangle(125, -18, 577, 805);
        width[0xdc] = 600;
        boundingBoxes[0xdc] = new Rectangle(125, -18, 577, 771);
        width[0xd9] = 600;
        boundingBoxes[0xd9] = new Rectangle(125, -18, 577, 823);
        width[0x56] = 600;
        boundingBoxes[0x56] = new Rectangle(105, -13, 618, 575);
        width[0x57] = 600;
        boundingBoxes[0x57] = new Rectangle(106, -13, 616, 575);
        width[0x58] = 600;
        boundingBoxes[0x58] = new Rectangle(23, 0, 652, 562);
        width[0x59] = 600;
        boundingBoxes[0x59] = new Rectangle(133, 0, 562, 562);
        width[0xdd] = 600;
        boundingBoxes[0xdd] = new Rectangle(133, 0, 562, 805);
        width[0x9f] = 600;
        boundingBoxes[0x9f] = new Rectangle(133, 0, 562, 753);
        width[0x5a] = 600;
        boundingBoxes[0x5a] = new Rectangle(86, 0, 524, 562);
        width[0x8e] = 600;
        boundingBoxes[0x8e] = new Rectangle(86, 0, 556, 802);
        width[0x61] = 600;
        boundingBoxes[0x61] = new Rectangle(76, -15, 493, 456);
        width[0xe1] = 600;
        boundingBoxes[0xe1] = new Rectangle(76, -15, 536, 687);
        width[0xe2] = 600;
        boundingBoxes[0xe2] = new Rectangle(76, -15, 505, 669);
        width[0xb4] = 600;
        boundingBoxes[0xb4] = new Rectangle(348, 497, 264, 175);
        width[0xe4] = 600;
        boundingBoxes[0xe4] = new Rectangle(76, -15, 499, 635);
        width[0xe6] = 600;
        boundingBoxes[0xe6] = new Rectangle(41, -15, 585, 456);
        width[0xe0] = 600;
        boundingBoxes[0xe0] = new Rectangle(76, -15, 493, 687);
        width[0x26] = 600;
        boundingBoxes[0x26] = new Rectangle(87, -15, 493, 558);
        width[0xe5] = 600;
        boundingBoxes[0xe5] = new Rectangle(76, -15, 493, 642);
        width[0x5e] = 600;
        boundingBoxes[0x5e] = new Rectangle(175, 354, 412, 268);
        width[0x7e] = 600;
        boundingBoxes[0x7e] = new Rectangle(116, 197, 484, 123);
        width[0x2a] = 600;
        boundingBoxes[0x2a] = new Rectangle(212, 257, 368, 350);
        width[0x40] = 600;
        boundingBoxes[0x40] = new Rectangle(127, -15, 455, 637);
        width[0xe3] = 600;
        boundingBoxes[0xe3] = new Rectangle(76, -15, 553, 621);
        width[0x62] = 600;
        boundingBoxes[0x62] = new Rectangle(29, -15, 596, 644);
        width[0x5c] = 600;
        boundingBoxes[0x5c] = new Rectangle(249, -80, 219, 709);
        width[0x7c] = 600;
        boundingBoxes[0x7c] = new Rectangle(222, -250, 263, 1000);
        width[0x7b] = 600;
        boundingBoxes[0x7b] = new Rectangle(233, -108, 336, 730);
        width[0x7d] = 600;
        boundingBoxes[0x7d] = new Rectangle(140, -108, 337, 730);
        width[0x5b] = 600;
        boundingBoxes[0x5b] = new Rectangle(246, -108, 328, 730);
        width[0x5d] = 600;
        boundingBoxes[0x5d] = new Rectangle(135, -108, 328, 730);

        width[0xa6] = 600;
        boundingBoxes[0xa6] = new Rectangle(238, -175, 231, 850);
        width[0x95] = 600;
        boundingBoxes[0x95] = new Rectangle(224, 130, 261, 253);
        width[0x63] = 600;
        boundingBoxes[0x63] = new Rectangle(106, -15, 502, 456);

        width[0xe7] = 600;
        boundingBoxes[0xe7] = new Rectangle(106, -151, 508, 592);
        width[0xb8] = 600;
        boundingBoxes[0xb8] = new Rectangle(197, -151, 147, 161);
        width[0xa2] = 600;
        boundingBoxes[0xa2] = new Rectangle(151, -49, 437, 663);
        width[0x88] = 600;
        boundingBoxes[0x88] = new Rectangle(229, 477, 352, 177);
        width[0x3a] = 600;
        boundingBoxes[0x3a] = new Rectangle(238, -15, 203, 400);
        width[0x2c] = 600;
        boundingBoxes[0x2c] = new Rectangle(157, -112, 213, 234);
        width[0xa9] = 600;
        boundingBoxes[0xa9] = new Rectangle(53, -18, 614, 598);
        width[0xa4] = 600;
        boundingBoxes[0xa4] = new Rectangle(94, 58, 534, 448);
        width[0x64] = 600;
        boundingBoxes[0x64] = new Rectangle(85, -15, 555, 644);
        width[0x86] = 600;
        boundingBoxes[0x86] = new Rectangle(217, -78, 329, 658);
        width[0x87] = 600;
        boundingBoxes[0x87] = new Rectangle(163, -78, 383, 658);
        width[0xb0] = 600;
        boundingBoxes[0xb0] = new Rectangle(214, 269, 362, 353);
        width[0xa8] = 600;
        boundingBoxes[0xa8] = new Rectangle(272, 537, 307, 103);
        width[0xf7] = 600;
        boundingBoxes[0xf7] = new Rectangle(136, 48, 437, 419);
        width[0x24] = 600;
        boundingBoxes[0x24] = new Rectangle(108, -126, 488, 788);


        width[0x65] = 600;
        boundingBoxes[0x65] = new Rectangle(106, -15, 492, 456);
        width[0xe9] = 600;
        boundingBoxes[0xe9] = new Rectangle(106, -15, 506, 687);
        width[0xea] = 600;
        boundingBoxes[0xea] = new Rectangle(106, -15, 492, 669);
        width[0xeb] = 600;
        boundingBoxes[0xeb] = new Rectangle(106, -15, 492, 635);
        width[0xe8] = 600;
        boundingBoxes[0xe8] = new Rectangle(106, -15, 492, 687);
        width[0x38] = 600;
        boundingBoxes[0x38] = new Rectangle(132, -15, 456, 637);
        width[0x85] = 600;
        boundingBoxes[0x85] = new Rectangle(46, -15, 529, 126);
        width[0x97] = 600;
        boundingBoxes[0x97] = new Rectangle(49, 231, 612, 54);
        width[0x96] = 600;
        boundingBoxes[0x96] = new Rectangle(124, 231, 462, 54);
        width[0x3d] = 600;
        boundingBoxes[0x3d] = new Rectangle(109, 138, 491, 238);
        width[0xf0] = 600;
        boundingBoxes[0xf0] = new Rectangle(102, -15, 537, 644);
        width[0x21] = 600;
        boundingBoxes[0x21] = new Rectangle(243, -15, 221, 587);
        width[0xa1] = 600;
        boundingBoxes[0xa1] = new Rectangle(225, -157, 220, 587);
        width[0x66] = 600;
        boundingBoxes[0x66] = new Rectangle(114, 0, 548, 629);

        width[0x35] = 600;
        boundingBoxes[0x35] = new Rectangle(99, -15, 490, 622);

        width[0x83] = 600;
        boundingBoxes[0x83] = new Rectangle(-26, -143, 697, 765);
        width[0x34] = 600;
        boundingBoxes[0x34] = new Rectangle(108, 0, 433, 622);

        width[0x67] = 600;
        boundingBoxes[0x67] = new Rectangle(61, -157, 596, 598);
        width[0xdf] = 600;
        boundingBoxes[0xdf] = new Rectangle(48, -15, 569, 644);
        width[0x60] = 600;
        boundingBoxes[0x60] = new Rectangle(294, 497, 190, 175);
        width[0x3e] = 600;
        boundingBoxes[0x3e] = new Rectangle(85, 42, 514, 430);
        width[0xab] = 600;
        boundingBoxes[0xab] = new Rectangle(92, 70, 560, 376);
        width[0xbb] = 600;
        boundingBoxes[0xbb] = new Rectangle(58, 70, 560, 376);
        width[0x8b] = 600;
        boundingBoxes[0x8b] = new Rectangle(204, 70, 336, 376);
        width[0x9b] = 600;
        boundingBoxes[0x9b] = new Rectangle(170, 70, 336, 376);
        width[0x68] = 600;
        boundingBoxes[0x68] = new Rectangle(33, 0, 559, 629);

        width[0x2d] = 600;
        boundingBoxes[0x2d] = new Rectangle(152, 231, 406, 54);
        width[0x69] = 600;
        boundingBoxes[0x69] = new Rectangle(95, 0, 420, 657);
        width[0xed] = 600;
        boundingBoxes[0xed] = new Rectangle(95, 0, 517, 672);
        width[0xee] = 600;
        boundingBoxes[0xee] = new Rectangle(95, 0, 456, 654);
        width[0xef] = 600;
        boundingBoxes[0xef] = new Rectangle(95, 0, 450, 620);
        width[0xec] = 600;
        boundingBoxes[0xec] = new Rectangle(95, 0, 420, 672);
        width[0x6a] = 600;
        boundingBoxes[0x6a] = new Rectangle(52, -157, 498, 814);
        width[0x6b] = 600;
        boundingBoxes[0x6b] = new Rectangle(58, 0, 575, 629);
        width[0x6c] = 600;
        boundingBoxes[0x6c] = new Rectangle(95, 0, 420, 629);
        width[0x3c] = 600;
        boundingBoxes[0x3c] = new Rectangle(96, 42, 514, 430);
        width[0xac] = 600;
        boundingBoxes[0xac] = new Rectangle(155, 108, 436, 261);

        width[0x6d] = 600;
        boundingBoxes[0x6d] = new Rectangle(-5, 0, 620, 441);
        width[0xaf] = 600;
        boundingBoxes[0xaf] = new Rectangle(232, 525, 368, 40);

        width[0xb5] = 600;
        boundingBoxes[0xb5] = new Rectangle(72, -157, 500, 583);
        width[0xd7] = 600;
        boundingBoxes[0xd7] = new Rectangle(103, 43, 504, 427);
        width[0x6e] = 600;
        boundingBoxes[0x6e] = new Rectangle(26, 0, 559, 441);
        width[0x39] = 600;
        boundingBoxes[0x39] = new Rectangle(93, -15, 481, 637);
        width[0xf1] = 600;
        boundingBoxes[0xf1] = new Rectangle(26, 0, 603, 606);
        width[0x23] = 600;
        boundingBoxes[0x23] = new Rectangle(133, -32, 463, 671);
        width[0x6f] = 600;
        boundingBoxes[0x6f] = new Rectangle(102, -15, 486, 456);
        width[0xf3] = 600;
        boundingBoxes[0xf3] = new Rectangle(102, -15, 510, 687);
        width[0xf4] = 600;
        boundingBoxes[0xf4] = new Rectangle(102, -15, 486, 669);
        width[0xf6] = 600;
        boundingBoxes[0xf6] = new Rectangle(102, -15, 486, 635);
        width[0x9c] = 600;
        boundingBoxes[0x9c] = new Rectangle(54, -15, 561, 456);

        width[0xf2] = 600;
        boundingBoxes[0xf2] = new Rectangle(102, -15, 486, 687);
        width[0x31] = 600;
        boundingBoxes[0x31] = new Rectangle(98, 0, 417, 622);
        width[0xbd] = 600;
        boundingBoxes[0xbd] = new Rectangle(65, -57, 604, 722);
        width[0xbc] = 600;
        boundingBoxes[0xbc] = new Rectangle(65, -57, 609, 722);
        width[0xb9] = 600;
        boundingBoxes[0xb9] = new Rectangle(231, 249, 260, 373);
        width[0xaa] = 600;
        boundingBoxes[0xaa] = new Rectangle(209, 249, 303, 331);
        width[0xba] = 600;
        boundingBoxes[0xba] = new Rectangle(210, 249, 325, 331);
        width[0xf8] = 600;
        boundingBoxes[0xf8] = new Rectangle(102, -80, 486, 586);
        width[0xf5] = 600;
        boundingBoxes[0xf5] = new Rectangle(102, -15, 527, 621);
        width[0x70] = 600;
        boundingBoxes[0x70] = new Rectangle(-24, -157, 629, 598);
        width[0xb6] = 600;
        boundingBoxes[0xb6] = new Rectangle(100, -78, 530, 640);
        width[0x28] = 600;
        boundingBoxes[0x28] = new Rectangle(313, -108, 259, 730);
        width[0x29] = 600;
        boundingBoxes[0x29] = new Rectangle(137, -108, 259, 730);
        width[0x25] = 600;
        boundingBoxes[0x25] = new Rectangle(134, -15, 465, 637);
        width[0x2e] = 600;
        boundingBoxes[0x2e] = new Rectangle(238, -15, 144, 124);
        width[0xb7] = 600;
        boundingBoxes[0xb7] = new Rectangle(275, 189, 159, 138);
        width[0x89] = 600;
        boundingBoxes[0x89] = new Rectangle(59, -15, 568, 637);
        width[0x2b] = 600;
        boundingBoxes[0x2b] = new Rectangle(129, 44, 451, 426);
        width[0xb1] = 600;
        boundingBoxes[0xb1] = new Rectangle(96, 44, 498, 514);
        width[0x71] = 600;
        boundingBoxes[0x71] = new Rectangle(85, -157, 597, 598);
        width[0x3f] = 600;
        boundingBoxes[0x3f] = new Rectangle(222, -15, 361, 587);
        width[0xbf] = 600;
        boundingBoxes[0xbf] = new Rectangle(105, -157, 361, 587);
        width[0x22] = 600;
        boundingBoxes[0x22] = new Rectangle(273, 328, 259, 234);
        width[0x84] = 600;
        boundingBoxes[0x84] = new Rectangle(115, -134, 363, 234);
        width[0x93] = 600;
        boundingBoxes[0x93] = new Rectangle(262, 328, 279, 234);
        width[0x94] = 600;
        boundingBoxes[0x94] = new Rectangle(213, 328, 363, 234);
        width[0x91] = 600;
        boundingBoxes[0x91] = new Rectangle(343, 328, 114, 234);
        width[0x92] = 600;
        boundingBoxes[0x92] = new Rectangle(283, 328, 212, 234);
        width[0x82] = 600;
        boundingBoxes[0x82] = new Rectangle(185, -134, 212, 234);
        width[0x27] = 600;
        boundingBoxes[0x27] = new Rectangle(345, 328, 115, 234);
        width[0x72] = 600;
        boundingBoxes[0x72] = new Rectangle(60, 0, 576, 441);
        width[0xae] = 600;
        boundingBoxes[0xae] = new Rectangle(53, -18, 614, 598);

        width[0x73] = 600;
        boundingBoxes[0x73] = new Rectangle(78, -15, 506, 456);
        width[0x9a] = 600;
        boundingBoxes[0x9a] = new Rectangle(78, -15, 536, 684);

        width[0xa7] = 600;
        boundingBoxes[0xa7] = new Rectangle(104, -78, 486, 658);
        width[0x3b] = 600;
        boundingBoxes[0x3b] = new Rectangle(157, -112, 284, 497);
        width[0x37] = 600;
        boundingBoxes[0x37] = new Rectangle(182, 0, 430, 607);
        width[0x36] = 600;
        boundingBoxes[0x36] = new Rectangle(155, -15, 474, 637);
        width[0x2f] = 600;
        boundingBoxes[0x2f] = new Rectangle(112, -80, 492, 709);
        width[0x20] = 600;
        boundingBoxes[0x20] = new Rectangle(0, 0, 0, 0);

        width[0xa3] = 600;
        boundingBoxes[0xa3] = new Rectangle(124, -21, 497, 632);
        width[0x74] = 600;
        boundingBoxes[0x74] = new Rectangle(167, -15, 394, 576);
        width[0xfe] = 600;
        boundingBoxes[0xfe] = new Rectangle(-24, -157, 629, 786);
        width[0x33] = 600;
        boundingBoxes[0x33] = new Rectangle(82, -15, 456, 637);
        width[0xbe] = 600;
        boundingBoxes[0xbe] = new Rectangle(73, -56, 586, 722);
        width[0xb3] = 600;
        boundingBoxes[0xb3] = new Rectangle(213, 240, 288, 382);
        width[0x98] = 600;
        boundingBoxes[0x98] = new Rectangle(212, 489, 417, 117);
        width[0x99] = 600;
        boundingBoxes[0x99] = new Rectangle(75, 263, 667, 299);
        width[0x32] = 600;
        boundingBoxes[0x32] = new Rectangle(70, 0, 498, 622);
        width[0xb2] = 600;
        boundingBoxes[0xb2] = new Rectangle(230, 249, 305, 373);
        width[0x75] = 600;
        boundingBoxes[0x75] = new Rectangle(101, -15, 471, 441);
        width[0xfa] = 600;
        boundingBoxes[0xfa] = new Rectangle(101, -15, 501, 687);
        width[0xfb] = 600;
        boundingBoxes[0xfb] = new Rectangle(101, -15, 471, 669);
        width[0xfc] = 600;
        boundingBoxes[0xfc] = new Rectangle(101, -15, 474, 635);
        width[0xf9] = 600;
        boundingBoxes[0xf9] = new Rectangle(101, -15, 471, 687);
        width[0x5f] = 600;
        boundingBoxes[0x5f] = new Rectangle(-27, -125, 611, 50);
        width[0x76] = 600;
        boundingBoxes[0x76] = new Rectangle(90, -10, 591, 436);
        width[0x77] = 600;
        boundingBoxes[0x77] = new Rectangle(76, -10, 619, 436);
        width[0x78] = 600;
        boundingBoxes[0x78] = new Rectangle(20, 0, 635, 426);
        width[0x79] = 600;
        boundingBoxes[0x79] = new Rectangle(-4, -157, 687, 583);
        width[0xfd] = 600;
        boundingBoxes[0xfd] = new Rectangle(-4, -157, 687, 829);
        width[0xff] = 600;
        boundingBoxes[0xff] = new Rectangle(-4, -157, 687, 777);
        width[0xa5] = 600;
        boundingBoxes[0xa5] = new Rectangle(120, 0, 573, 562);
        width[0x7a] = 600;
        boundingBoxes[0x7a] = new Rectangle(99, 0, 494, 426);
        width[0x9e] = 600;
        boundingBoxes[0x9e] = new Rectangle(99, 0, 525, 669);
        width[0x30] = 600;
        boundingBoxes[0x30] = new Rectangle(154, -15, 421, 637);

        familyNames = new java.util.HashSet();
        familyNames.add("Courier");

    }

    public CourierOblique() {
        this(false);
    }

    public CourierOblique(boolean enableKerning) {
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
