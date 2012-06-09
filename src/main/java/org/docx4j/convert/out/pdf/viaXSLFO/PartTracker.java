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
 
package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.HashMap;

import org.docx4j.model.TransformState;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;

/**
 * The XSLT applies mainly to the MainDocumentPart,
 * but it also processes some other parts (eg headers, footers),
 * and for those, it is sometimes necessary
 * to know which part we're in (eg image processing).
 * 
 * @author jharrop
 *
 */
public class PartTracker implements TransformState {
	
	Part currentPart;

	public Part getCurrentPart() {
		return currentPart;
	}

	private void setCurrentPart(Part currentPart) {
		this.currentPart = currentPart;
	}

	public static void setPartTrackerState(Part currentPart, 
			HashMap<String, TransformState> modelStates) {
		
		PartTracker pt = (PartTracker)modelStates.get(Conversion.PART_TRACKER); 
		pt.setCurrentPart(currentPart);		
	}

	public static Part getPartTrackerState( 
			HashMap<String, TransformState> modelStates) {
		
		PartTracker pt = (PartTracker)modelStates.get(Conversion.PART_TRACKER); 
		return pt.getCurrentPart();		
	}
	
	public static void inMainDocumentPart(WordprocessingMLPackage wordmlPackage,
			HashMap<String, TransformState> modelStates) {
		PartTracker.setPartTrackerState(wordmlPackage.getMainDocumentPart(),
				modelStates);
	}
	
}
