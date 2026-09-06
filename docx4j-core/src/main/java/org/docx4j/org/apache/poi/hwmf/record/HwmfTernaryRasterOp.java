/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.hwmf.record;

import java.util.Arrays;
import java.util.Deque;

/**
 * Each ternary raster operation code represents a Boolean operation in which the values of the pixels in
 * the source, the selected brush, and the destination are combined. Following are the three operands
 * used in these operations.
 *
 * <table>
 * <caption>Raster-operation code meaning</caption>
 * <tr><th>Operand</th><th>Meaning</th></tr>
 * <tr><td>D</td><td>Destination bitmap</td></tr>
 * <tr><td>P</td><td>Selected brush (also called pattern)</td></tr>
 * <tr><td>S</td><td>Source bitmap</td></tr>
 * </table>
 *
 * Following are the Boolean operators used in these operations.
 * <table>
 * <caption>Boolean operator meaning</caption>
 * <tr><th>Operand</th><th>Meaning</th></tr>
 * <tr><td>a</td><td>Bitwise AND</td></tr>
 * <tr><td>n</td><td>Bitwise NOT (inverse)</td></tr>
 * <tr><td>o</td><td>Bitwise OR</td></tr>
 * <tr><td>x</td><td>Bitwise exclusive OR (XOR)</td></tr>
 * </table>
 *
 * All Boolean operations are presented in reverse Polish notation. For example, the following operation
 * replaces the values of the pixels in the destination bitmap with a combination of the pixel values of the
 * source and brush: PSo.
 *
 * The following operation combines the values of the pixels in the source and brush with the pixel values
 * of the destination bitmap: DPSoo (there are alternative spellings of some functions, so although a
 * particular spelling MAY NOT be listed in the enumeration, an equivalent form SHOULD be).
 *
 * Each raster operation code is a 32-bit integer whose high-order word is a Boolean operation index and
 * whose low-order word is the operation code. The 16-bit operation index is a zero-extended, 8-bit
 * value that represents the result of the Boolean operation on predefined brush, source, and destination
 * values. For example, the operation indexes for the PSo and DPSoo operations are shown in the
 * following list.
 *
 * <table>
 * <caption>Raster-operation examples</caption>
 * <tr><th>P</th><th>S</th><th>D</th><th>DPo</th><th>DPan</th></tr>
 * <tr><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td></tr>
 * <tr><td>0</td><td>0</td><td>1</td><td>0</td><td>1</td></tr>
 * <tr><td>0</td><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>0</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>0</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>0</td><td>1</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>1</td><td>0</td><td>1</td><td>1</td></tr>
 * <tr><td>1</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
 * </table>
 *
 * The operation indexes are determined by reading the binary values in a column of the table from the
 * bottom up. For example, in the PSo column, the binary value is 11111100, which is equivalent to 00FC
 * (hexadecimal is implicit for these values), which is the operation index for PSo.
 *
 * Using this method, DPSoo can be seen to have the operation index 00FE. Operation indexes define the
 * locations of corresponding raster operation codes in the preceding enumeration. The PSo operation is
 * in line 252 (0x00FC) of the enumeration; DPSoo is in line 254 (0x00FE).
 *
 * The most commonly used raster operations have been given explicit enumeration names, which
 * SHOULD be used; examples are PATCOPY and WHITENESS.
 *
 * When the source and destination bitmaps are monochrome, a bit value of 0 represents a black pixel
 * and a bit value of 1 represents a white pixel. When the source and the destination bitmaps are color,
 * those colors are represented with red green blue (RGB) values.
 */
@SuppressWarnings("unused")
public enum HwmfTernaryRasterOp {
    /** Fills the destination rectangle with black */
    BLACKNESS(0x00000042),
    DPSOON(0x00010289),
    DPSONA(0x00020C89),
    PSON(0x000300AA),
    SDPONA(0x00040C88),
    DPON(0x000500A9),
    PDSXNON(0x00060865),
    PDSAON(0x000702C5),
    SDPNAA(0x00080F08),
    PDSXON(0x00090245),
    DPNA(0x000A0329),
    PSDNAON(0x000B0B2A),
    SPNA(0x000C0324),
    PDSNAON(0x000D0B25),
    PDSONON(0x000E08A5),
    PN(0x000F0001),
    PDSONA(0x00100C85),
    /** Fills the destination area with (not (Dst or Src)) */
    NOTSRCERASE(0x001100A6),
    SDPXNON(0x00120868),
    SDPAON(0x001302C8),
    DPSXNON(0x00140869),
    DPSAON(0x001502C9),
    PSDPSANAXX(0x00165CCA),
    SSPXDSXAXN(0x00171D54),
    SPXPDXA(0x00180D59),
    SDPSANAXN(0x00191CC8),
    PDSPAOX(0x001A06C5),
    SDPSXAXN(0x001B0768),
    PSDPAOX(0x001C06CA),
    DSPDXAXN(0x001D0766),
    PDSOX(0x001E01A5),
    PDSOAN(0x001F0385),
    DPSNAA(0x00200F09),
    SDPXON(0x00210248),
    DSNA(0x00220326),
    SPDNAON(0x00230B24),
    SPXDSXA(0x00240D55),
    PDSPANAXN(0x00251CC5),
    SDPSAOX(0x002606C8),
    SDPSXNOX(0x00271868),
    DPSXA(0x00280369),
    PSDPSAOXXN(0x002916CA),
    DPSANA(0x002A0CC9),
    SSPXPDXAXN(0x002B1D58),
    SPDSOAX(0x002C0784),
    PSDNOX(0x002D060A),
    PSDPXOX(0x002E064A),
    PSDNOAN(0x002F0E2A),
    PSNA(0x0030032A),
    SDPNAON(0x00310B28),
    SDPSOOX(0x00320688),
    /** Fills the destination area with (not Src) */
    NOTSRCCOPY(0x00330008),
    SPDSAOX(0x003406C4),
    SPDSXNOX(0x00351864),
    SDPOX(0x003601A8),
    SDPOAN(0x00370388),
    PSDPOAX(0x0038078A),
    SPDNOX(0x0390604),
    SPDSXOX(0x003A0644),
    SPDNOAN(0x003B0E24),
    PSX(0x003C004A),
    SPDSONOX(0x003D18A4),
    SPDSNAOX(0x003E1B24),
    PSAN(0x003F00EA),
    PSDNAA(0x00400F0A),
    DPSXON(0x00410249),
    SDXPDXA(0x00420D5D),
    SPDSANAXN(0x00431CC4),
    /** Fills the destination area with ((not Dst) and Src) */
    SRCERASE(0x00440328),
    DPSNAON(0x00450B29),
    DSPDAOX(0x004606C6),
    PSDPXAXN(0x0047076A),
    SDPXA(0x00480368),
    PDSPDAOXXN(0x004916C5),
    DPSDOAX(0x004A0789),
    PDSNOX(0x004B0605),
    SDPANA(0x004C0CC8),
    SSPXDSXOXN(0x004D1954),
    PDSPXOX(0x004E0645),
    PDSNOAN(0x004F0E25),
    PDNA(0x00500325),
    DSPNAON(0x00510B26),
    DPSDAOX(0x005206C9),
    SPDSXAXN(0x00530764),
    DPSONON(0x005408A9),
    /** Inverts the colors of the destination area */
    DSTINVERT(0x00550009),
    DPSOX(0x005601A9),
    DPSOAN(0x000570389),
    PDSPOAX(0x00580785),
    DPSNOX(0x00590609),
    /** Fills the destination area with (Dst xor Pattern) */
    PATINVERT(0x005A0049),
    DPSDONOX(0x005B18A9),
    DPSDXOX(0x005C0649),
    DPSNOAN(0x005D0E29),
    DPSDNAOX(0x005E1B29),
    DPAN(0x005F00E9),
    PDSXA(0x00600365),
    DSPDSAOXXN(0x006116C6),
    DSPDOAX(0x00620786),
    SDPNOX(0x00630608),
    SDPSOAX(0x00640788),
    DSPNOX(0x00650606),
    /** Fills the destination area with (Dst xor Src) */
    SRCINVERT(0x00660046),
    SDPSONOX(0x006718A8),
    DSPDSONOXXN(0x006858A6),
    PDSXXN(0x00690145),
    DPSAX(0x006A01E9),
    PSDPSOAXXN(0x006B178A),
    SDPAX(0x006C01E8),
    PDSPDOAXXN(0x006D1785),
    SDPSNOAX(0x006E1E28),
    PDSXNAN(0x006F0C65),
    PDSANA(0x00700CC5),
    SSDXPDXAXN(0x00711D5C),
    SDPSXOX(0x00720648),
    SDPNOAN(0x00730E28),
    DSPDXOX(0x00740646),
    DSPNOAN(0x00750E26),
    SDPSNAOX(0x00761B28),
    DSAN(0x007700E6),
    PDSAX(0x007801E5),
    DSPDSOAXXN(0x00791786),
    DPSDNOAX(0x007A1E29),
    SDPXNAN(0x007B0C68),
    SPDSNOAX(0x007C1E24),
    DPSXNAN(0x007D0C69),
    SPXDSXO(0x007E0955),
    DPSAAN(0x007F03C9),
    DPSAA(0x008003E9),
    SPXDSXON(0x00810975),
    DPSXNA(0x00820C49),
    SPDSNOAXN(0x00831E04),
    SDPXNA(0x00840C48),
    PDSPNOAXN(0x00851E05),
    DSPDSOAXX(0x008617A6),
    PDSAXN(0x008701C5),
    /** Fills the destination area with (Dst and Src) */
    SRCAND(0x008800C6),
    SDPSNAOXN(0x00891B08),
    DSPNOA(0x008A0E06),
    DSPDXOXN(0x008B0666),
    SDPNOA(0x008C0E08),
    SDPSXOXN(0x008D0668),
    SSDXPDXAX(0x008E1D7C),
    PDSANAN(0x008F0CE5),
    PDSXNA(0x00900C45),
    SDPSNOAXN(0x00911E08),
    DPSDPOAXX(0x009217A9),
    SPDAXN(0x009301C4),
    PSDPSOAXX(0x009417AA),
    DPSAXN(0x009501C9),
    DPSXX(0x00960169),
    PSDPSONOXX(0x0097588A),
    SDPSONOXN(0x00981888),
    DSXN(0x00990066),
    DPSNAX(0x009A0709),
    SDPSOAXN(0x009B07A8),
    SPDNAX(0x009C0704),
    DSPDOAXN(0x009D07A6),
    DSPDSAOXX(0x009E16E6),
    PDSXAN(0x009F0345),
    DPA(0x00A000C9),
    PDSPNAOXN(0x00A11B05),
    DPSNOA(0x00A20E09),
    DPSDXOXN(0x00A30669),
    PDSPONOXN(0x00A41885),
    PDXN(0x00A50065),
    DSPNAX(0x00A60706),
    PDSPOAXN(0x00A707A5),
    DPSOA(0x00A803A9),
    DPSOXN(0x00A90189),
    D(0x00AA0029),
    DPSONO(0x00AB0889),
    SPDSXAX(0x00AC0744),
    DPSDAOXN(0x00AD06E9),
    DSPNAO(0x00AE0B06),
    DPNO(0x00AF0229),
    PDSNOA(0x00B00E05),
    PDSPXOXN(0x00B10665),
    SSPXDSXOX(0x00B21974),
    SDPANAN(0x00B30CE8),
    PSDNAX(0x00B4070A),
    DPSDOAXN(0x00B507A9),
    DPSDPAOXX(0x00B616E9),
    SDPXAN(0x00B70348),
    PSDPXAX(0x00B8074A),
    DSPDAOXN(0x00B906E6),
    DPSNAO(0x00BA0B09),
    /** Fills the destination area with (Dst or not Src) */
    MERGEPAINT(0x00BB0226),
    SPDSANAX(0x00BC1CE4),
    SDXPDXAN(0x00BD0D7D),
    DPSXO(0x00BE0269),
    DPSANO(0x00BF08C9),
    /** Fills the destination area with (Src and Pattern) */
    MERGECOPY(0x00C000CA),
    SPDSNAOXN(0x00C11B04),
    SPDSONOXN(0x00C21884),
    PSXN(0x00C3006A),
    SPDNOA(0x00C40E04),
    SPDSXOXN(0x00C50664),
    SDPNAX(0x00C60708),
    PSDPOAXN(0x00C707AA),
    SDPOA(0x00C803A8),
    SPDOXN(0x00C90184),
    DPSDXAX(0x00CA0749),
    SPDSAOXN(0x00CB06E4),
    /** Fills the destination area with Src */
    SRCCOPY(0x00CC0020),
    SDPONO(0x00CD0888),
    SDPNAO(0x00CE0B08),
    SPNO(0x00CF0224),
    PSDNOA(0x00D00E0A),
    PSDPXOXN(0x00D1066A),
    PDSNAX(0x00D20705),
    SPDSOAXN(0x00D307A4),
    SSPXPDXAX(0x00D41D78),
    DPSANAN(0x00D50CE9),
    PSDPSAOXX(0x00D616EA),
    DPSXAN(0x00D70349),
    PDSPXAX(0x00D80745),
    SDPSAOXN(0x00D906E8),
    DPSDANAX(0x00DA1CE9),
    SPXDSXAN(0x00DB0D75),
    SPDNAO(0x00DC0B04),
    SDNO(0x00DD0228),
    SDPXO(0x00DE0268),
    SDPANO(0x00DF08C8),
    PDSOA(0x00E003A5),
    PDSOXN(0x00E10185),
    DSPDXAX(0x00E20746),
    PSDPAOXN(0x00E306EA),
    SDPSXAX(0x00E40748),
    PDSPAOXN(0x00E506E5),
    SDPSANAX(0x00E61CE8),
    SPXPDXAN(0x00E70D79),
    SSPXDSXAX(0x00E81D74),
    DSPDSANAXXN(0x00E95CE6),
    DPSAO(0x00EA02E9),
    DPSXNO(0x00EB0849),
    SDPAO(0x00EC02E8),
    SDPXNO(0x00ED0848),
    /** Combines the colors of the source and the destination using the operator OR on each pixel */
    SRCPAINT(0x00EE0086),
    SDPNOO(0x00EF0A08),
    /** Fills the destination area with (Pattern) */
    PATCOPY(0x00F00021),
    PDSONO(0x00F10885),
    PDSNAO(0x00F20B05),
    PSNO(0x00F3022A),
    PSDNAO(0x00F40B0A),
    PDNO(0x00F50225),
    PDSXO(0x00F60265),
    PDSANO(0x00F708C5),
    PDSAO(0x00F802E5),
    PDSXNO(0x00F90845),
    DPO(0x00FA0089),
    /** Fills the destination area with (Dst or (not Src) or Pattern) */
    PATPAINT(0x00FB0A09),
    PSO(0x00FC008A),
    PSDNOO(0x00FD0A0A),
    DPSOO(0x00FE02A9),
    /** Fills the destination rectangle with white */
    WHITENESS(0x00FF0062);


    private static final String[] ARG_ORDER = {
        "SSSSSS","PPPPPP","DDDDDD",null,
        "SPDSPD","PDSPDS","DSPDSP",null,
        "SDPSDP","DPSDPS","PSDPSD",null,
        null,     null,    null,   null,
        null,     null,    null,   null,
        "SSP.DS", "SP.DS", null,   null,
        "SSP.PD", "SP.PD", null,   null,
        "SSD.PD", "SD.PD", null,   null
    };

    private static final String OPS = "nxoa";

    private final int opValue;

    HwmfTernaryRasterOp(int opValue) {
        this.opValue=opValue;
    }

    public static HwmfTernaryRasterOp valueOf(int opIndex) {
        for (HwmfTernaryRasterOp bb : HwmfTernaryRasterOp.values()) {
            if (bb.getOpIndex() == opIndex) {
                return bb;
            }
        }
        return null;
    }

    public int getOpIndex() {
        return opValue >>> 16;
    }

    public int getOpCode() {
        return opValue & 0xFFFF;
    }

    public String describeCmd() {
        String[] stack = new String[10];
        int stackPnt = 0;

        for (char c : calcCmd().toCharArray()) {
            switch (c) {
                case 'S':
                case 'D':
                case 'P':
                    stack[stackPnt++] = ""+c;
                    break;
                case 'n':
                    stack[stackPnt-1] = "not("+stack[stackPnt-1]+")";
                    break;
                case 'a':
                    stack[stackPnt-2] = "("+stack[stackPnt-1]+" and "+stack[stackPnt-2]+")";
                    stackPnt--;
                    break;
                case 'o':
                    stack[stackPnt-2] = "("+stack[stackPnt-1]+" or "+stack[stackPnt-2]+")";
                    stackPnt--;
                    break;
                case 'x':
                    stack[stackPnt-2] = "("+stack[stackPnt-1]+" xor "+stack[stackPnt-2]+")";
                    stackPnt--;
                    break;
                case '1':
                    stack[stackPnt++] = "all white";
                    break;
                case '0':
                    stack[stackPnt++] = "all black";
                    break;
                default:
                    throw new IllegalStateException("unknown cmd '"+c+"'.");
            }
        }

        return stack[--stackPnt];
    }


    public String calcCmd() {
        // taken from https://wiki.winehq.org/Ternary_Raster_Ops

        // bit 0-4: Specify the order of arguments to the raster operation
        String argOrder = ARG_ORDER[this.opValue & 0x001F];
        assert(argOrder != null);

        // The boolean operators, 1st (6-7 bit), 2nd (8-9 bit), 3rd (a-b bit), 4th (c-d bit), 5th (e-f bit)
        int nbrOfOps = 0;
        int[] opArr = new int[5];
        for (int i=0, bit=6; i<opArr.length; i++, bit+=2) {
            if ((opArr[i] = (this.opValue >>> bit) & 0x03) != 0) {
                nbrOfOps = i+1;
            }
        }

        StringBuilder sbArg = new StringBuilder(), sbOp = new StringBuilder();
        sbArg.append(argOrder.charAt(0));
        for (int opIdx=0,argIdx=1; opIdx < nbrOfOps; opIdx++) {
            char opCh = OPS.charAt(opArr[opIdx]);
            char ch = argOrder.charAt(argIdx);
            sbOp.append(opCh);
            if (ch == '.') {
                sbArg.insert(argIdx, sbOp.charAt(0));
                sbOp.deleteCharAt(0);
            }
            if (opCh == 'n') {
                continue;
            }
            sbArg.append(ch == '.' ? argOrder.charAt(++argIdx) : ch);
            argIdx++;
        }
        sbArg.append(sbOp);

        // bit 5: Used to apply the NOT operator to the results of the other operations
        // if 0, there are a ODD number of operations, even number otherwise
        if ((nbrOfOps % 2) == ((this.opValue >>> 0x05) & 1)) {
            sbArg.append('n');
        }

        String ret = sbArg.toString();
        return ret.startsWith("DDx") ? "DDx".equals(ret) ? "0" : "1" : ret;
    }

    public void process(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        for (char op : calcCmd().toCharArray()) {
            switch (op) {
                case 'S': opS(stack, dst, src, pat); break;
                case 'P': opP(stack, dst, src, pat); break;
                case 'D': opD(stack, dst, src, pat); break;
                case 'n': opN(stack, dst, src, pat); break;
                case 'a': opA(stack, dst, src, pat); break;
                case 'o': opO(stack, dst, src, pat); break;
                case 'x': opX(stack, dst, src, pat); break;
                case '1': op1(stack, dst, src, pat); break;
                case '0': op0(stack, dst, src, pat); break;
                default: throw new IllegalStateException();
            }
        }
    }

    private static void opS(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        stack.push(src);
    }

    private static void opP(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        stack.push(pat);
    }

    private static void opD(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        stack.push(dst);
    }

    private static void opN(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        int[] oper = checkClone(stack.pop(), dst, src, pat, true);
        for (int i=0; i<oper.length; i++) {
            oper[i] = (oper[i]&0xFF000000) | (~oper[i] & 0x00FFFFFF);
        }
        stack.push(oper);
    }

    private static void opA(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        int[] oper1 = checkClone(stack.pop(), dst, src, pat, true);
        int[] oper2 = checkClone(stack.pop(), dst, src, pat, false);
        for (int i=0; i<oper1.length; i++) {
            oper1[i] = (oper1[i]&0xFF000000) | ((oper1[i] & oper2[i]) & 0x00FFFFFF);
        }
        stack.push(oper1);
    }

    private static void opO(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        int[] oper1 = checkClone(stack.pop(), dst, src, pat, true);
        int[] oper2 = checkClone(stack.pop(), dst, src, pat, false);
        for (int i=0; i<oper1.length; i++) {
            oper1[i] = (oper1[i]&0xFF000000) | ((oper1[i] | oper2[i]) & 0x00FFFFFF);
        }
        stack.push(oper1);
    }

    private static void opX(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        int[] oper1 = checkClone(stack.pop(), dst, src, pat, true);
        int[] oper2 = checkClone(stack.pop(), dst, src, pat, false);
        for (int i=0; i<oper1.length; i++) {
            oper1[i] = (oper1[i]&0xFF000000) | ((oper1[i] ^ oper2[i]) & 0x00FFFFFF);
        }
        stack.push(oper1);
    }

    private static void op1(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        int[] oper = new int[dst.length];
        Arrays.fill(oper, 0xFFFFFFFF);
        stack.push(oper);
    }

    private static void op0(Deque<int[]> stack, int[] dst, int[] src, int[] pat) {
        int[] oper = new int[dst.length];
        Arrays.fill(oper, 0xFF000000);
        stack.push(oper);
    }

    private static int[] checkClone(int[] oper, int[] dst, int[] src, int[] pat, boolean force) {
        if (force && (oper == src || oper == pat || oper == dst)) {
            return oper.clone();
        } else {
            return oper;
        }
    }
}