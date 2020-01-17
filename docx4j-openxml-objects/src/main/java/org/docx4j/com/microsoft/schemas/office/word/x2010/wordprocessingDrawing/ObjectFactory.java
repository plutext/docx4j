
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing package. 
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

    private final static QName _PctPosHOffset_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", "pctPosHOffset");
    private final static QName _PctPosVOffset_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", "pctPosVOffset");
    private final static QName _SizeRelH_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", "sizeRelH");
    private final static QName _SizeRelV_QNAME = new QName("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", "sizeRelV");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTSizeRelH }
     * 
     */
    public CTSizeRelH createCTSizeRelH() {
        return new CTSizeRelH();
    }

    /**
     * Create an instance of {@link CTSizeRelV }
     * 
     */
    public CTSizeRelV createCTSizeRelV() {
        return new CTSizeRelV();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", name = "pctPosHOffset")
    public JAXBElement<Integer> createPctPosHOffset(Integer value) {
        return new JAXBElement<Integer>(_PctPosHOffset_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", name = "pctPosVOffset")
    public JAXBElement<Integer> createPctPosVOffset(Integer value) {
        return new JAXBElement<Integer>(_PctPosVOffset_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSizeRelH }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", name = "sizeRelH")
    public JAXBElement<CTSizeRelH> createSizeRelH(CTSizeRelH value) {
        return new JAXBElement<CTSizeRelH>(_SizeRelH_QNAME, CTSizeRelH.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTSizeRelV }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing", name = "sizeRelV")
    public JAXBElement<CTSizeRelV> createSizeRelV(CTSizeRelV value) {
        return new JAXBElement<CTSizeRelV>(_SizeRelV_QNAME, CTSizeRelV.class, null, value);
    }

}
