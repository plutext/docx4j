/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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
package org.docx4j.finders;

import java.util.List;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SectPrFindFirst  extends CallbackImpl {
	
	protected static Logger log = LoggerFactory.getLogger(SectPrFindFirst.class);
	
	public SectPr firstSectPr = null; 
	public P enclosingP;
	
	
	@Override
	public List<Object> apply(Object o) {
		
		if (firstSectPr==null
				&& (o instanceof P)) {
			P p = (P)o;
			if ( p.getPPr()!=null && p.getPPr().getSectPr()!=null ) {
//				System.out.println("Found sectpr!");
				enclosingP = (P)o; // just for sample MergeBlockRangeNViaTraversalUtilsSectPr
				firstSectPr = p.getPPr().getSectPr();
			}
		}
		return null;
	}
	
	public boolean shouldTraverse(Object o) {
		
//		System.out.println(o.getClass().getName() );
		
		return (firstSectPr==null) // Stop as soon as we've found it
		  && (!(o instanceof P
				|| o instanceof org.docx4j.wml.Tbl)); 
	}
	
}
