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
 
package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.conditions.Condition;

public class ConditionsPart extends JaxbCustomXmlDataStoragePart<org.opendope.conditions.Conditions> {
	
	private static Logger log = Logger.getLogger(ConditionsPart.class);		
	
	public ConditionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ConditionsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public static Condition getConditionById(
			org.opendope.conditions.Conditions conditions,
			String id) {
		
		for (Condition c : conditions.getCondition() ) {
			
			if (c.getId().equals(id))
				return c;
		}
		
		log.warn("Condition " + id + " is missing");
		return null;
	}

}
