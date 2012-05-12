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

public class Symbol extends Base14Font {
    private final static String fontName = "Symbol";
    private final static String fullName = "Symbol";
    private final static Set familyNames;
    private final static String encoding = "SymbolEncoding";
    private final static int capHeight = 1010;
    private final static int xHeight = 520;
    private final static int ascender = 1010;
    private final static int descender = -293;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("SymbolEncoding");


    private boolean enableKerning = false;

    static {
        width = new int[256];
        
            width[0x20] = 250;
            width[0x21] = 333;
            width[0x22] = 713;
            width[0x23] = 500;
            width[0x24] = 549;
            width[0x25] = 833;
            width[0x26] = 778;
            width[0x27] = 439;
            width[0x28] = 333;
            width[0x29] = 333;
            width[0x2a] = 500;
            width[0x2b] = 549;
            width[0x2c] = 250;
            width[0x2d] = 549;
            width[0x2e] = 250;
            width[0x2f] = 278;
            width[0x30] = 500;
            width[0x31] = 500;
            width[0x32] = 500;
            width[0x33] = 500;
            width[0x34] = 500;
            width[0x35] = 500;
            width[0x36] = 500;
            width[0x37] = 500;
            width[0x38] = 500;
            width[0x39] = 500;
            width[0x3a] = 278;
            width[0x3b] = 278;
            width[0x3c] = 549;
            width[0x3d] = 549;
            width[0x3e] = 549;
            width[0x3f] = 444;
            width[0x40] = 549;
            width[0x41] = 722;
            width[0x42] = 667;
            width[0x43] = 722;
            width[0x44] = 612;
            width[0x45] = 611;
            width[0x46] = 763;
            width[0x47] = 603;
            width[0x48] = 722;
            width[0x49] = 333;
            width[0x4a] = 631;
            width[0x4b] = 722;
            width[0x4c] = 686;
            width[0x4d] = 889;
            width[0x4e] = 722;
            width[0x4f] = 722;
            width[0x50] = 768;
            width[0x51] = 741;
            width[0x52] = 556;
            width[0x53] = 592;
            width[0x54] = 611;
            width[0x55] = 690;
            width[0x56] = 439;
            width[0x57] = 768;
            width[0x58] = 645;
            width[0x59] = 795;
            width[0x5a] = 611;
            width[0x5b] = 333;
            width[0x5c] = 863;
            width[0x5d] = 333;
            width[0x5e] = 658;
            width[0x5f] = 500;
            width[0x60] = 500;
            width[0x61] = 631;
            width[0x62] = 549;
            width[0x63] = 549;
            width[0x64] = 494;
            width[0x65] = 439;
            width[0x66] = 521;
            width[0x67] = 411;
            width[0x68] = 603;
            width[0x69] = 329;
            width[0x6a] = 603;
            width[0x6b] = 549;
            width[0x6c] = 549;
    
            width[0x6d] = 576;
            width[0x6e] = 521;
            width[0x6f] = 549;
            width[0x70] = 549;
            width[0x71] = 521;
            width[0x72] = 549;
            width[0x73] = 603;
            width[0x74] = 439;
            width[0x75] = 576;
            width[0x76] = 713;
            width[0x77] = 686;
            width[0x78] = 493;
            width[0x79] = 686;
            width[0x7a] = 494;
            width[0x7b] = 480;
            width[0x7c] = 200;
            width[0x7d] = 480;
            width[0x7e] = 549;
            width[0xa0] = 750;
            width[0xa1] = 620;
            width[0xa2] = 247;
            width[0xa3] = 549;
            width[0xa4] = 167;
            width[0xa5] = 713;
            width[0xa6] = 500;
            width[0xa7] = 753;
            width[0xa8] = 753;
            width[0xa9] = 753;
            width[0xaa] = 753;
            width[0xab] = 1042;
            width[0xac] = 987;
            width[0xad] = 603;
            width[0xae] = 987;
            width[0xaf] = 603;
            width[0xb0] = 400;
            width[0xb1] = 549;
            width[0xb2] = 411;
            width[0xb3] = 549;
            width[0xb4] = 549;
            width[0xb5] = 713;
            width[0xb6] = 494;
            width[0xb7] = 460;
            width[0xb8] = 549;
            width[0xb9] = 549;
            width[0xba] = 549;
            width[0xbb] = 549;
            width[0xbc] = 1000;
            width[0xbd] = 603;
            width[0xbe] = 1000;
            width[0xbf] = 658;
            width[0xc0] = 823;
            width[0xc1] = 686;
            width[0xc2] = 795;
            width[0xc3] = 987;
            width[0xc4] = 768;
            width[0xc5] = 768;
            width[0xc6] = 823;
            width[0xc7] = 768;
            width[0xc8] = 768;
            width[0xc9] = 713;
            width[0xca] = 713;
            width[0xcb] = 713;
            width[0xcc] = 713;
            width[0xcd] = 713;
            width[0xce] = 713;
            width[0xcf] = 713;
            width[0xd0] = 768;
            width[0xd1] = 713;
            width[0xd2] = 790;
            width[0xd3] = 790;
            width[0xd4] = 890;
            width[0xd5] = 823;
            width[0xd6] = 549;
            width[0xd7] = 250;
            width[0xd8] = 713;
            width[0xd9] = 603;
            width[0xda] = 603;
            width[0xdb] = 1042;
            width[0xdc] = 987;
            width[0xdd] = 603;
            width[0xde] = 987;
            width[0xdf] = 603;
            width[0xe0] = 494;
            width[0xe1] = 329;
            width[0xe2] = 790;
            width[0xe3] = 790;
            width[0xe4] = 786;
            width[0xe5] = 713;
            width[0xe6] = 384;
            width[0xe7] = 384;
            width[0xe8] = 384;
            width[0xe9] = 384;
            width[0xea] = 384;
            width[0xeb] = 384;
            width[0xec] = 494;
            width[0xed] = 494;
            width[0xee] = 494;
            width[0xef] = 494;
            width[0xf1] = 329;
            width[0xf2] = 274;
            width[0xf3] = 686;
            width[0xf4] = 686;
            width[0xf5] = 686;
            width[0xf6] = 384;
            width[0xf7] = 384;
            width[0xf8] = 384;
            width[0xf9] = 384;
            width[0xfa] = 384;
            width[0xfb] = 384;
            width[0xfc] = 494;
            width[0xfd] = 494;
            width[0xfe] = 494;
    
  
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
  