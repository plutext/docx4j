package org.docx4j.model.datastorage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingTraverserTableHelper {

    private static Logger log = LoggerFactory.getLogger(BindingTraverserTableHelper.class);
    
    /**
     * Get table cell width from tc and table style from tbl and set it to xHTMLImporter.
     * @param tbl
     * @param tc
     * @param xHTMLImporter
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    public static void setupMaxWidthAndStyleForTc(Tbl tbl, Tc tc, Object xHTMLImporter, Method m) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if(tc.getTcPr() != null  && tc.getTcPr().getTcW() != null) {
            TblWidth tcW = tc.getTcPr().getTcW();
            if (tcW.getW()!= null && tcW.getType().equals(TblWidth.TYPE_DXA)) {
                int maxWidth = tcW.getW().intValue();
                String styleVal = null;
                if(tbl != null && tbl.getTblPr() != null && tbl.getTblPr().getTblStyle() != null) {
                    styleVal = tbl.getTblPr().getTblStyle().getVal();
                }
                log.debug("inserting in a tc, with maxwidth: " + maxWidth + ", and table style: " + styleVal);
                //xHTMLImporter.setMaxWidth(maxWidth, styleVal);
				m.invoke(xHTMLImporter, maxWidth, styleVal);

            } else {
                log.debug("w:tcPr/w:tcW present, but width not in dxa units ");
            }
        } else {
            log.debug("w:tcPr/w:tcW not present");
        }
    }
}
