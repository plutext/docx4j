
package org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AutoBuNodeInfoLst_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2016/11/diagram", "autoBuNodeInfoLst");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTNumberDiagramInfoList }
     * 
     */
    public CTNumberDiagramInfoList createCTNumberDiagramInfoList() {
        return new CTNumberDiagramInfoList();
    }

    /**
     * Create an instance of {@link CTDiagramAutoBullet }
     * 
     */
    public CTDiagramAutoBullet createCTDiagramAutoBullet() {
        return new CTDiagramAutoBullet();
    }

    /**
     * Create an instance of {@link CTNumberDiagramInfo }
     * 
     */
    public CTNumberDiagramInfo createCTNumberDiagramInfo() {
        return new CTNumberDiagramInfo();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTNumberDiagramInfoList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2016/11/diagram", name = "autoBuNodeInfoLst")
    public JAXBElement<CTNumberDiagramInfoList> createAutoBuNodeInfoLst(CTNumberDiagramInfoList value) {
        return new JAXBElement<CTNumberDiagramInfoList>(_AutoBuNodeInfoLst_QNAME, CTNumberDiagramInfoList.class, null, value);
    }

}
