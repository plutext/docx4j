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

import java.util.List;
import org.docx4j.dml.CTShapeProperties;

/**
 * Set/Get the content for Bubble and scatter graphic  */ public
interface SerContentXY {

    public CTUnsignedInt getIdx();
    public void setIdx(CTUnsignedInt parValue);

    public CTUnsignedInt getOrder();
    public void setOrder(CTUnsignedInt parValue);

    public CTShapeProperties getSpPr();
    public void setSpPr(CTShapeProperties parValue);

    public List<CTDPt> getDPt();

    public CTDLbls getDLbls();
    public void setDLbls(CTDLbls parValue);

    public List<CTTrendline> getTrendline();

    public List<CTErrBars> getErrBars();

    // Tx (series)
    public CTSerTx getTx();
    public void setTx(CTSerTx parValue);

    // Categories
    public CTNumDataSource getYVal();
    public void setYVal(CTNumDataSource parValue);

    // Values
    public CTAxDataSource getXVal();
    public void setXVal(CTAxDataSource parValue);

}
