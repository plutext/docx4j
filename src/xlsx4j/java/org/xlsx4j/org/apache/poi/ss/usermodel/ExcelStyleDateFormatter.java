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
package org.xlsx4j.org.apache.poi.ss.usermodel;

import java.util.*;
import java.math.RoundingMode;
import java.text.*;

import org.docx4j.org.apache.poi.util.LocaleUtil;

/**
 * A wrapper around a {@link SimpleDateFormat} instance,
 * which handles a few Excel-style extensions that
 * are not supported by {@link SimpleDateFormat}.
 * Currently, the extensions are around the handling
 * of elapsed time, eg rendering 1 day 2 hours
 * as 26 hours.
 */
public class ExcelStyleDateFormatter extends SimpleDateFormat {
    public static final char MMMMM_START_SYMBOL = '\ue001';
    public static final char MMMMM_TRUNCATE_SYMBOL = '\ue002';
    public static final char H_BRACKET_SYMBOL = '\ue010';
    public static final char HH_BRACKET_SYMBOL = '\ue011';
    public static final char M_BRACKET_SYMBOL = '\ue012';
    public static final char MM_BRACKET_SYMBOL = '\ue013';
    public static final char S_BRACKET_SYMBOL = '\ue014';
    public static final char SS_BRACKET_SYMBOL = '\ue015';
    public static final char L_BRACKET_SYMBOL = '\ue016';
    public static final char LL_BRACKET_SYMBOL = '\ue017';

    private static final DecimalFormat format1digit;
    private static final DecimalFormat format2digits;

    private static final DecimalFormat format3digit;
    private static final DecimalFormat format4digits;

    static {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ROOT);
        format1digit = new DecimalFormat("0", dfs);
        format2digits = new DecimalFormat("00", dfs);
        format3digit = new DecimalFormat("0", dfs);
        format4digits = new DecimalFormat("00", dfs);
        DataFormatter.setExcelStyleRoundingMode(format1digit, RoundingMode.DOWN);
        DataFormatter.setExcelStyleRoundingMode(format2digits, RoundingMode.DOWN);
        DataFormatter.setExcelStyleRoundingMode(format3digit);
        DataFormatter.setExcelStyleRoundingMode(format4digits);
    }
    
    {
        setTimeZone(LocaleUtil.getUserTimeZone());
    }

    private double dateToBeFormatted;

    // no-arg constructor is private because of undefined super call with locale

    public ExcelStyleDateFormatter(String pattern) {
        super(processFormatPattern(pattern), LocaleUtil.getUserLocale());
    }

    public ExcelStyleDateFormatter(String pattern,
                                   DateFormatSymbols formatSymbols) {
        super(processFormatPattern(pattern), formatSymbols);
    }

    public ExcelStyleDateFormatter(String pattern, Locale locale) {
        super(processFormatPattern(pattern), locale);
    }

    /**
     * Takes a format String, and replaces Excel specific bits
     * with our detection sequences
     */
    private static String processFormatPattern(String f) {
        String t = f.replaceAll("MMMMM", MMMMM_START_SYMBOL + "MMM" + MMMMM_TRUNCATE_SYMBOL);
        t = t.replaceAll("\\[H]", String.valueOf(H_BRACKET_SYMBOL));
        t = t.replaceAll("\\[HH]", String.valueOf(HH_BRACKET_SYMBOL));
        t = t.replaceAll("\\[m]", String.valueOf(M_BRACKET_SYMBOL));
        t = t.replaceAll("\\[mm]", String.valueOf(MM_BRACKET_SYMBOL));
        t = t.replaceAll("\\[s]", String.valueOf(S_BRACKET_SYMBOL));
        t = t.replaceAll("\\[ss]", String.valueOf(SS_BRACKET_SYMBOL));
        t = t.replaceAll("s.000", "s.SSS");
        t = t.replaceAll("s.00", "s." + LL_BRACKET_SYMBOL);
        t = t.replaceAll("s.0", "s." + L_BRACKET_SYMBOL);
        return t;
    }

    /**
     * Used to let us know what the date being
     * formatted is, in Excel terms, which we
     * may wish to use when handling elapsed
     * times.
     */
    public void setDateToBeFormatted(double date) {
        this.dateToBeFormatted = date;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer paramStringBuffer,
                               FieldPosition paramFieldPosition) {
        // Do the normal format
        String s = super.format(date, paramStringBuffer, paramFieldPosition).toString();

        // Now handle our special cases
        if (s.indexOf(MMMMM_START_SYMBOL) != -1) {
            s = s.replaceAll(
                    MMMMM_START_SYMBOL + "(\\p{L}|\\p{P})[\\p{L}\\p{P}]+" + MMMMM_TRUNCATE_SYMBOL,
                    "$1"
            );
        }

        if (s.indexOf(H_BRACKET_SYMBOL) != -1 ||
                s.indexOf(HH_BRACKET_SYMBOL) != -1) {
            float hours = (float) dateToBeFormatted * 24;

            s = s.replaceAll(
                    String.valueOf(H_BRACKET_SYMBOL),
                    format1digit.format(hours)
            );
            s = s.replaceAll(
                    String.valueOf(HH_BRACKET_SYMBOL),
                    format2digits.format(hours)
            );
        }

        if (s.indexOf(M_BRACKET_SYMBOL) != -1 ||
                s.indexOf(MM_BRACKET_SYMBOL) != -1) {
            float minutes = (float) dateToBeFormatted * 24 * 60;
            s = s.replaceAll(
                    String.valueOf(M_BRACKET_SYMBOL),
                    format1digit.format(minutes)
            );
            s = s.replaceAll(
                    String.valueOf(MM_BRACKET_SYMBOL),
                    format2digits.format(minutes)
            );
        }
        if (s.indexOf(S_BRACKET_SYMBOL) != -1 ||
                s.indexOf(SS_BRACKET_SYMBOL) != -1) {
            float seconds = (float) (dateToBeFormatted * 24.0 * 60.0 * 60.0);
            s = s.replaceAll(
                    String.valueOf(S_BRACKET_SYMBOL),
                    format1digit.format(seconds)
            );
            s = s.replaceAll(
                    String.valueOf(SS_BRACKET_SYMBOL),
                    format2digits.format(seconds)
            );
        }

        if (s.indexOf(L_BRACKET_SYMBOL) != -1 ||
                s.indexOf(LL_BRACKET_SYMBOL) != -1) {
            float millisTemp = (float) ((dateToBeFormatted - Math.floor(dateToBeFormatted)) * 24.0 * 60.0 * 60.0);
            float millis = (millisTemp - (int) millisTemp);
            s = s.replaceAll(
                    String.valueOf(L_BRACKET_SYMBOL),
                    format3digit.format(millis * 10)
            );
            s = s.replaceAll(
                    String.valueOf(LL_BRACKET_SYMBOL),
                    format4digits.format(millis * 100)
            );
        }

        return new StringBuffer(s);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExcelStyleDateFormatter)) {
            return false;
        }
        
        ExcelStyleDateFormatter other = (ExcelStyleDateFormatter) o;
        return dateToBeFormatted == other.dateToBeFormatted;
    }
    
    @Override
    public int hashCode() {
        return new Double(dateToBeFormatted).hashCode();
    }
}
