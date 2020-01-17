/*
 *  Copyright 2015-2016, Plutext Pty Ltd.
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
package org.docx4j.services.client;

import org.apache.http.HttpResponse;


/**
 * @author jharrop
 * @since 3.3.0
 */
@SuppressWarnings("serial")
public class ConversionException extends Exception {

	private HttpResponse response;
	
	public ConversionException(String msg) {
		super(msg);
	}

	public ConversionException(String msg, Exception e, HttpResponse response) {
		super(msg, e);
		this.response=response;
	}

	public ConversionException(String msg, HttpResponse response) {
		super(msg);
		this.response=response;
	}
	
	public ConversionException(HttpResponse response) {
		super();
		this.response=response;
	}
	
	public ConversionException(String msg, Exception e) {
		super(msg, e);
	}

	public ConversionException(String msg, Throwable t) {
		super(msg, t);
	}

	public HttpResponse getResponse() {
		return response;
	}

	
}