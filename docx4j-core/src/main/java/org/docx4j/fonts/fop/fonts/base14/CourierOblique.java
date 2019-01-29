/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 
package org.docx4j.fonts.fop.fonts.base14;


import java.util.Set;

import org.docx4j.fonts.fop.fonts.Base14Font;
import org.docx4j.fonts.fop.fonts.CodePointMapping;
import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.Typeface;

public class CourierOblique extends Base14Font {
    private final static String fontName = "Courier-Oblique";
    private final static String fullName = "Courier Oblique";
    private final static Set familyNames;
    private final static String encoding = "WinAnsiEncoding";
    private final static int capHeight = 562;
    private final static int xHeight = 426;
    private final static int ascender = 629;
    private final static int descender = -157;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("WinAnsiEncoding");


    private boolean enableKerning = false;

    static {
        width = new int[256];
        
              width[0x41] = 600;
              width[0xc6] = 600;
              width[0xc1] = 600;
              width[0xc2] = 600;
              width[0xc4] = 600;
              width[0xc0] = 600;
              width[0xc5] = 600;
              width[0xc3] = 600;
              width[0x42] = 600;
              width[0x43] = 600;
              width[0xc7] = 600;
              width[0x44] = 600;
              width[0x45] = 600;
              width[0xc9] = 600;
              width[0xca] = 600;
              width[0xcb] = 600;
              width[0xc8] = 600;
              width[0xd0] = 600;
              width[0x80] = 600;
              width[0x46] = 600;
              width[0x47] = 600;
      
              width[0x48] = 600;
              width[0x49] = 600;
      
              width[0xcd] = 600;
              width[0xce] = 600;
              width[0xcf] = 600;
      
              width[0xcc] = 600;
              width[0x4a] = 600;
              width[0x4b] = 600;
              width[0x4c] = 600;
      
      
              width[0x4d] = 600;
              width[0x4e] = 600;
              width[0xd1] = 600;
              width[0x4f] = 600;
              width[0x8c] = 600;
              width[0xd3] = 600;
              width[0xd4] = 600;
              width[0xd6] = 600;
              width[0xd2] = 600;
              width[0xd8] = 600;
              width[0xd5] = 600;
              width[0x50] = 600;
              width[0x51] = 600;
              width[0x52] = 600;
              width[0x53] = 600;
              width[0x8a] = 600;
      
              width[0x54] = 600;
              width[0xde] = 600;
              width[0x55] = 600;
              width[0xda] = 600;
              width[0xdb] = 600;
              width[0xdc] = 600;
              width[0xd9] = 600;
              width[0x56] = 600;
              width[0x57] = 600;
              width[0x58] = 600;
              width[0x59] = 600;
              width[0xdd] = 600;
              width[0x9f] = 600;
              width[0x5a] = 600;
              width[0x8e] = 600;
              width[0x61] = 600;
              width[0xe1] = 600;
              width[0xe2] = 600;
              width[0xb4] = 600;
              width[0xe4] = 600;
              width[0xe6] = 600;
              width[0xe0] = 600;
              width[0x26] = 600;
              width[0xe5] = 600;
      
      
      
      
      
              width[0x5e] = 600;
              width[0x7e] = 600;
              width[0x2a] = 600;
              width[0x40] = 600;
              width[0xe3] = 600;
              width[0x62] = 600;
              width[0x5c] = 600;
              width[0x7c] = 600;
              width[0x7b] = 600;
              width[0x7d] = 600;
              width[0x5b] = 600;
              width[0x5d] = 600;
      
              width[0xa6] = 600;
              width[0x95] = 600;
              width[0x63] = 600;
      
              width[0xe7] = 600;
              width[0xb8] = 600;
              width[0xa2] = 600;
      
              width[0x88] = 600;
              width[0x3a] = 600;
              width[0x2c] = 600;
              width[0xa9] = 600;
              width[0xa4] = 600;
              width[0x64] = 600;
              width[0x86] = 600;
              width[0x87] = 600;
      
              width[0xb0] = 600;
              width[0xa8] = 600;
              width[0xf7] = 600;
              width[0x24] = 600;
      
      
      
              width[0x65] = 600;
              width[0xe9] = 600;
              width[0xea] = 600;
              width[0xeb] = 600;
              width[0xe8] = 600;
              width[0x38] = 600;
              width[0x85] = 600;
              width[0x97] = 600;
              width[0x96] = 600;
              width[0x3d] = 600;
              width[0xf0] = 600;
              width[0x21] = 600;
              width[0xa1] = 600;
              width[0x66] = 600;
      
              width[0x35] = 600;
      
              width[0x83] = 600;
      
              width[0x34] = 600;
      
              width[0x67] = 600;
      
              width[0xdf] = 600;
              width[0x60] = 600;
      
              width[0x3e] = 600;
              width[0xab] = 600;
              width[0xbb] = 600;
              width[0x8b] = 600;
              width[0x9b] = 600;
              width[0x68] = 600;
      
              width[0x2d] = 600;
              width[0x69] = 600;
              width[0xed] = 600;
              width[0xee] = 600;
              width[0xef] = 600;
              width[0xec] = 600;
      
      
              width[0x6a] = 600;
              width[0x6b] = 600;
              width[0x6c] = 600;
      
      
              width[0x3c] = 600;
      
      
              width[0xac] = 600;
      
              width[0x6d] = 600;
              width[0xaf] = 600;
      
      
              width[0xb5] = 600;
              width[0xd7] = 600;
              width[0x6e] = 600;
              width[0x39] = 600;
      
              width[0xf1] = 600;
              width[0x23] = 600;
              width[0x6f] = 600;
              width[0xf3] = 600;
              width[0xf4] = 600;
              width[0xf6] = 600;
              width[0x9c] = 600;
      
              width[0xf2] = 600;
              width[0x31] = 600;
              width[0xbd] = 600;
              width[0xbc] = 600;
              width[0xb9] = 600;
              width[0xaa] = 600;
              width[0xba] = 600;
              width[0xf8] = 600;
              width[0xf5] = 600;
      
              width[0x70] = 600;
              width[0xb6] = 600;
              width[0x28] = 600;
              width[0x29] = 600;
              width[0x25] = 600;
              width[0x2e] = 600;
              width[0xb7] = 600;
              width[0x89] = 600;
              width[0x2b] = 600;
              width[0xb1] = 600;
      
              width[0x71] = 600;
              width[0x3f] = 600;
              width[0xbf] = 600;
              width[0x22] = 600;
              width[0x84] = 600;
              width[0x93] = 600;
              width[0x94] = 600;
              width[0x91] = 600;
              width[0x92] = 600;
              width[0x82] = 600;
              width[0x27] = 600;
              width[0x72] = 600;
              width[0xae] = 600;
      
      
              width[0x73] = 600;
              width[0x9a] = 600;
      
              width[0xa7] = 600;
              width[0x3b] = 600;
              width[0x37] = 600;
              width[0x36] = 600;
              width[0x2f] = 600;
              width[0x20] = 600;
      
      
      
              width[0xa3] = 600;
      
              width[0x74] = 600;
      
              width[0xfe] = 600;
              width[0x33] = 600;
              width[0xbe] = 600;
              width[0xb3] = 600;
              width[0x98] = 600;
              width[0x99] = 600;
              width[0x32] = 600;
              width[0xb2] = 600;
              width[0x75] = 600;
              width[0xfa] = 600;
              width[0xfb] = 600;
              width[0xfc] = 600;
              width[0xf9] = 600;
              width[0x5f] = 600;
      
              width[0x76] = 600;
              width[0x77] = 600;
              width[0x78] = 600;
              width[0x79] = 600;
              width[0xfd] = 600;
              width[0xff] = 600;
              width[0xa5] = 600;
              width[0x7a] = 600;
              width[0x9e] = 600;
              width[0x30] = 600;
   
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
  