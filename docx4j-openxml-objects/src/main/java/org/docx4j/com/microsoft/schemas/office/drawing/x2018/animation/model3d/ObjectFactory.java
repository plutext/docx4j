
package org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d package. 
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

    private final static QName _EmbedAnim_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2018/animation/model3d", "embedAnim");
    private final static QName _PosterFrame_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2018/animation/model3d", "posterFrame");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTEmbeddedAnimation }
     * 
     */
    public CTEmbeddedAnimation createCTEmbeddedAnimation() {
        return new CTEmbeddedAnimation();
    }

    /**
     * Create an instance of {@link CTPosterFrame }
     * 
     */
    public CTPosterFrame createCTPosterFrame() {
        return new CTPosterFrame();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTEmbeddedAnimation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2018/animation/model3d", name = "embedAnim")
    public JAXBElement<CTEmbeddedAnimation> createEmbedAnim(CTEmbeddedAnimation value) {
        return new JAXBElement<CTEmbeddedAnimation>(_EmbedAnim_QNAME, CTEmbeddedAnimation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTPosterFrame }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2018/animation/model3d", name = "posterFrame")
    public JAXBElement<CTPosterFrame> createPosterFrame(CTPosterFrame value) {
        return new JAXBElement<CTPosterFrame>(_PosterFrame_QNAME, CTPosterFrame.class, null, value);
    }

}
