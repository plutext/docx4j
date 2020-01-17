
package org.xlsx4j.schemas.microsoft.com.office.excel_2006.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.xlsx4j.schemas.microsoft.com.office.excel_2006.main package. 
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

    private final static QName _Macrosheet_QNAME = new QName("http://schemas.microsoft.com/office/excel/2006/main", "macrosheet");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.xlsx4j.schemas.microsoft.com.office.excel_2006.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTWorksheet }
     * 
     */
    public CTWorksheet createCTWorksheet() {
        return new CTWorksheet();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWorksheet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/excel/2006/main", name = "macrosheet")
    public JAXBElement<CTWorksheet> createMacrosheet(CTWorksheet value) {
        return new JAXBElement<CTWorksheet>(_Macrosheet_QNAME, CTWorksheet.class, null, value);
    }

}
