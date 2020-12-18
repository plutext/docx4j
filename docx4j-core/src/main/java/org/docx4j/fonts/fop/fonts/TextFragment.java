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

package org.docx4j.fonts.fop.fonts;

import java.text.CharacterIterator;

/**
 * Encapsulates a sub-sequence (fragement) of a text iterator (or other text source),
 * where begin index and end index are indices into larger text iterator that denote
 * [begin,end) of sub-sequence range. Additionally associated with a designated script
 * (or "auto"), a designated language (or "none"), and a (single) bidi level (or -1
 * if not known).
 */
public interface TextFragment {

    /**
     * Obtain reference to underlying iterator.
     */
    CharacterIterator getIterator();

    /**
     * Obtain beginning index (inclusive) of sub-sequence of fragment in overall text source.
     */
    int getBeginIndex();

    /**
     * Obtain ending index (exclusive) of sub-sequence of fragment in overall text source.
     */
    int getEndIndex();

    /**
     * Obtain associated script (if designated) or "auto" if not.
     */
    String getScript();

    /**
     * Obtain associated language (if designated) or "none" if not.
     */
    String getLanguage();

    /**
     * Obtain associated bidi level (if known) or -1 if not.
     */
    int getBidiLevel();

    /**
     * Obtain character at specified index within this fragment's sub-sequence,
     * where index 0 corresponds to beginning index in overal text source, and
     * subSequenceIndex must be less than ending index - beginning index.
     */
    char charAt(int subSequenceIndex);

    CharSequence subSequence(int startIndex, int endIndex);
}
