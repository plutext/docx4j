/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.fo;

import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;

/**
 * Converts the document to fo with a visitor
 * 
 * Uses Delegate: org.docx4j.convert.out.fo.FOExporterVisitorDelegate
 * Uses Generator: org.docx4j.convert.out.fo.FOExporterVisitorGenerator 
 * 
 * @since 3.0
 */
public class FOExporterVisitor extends AbstractFOExporter {
	protected static final FOExporterVisitorDelegate EXPORTER_DELEGATE_INSTANCE = new 
			FOExporterVisitorDelegate();
	
	protected static FOExporterVisitor instance = null;
	
	protected FOExporterVisitor() {
		super(EXPORTER_DELEGATE_INSTANCE);
	}
	
	public static Exporter<FOSettings> getInstance() {
		if (instance == null) {
			synchronized(FOExporterVisitor.class) {
				if (instance == null) {
					instance = new FOExporterVisitor();
				}
			}
		}
		return instance;
	}

}
