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
 
package org.docx4j.dml.chart;

import org.docx4j.dml.CTShapeProperties;

/**
 * Set/Get the content for all the graphics except Bubble and Scattered
Graphics
 * (elements organization are different for these graphics, see the
interface SerContentXY)  */ public interface SerContent {

    public CTUnsignedInt getIdx();
    public void setIdx(CTUnsignedInt parValue);

    public CTUnsignedInt getOrder();
    public void setOrder(CTUnsignedInt parValue);

    public CTShapeProperties getSpPr();
    public void setSpPr(CTShapeProperties parValue);

    public CTExtensionList getExtLst();
    public void setExtLst(CTExtensionList parValue);

    // Tx (series)
    public CTSerTx getTx();
    public void setTx(CTSerTx parValue);

    // Categories
    public CTAxDataSource getCat();
    public void setCat(CTAxDataSource parValue);

    // Values
    public CTNumDataSource getVal();
    public void setVal(CTNumDataSource parValue); }
