/*
 * Copyright (c) 2006, Wygwam
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

package org.docx4j.document.wordprocessingml;

public interface Constants {

	// TODO -replace with Tag class 16/11/07xx
	
	/////////////////////////////////////////////////////////////////////////////
	// 				Open Xml word keywords
	////////////////////////////////////////////////////////////////////////////
	public static final String WORD_DOC_BODY_TAG_NAME = "body";

	//table
	public static final String TABLE_BODY_TAG_NAME = "tbl";
	public static final String TABLE_PROPERTIES_TAG_NAME="tblPr";
	public static final String TABLE_PROPERTIES_PREFRED_TABLE_WIDTH="tblW";
	public static final String TABLE_BORDER_TAG_NAME = "tblBorders";
	public static final String TABLE_BORDER_TOP_TAG_NAME = "top";
	public static final String TABLE_BORDER_LEFT_TAG_NAME ="left";
	public static final String TABLE_BORDER_RIGHT_TAG_NAME ="right";
	public static final String TABLE_BORDER_BOTTOM_TAG_NAME ="bottom";
	public static final String TABLE_BORDER_INSIDE_V_TAG_NAME ="insideV";
	public static final String TABLE_BORDER_INSIDE_H_TAG_NAME ="insideH";
	public static final String TABLE_CELL="tc";
	public static final String TABLE_CELL_PROPERTIES="tcPr";
	public static final String TABLE_CELL_SHADING="shd";
	public static final String TABLE_CELL_HORIZONTAL_WIDTH="gridSpan";

	public static final String TABLE_CELL_WIDTH="tcW";
	public static final String TABLE_CELL_WIDTH_VALUE="w";
	public static final String TABLE_CELL_WIDTH_TYPE="type";

	public static final String TABLE_CELL_ROW= "tr";

	//paragraph
	public static final String RUN_VERTICAL_ALIGNEMENT_PROPERTY_TAG_NAME = "vertAlign";
	public static final String RUN_FONT_SIZE_PROPERTY_TAG_NAME = "sz";
	public static final String RUN_UNDERLINE_PROPERTY_TAG_NAME = "u";
	public static final String RUN_ITALIC_PROPERTY_TAG_NAME = "i";
	public static final String RUN_BOLD_PROPERTY_TAG_NAME = "b";
	public static final String RUN_PROPERTIES_TAG_NAME = "rPr";
	public static final String RUN_TEXT = "t";

	public static final String PARAGRAPH_RUN_TAG_NAME = "r";
	public static final String PROPERTIES_VALUE_TAG_NAME = "val";
	public static final String PARAGRAPH_ALIGNEMENT_TAG_NAME = "jc";
	public static final String PARAGRAPH_PROPERTIES_TAG_NAME = "pPr";
	public static final String PARAGRAPH_BODY_TAG_NAME = "p";
	public static final String PARAGRAPH_NUMBERING_TAG_NAME="numPr";
	public static final String PARAGRAPH_NUMBERING_LEVEL_REFERENCE="ilvl";
	public static final String PARAGRAPH_NUMBERING_REFERENCE="numId";
	public static final String PARAGRAPH_SPACING="spacing"; //space before/after
	public static final String PARAGRAPH_SPACING_BEFORE="before";
	public static final String PARAGRAPH_SPACING_AFTER="after";

	public static final String PARAGRAPH_STYLE="pStyle";
	public static final String PARAGRAPH_PERM_START_TAG_NAME = "permStart";
	public static final String PARAGRAPH_PERM_END_TAG_NAME = "permEnd";
	public static final String PARAGRAPH_PERM_ID_TAG_NAME = "id";

	//run content
	public static final String BR = "br";
	public static final String CR = "cr";
	
	// misc
	public static final String ATTRIBUTE_VAL="val";
	public static final String ATTRIBUTE_SIZE="sz";
	public static final String ATTRIBUTE_SPACE="space";
	public static final String ATTRIBUTE_COLOR="color";
	public static final String ATTRIBUTE_FILL = "fill"; //used for TABLE_CELL_SHADING

	public static final String VALUE_AUTO="auto";

	//keyword in setting.xml
	public static final String WORD_DOC_PROTECTION = "documentProtection";
	public static final String PROPERTIES_EDIT_TAG_NAME = "edit";
	public static final String PROPERTIES_READ_ONLY = "readOnly";
	public static final String PROPERTIES_ENFORCEMENT_TAG_NAME = "enforcement";
	public static final String PROPERTIES_ENFORCEMENT_ON = "1";
	public static final String SETTINGS_ROOT="settings";

	public static final String ID_TAG_NAME = "id";//TODO see also PARAGRAPH_PERM_ID_TAG_NAME

	public static final String IMAGEDATA_TAG_NAME = "imagedata";
	public static final String PICTURE_TAG_NAME = "pict";
	public static final String SHAPE_TAG_NAME="shape";
}