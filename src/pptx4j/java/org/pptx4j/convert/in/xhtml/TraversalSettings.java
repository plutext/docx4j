package org.pptx4j.convert.in.xhtml;

import java.util.Map;

import org.w3c.dom.css.CSSValue;

public class TraversalSettings implements Cloneable {
    
    private Map<String, CSSValue> cssMap;
    
    private String hyperlink;

    private boolean inTableCell;

    public Map<String, CSSValue> getCssMap() {
        return cssMap;
    }

    public void setCssMap(Map<String, CSSValue> cssMap) {
        this.cssMap = cssMap;
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    public boolean isHyperlinkTraversal() {
        return hyperlink != null;
    }

    public boolean isInTableCell() {
        return inTableCell;
    }

    public void setInTableCell(boolean inTableCell) {
        this.inTableCell = inTableCell;
    }

    public TraversalSettings clone(){
        try {
            return (TraversalSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
