
package org.docx4j.w16cid;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.w16cid package. 
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

    private final static QName _CommentsIds_QNAME = new QName("http://schemas.microsoft.com/office/word/2016/wordml/cid", "commentsIds");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.w16cid
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTCommentsIds }
     * 
     */
    public CTCommentsIds createCTCommentsIds() {
        return new CTCommentsIds();
    }

    /**
     * Create an instance of {@link CTCommentId }
     * 
     */
    public CTCommentId createCTCommentId() {
        return new CTCommentId();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTCommentsIds }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/word/2016/wordml/cid", name = "commentsIds")
    public JAXBElement<CTCommentsIds> createCommentsIds(CTCommentsIds value) {
        return new JAXBElement<CTCommentsIds>(_CommentsIds_QNAME, CTCommentsIds.class, null, value);
    }

}
