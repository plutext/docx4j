/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.model.fields;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.STFldCharType;

	
public class FieldLocator extends CallbackImpl {
	
	/**
	 * A list of paragraphs containing field begins.
	 * 
	 * If the paragraph contains 2 fields or nested fields, 
	 * it will still be listed just once
	 */
	List<P> starts = new ArrayList<P>();	
	public List<P> getStarts() {
		return starts;
	}

	P currentP;
	int depth=0;

	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof P) {
			currentP = (P)o;
		}
		
		if (o instanceof org.docx4j.wml.FldChar) {
			FldChar fldChar = (FldChar)o;
			if (fldChar.getFldCharType().equals(STFldCharType.BEGIN) ) {
				//System.out.println("Found a BEGIN");
				depth++;
				if (depth==1 && !starts.contains(currentP)) {
					starts.add(currentP);
				}
			}
			if (fldChar.getFldCharType().equals(STFldCharType.SEPARATE) ) {
				depth--;
			}
		}
		
		return null;
	}
}
	

