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

/* $Id$ */

package org.docx4j.fonts.fop.fonts.type1;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.fonts.Glyphs;
import org.docx4j.fonts.fop.fonts.NamedCharacter;
import org.docx4j.fonts.fop.fonts.type1.AFMParser.ValueHandler;

/**
 * A handler that parses the various types of character metrics in an AFM file.
 */
abstract class CharMetricsHandler {

    private static final Log LOG = LogFactory.getLog(CharMetricsHandler.class);

    private static final String WHITE_SPACE = "\\s*";
    private static final String OPERATOR = "([A-Z0-9]{1,3})";
    private static final String OPERANDS = "(.*)";

    private static final Pattern METRICS_REGEX = Pattern.compile(
             WHITE_SPACE + OPERATOR + WHITE_SPACE + OPERANDS + WHITE_SPACE);
    private static final Pattern SPLIT_REGEX = Pattern.compile(WHITE_SPACE + ";" + WHITE_SPACE);

    private CharMetricsHandler() {
    }

    abstract AFMCharMetrics parse(String line, Stack<Object> stack, String afmFileName)
            throws IOException;

    static CharMetricsHandler getHandler(Map<String, ValueHandler> valueParsers,
            String line) {
        if (line != null && line.contains(AdobeStandardEncoding.NAME)) {
            return new AdobeStandardCharMetricsHandler(valueParsers);
        } else {
            return new DefaultCharMetricsHandler(valueParsers);
        }
    }

    private static final class DefaultCharMetricsHandler extends CharMetricsHandler {
        private final Map<String, ValueHandler> valueParsers;


        private DefaultCharMetricsHandler(Map<String, ValueHandler> valueParsers) {
            this.valueParsers = valueParsers;
        }

        AFMCharMetrics parse(String line, Stack<Object> stack, String afmFileName)
                throws IOException {
            AFMCharMetrics chm = new AFMCharMetrics();
            stack.push(chm);
            String[] metrics = SPLIT_REGEX.split(line);
            for (String metric : metrics) {
                Matcher matcher = METRICS_REGEX.matcher(metric);
                if (matcher.matches()) {
                    String operator = matcher.group(1);
                    String operands = matcher.group(2);
                    ValueHandler handler = valueParsers.get(operator);
                    if (handler != null) {
                        handler.parse(operands, 0, stack);
                    }
                }
            }
            stack.pop();
            return chm;
        }
    }

    private static final class AdobeStandardCharMetricsHandler extends CharMetricsHandler {
        private final DefaultCharMetricsHandler defaultHandler;

        private AdobeStandardCharMetricsHandler(Map<String, ValueHandler> valueParsers) {
            defaultHandler = new DefaultCharMetricsHandler(valueParsers);
        }

        AFMCharMetrics parse(String line, Stack<Object> stack, String afmFileName)
                throws IOException {
            AFMCharMetrics chm = defaultHandler.parse(line, stack, afmFileName);
            NamedCharacter namedChar = chm.getCharacter();
            if (namedChar != null) {
                String charName = namedChar.getName();
                int codePoint = AdobeStandardEncoding.getAdobeCodePoint(charName);
                if (chm.getCharCode() != codePoint && !Glyphs.NOTDEF.equals(charName)) {
                    LOG.info(afmFileName + ": named character '" + charName + "'"
                            + " has an incorrect code point: " + chm.getCharCode()
                            + ". Changed to " + codePoint);
                    chm.setCharCode(codePoint);
                }
            }
            return chm;
        }
    }

}
