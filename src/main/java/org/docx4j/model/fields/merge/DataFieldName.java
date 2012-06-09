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
 
package org.docx4j.model.fields.merge;

/**
 * The name of the data field.
 * 
 * When Word performs a mail merge, it treats this as case-insensitive
 * (and takes the first matching field).
 * 
 * The purpose of this class is to ensure the key provided is
 * case-insensitive.
 * 
 * @author jharrop
 *
 */
public class DataFieldName {
	
	String name;
	
	public DataFieldName(String name) {
		
		this.name = name.toUpperCase();
	}
	
	
	@Override public boolean equals(Object aThat) {
	    
		if (aThat instanceof DataFieldName) {
			return ( name.equals(
					((DataFieldName)aThat).name
					) );	    
		} else {
			return super.equals(aThat);
		}
	}
	
	@Override public int hashCode() {
		return name.hashCode();
	}

}
