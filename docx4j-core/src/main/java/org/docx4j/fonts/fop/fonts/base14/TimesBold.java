/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 
package org.docx4j.fonts.fop.fonts.base14;


import java.util.Map;

import java.util.Set;

import org.docx4j.fonts.fop.fonts.Base14Font;
import org.docx4j.fonts.fop.fonts.CodePointMapping;
import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.Typeface;

public class TimesBold extends Base14Font {
    private final static String fontName = "Times-Bold";
    private final static String fullName = "Times Bold";
    private final static Set familyNames;
    private final static String encoding = "WinAnsiEncoding";
    private final static int capHeight = 676;
    private final static int xHeight = 461;
    private final static int ascender = 676;
    private final static int descender = -205;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("WinAnsiEncoding");

    private final static Map kerning;


    private boolean enableKerning = false;

    static {
        width = new int[256];
        
              width[0x41] = 722;
              width[0xc6] = 1000;
              width[0xc1] = 722;
              width[0xc2] = 722;
              width[0xc4] = 722;
              width[0xc0] = 722;
              width[0xc5] = 722;
              width[0xc3] = 722;
              width[0x42] = 667;
              width[0x43] = 722;
              width[0xc7] = 722;
              width[0x44] = 722;
              width[0x45] = 667;
              width[0xc9] = 667;
              width[0xca] = 667;
              width[0xcb] = 667;
              width[0xc8] = 667;
              width[0xd0] = 722;
              width[0x80] = 500;
              width[0x46] = 611;
              width[0x47] = 778;
              width[0x48] = 778;
              width[0x49] = 389;
              width[0xcd] = 389;
              width[0xce] = 389;
              width[0xcf] = 389;
              width[0xcc] = 389;
              width[0x4a] = 500;
              width[0x4b] = 778;
              width[0x4c] = 667;
      
              width[0x4d] = 944;
              width[0x4e] = 722;
              width[0xd1] = 722;
              width[0x4f] = 778;
              width[0x8c] = 1000;
              width[0xd3] = 778;
              width[0xd4] = 778;
              width[0xd6] = 778;
              width[0xd2] = 778;
              width[0xd8] = 778;
              width[0xd5] = 778;
              width[0x50] = 611;
              width[0x51] = 778;
              width[0x52] = 722;
              width[0x53] = 556;
              width[0x8a] = 556;
              width[0x54] = 667;
              width[0xde] = 611;
              width[0x55] = 722;
              width[0xda] = 722;
              width[0xdb] = 722;
              width[0xdc] = 722;
              width[0xd9] = 722;
              width[0x56] = 722;
              width[0x57] = 1000;
              width[0x58] = 722;
              width[0x59] = 722;
              width[0xdd] = 722;
              width[0x9f] = 722;
              width[0x5a] = 667;
              width[0x8e] = 667;
              width[0x61] = 500;
              width[0xe1] = 500;
              width[0xe2] = 500;
              width[0xb4] = 333;
              width[0xe4] = 500;
              width[0xe6] = 722;
              width[0xe0] = 500;
              width[0x26] = 833;
              width[0xe5] = 500;
              width[0x5e] = 581;
              width[0x7e] = 520;
              width[0x2a] = 500;
              width[0x40] = 930;
              width[0xe3] = 500;
              width[0x62] = 556;
              width[0x5c] = 278;
              width[0x7c] = 220;
              width[0x7b] = 394;
              width[0x7d] = 394;
              width[0x5b] = 333;
              width[0x5d] = 333;
      
              width[0xa6] = 220;
              width[0x95] = 350;
              width[0x63] = 444;
      
              width[0xe7] = 444;
              width[0xb8] = 333;
              width[0xa2] = 500;
              width[0x88] = 333;
              width[0x3a] = 333;
              width[0x2c] = 250;
              width[0xa9] = 747;
              width[0xa4] = 500;
              width[0x64] = 556;
              width[0x86] = 500;
              width[0x87] = 500;
              width[0xb0] = 400;
              width[0xa8] = 333;
              width[0xf7] = 570;
              width[0x24] = 500;
      
      
              width[0x65] = 444;
              width[0xe9] = 444;
              width[0xea] = 444;
              width[0xeb] = 444;
              width[0xe8] = 444;
              width[0x38] = 500;
              width[0x85] = 1000;
              width[0x97] = 1000;
              width[0x96] = 500;
              width[0x3d] = 570;
              width[0xf0] = 500;
              width[0x21] = 333;
              width[0xa1] = 333;
              width[0x66] = 333;
      
              width[0x35] = 500;
      
              width[0x83] = 500;
              width[0x34] = 500;
      
              width[0x67] = 500;
              width[0xdf] = 556;
              width[0x60] = 333;
              width[0x3e] = 570;
              width[0xab] = 500;
              width[0xbb] = 500;
              width[0x8b] = 333;
              width[0x9b] = 333;
              width[0x68] = 556;
      
              width[0x2d] = 333;
              width[0x69] = 278;
              width[0xed] = 278;
              width[0xee] = 278;
              width[0xef] = 278;
              width[0xec] = 278;
              width[0x6a] = 333;
              width[0x6b] = 556;
              width[0x6c] = 278;
              width[0x3c] = 570;
              width[0xac] = 570;
      
              width[0x6d] = 833;
              width[0xaf] = 333;
      
              width[0xb5] = 556;
              width[0xd7] = 570;
              width[0x6e] = 556;
              width[0x39] = 500;
              width[0xf1] = 556;
              width[0x23] = 500;
              width[0x6f] = 500;
              width[0xf3] = 500;
              width[0xf4] = 500;
              width[0xf6] = 500;
              width[0x9c] = 722;
      
              width[0xf2] = 500;
              width[0x31] = 500;
              width[0xbd] = 750;
              width[0xbc] = 750;
              width[0xb9] = 300;
              width[0xaa] = 300;
              width[0xba] = 330;
              width[0xf8] = 500;
              width[0xf5] = 500;
              width[0x70] = 556;
              width[0xb6] = 540;
              width[0x28] = 333;
              width[0x29] = 333;
              width[0x25] = 1000;
              width[0x2e] = 250;
              width[0xb7] = 250;
              width[0x89] = 1000;
              width[0x2b] = 570;
              width[0xb1] = 570;
              width[0x71] = 556;
              width[0x3f] = 500;
              width[0xbf] = 500;
              width[0x22] = 555;
              width[0x84] = 500;
              width[0x93] = 500;
              width[0x94] = 500;
              width[0x91] = 333;
              width[0x92] = 333;
              width[0x82] = 333;
              width[0x27] = 278;
              width[0x72] = 444;
              width[0xae] = 747;
      
              width[0x73] = 389;
              width[0x9a] = 389;
              width[0xa7] = 500;
              width[0x3b] = 333;
              width[0x37] = 500;
              width[0x36] = 500;
              width[0x2f] = 278;
              width[0x20] = 250;
      
      
              width[0xa3] = 500;
              width[0x74] = 333;
              width[0xfe] = 556;
              width[0x33] = 500;
              width[0xbe] = 750;
              width[0xb3] = 300;
              width[0x98] = 333;
              width[0x99] = 1000;
              width[0x32] = 500;
              width[0xb2] = 300;
              width[0x75] = 556;
              width[0xfa] = 556;
              width[0xfb] = 556;
              width[0xfc] = 556;
              width[0xf9] = 556;
              width[0x5f] = 500;
              width[0x76] = 500;
              width[0x77] = 722;
              width[0x78] = 500;
              width[0x79] = 500;
              width[0xfd] = 500;
              width[0xff] = 500;
              width[0xa5] = 500;
              width[0x7a] = 444;
              width[0x9e] = 444;
              width[0x30] = 500;
   
        kerning = new java.util.HashMap();
        Integer first, second;
        Map pairs;
        
        first = new Integer(79);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(87);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(89);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(84);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(86);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(88);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(107);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(121);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-10));
  
        first = new Integer(112);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        first = new Integer(80);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-110));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-92));
  
        first = new Integer(86);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-100));
  
        second = new Integer(79);
        pairs.put(second, new Integer(-45));
  
        second = new Integer(58);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(71);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-129));
  
        second = new Integer(59);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(45);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(105);
        pairs.put(second, new Integer(-37));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-135));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-145));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-100));
  
        first = new Integer(118);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-70));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-55));
  
        first = new Integer(32);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(87);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(147);
        pairs.put(second, new Integer(0));
  
        second = new Integer(89);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(84);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(145);
        pairs.put(second, new Integer(0));
  
        second = new Integer(86);
        pairs.put(second, new Integer(-45));
  
        first = new Integer(97);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(119);
        pairs.put(second, new Integer(0));
  
        second = new Integer(116);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(112);
        pairs.put(second, new Integer(0));
  
        second = new Integer(103);
        pairs.put(second, new Integer(0));
  
        second = new Integer(98);
        pairs.put(second, new Integer(0));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-25));
  
        first = new Integer(70);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(105);
        pairs.put(second, new Integer(0));
  
        second = new Integer(114);
        pairs.put(second, new Integer(0));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-90));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-110));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-92));
  
        first = new Integer(85);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-60));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-50));
  
        first = new Integer(100);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(100);
        pairs.put(second, new Integer(0));
  
        second = new Integer(119);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(118);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(83);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(122);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(0));
  
        second = new Integer(101);
        pairs.put(second, new Integer(0));
  
        first = new Integer(68);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-35));
  
        second = new Integer(87);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(89);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(86);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(146);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(148);
        pairs.put(second, new Integer(0));
  
        second = new Integer(100);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(32);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(146);
        pairs.put(second, new Integer(-63));
  
        second = new Integer(114);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(116);
        pairs.put(second, new Integer(0));
  
        second = new Integer(108);
        pairs.put(second, new Integer(0));
  
        second = new Integer(115);
        pairs.put(second, new Integer(-37));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-20));
  
        first = new Integer(58);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(32);
        pairs.put(second, new Integer(0));
  
        first = new Integer(119);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(97);
        pairs.put(second, new Integer(0));
  
        second = new Integer(104);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-70));
  
        second = new Integer(101);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-55));
  
        first = new Integer(75);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(79);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(121);
        pairs.put(second, new Integer(-45));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-25));
  
        first = new Integer(82);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(79);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(87);
        pairs.put(second, new Integer(-35));
  
        second = new Integer(85);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(89);
        pairs.put(second, new Integer(-35));
  
        second = new Integer(84);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(86);
        pairs.put(second, new Integer(-55));
  
        first = new Integer(145);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(145);
        pairs.put(second, new Integer(-63));
  
        first = new Integer(103);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(0));
  
        second = new Integer(105);
        pairs.put(second, new Integer(0));
  
        second = new Integer(114);
        pairs.put(second, new Integer(0));
  
        second = new Integer(97);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(103);
        pairs.put(second, new Integer(0));
  
        second = new Integer(101);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(66);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(85);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(98);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(117);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-40));
  
        second = new Integer(108);
        pairs.put(second, new Integer(0));
  
        second = new Integer(98);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(81);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(85);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(44);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(148);
        pairs.put(second, new Integer(-45));
  
        second = new Integer(32);
        pairs.put(second, new Integer(0));
  
        second = new Integer(146);
        pairs.put(second, new Integer(-55));
  
        first = new Integer(102);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(148);
        pairs.put(second, new Integer(50));
  
        second = new Integer(111);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(105);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(146);
        pairs.put(second, new Integer(55));
  
        second = new Integer(97);
        pairs.put(second, new Integer(0));
  
        second = new Integer(102);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(108);
        pairs.put(second, new Integer(0));
  
        second = new Integer(101);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-15));
  
        first = new Integer(84);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(79);
        pairs.put(second, new Integer(-18));
  
        second = new Integer(119);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(58);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(114);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(104);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(59);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(45);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(105);
        pairs.put(second, new Integer(-18));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-90));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(121);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-90));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-92));
  
        first = new Integer(121);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(97);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-70));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-55));
  
        first = new Integer(120);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(101);
        pairs.put(second, new Integer(0));
  
        first = new Integer(101);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(119);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(112);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(103);
        pairs.put(second, new Integer(0));
  
        second = new Integer(98);
        pairs.put(second, new Integer(0));
  
        second = new Integer(120);
        pairs.put(second, new Integer(0));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(99);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(107);
        pairs.put(second, new Integer(0));
  
        second = new Integer(104);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(108);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(87);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-75));
  
        second = new Integer(79);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(58);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(104);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(59);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(45);
        pairs.put(second, new Integer(-37));
  
        second = new Integer(105);
        pairs.put(second, new Integer(-18));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-120));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-65));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(121);
        pairs.put(second, new Integer(-60));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-65));
  
        first = new Integer(104);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(121);
        pairs.put(second, new Integer(-15));
  
        first = new Integer(71);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(105);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(118);
        pairs.put(second, new Integer(-10));
  
        first = new Integer(65);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(79);
        pairs.put(second, new Integer(-45));
  
        second = new Integer(146);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(119);
        pairs.put(second, new Integer(-90));
  
        second = new Integer(87);
        pairs.put(second, new Integer(-130));
  
        second = new Integer(67);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(112);
        pairs.put(second, new Integer(-25));
  
        second = new Integer(81);
        pairs.put(second, new Integer(-45));
  
        second = new Integer(71);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(86);
        pairs.put(second, new Integer(-145));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-100));
  
        second = new Integer(148);
        pairs.put(second, new Integer(0));
  
        second = new Integer(85);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-50));
  
        second = new Integer(89);
        pairs.put(second, new Integer(-100));
  
        second = new Integer(121);
        pairs.put(second, new Integer(-74));
  
        second = new Integer(84);
        pairs.put(second, new Integer(-95));
  
        first = new Integer(147);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(145);
        pairs.put(second, new Integer(0));
  
        first = new Integer(78);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(65);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(46);
        pairs.put(second, new Integer(0));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(115);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(119);
        pairs.put(second, new Integer(0));
  
        first = new Integer(111);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(119);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(103);
        pairs.put(second, new Integer(0));
  
        second = new Integer(120);
        pairs.put(second, new Integer(0));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-10));
  
        first = new Integer(114);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-18));
  
        second = new Integer(100);
        pairs.put(second, new Integer(0));
  
        second = new Integer(107);
        pairs.put(second, new Integer(0));
  
        second = new Integer(114);
        pairs.put(second, new Integer(0));
  
        second = new Integer(99);
        pairs.put(second, new Integer(-18));
  
        second = new Integer(112);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(103);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(108);
        pairs.put(second, new Integer(0));
  
        second = new Integer(113);
        pairs.put(second, new Integer(-18));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-10));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(45);
        pairs.put(second, new Integer(-37));
  
        second = new Integer(105);
        pairs.put(second, new Integer(0));
  
        second = new Integer(109);
        pairs.put(second, new Integer(0));
  
        second = new Integer(97);
        pairs.put(second, new Integer(0));
  
        second = new Integer(117);
        pairs.put(second, new Integer(0));
  
        second = new Integer(116);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-100));
  
        second = new Integer(110);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(115);
        pairs.put(second, new Integer(0));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-18));
  
        first = new Integer(108);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(119);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        first = new Integer(76);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(148);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(146);
        pairs.put(second, new Integer(-110));
  
        second = new Integer(87);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(89);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(121);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(84);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(86);
        pairs.put(second, new Integer(-92));
  
        first = new Integer(148);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(32);
        pairs.put(second, new Integer(0));
  
        first = new Integer(109);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(117);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        first = new Integer(89);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-111));
  
        second = new Integer(45);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(105);
        pairs.put(second, new Integer(-37));
  
        second = new Integer(79);
        pairs.put(second, new Integer(-35));
  
        second = new Integer(58);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-85));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-110));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-111));
  
        second = new Integer(59);
        pairs.put(second, new Integer(-92));
  
        second = new Integer(44);
        pairs.put(second, new Integer(-92));
  
        first = new Integer(74);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(111);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(97);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(65);
        pairs.put(second, new Integer(-30));
  
        second = new Integer(117);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(46);
        pairs.put(second, new Integer(-20));
  
        second = new Integer(101);
        pairs.put(second, new Integer(-15));
  
        second = new Integer(44);
        pairs.put(second, new Integer(0));
  
        first = new Integer(46);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(148);
        pairs.put(second, new Integer(-55));
  
        second = new Integer(146);
        pairs.put(second, new Integer(-55));
  
        first = new Integer(110);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        
        second = new Integer(117);
        pairs.put(second, new Integer(0));
  
        second = new Integer(121);
        pairs.put(second, new Integer(0));
  
        second = new Integer(118);
        pairs.put(second, new Integer(-40));
  
        familyNames = new java.util.HashSet();
        familyNames.add("Times");
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

    public int getFirstChar() {
        return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }

    public int getWidth(int i,int size) {
        return size * width[i];
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
  