/*
 *  Copyright 2013, Plutext Pty Ltd.
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

package org.docx4j.model.fields;

import org.docx4j.openpackaging.exceptions.Docx4JException;


@SuppressWarnings("serial")
public class FieldFormattingException extends Docx4JException {

	public FieldFormattingException(String message){
		super(message);
	}
	
	public FieldFormattingException(String msg, Exception e) {
		super(msg, e);
	}

	public FieldFormattingException(String msg, Throwable t) {
		super(msg, t);
	}
	
}