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
 
package org.docx4j.openpackaging.parts;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.OpcPackage;

/**
 * A collection of all the parts in the package
 * 
 * @author Jason Harrop; Julien Chable
 * @version 0.1
 */
public class Parts {

	private static Logger log = Logger.getLogger(Parts.class);

	private HashMap<PartName, Part> parts;

	public Parts() {
		parts = new HashMap<PartName, Part>();
	}

	public void put(Part part) {
		if (get(part.getPartName()) != null) {
			log.warn("Overwriting existing part " + part.getPartName());
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