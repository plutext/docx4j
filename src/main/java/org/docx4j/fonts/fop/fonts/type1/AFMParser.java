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

/* $Id: AFMParser.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.type1;

import java.awt.Rectangle;
import java.beans.Statement;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.IOUtils;

import org.docx4j.fonts.fop.fonts.NamedCharacter;

/**
 * Parses the contents of a Type 1 AFM font metrics file into an object structure ({@link AFMFile}).
 */
public class AFMParser {

    private static final String START_FONT_METRICS = "StartFontMetrics";
    //private static final String END_FONT_METRICS = "EndFontMetrics";
    private static final String FONT_NAME = "FontName";
    private static final String FULL_NAME = "FullName";
    private static final String FAMILY_NAME = "FamilyName";
    private static final String WEIGHT = "Weight";
    private static final String FONT_BBOX = "FontBBox";
    private static final String ENCODING_SCHEME = "EncodingScheme";
    private static final String CHARACTER_SET = "CharacterSet";
    private static final String IS_BASE_FONT = "IsBaseFont";
    private static final String IS_CID_FONT = "IsCIDFont";
    private static final String CAP_HEIGHT = "CapHeight";
    private static final String X_HEIGHT = "XHeight";
    private static final String ASCENDER = "Ascender";
    private static final String DESCENDER = "Descender";
    private static final String STDHW = "StdHW";
    private static final String STDVW = "StdVW";
    private static final String UNDERLINE_POSITION = "UnderlinePosition";
    private static final String UNDERLINE_THICKNESS = "UnderlineThickness";
    private static final String ITALIC_ANGLE = "ItalicAngle";
    private static final String IS_FIXED_PITCH = "IsFixedPitch";
    private static final String START_DIRECTION = "StartDirection";
    private static final String END_DIRECTION = "EndDirection";
    private static final String START_CHAR_METRICS = "StartCharMetrics";
    private static final String END_CHAR_METRICS = "EndCharMetrics";
    private static final String C = "C";
    private static final String CH = "CH";
    private static final String WX = "WX";
    private static final String W0X = "W0X";
    private static final String W1X = "W1X";
    private static final String WY = "WY";
    private static final String W0Y = "W0Y";
    private static final String W1Y = "W1Y";
    private static final String W = "W";
    private static final String W0 = "W0";
    private static final String W1 = "W1";
    private static final String N = "N";
    private static final String B = "B";
    private static final String START_TRACK_KERN = "StartTrackKern";
    private static final String END_TRACK_KERN = "EndTrackKern";
    //private static final String START_KERN_PAIRS = "StartKernPairs";
    //private static final String START_KERN_PAIRS0 = "StartKernPairs0";
    private static final String START_KERN_PAIRS1 = "StartKernPairs1";
    //private static final String END_KERN_PAIRS = "EndKernPairs";
    private static final String KP = "KP";
    private static final String KPH = "KPH";
    private static final String KPX = "KPX";
    private static final String KPY = "KPY";

    private static final int PARSE_NORMAL = 0;
    private static final int PARSE_CHAR_METRICS = 1;

    private static final Map VALUE_PARSERS;
    private static final Map PARSE_MODE_CHANGES;

    static {
        VALUE_PARSERS = new java.util.HashMap();
        VALUE_PARSERS.put(START_FONT_METRICS, new StartFontMetrics());
        VALUE_PARSERS.put(FONT_NAME, new StringSetter(FONT_NAME));
        VALUE_PARSERS.put(FULL_NAME, new StringSetter(FULL_NAME));
        VALUE_PARSERS.put(FAMILY_NAME, new StringSetter(FAMILY_NAME));
        VALUE_PARSERS.put(WEIGHT, new StringSetter(WEIGHT));
        VALUE_PARSERS.put(ENCODING_SCHEME, new StringSetter(ENCODING_SCHEME));
        VALUE_PARSERS.put(FONT_BBOX, new FontBBox());
        VALUE_PARSERS.put(CHARACTER_SET, new StringSetter(CHARACTER_SET));
        VALUE_PARSERS.put(IS_BASE_FONT, new IsBaseFont());
        VALUE_PARSERS.put(IS_CID_FONT, new IsCIDFont());
        VALUE_PARSERS.put(CAP_HEIGHT, new NumberSetter(CAP_HEIGHT));
        VALUE_PARSERS.put(X_HEIGHT, new NumberSetter(X_HEIGHT));
        VALUE_PARSERS.put(ASCENDER, new NumberSetter(ASCENDER));
        VALUE_PARSERS.put(DESCENDER, new NumberSetter(DESCENDER));
        VALUE_PARSERS.put(STDHW, new NumberSetter(STDHW));
        VALUE_PARSERS.put(STDVW, new NumberSetter(STDVW));
        VALUE_PARSERS.put(START_DIRECTION, new StartDirection());
        VALUE_PARSERS.put(END_DIRECTION, new EndDirection());
        VALUE_PARSERS.put(UNDERLINE_POSITION, new WritingDirNumberSetter(UNDERLINE_POSITION));
        VALUE_PARSERS.put(UNDERLINE_THICKNESS, new WritingDirNumberSetter(UNDERLINE_THICKNESS));
        VALUE_PARSERS.put(ITALIC_ANGLE, new WritingDirDoubleSetter(ITALIC_ANGLE));
        VALUE_PARSERS.put(IS_FIXED_PITCH, new WritingDirBooleanSetter(IS_FIXED_PITCH));
        VALUE_PARSERS.put(C, new IntegerSetter("CharCode"));
        VALUE_PARSERS.put(CH, new NotImplementedYet(CH));
        VALUE_PARSERS.put(WX, new DoubleSetter("WidthX"));
        VALUE_PARSERS.put(W0X, new DoubleSetter("WidthX"));
        VALUE_PARSERS.put(W1X, new NotImplementedYet(W1X));
        VALUE_PARSERS.put(WY, new DoubleSetter("WidthY"));
        VALUE_PARSERS.put(W0Y, new DoubleSetter("WidthY"));
        VALUE_PARSERS.put(W1Y, new NotImplementedYet(W1Y));
        VALUE_PARSERS.put(W, new NotImplementedYet(W));
        VALUE_PARSERS.put(W0, new NotImplementedYet(W0));
        VALUE_PARSERS.put(W1, new NotImplementedYet(W1));
        VALUE_PARSERS.put(N, new NamedCharacterSetter("Character"));
        VALUE_PARSERS.put(B, new CharBBox());
        VALUE_PARSERS.put(START_TRACK_KERN, new NotImplementedYet(START_TRACK_KERN));
        VALUE_PARSERS.put(END_TRACK_KERN, new NotImplementedYet(END_TRACK_KERN));
        VALUE_PARSERS.put(START_KERN_PAIRS1, new NotImplementedYet(START_KERN_PAIRS1));
        VALUE_PARSERS.put(KP, new NotImplementedYet(KP));
        VALUE_PARSERS.put(KPH, new NotImplementedYet(KPH));
        VALUE_PARSERS.put(KPX, new KPXHandler());
        VALUE_PARSERS.put(KPY, new NotImplementedYet(KPY));

        PARSE_MODE_CHANGES = new java.util.HashMap();
        PARSE_MODE_CHANGES.put(START_CHAR_METRICS, new Integer(PARSE_CHAR_METRICS));
        PARSE_MODE_CHANGES.put(END_CHAR_METRICS, new Integer(PARSE_NORMAL));
    }

    /**
     * Main constructor.
     */
    public AFMParser() {
        //nop
    }

    /**
     * Parses an AFM file from a local file.
     * @param afmFile the AFM file
     * @return the parsed AFM file
     * @throws IOException if an I/O error occurs
     */
    public AFMFile parse(File afmFile) throws IOException {
        InputStream in = new java.io.FileInputStream(afmFile);
        try {
            return parse(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Parses an AFM file from a stream.
     * @param in the stream to read from
     * @return the parsed AFM file
     * @throws IOException if an I/O error occurs
     */
    public AFMFile parse(InputStream in) throws IOException {
        Reader reader = new java.io.InputStreamReader(in, "US-ASCII");
        try {
            return parse(new BufferedReader(reader));
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Parses an AFM file from a BufferedReader.
     * @param reader the BufferedReader instance to read from
     * @return the parsed AFM file
     * @throws IOException if an I/O error occurs
     */
    public AFMFile parse(BufferedReader reader) throws IOException {
        Stack stack = new Stack();
        int parseMode = PARSE_NORMAL;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String key = null;
            switch (parseMode) {
            case PARSE_NORMAL:
                key = parseLine(line, stack);
                break;
            case PARSE_CHAR_METRICS:
                key = parseCharMetrics(line, stack);
                break;
            default:
                throw new IllegalStateException("Invalid parse mode");
            }
            Integer newParseMode = (Integer)PARSE_MODE_CHANGES.get(key);
            if (newParseMode != null) {
                parseMode = newParseMode.intValue();
            }
        }
        return (AFMFile)stack.pop();
    }

    private String parseLine(String line, Stack stack) throws IOException {
        int startpos = 0;
        //Find key
        startpos = skipToNonWhiteSpace(line, startpos);
        int endpos = skipToWhiteSpace(line, startpos);
        String key = line.substring(startpos, endpos);

        //Parse value
        startpos = skipToNonWhiteSpace(line, endpos);
        ValueHandler vp = (ValueHandler)VALUE_PARSERS.get(key);
        if (vp != null) {
            vp.parse(line, startpos, stack);
        }
        return key;
    }

    private String parseCharMetrics(String line, Stack stack) throws IOException {
        int startpos = 0;
        AFMCharMetrics chm = new AFMCharMetrics();
        stack.push(chm);
        while (true) {
            //Find key
            startpos = skipToNonWhiteSpace(line, startpos);
            int endpos = skipToWhiteSpace(line, startpos);
            String key = line.substring(startpos, endpos);
            if (END_CHAR_METRICS.equals(key)) {
                stack.pop(); //Pop and forget unused AFMCharMetrics instance
                return key;
            } else if (key.length() == 0) {
                //EOL: No more key so break
                break;
            }

            //Extract value
            startpos = skipToNonWhiteSpace(line, endpos);
            endpos = skipToSemicolon(line, startpos);
            String value = line.substring(startpos, endpos).trim();
            startpos = endpos + 1;

            //Parse value
            ValueHandler vp = (ValueHandler)VALUE_PARSERS.get(key);
            if (vp != null) {
                vp.parse(value, 0, stack);
            }
            if (false) {
                break;
            }
        }
        stack.pop();
        AFMFile afm = (AFMFile)stack.peek();
        afm.addCharMetrics(chm);
        return null;
    }

    private static int skipToNonWhiteSpace(String line, int startpos) {
        int pos = startpos;
        while (pos < line.length() && isWhitespace(line.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static int skipToWhiteSpace(String line, int startpos) {
        int pos = startpos;
        while (pos < line.length() && !isWhitespace(line.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static int skipToSemicolon(String line, int startpos) {
        int pos = startpos;
        while (pos < line.length() && ';' != line.charAt(pos)) {
            pos++;
        }
        return pos;
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' '
            || ch == '\t';
    }

    // ---------------- Value Handlers ---------------------------

    private interface ValueHandler {
        void parse(String line, int startpos, Stack stack) throws IOException;
    }

    private abstract static class AbstractValueHandler implements ValueHandler {

        protected int findValue(String line, int startpos) {
            return skipToWhiteSpace(line, startpos);
        }

        protected String getStringValue(String line, int startpos) {
            return line.substring(startpos);
        }

        protected Number getNumberValue(String line, int startpos) {
            try {
                return new Integer(getIntegerValue(line, startpos));
            } catch (NumberFormatException nfe) {
                return new Double(getDoubleValue(line, startpos));
            }
        }

        protected int getIntegerValue(String line, int startpos) {
            int endpos = findValue(line, startpos);
            return Integer.parseInt(line.substring(startpos, endpos));
        }

        protected double getDoubleValue(String line, int startpos) {
            int endpos = findValue(line, startpos);
            return Double.parseDouble(line.substring(startpos, endpos));
        }

        protected Boolean getBooleanValue(String line, int startpos) {
            return Boolean.valueOf(getStringValue(line, startpos));
        }

    }

    private static class StartFontMetrics extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            int endpos = findValue(line, startpos);
            double version = Double.parseDouble(line.substring(startpos, endpos));
            if (version < 2) {
                throw new IOException(
                        "AFM version must be at least 2.0 but it is " + version + "!");
            }
            AFMFile afm = new AFMFile();
            stack.push(afm);
        }
    }

    private abstract static class BeanSetter extends AbstractValueHandler {
        private String method;

        public BeanSetter(String variable) {
            this.method = "set" + variable;
        }

        protected void setValue(Object target, Object value) {
            //Uses Java Beans API
            Statement statement = new Statement(target, method, new Object[] {value});
            try {
                statement.execute();
            } catch (Exception e) {
                //Should never happen
                throw new RuntimeException("Bean error: " + e.getMessage());
            }
        }
    }

    private static class StringSetter extends BeanSetter {

        public StringSetter(String variable) {
            super(variable);
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            String s = getStringValue(line, startpos);
            Object obj = stack.peek();
            setValue(obj, s);
        }
    }

    private static class NamedCharacterSetter extends BeanSetter {

        public NamedCharacterSetter(String variable) {
            super(variable);
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            NamedCharacter ch = new NamedCharacter(getStringValue(line, startpos));
            Object obj = stack.peek();
            setValue(obj, ch);
        }
    }

    private static class NumberSetter extends BeanSetter {
        public NumberSetter(String variable) {
            super(variable);
        }

        protected Object getContextObject(Stack stack) {
            return stack.peek();
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            Number num = getNumberValue(line, startpos);
            setValue(getContextObject(stack), num);
        }
    }

    private static class IntegerSetter extends NumberSetter {
        public IntegerSetter(String variable) {
            super(variable);
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            int value = getIntegerValue(line, startpos);
            setValue(getContextObject(stack), new Integer(value));
        }
    }

    private static class DoubleSetter extends NumberSetter {
        public DoubleSetter(String variable) {
            super(variable);
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            double value = getDoubleValue(line, startpos);
            setValue(getContextObject(stack), new Double(value));
        }
    }

    private static class WritingDirNumberSetter extends NumberSetter {

        public WritingDirNumberSetter(String variable) {
            super(variable);
        }

        protected Object getContextObject(Stack stack) {
            if (stack.peek() instanceof AFMWritingDirectionMetrics) {
                return (AFMWritingDirectionMetrics)stack.peek();
            } else {
                AFMFile afm = (AFMFile)stack.peek();
                AFMWritingDirectionMetrics wdm = afm.getWritingDirectionMetrics(0);
                if (wdm == null) {
                    wdm = new AFMWritingDirectionMetrics();
                    afm.setWritingDirectionMetrics(0, wdm);
                }
                return wdm;
            }
        }

    }

    private static class WritingDirDoubleSetter extends WritingDirNumberSetter {

        public WritingDirDoubleSetter(String variable) {
            super(variable);
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            double value = getDoubleValue(line, startpos);
            setValue(getContextObject(stack), new Double(value));
        }
    }

    private static class BooleanSetter extends AbstractValueHandler {
        private String method;

        public BooleanSetter(String variable) {
            this.method = "set" + variable.substring(2); //Cut "Is" in front
        }

        protected Object getContextObject(Stack stack) {
            return (AFMFile)stack.peek();
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            Boolean b = getBooleanValue(line, startpos);
            //Uses Java Beans API
            Statement statement = new Statement(getContextObject(stack),
                    method, new Object[] {b});
            try {
                statement.execute();
            } catch (Exception e) {
                //Should never happen
                throw new RuntimeException("Bean error: " + e.getMessage());
            }
        }
    }

    private static class WritingDirBooleanSetter extends BooleanSetter {

        public WritingDirBooleanSetter(String variable) {
            super(variable);
        }

        protected Object getContextObject(Stack stack) {
            if (stack.peek() instanceof AFMWritingDirectionMetrics) {
                return (AFMWritingDirectionMetrics)stack.peek();
            } else {
                AFMFile afm = (AFMFile)stack.peek();
                AFMWritingDirectionMetrics wdm = afm.getWritingDirectionMetrics(0);
                if (wdm == null) {
                    wdm = new AFMWritingDirectionMetrics();
                    afm.setWritingDirectionMetrics(0, wdm);
                }
                return wdm;
            }
        }

    }

    private static class FontBBox extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            Rectangle rect = parseBBox(line, startpos);

            AFMFile afm = (AFMFile)stack.peek();
            afm.setFontBBox(rect);
        }

        protected Rectangle parseBBox(String line, int startpos) {
            Rectangle rect = new Rectangle();
            int endpos;

            endpos = findValue(line, startpos);
            rect.x = Integer.parseInt(line.substring(startpos, endpos));
            startpos = skipToNonWhiteSpace(line, endpos);

            endpos = findValue(line, startpos);
            rect.y = Integer.parseInt(line.substring(startpos, endpos));
            startpos = skipToNonWhiteSpace(line, endpos);

            endpos = findValue(line, startpos);
            int v = Integer.parseInt(line.substring(startpos, endpos));
            rect.width = v - rect.x;
            startpos = skipToNonWhiteSpace(line, endpos);

            endpos = findValue(line, startpos);
            v = Integer.parseInt(line.substring(startpos, endpos));
            rect.height = v - rect.y;
            startpos = skipToNonWhiteSpace(line, endpos);
            return rect;
        }
    }

    private static class CharBBox extends FontBBox {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            Rectangle rect = parseBBox(line, startpos);

            AFMCharMetrics metrics = (AFMCharMetrics)stack.peek();
            metrics.setBBox(rect);
        }
    }

    private static class IsBaseFont extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            if (getBooleanValue(line, startpos).booleanValue()) {
                throw new IOException("Only base fonts are currently supported!");
            }
        }
    }

    private static class IsCIDFont extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            if (getBooleanValue(line, startpos).booleanValue()) {
                throw new IOException("CID fonts are currently not supported!");
            }
        }
    }

    private static class NotImplementedYet extends AbstractValueHandler {
        private String key;

        public NotImplementedYet(String key) {
            this.key = key;
        }

        public void parse(String line, int startpos, Stack stack) throws IOException {
            throw new IOException("Support for '" + key
                    + "' has not been implemented, yet! Font is not supported.");
        }
    }

    private static class StartDirection extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            int index = getIntegerValue(line, startpos);
            AFMWritingDirectionMetrics wdm = new AFMWritingDirectionMetrics();
            AFMFile afm = (AFMFile)stack.peek();
            afm.setWritingDirectionMetrics(index, wdm);
            stack.push(wdm);
        }
    }

    private static class EndDirection extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            if (!(stack.pop() instanceof AFMWritingDirectionMetrics)) {
                throw new IOException("AFM format error: nesting incorrect");
            }
        }
    }

    private static class KPXHandler extends AbstractValueHandler {
        public void parse(String line, int startpos, Stack stack) throws IOException {
            AFMFile afm = (AFMFile)stack.peek();
            int endpos;

            endpos = findValue(line, startpos);
            String name1 = line.substring(startpos, endpos);
            startpos = skipToNonWhiteSpace(line, endpos);

            endpos = findValue(line, startpos);
            String name2 = line.substring(startpos, endpos);
            startpos = skipToNonWhiteSpace(line, endpos);

            endpos = findValue(line, startpos);
            double kx = Double.parseDouble(line.substring(startpos, endpos));
            startpos = skipToNonWhiteSpace(line, endpos);

            afm.addXKerning(name1, name2, kx);
        }
    }

}
