
package org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.xlsx4j.schemas.microsoft.com.office.excel_2006.main.CTWorksheet;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main package. 
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

    private final static QName _Macrosheet_QNAME = new QName("http://schemas.microsoft.com/office/excel/2008/2/main", "macrosheet");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTDefinedNameArgumentDescriptions }
     * 
     */
    public CTDefinedNameArgumentDescriptions createCTDefinedNameArgumentDescriptions() {
        return new CTDefinedNameArgumentDescriptions();
    }

    /**
     * Create an instance of {@link CTDefinedName }
     * 
     */
    public CTDefinedName createCTDefinedName() {
        return new CTDefinedName();
    }

    /**
     * Create an instance of {@link CTDefinedNameArgumentDescription }
     * 
     */
    public CTDefinedNameArgumentDescription createCTDefinedNameArgumentDescription() {
        return new CTDefinedNameArgumentDescription();
    }

    /**
     * Create an instance of {@link CTDefinedNames }
     * 
     */
    public CTDefinedNames createCTDefinedNames() {
        return new CTDefinedNames();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTWorksheet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/excel/2008/2/main", name = "macrosheet")
    public JAXBElement<CTWorksheet> createMacrosheet(CTWorksheet value) {
        return new JAXBElement<CTWorksheet>(_Macrosheet_QNAME, CTWorksheet.class, null, value);
    }

}
