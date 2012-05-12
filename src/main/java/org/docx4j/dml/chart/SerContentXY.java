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
