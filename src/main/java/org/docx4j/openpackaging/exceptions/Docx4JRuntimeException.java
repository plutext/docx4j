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
 
package org.docx4j.openpackaging.exceptions;

/**
 * Global exception throws when a critical error occurs (this exception is
 * set as Runtime in order not to force the user to manage the exception in a
 * try/catch).
 * 
 * @author Julien Chable
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Docx4JRuntimeException extends RuntimeException {

	public Docx4JRuntimeException(String msg) {
		super(msg);
	}
}