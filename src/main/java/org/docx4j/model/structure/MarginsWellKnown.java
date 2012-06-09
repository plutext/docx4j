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
 
package org.docx4j.model.structure;

/**
 * These margin settings reflect the Word 2007 user interface.
 * 
 * w:header="708" w:footer="708" w:gutter="0" are all invariant
 * across these
 * 
 *  Normal: all are 1 inch = 2.54 cm
 *  Narrow: all are 1/2 inch
 *  Wide: left/right are 2 inch
 * 
 * @since 2.7
 */
public enum MarginsWellKnown {

		    NORMAL,     // <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>
		    NARROW,     // <w:pgMar w:top="720"  w:right="720"  w:bottom="720"  w:left="720" w:header="708" w:footer="708" w:gutter="0"/>
		    MODERATE, // <w:pgMar w:top="1440" w:right="1080" w:bottom="1440" w:left="1080" w:header="708" w:footer="708" w:gutter="0"/>
		    WIDE;         // <w:pgMar w:top="1440" w:right="2880" w:bottom="1440" w:left="2880" w:header="708" w:footer="708" w:gutter="0"/>
		    
	  }
