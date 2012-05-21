/**
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.utils;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.CTAltChunk;

/**
 * @author jharrop
 * @since 2.8
 */
public class AltChunkFinder extends CallbackImpl {
	
	private List<LocatedChunk> altChunks = new ArrayList<LocatedChunk>(); 
	

	@Override
	public List<Object> apply(Object o) {
		// Not used
		return null;
	}
	
	public List<Object> apply(Object o, List contentList, int index) {
		
		if (o instanceof CTAltChunk) {				
			getAltChunks().add(
					new LocatedChunk((CTAltChunk)o, contentList, index));
		}			
		return null;
	}
	
	@Override
	public void walkJAXBElements(Object parent) {
		
		
		List children = getChildren(parent);
		if (children != null) {

			int index = 0;
			for (Object o : children) {

				o = XmlUtils.unwrap(o);
				
				this.apply(o, children, index);

				if (this.shouldTraverse(o)) {
					walkJAXBElements(o);
				}
				index++;

			}
		}
	}
	
	
	public List<LocatedChunk> getAltChunks() {
		return altChunks;
	}


	/**
	 * Track the parent of altChunk.  This is a workaround
	 * for the problem getting the parent of something
	 * wrapped in a JAXBElement (which is sometimes the
	 * case).
	 * 
	 * @author jharrop
	 *
	 */
	public static class LocatedChunk {
		
		LocatedChunk(CTAltChunk altChunk, List contentList, int index) {
			this.setAltChunk(altChunk);
			this.contentList = contentList;
			this.index=index;
		}
		
		public CTAltChunk getAltChunk() {
			return altChunk;
		}
		public void setAltChunk(CTAltChunk altChunk) {
			this.altChunk = altChunk;
		}

		private CTAltChunk altChunk;		
		List contentList;
		public List getContentList() {
			return contentList;
		}
		int index;
		public int getIndex() {
			return index;			
		}
	}
}