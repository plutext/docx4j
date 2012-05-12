
package org.opendope.conditions;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opendope.conditions package. 
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

    private final static QName _False_QNAME = new QName("http://opendope.org/conditions", "false");
    private final static QName _True_QNAME = new QName("http://opendope.org/conditions", "true");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opendope.conditions
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Condition }
     * 
     */
    public Condition createCondition() {
        return new Condition();
    }

    /**
     * Create an instance of {@link And }
     * 
     */
    public And createAnd() {
        return new And();
    }

    /**
     * Create an instance of {@link Xpathref }
     * 
     */
    public Xpathref createXpathref() {
        return new Xpathref();
    }

    /**
     * Create an instance of {@link Not }
     * 
     */
    public Not createNot() {
        return new Not();
    }

    /**
     * Create an instance of {@link Or }
     * 
     */
    public Or createOr() {
        return new Or();
    }

    /**
     * Create an instance of {@link Conditions }
     * 
     */
    public Conditions createConditions() {
        return new Conditions();
    }

    /**
     * Create an instance of {@link Conditionref }
     * 
     */
    public Conditionref createConditionref() {
        return new Conditionref();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://opendope.org/conditions", name = "false")
    public JAXBElement<Object> createFalse(Object value) {
        return new JAXBElement<Object>(_False_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://opendope.org/conditions", name = "true")
    public JAXBElement<Object> createTrue(Object value) {
        return new JAXBElement<Object>(_True_QNAME, Object.class, null, value);
    }

}
