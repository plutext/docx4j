package org.docx4j.wml;



/**
 * Content controls (Sdt) contain properties (SdtPr) and content (SdtContent).
 * 
 * In JAXB, there are different Java objects for the content controls, depending on whether they
 * wrap block-level, row-level, cell-level, or run-level content (ie SdtBlock, SdtRun, CTSdtRow, CTSdtCell).  
 * 
 * Those objects all implement this SdtElement interface.
 * 
 * Similarly, there are different Java objects for the SdtContent, depending on whether they
 * wrap block-level, row-level, cell-level, or run-level content.  Those objects all implement the SdtContent interface
 * (new in 3.3.4).
 * 
 * @since 2.7
 */
public interface SdtElement  {

    /**
     * Gets the value of the sdtPr property.
     * 
     * @return
     *     possible object is
     *     {@link SdtPr }
     *     
     */
    public SdtPr getSdtPr();

    /**
     * Sets the value of the sdtPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link SdtPr }
     *     
     */
    public void setSdtPr(SdtPr value);

    /**
     * Gets the value of the sdtEndPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtEndPr }
     *     
     */
    public CTSdtEndPr getSdtEndPr();

    /**
     * Sets the value of the sdtEndPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtEndPr }
     *     
     */
    public void setSdtEndPr(CTSdtEndPr value);

    
    /**
     * Changed in v3.3.4
     * @return
     */
    public SdtContent getSdtContent();
    
    /**
     * @param sdtContent
     * @since 3.3.4
     */
    public void setSdtContent(SdtContent sdtContent);
    
//    /**
//     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
//     * 
//     * @param parent
//     *     The parent object in the object tree.
//     * @param unmarshaller
//     *     The unmarshaller that generated the instance.
//     */
//    void afterUnmarshal(Unmarshaller unmarshaller, Object parent);

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     * @since 6.0.0
     */
    public Object getParent();
    
}