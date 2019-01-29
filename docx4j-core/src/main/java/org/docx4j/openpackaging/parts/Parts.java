/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

/*
 * Portions Copyright (c) 2006, Wygwam
 * With respect to those portions:
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * - Neither the name of Wygwam nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without 
 * specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.docx4j.openpackaging.parts;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of all the parts in the package
 * 
 * @author Jason Harrop; Julien Chable
 * @version 0.1
 */
public class Parts {

	private static Logger log = LoggerFactory.getLogger(Parts.class);

	private HashMap<PartName, Part> parts;

	public Parts() {
		parts = new HashMap<PartName, Part>();
	}

	public void put(Part part) {
		if (get(part.getPartName()) != null) {
			log.info("Overwriting existing part " + part.getPartName());
		} else {
			log.debug("put part " + part.getPartName());			
		}

		parts.put(part.getPartName(), part);
	}

	public Part get(PartName partName) {
		return (Part) parts.get(partName);
	}

	/**
	 * Getter method for parts
	 * 
	 * @return parts - A HashMap of all parts
	 */
	public HashMap<PartName, Part> getParts() {
		return parts;
	}

	public void remove(PartName partName) {

		if (get(partName) != null) {
			log.info("Deleting part " + partName);
			parts.remove(partName);
		} else {
			log.error("Couldn't delete part " + partName
					+ " - nothing by that name");
		}
	}

	// @Override
	// public Object clone() {
	// return super.clone();
	// }

}
