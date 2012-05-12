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

public class ZapfDingbats extends Base14Font {
    private final static String fontName = "ZapfDingbats";
    private final static String fullName = "ITC Zapf Dingbats";
    private final static Set familyNames;
    private final static String encoding = "ZapfDingbatsEncoding";
    private final static int capHeight = 820;
    private final static int xHeight = 426;
    private final static int ascender = 820;
    private final static int descender = -143;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("ZapfDingbatsEncoding");


    private boolean enableKerning = false;

    static {
        width = new int[256];
        
            width[0x20] = 278;
            width[0x21] = 974;
            width[0x22] = 961;
            width[0x23] = 974;
            width[0x24] = 980;
            width[0x25] = 719;
            width[0x26] = 789;
            width[0x27] = 790;
            width[0x28] = 791;
            width[0x29] = 690;
            width[0x2A] = 960;
            width[0x2B] = 939;
            width[0x2C] = 549;
            width[0x2D] = 855;
            width[0x2E] = 911;
            width[0x2F] = 933;
            width[0x30] = 911;
            width[0x31] = 945;
            width[0x32] = 974;
            width[0x33] = 755;
            width[0x34] = 846;
            width[0x35] = 762;
            width[0x36] = 761;
            width[0x37] = 571;
            width[0x38] = 677;
            width[0x39] = 763;
            width[0x3A] = 760;
            width[0x3B] = 759;
            width[0x3C] = 754;
            width[0x3D] = 494;
            width[0x3E] = 552;
            width[0x3F] = 537;
            width[0x40] = 577;
            width[0x41] = 692;
            width[0x42] = 786;
            width[0x43] = 788;
            width[0x44] = 788;
            width[0x45] = 790;
            width[0x46] = 793;
            width[0x47] = 794;
            width[0x48] = 816;
            width[0x49] = 823;
            width[0x4A] = 789;
            width[0x4B] = 841;
            width[0x4C] = 823;
            width[0x4D] = 833;
            width[0x4E] = 816;
            width[0x4F] = 831;
            width[0x50] = 923;
            width[0x51] = 744;
            width[0x52] = 723;
            width[0x53] = 749;
            width[0x54] = 790;
            width[0x55] = 792;
            width[0x56] = 695;
            width[0x57] = 776;
            width[0x58] = 768;
            width[0x59] = 792;
            width[0x5A] = 759;
            width[0x5B] = 707;
            width[0x5C] = 708;
            width[0x5D] = 682;
            width[0x5E] = 701;
            width[0x5F] = 826;
            width[0x60] = 815;
            width[0x61] = 789;
            width[0x62] = 789;
            width[0x63] = 707;
            width[0x64] = 687;
            width[0x65] = 696;
            width[0x66] = 689;
            width[0x67] = 786;
            width[0x68] = 787;
            width[0x69] = 713;
            width[0x6A] = 791;
            width[0x6B] = 785;
            width[0x6C] = 791;
            width[0x6D] = 873;
            width[0x6E] = 761;
            width[0x6F] = 762;
            width[0x70] = 762;
            width[0x71] = 759;
            width[0x72] = 759;
            width[0x73] = 892;
            width[0x74] = 892;
            width[0x75] = 788;
            width[0x76] = 784;
            width[0x77] = 438;
            width[0x78] = 138;
            width[0x79] = 277;
            width[0x7A] = 415;
            width[0x7B] = 392;
            width[0x7C] = 392;
            width[0x7D] = 668;
            width[0x7E] = 668;
            width[0xA1] = 732;
            width[0xA2] = 544;
            width[0xA3] = 544;
            width[0xA4] = 910;
            width[0xA5] = 667;
            width[0xA6] = 760;
            width[0xA7] = 760;
            width[0xA8] = 776;
            width[0xA9] = 595;
            width[0xAA] = 694;
            width[0xAB] = 626;
            width[0xAC] = 788;
            width[0xAD] = 788;
            width[0xAE] = 788;
            width[0xAF] = 788;
            width[0xB0] = 788;
            width[0xB1] = 788;
            width[0xB2] = 788;
            width[0xB3] = 788;
            width[0xB4] = 788;
            width[0xB5] = 788;
            width[0xB6] = 788;
            width[0xB7] = 788;
            width[0xB8] = 788;
            width[0xB9] = 788;
            width[0xBA] = 788;
            width[0xBB] = 788;
            width[0xBC] = 788;
            width[0xBD] = 788;
            width[0xBE] = 788;
            width[0xBF] = 788;
            width[0xC0] = 788;
            width[0xC1] = 788;
            width[0xC2] = 788;
            width[0xC3] = 788;
            width[0xC4] = 788;
            width[0xC5] = 788;
            width[0xC6] = 788;
            width[0xC7] = 788;
            width[0xC8] = 788;
            width[0xC9] = 788;
            width[0xCA] = 788;
            width[0xCB] = 788;
            width[0xCC] = 788;
            width[0xCD] = 788;
            width[0xCE] = 788;
            width[0xCF] = 788;
            width[0xD0] = 788;
            width[0xD1] = 788;
            width[0xD2] = 788;
            width[0xD3] = 788;
            width[0xD4] = 894;
            width[0xD5] = 838;
            width[0xD6] = 1016;
            width[0xD7] = 458;
            width[0xD8] = 748;
            width[0xD9] = 924;
            width[0xDA] = 748;
            width[0xDB] = 918;
            width[0xDC] = 927;
            width[0xDD] = 928;
            width[0xDE] = 928;
            width[0xDF] = 834;
            width[0xE0] = 873;
            width[0xE1] = 828;
            width[0xE2] = 924;
            width[0xE3] = 924;
            width[0xE4] = 917;
            width[0xE5] = 930;
            width[0xE6] = 931;
            width[0xE7] = 463;
            width[0xE8] = 883;
            width[0xE9] = 836;
            width[0xEA] = 836;
            width[0xEB] = 867;
            width[0xEC] = 867;
            width[0xED] = 696;
            width[0xEE] = 696;
            width[0xEF] = 874;
            width[0xF1] = 874;
            width[0xF2] = 760;
            width[0xF3] = 946;
            width[0xF4] = 771;
            width[0xF5] = 865;
            width[0xF6] = 771;
            width[0xF7] = 888;
            width[0xF8] = 967;
            width[0xF9] = 888;
            width[0xFA] = 831;
            width[0xFB] = 873;
            width[0xFC] = 927;
            width[0xFD] = 970;
            width[0xFE] = 918;
            width[0x89] = 410;
            width[0x87] = 509;
            width[0x8C] = 334;
            width[0x86] = 509;
            width[0x80] = 390;
            width[0x8A] = 234;
            width[0x84] = 276;
            width[0x81] = 390;
            width[0x88] = 410;
            width[0x83] = 317;
            width[0x82] = 317;
            width[0x85] = 276;
            width[0x8D] = 334;
            width[0x8B] = 234;
  
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
  