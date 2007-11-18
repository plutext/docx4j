/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
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

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.Package;



/**
 * A collection of all the parts in the package
 * 
 * @author Jason Harrop; Julien Chable
 * @version 0.1
 */
public class Parts {
	
	private static Logger log = Logger.getLogger(Parts.class);
	

	private HashMap parts;

	public Parts() {
		parts = new HashMap();
	}

	public void put(Part part) {
		if (get( part.getPartName() )!=null ) {
			log.warn("Overwriting existing part " + part.getPartName() );
		}
		
		parts.put(part.getPartName(), part);
	}

	public Part get(PartName partName) {
		return (Part)parts.get(partName);
	}
	
	public void remove(PartName partName) {
		
		if (get( partName )!=null ) {
			log.info("Deleting part " + partName );
			parts.remove(partName);
		} else {
			log.error("Couldn't delete part " + partName + " - nothing by that name");
		}
	}
	
//	@Override
//	public Object clone() {
//		return super.clone();
//	}
	
}