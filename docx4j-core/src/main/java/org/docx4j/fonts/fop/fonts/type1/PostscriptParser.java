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

/* $Id$ */

package org.docx4j.fonts.fop.fonts.type1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PostscriptParser {

    protected static final Log LOG = LogFactory.getLog(PostscriptParser.class);
    /* Patterns used to identify Postscript elements */
    private static final String DICTIONARY = "dict";
    private static final String FIXED_ARRAY = "array";
    private static final String VARIABLE_ARRAY = "[";
    private static final String SUBROUTINE = "{";
    /* A list of parsed subroutines so if they are encountered during the parsing
     * phase of another element, they can be read and pattern matched. */
    private HashMap<String, PSSubroutine> subroutines = new HashMap<String, PSSubroutine>();

    /**
     * Parses the postscript document and returns a list of elements
     * @param segment The byte array containing the postscript data
     * @return A list of found Postscript elements
     * @throws IOException
     */
    public List<PSElement> parse(byte[] segment) throws IOException {
        List<PSElement> parsedElements = new ArrayList<PSElement>();
        /* Currently only scan and store the top level element. For deeper
         * Postscript parsing you can push and pop elements from a stack */
        PSElement foundElement = null;
        String operator = null;
        StringBuilder token = new StringBuilder();
        List<String> tokens = new ArrayList<String>();
        int startPoint = -1;
        boolean specialDelimiter = false;
        boolean lastWasSpecial = false;
        for (int i = 0; i < segment.length; i++) {
            byte cur = segment[i];
            if (foundElement != null && foundElement.hasMore()) {
                foundElement.parse(cur, i);
                continue;
            } else {
                char c = (char)cur;
                if (!lastWasSpecial) {
                    specialDelimiter = (c == '{' || c == '}' || c == '[' || c == ']'
                            || (!token.toString().equals("") && c == '/'));
                    boolean isNotBreak = !(c == ' ' || c == '\r' || cur == 15 || cur == 12
                            || cur == 10);
                    if (isNotBreak && !specialDelimiter) {
                        token.append(c);
                        continue;
                    }
                } else {
                    lastWasSpecial = false;
                    token.append(c);
                    if (token.toString().equals("/")) {
                        continue;
                    }
                }
            }
            try {
                boolean setOp = false;
                if ((foundElement == null || !foundElement.hasMore()) && token.length() > 1
                        && token.charAt(0) == '/' && tokens.size() != 1 || hasEndToken(token.toString())) {
                    operator = token.toString();
                    setOp = true;
                    if (tokens.size() > 2 && tokens.get(tokens.size() - 1).equals("def")) {
                        PSVariable newVar = new PSVariable(tokens.get(0), startPoint);
                        newVar.setValue(tokens.get(1));
                        newVar.setEndPoint(i - operator.length());
                        parsedElements.add(newVar);
                    }
                    tokens.clear();
                    startPoint = i - token.length();
                }
                if (operator != null) {
                    if (foundElement instanceof PSSubroutine) {
                        PSSubroutine sub = (PSSubroutine)foundElement;
                        subroutines.put(sub.getOperator(), sub);
                        parsedElements.add(sub);
                        if (!setOp) {
                            operator = "";
                        }
                    } else {
                        if (foundElement != null) {
                            if (!hasMatch(foundElement.getOperator(), parsedElements)) {
                                parsedElements.add(foundElement);
                            } else {
                                LOG.warn("Duplicate " + foundElement.getOperator()
                                        + " in font file, Ignoring.");
                            }
                        }
                    }
                    //Compare token against patterns and create an element if matched
                    foundElement = createElement(operator, token.toString(), startPoint);
                }
            } finally {
                tokens.add(token.toString());
                token = new StringBuilder();
                if (specialDelimiter) {
                    specialDelimiter = false;
                    lastWasSpecial = true;
                    //Retrace special postscript character so it can be processed separately
                    i--;
                }
            }
        }
        return parsedElements;
    }

    private boolean hasEndToken(String token) {
        return token.equals("currentdict");
    }

    private boolean hasMatch(String operator, List<PSElement> elements) {
        for (PSElement element : elements) {
            if (element.getOperator().equals(operator)) {
                return true;
            }
        }
        return false;
    }

    public PSElement createElement(String operator, String elementID, int startPoint) {
        if (operator.equals("")) {
            return null;
        }
        if (elementID.equals(FIXED_ARRAY)) {
            return new PSFixedArray(operator, startPoint);
        } else if (elementID.equals(VARIABLE_ARRAY)) {
            return new PSVariableArray(operator, startPoint);
        } else if (elementID.equals(SUBROUTINE)) {
            return new PSSubroutine(operator, startPoint);
        } else if (!operator.equals("/Private") && elementID.equals(DICTIONARY)) {
            return new PSDictionary(operator, startPoint);
        }
        return null;
    }

    /**
     * A base Postscript element class
     */
    public abstract class PSElement {
        /* The identifying operator for this element */
        protected String operator;
        private List<Byte> token;
        /* Determines whether there is any more data to be read whilst parsing */
        protected boolean hasMore = true;
        /* The locations of any entries containing binary data (e.g. arrays) */
        protected LinkedHashMap<String, int[]> binaryEntries;
        /* The tokens parsed from the current element */
        protected List<String> tokens;
        /* Determines whether binary data is currently being read / parsed */
        protected boolean readBinary;
        /* The location of the element within the binary data */
        private int startPoint = -1;
        protected int endPoint = -1;
        /* A flag to determine if unexpected postscript has been found in the element */
        private boolean foundUnexpected;

        public PSElement(String operator, int startPoint) {
            this.operator = operator;
            this.startPoint = startPoint;
            token = new ArrayList<Byte>();
            binaryEntries = new LinkedHashMap<String, int[]>();
            tokens = new ArrayList<String>();
        }

        /**
         * Gets the Postscript element operator
         * @return The operator returned as a string
         */
        public String getOperator() {
            return operator;
        }

        /**
         * The start location of the element within the source binary data
         * @return The start location returned as an integer
         */
        public int getStartPoint() {
            return startPoint;
        }

        /**
         * The end location of the element within the source binary data
         * @return The end location returned as an integer
         */
        public int getEndPoint() {
            return endPoint;
        }

        /**
         * Takes over the task of tokenizing the byte data
         * @param cur The current byte being read
         */
        public void parse(byte cur, int pos) throws UnsupportedEncodingException {
            if (!readBinary) {
                char c = (char)cur;
                boolean specialDelimiter = (c == '{' || c == '}' || c == '[' || c == ']'
                        || c == '(' || c == ')');
                boolean isNotValidBreak = !(c == ' ' || cur == 15 || cur == 12 || c == '\r'
                        || c == 10);
                if (isNotValidBreak && !specialDelimiter) {
                    token.add(cur);
                } else {
                    parseToken(pos);
                }
                if (specialDelimiter) {
                    token.add(cur);
                    parseToken(pos);
                }
            } else {
                parseByte(cur, pos);
            }
        }

        private void parseToken(int pos) throws UnsupportedEncodingException {
            byte[] bytesToken = new byte[token.size()];
            for (int i = 0; i < token.size(); i++) {
                bytesToken[i] = token.get(i);
            }
            parseToken(new String(bytesToken, "ASCII"), pos);
            token.clear();
        }

        /**
         * Passes responsibility for processing the byte stream to the PostScript object
         * @param cur The byte currently being read
         * @param pos The position of the given byte
         */
        public abstract void parseByte(byte cur, int pos);

        /**
         * Delegates the parse routine to a sub class
         * @param token The token which to parse
         */
        public abstract void parseToken(String token, int curPos);

        protected boolean isInteger(String intValue) {
            try {
                Integer.parseInt(intValue);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        public LinkedHashMap<String, int[]> getBinaryEntries() {
            return binaryEntries;
        }

        /**
         * Gets the binary entry location of a given index from the array
         * @param index The index for which to retrieve the binary data location
         * @return
         */
        public int[] getBinaryEntryByIndex(int index) {
            int count = 0;
            for (Entry<String, int[]> entry : binaryEntries.entrySet()) {
                if (count == index) {
                    return entry.getValue();
                }
                count++;
            }
            return new int[0];
        }

        /**
         * Determines if more data is still to be parsed for the Postscript element.
         * @return Returns true if more data exists
         */
        public boolean hasMore() {
            return hasMore;
        }

        /**
         * Sets a value to be true if an expected entry postscript is found in the element.
         * An example is where the encoding table may have a series of postscript operators
         * altering the state of the array. In this case the only option will be to
         * fully embed the font to avoid incorrect encoding in the resulting subset.
         * @param foundUnexpected true if unexpected postscript is found.
         */
        protected void setFoundUnexpected(boolean foundUnexpected) {
            this.foundUnexpected = foundUnexpected;
        }

        /**
         * Returns whether unexpected postscript has been found in the element
         * @return true if unexpected postscript is found
         */
        public boolean getFoundUnexpected() {
            return this.foundUnexpected;
        }
    }

    /**
     * An object representing a Postscript array with a fixed number of entries
     */
    public class PSFixedArray extends PSElement {

        private String entry = "";
        private String token = "";
        private boolean finished;
        protected int binaryLength;
        /* A list containing each entry and it's contents in the array */
        private HashMap<Integer, String> entries;
        private static final String READ_ONLY = "readonly";

        public PSFixedArray(String operator, int startPoint) {
            super(operator, startPoint);
            entries = new HashMap<Integer, String>();
        }

        @Override
        public void parseToken(String token, int curPos) {
            if (!checkForEnd(token) || token.equals("def")) {
                hasMore = false;
                endPoint = curPos;
                return;
            }
            if (token.equals("dup")) {
                if (entry.startsWith("dup")) {
                    addEntry(entry);
                }
                entry = "";
                tokens.clear();
            }
            if (!token.equals(READ_ONLY)) {
                entry += token + " ";
            }
            if (!token.trim().equals("")) {
                tokens.add(token);
            }
            if (tokens.size() == 4 && tokens.get(0).equals("dup") && isInteger(tokens.get(2))) {
                binaryLength = Integer.parseInt(tokens.get(2));
                readBinary = true;
            }
        }

        private boolean checkForEnd(String checkToken) {
            boolean subFound = false;
            //Check for a subroutine matching that of an array end definition
            PSSubroutine sub = subroutines.get("/" + checkToken);
            if (sub != null && sub.getSubroutine().contains("def")) {
                subFound = true;
            }
            if (!finished && (subFound || checkToken.equals("def"))) {
                finished = true;
                addEntry(entry);
                return false;
            } else {
                return !finished;
            }
        }

        /**
         * Gets a map of array entries identified by index
         * @return Returns the map of array entries
         */
        public HashMap<Integer, String> getEntries() {
            return entries;
        }

        private void addEntry(String entry) {
            if (!entry.equals("")) {
                if (entry.indexOf('/') != -1 && entry.charAt(entry.indexOf('/') - 1) != ' ') {
                    entry = entry.replace("/", " /");
                }
                int entryLen;
                do {
                    entryLen = entry.length();
                    entry = entry.replace("  ", " ");
                } while (entry.length() != entryLen);
                Scanner s = new Scanner(entry).useDelimiter(" ");
                boolean valid = false;
                do {
                    s.next();
                    if (!s.hasNext()) {
                        break;
                    }
                    int id = s.nextInt();
                    entries.put(id, entry);
                    valid = true;
                } while (false);
                if (!valid) {
                    setFoundUnexpected(true);
                }
            }
        }

        @Override
        public void parseByte(byte cur, int pos) {
            if (binaryLength > 0) {
                token += (char)cur;
                binaryLength--;
            } else {
                if (readBinary) {
                    int bLength = Integer.parseInt(tokens.get(2));
                    int start = pos - bLength;
                    int end = start + bLength;
                    binaryEntries.put(tokens.get(1), new int[] {start, end});
                    token = "";
                    readBinary = false;
                } else {
                    tokens.add(token);
                    parseToken(token, pos);
                    token = "";
                }
            }
        }
    }

    /**
     * An object representing a Postscript array with a variable number of entries
     */
    public class PSVariableArray extends PSElement {
        private int level;
        private List<String> arrayItems;
        private String entry = "";

        public PSVariableArray(String operator, int startPoint) {
            super(operator, startPoint);
            arrayItems = new ArrayList<String>();
        }

        @Override
        public void parseToken(String token, int curPos) {
            entry += token + " ";
            if (level <= 0 && token.length() > 0 && token.charAt(0) == ']') {
                hasMore = false;
                endPoint = curPos;
                return;
            }
            /* If the array item is a subroutine, the following keeps track of the current level
             * of the tokens being parsed so that it can identify the finish */
            if (token.equals("{")) {
                level++;
            } else if (token.equals("}")) {
                level--;
                if (!entry.equals("") && level == 0) {
                    arrayItems.add(entry);
                    entry = "";
                }
            }
        }

        /**
         * Gets a list of found array entries within the variable array
         * @return Returns the found array elements as a list
         */
        public List<String> getEntries() {
            return arrayItems;
        }

        @Override
        public void parseByte(byte cur, int pos) {
            //Not currently used
        }
    }

    /**
     * An object representing a Postscript subroutine element
     */
    public class PSSubroutine extends PSElement {
        private int level = 1;
        private String entry = "";

        public PSSubroutine(String operator, int startPoint) {
            super(operator, startPoint);
        }

        @Override
        public void parseToken(String token, int curPos) {
            if (level == 0 && token.length() > 0 && (token.equals("def") || token.equals("ifelse")
                    || token.charAt(0) == '}')) {
                hasMore = false;
                endPoint = curPos;
                return;
            }
            if (token.equals("{")) {
                level++;
            } else if (token.equals("}")) {
                level--;
            }
            entry += token + " ";
        }

        /**
         * Gets the parsed subroutine element as unmodified string
         * @return The subroutine as a string
         */
        public String getSubroutine() {
            return entry.trim();
        }

        @Override
        public void parseByte(byte cur, int pos) {
            //Not currently used
        }
    }

    /**
     * An object representing a Postscript dictionary
     */
    public class PSDictionary extends PSElement {
        /* A list of dictionary entries which they themselves could be variables,
         * subroutines and arrays, This is currently left as parsed Strings as there is
         * no need to delve deeper for our current purposes. */
        private HashMap<String, String> entries;
        private String entry = "";
        private String token = "";
        protected int binaryLength;

        public PSDictionary(String operator, int startPoint) {
            super(operator, startPoint);
            entries = new HashMap<String, String>();
        }

        @Override
        public void parseToken(String token, int curPos) {
            if (token.equals("end")) {
                addEntry(entry);
                hasMore = false;
                endPoint = curPos;
                return;
            }
            if (token.startsWith("/")) {
                if (entry.trim().startsWith("/")) {
                    tokens.clear();
                    addEntry(entry);
                }
                entry = "";
            }
            if (tokens.size() >= 1 || token.startsWith("/")) {
                tokens.add(token);
            }
            entry += token + " ";
            if (tokens.size() == 3 && tokens.get(0).startsWith("/") && !tokens.get(2).equals("def")
                    && isInteger(tokens.get(1))) {
                binaryLength = Integer.parseInt(tokens.get(1));
                readBinary = true;
            }
        }

        /**
         * Gets a map of dictionary entries identified by their name
         * @return Returns the dictionary entries as a map
         */
        public HashMap<String, String> getEntries() {
            return entries;
        }

        private void addEntry(String entry) {
            Scanner s = new Scanner(entry).useDelimiter(" ");
            String id = s.next();
            entries.put(id, entry);
        }

        @Override
        public void parseByte(byte cur, int pos) {
            if (binaryLength > 0) {
                binaryLength--;
            } else {
                if (readBinary) {
                    int start = pos - Integer.parseInt(tokens.get(1));
                    int end = pos;
                    binaryEntries.put(tokens.get(0), new int[] {start, end});
                    readBinary = false;
                } else {
                    tokens.add(token);
                    parseToken(token, pos);
                }
            }
        }
    }

    /**
     * An object representing a Postscript variable
     */
    public class PSVariable extends PSElement {

        /* The value of the parsed Postscript variable. */
        private String value = "";

        public PSVariable(String operator, int startPoint) {
            super(operator, startPoint);
        }

        @Override
        public void parseToken(String token, int curPos) {
            if (token.equals("def")) {
                hasMore = false;
                endPoint = curPos;
                return;
            }
        }

        @Override
        public void parseByte(byte cur, int pos) {
            //Not currently used
        }

        /**
         * Sets the value of the Postscript variable value
         * @param value The value to set
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the Postscript variable
         * @return Returns the value as a String
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the end point location of the current Postscript variable.
         * @param endPoint The end point location as an integer
         */
        public void setEndPoint(int endPoint) {
            this.endPoint = endPoint;
        }

    }
}
