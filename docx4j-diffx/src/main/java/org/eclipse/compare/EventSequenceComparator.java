/*
 *  Copyright 2009, Plutext Pty Ltd.
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

package org.eclipse.compare;

import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

import com.topologi.diffx.Docx4jDriver;
import com.topologi.diffx.sequence.EventSequence;

public class EventSequenceComparator implements IRangeComparator {

	List<EventSequence> esList;
	
	public EventSequenceComparator(List<EventSequence> esList) {
		this.esList = esList;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
	 */
	public int getRangeCount() {
		return esList.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int, org.eclipse.compare.rangedifferencer.IRangeComparator, int)
	 */
	public boolean rangesEqual(int idx, IRangeComparator rc2, int idx2) {
		//boolean result = this.esList.get(idx).equals(((EventSequenceComparator)rc2).getItem(idx2) );
		boolean result = (this.esList.get(idx).hashCode() ==
				((EventSequenceComparator)rc2).getItem(idx2).hashCode() );
		//if (result) { Docx4jDriver.log("matched!");}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int, int, org.eclipse.compare.rangedifferencer.IRangeComparator)
	 */
	public boolean skipRangeComparison(int length, int maxLength,
			IRangeComparator other) {
		return false;
	}

	
	public EventSequence getItem(int idx) {
		return this.esList.get(idx);			
	}
	

}
