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
