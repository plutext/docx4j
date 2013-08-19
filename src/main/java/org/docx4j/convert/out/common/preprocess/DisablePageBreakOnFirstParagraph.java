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
package org.docx4j.convert.out.common.preprocess;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;

/**
 * Workaround for https://issues.apache.org/bugzilla/show_bug.cgi?id=54094  
 * You can disable this step if you are using FOP post 1.1
 */
public class DisablePageBreakOnFirstParagraph {
	public static void process(WordprocessingMLPackage wmlPackage) {
		Object o = wmlPackage.getMainDocumentPart().getContent().get(0);
		if (o instanceof P
				&& ((P)o).getPPr()!=null) {
			PPr pPr = ((P)o).getPPr();
			BooleanDefaultTrue val = new BooleanDefaultTrue();
			val.setVal(Boolean.FALSE);
			pPr.setPageBreakBefore(val);
		}
	}
}
