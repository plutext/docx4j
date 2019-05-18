
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d package. 
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

    private final static QName _Model3D_QNAME = new QName("http://schemas.microsoft.com/office/drawing/2017/model3d", "model3D");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CTModel3D }
     * 
     */
    public CTModel3D createCTModel3D() {
        return new CTModel3D();
    }

    /**
     * Create an instance of {@link CTPositiveRatio }
     * 
     */
    public CTPositiveRatio createCTPositiveRatio() {
        return new CTPositiveRatio();
    }

    /**
     * Create an instance of {@link CTRotate3D }
     * 
     */
    public CTRotate3D createCTRotate3D() {
        return new CTRotate3D();
    }

    /**
     * Create an instance of {@link CTScale3D }
     * 
     */
    public CTScale3D createCTScale3D() {
        return new CTScale3D();
    }

    /**
     * Create an instance of {@link CTModel3DTransform }
     * 
     */
    public CTModel3DTransform createCTModel3DTransform() {
        return new CTModel3DTransform();
    }

    /**
     * Create an instance of {@link CTOrthographicProjection }
     * 
     */
    public CTOrthographicProjection createCTOrthographicProjection() {
        return new CTOrthographicProjection();
    }

    /**
     * Create an instance of {@link CTPerspectiveProjection }
     * 
     */
    public CTPerspectiveProjection createCTPerspectiveProjection() {
        return new CTPerspectiveProjection();
    }

    /**
     * Create an instance of {@link CTModel3DCamera }
     * 
     */
    public CTModel3DCamera createCTModel3DCamera() {
        return new CTModel3DCamera();
    }

    /**
     * Create an instance of {@link CTModel3DRaster }
     * 
     */
    public CTModel3DRaster createCTModel3DRaster() {
        return new CTModel3DRaster();
    }

    /**
     * Create an instance of {@link CTObjectViewport }
     * 
     */
    public CTObjectViewport createCTObjectViewport() {
        return new CTObjectViewport();
    }

    /**
     * Create an instance of {@link CTWindowViewport }
     * 
     */
    public CTWindowViewport createCTWindowViewport() {
        return new CTWindowViewport();
    }

    /**
     * Create an instance of {@link CTAmbientLight }
     * 
     */
    public CTAmbientLight createCTAmbientLight() {
        return new CTAmbientLight();
    }

    /**
     * Create an instance of {@link CTPointLight }
     * 
     */
    public CTPointLight createCTPointLight() {
        return new CTPointLight();
    }

    /**
     * Create an instance of {@link CTSpotLight }
     * 
     */
    public CTSpotLight createCTSpotLight() {
        return new CTSpotLight();
    }

    /**
     * Create an instance of {@link CTDirectionalLight }
     * 
     */
    public CTDirectionalLight createCTDirectionalLight() {
        return new CTDirectionalLight();
    }

    /**
     * Create an instance of {@link CTUnknownLight }
     * 
     */
    public CTUnknownLight createCTUnknownLight() {
        return new CTUnknownLight();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CTModel3D }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/office/drawing/2017/model3d", name = "model3D")
    public JAXBElement<CTModel3D> createModel3D(CTModel3D value) {
        return new JAXBElement<CTModel3D>(_Model3D_QNAME, CTModel3D.class, null, value);
    }

}
