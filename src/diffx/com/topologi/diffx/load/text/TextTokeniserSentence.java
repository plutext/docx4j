/*
 *  Copyright 2007-2008, Plutext Pty Ltd or in the case of a Contribution,
 *  the relevant Contributor
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package com.topologi.diffx.load.text;

import com.topologi.diffx.event.TextEvent;
import com.topologi.diffx.event.impl.CharactersEvent;
import com.topologi.diffx.event.lang.Repertory;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * A tokeniser that produces a single token for every sentence, as delimited by
 * <code>'.' '?' '!'</code>.   This provides coarser grained tokens than producing
 * a token for every word.
 */
public class TextTokeniserSentence implements TextTokeniser {

    private List<String> tokens;
    private int index = 0;
    public TextTokeniserSentence(CharSequence seq) {
        String str;
        if (seq instanceof String) str = (String)seq;
        else str = seq.toString();
        StringTokenizer st = new StringTokenizer(str, ".!?", true);
        tokens = new ArrayList();
        String lastText = "";
        while (st.hasMoreTokens()) {
            str = st.nextToken();
            if (isPunc(str)) {
                tokens.add(lastText + str);
                lastText = "";
            } else {
                lastText = str;
            }
        }
        // get the last
        if (!"".equals(lastText)) tokens.add(lastText);
        //p("tokenized ##" + seq + "## to count=" + countTokens());
    }

    private boolean isPunc(String s) {
        if (s.length() != 1) return false;
        char c = s.charAt(0);
        return c == '.' || c == '?' || c == '!';
    }
    @Override
    public int countTokens() {
        return tokens.size();
    }

    @Override
    public TextEvent nextToken() throws NoSuchElementException {
        if (index >= countTokens())
            throw new NoSuchElementException("all tokens passed");
        return new CharactersEvent(tokens.get(index++));
    }

    @Override
    public void useRepertory(Repertory repertory) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private static void p(String s) {
        System.out.println("[TextTokeniser] " + s);
    }
}
