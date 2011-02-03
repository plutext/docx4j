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

import java.util.NoSuchElementException;

/**
 * A tokeniser that produces a single token for every block of text.
 * This provides coarser grained tokens than producing
 * a token for every sentence or word.
 */
public class TextTokeniserSingleBlock implements TextTokeniser {

    CharSequence seq;
    boolean gotToken = false;

    public TextTokeniserSingleBlock(CharSequence seq) {
        this.seq = seq;
    }
    @Override
    public int countTokens() {
        return 1;
    }

    @Override
    public TextEvent nextToken() throws NoSuchElementException {
        if (gotToken) throw new NoSuchElementException("already got all tokens");
        gotToken = true;
        return new CharactersEvent(seq);
    }

    @Override
    public void useRepertory(Repertory repertory) {

    }
}

