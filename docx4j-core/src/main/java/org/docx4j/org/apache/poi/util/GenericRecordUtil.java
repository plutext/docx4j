/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.docx4j.org.apache.poi.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1452")
@Internal
public final class GenericRecordUtil {
    private GenericRecordUtil() {}

    public static Map<String, Supplier<?>>
    getGenericProperties(String val1, Supplier<?> sup1) {
        return Collections.singletonMap(val1, sup1);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
        String val1, Supplier<?> sup1,
        String val2, Supplier<?> sup2
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
            String val1, Supplier<?> sup1,
            String val2, Supplier<?> sup2,
            String val3, Supplier<?> sup3
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, val3, sup3, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
            String val1, Supplier<?> sup1,
            String val2, Supplier<?> sup2,
            String val3, Supplier<?> sup3,
            String val4, Supplier<?> sup4
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, val3, sup3, val4, sup4, null, null, null, null, null, null, null, null, null, null);

    }

    public static Map<String, Supplier<?>> getGenericProperties(
            String val1, Supplier<?> sup1,
            String val2, Supplier<?> sup2,
            String val3, Supplier<?> sup3,
            String val4, Supplier<?> sup4,
            String val5, Supplier<?> sup5
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, val3, sup3, val4, sup4, val5, sup5, null, null, null, null, null, null, null, null);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
            String val1, Supplier<?> sup1,
            String val2, Supplier<?> sup2,
            String val3, Supplier<?> sup3,
            String val4, Supplier<?> sup4,
            String val5, Supplier<?> sup5,
            String val6, Supplier<?> sup6
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, val3, sup3, val4, sup4, val5, sup5, val6, sup6, null, null, null, null, null, null);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
        String val1, Supplier<?> sup1,
        String val2, Supplier<?> sup2,
        String val3, Supplier<?> sup3,
        String val4, Supplier<?> sup4,
        String val5, Supplier<?> sup5,
        String val6, Supplier<?> sup6,
        String val7, Supplier<?> sup7
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, val3, sup3, val4, sup4, val5, sup5, val6, sup6, val7, sup7, null, null, null, null);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
        String val1, Supplier<?> sup1,
        String val2, Supplier<?> sup2,
        String val3, Supplier<?> sup3,
        String val4, Supplier<?> sup4,
        String val5, Supplier<?> sup5,
        String val6, Supplier<?> sup6,
        String val7, Supplier<?> sup7,
        String val8, Supplier<?> sup8
    ) {
        return getGenericProperties(val1, sup1, val2, sup2, val3, sup3, val4, sup4, val5, sup5, val6, sup6, val7, sup7, val8, sup8, null, null);
    }

    public static Map<String, Supplier<?>> getGenericProperties(
        String val1, Supplier<?> sup1,
        String val2, Supplier<?> sup2,
        String val3, Supplier<?> sup3,
        String val4, Supplier<?> sup4,
        String val5, Supplier<?> sup5,
        String val6, Supplier<?> sup6,
        String val7, Supplier<?> sup7,
        String val8, Supplier<?> sup8,
        String val9, Supplier<?> sup9
    ) {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();

        final String[] vals = { val1, val2, val3, val4, val5, val6, val7, val8, val9 };
        final Supplier<?>[] sups = { sup1, sup2, sup3, sup4, sup5, sup6, sup7, sup8, sup9 };

        for (int i=0; i<vals.length && vals[i] != null; i++) {
            assert(sups[i] != null);
            if ("base".equals(vals[i])) {
                Object baseMap = sups[i].get();
                assert(baseMap instanceof Map);
                //noinspection unchecked
                m.putAll((Map<String,Supplier<?>>)baseMap);
            } else {
                m.put(vals[i], sups[i]);
            }
        }

        return Collections.unmodifiableMap(m);
    }

    public static <T extends Enum<?>> Supplier<T> safeEnum(T[] values, Supplier<Number> ordinal) {
        return safeEnum(values, ordinal, null);
    }

    public static <T extends Enum<?>> Supplier<T> safeEnum(T[] values, Supplier<Number> ordinal, T defaultVal) {
        int ord = ordinal.get().intValue();
        return () -> (0 <= ord && ord < values.length) ? values[ord] : defaultVal;
    }

    public static Supplier<AnnotatedFlag> getBitsAsString(Supplier<Number> flags, final BitField[] masks, final String[] names) {
        int[] iMasks = Arrays.stream(masks).mapToInt(BitField::getMask).toArray();
        return () -> new AnnotatedFlag(flags, iMasks, names, false);
    }

    public static Supplier<AnnotatedFlag> getBitsAsString(Supplier<Number> flags, final int[] masks, final String[] names) {
        return () -> new AnnotatedFlag(flags, masks, names, false);
    }

    public static Supplier<AnnotatedFlag> getEnumBitsAsString(Supplier<Number> flags, final int[] masks, final String[] names) {
        return () -> new AnnotatedFlag(flags, masks, names, true);
    }

    public static class AnnotatedFlag {
        private final Supplier<Number> value;
        private final Map<Integer,String> masks = new LinkedHashMap<>();
        private final boolean exactMatch;

        AnnotatedFlag(Supplier<Number> value, int[] masks, String[] names, boolean exactMatch) {
            assert(masks.length == names.length);

            this.value = value;
            this.exactMatch = exactMatch;
            for (int i=0; i<masks.length; i++) {
                this.masks.put(masks[i], names[i]);
            }
        }

        public Supplier<Number> getValue() {
            return value;
        }

        public String getDescription() {
            final int val = value.get().intValue();
            return masks.entrySet().stream().
                filter(e -> match(val, e.getKey())).
                map(Map.Entry::getValue).
                collect(Collectors.joining(" | "));
        }

        private boolean match(final int val, int mask) {
            return exactMatch ? (val == mask) : ((val & mask) == mask);
        }
    }
}
