
package org.opendope.SmartArt.dataHierarchy;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opendope.SmartArt.dataHierarchy package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opendope.SmartArt.dataHierarchy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SmartArtDataHierarchy }
     * 
     */
    public SmartArtDataHierarchy createSmartArtDataHierarchy() {
        return new SmartArtDataHierarchy();
    }

    /**
     * Create an instance of {@link SmartArtDataHierarchy.Texts.IdentifiedText }
     * 
     */
    public SmartArtDataHierarchy.Texts.IdentifiedText createSmartArtDataHierarchyTextsIdentifiedText() {
        return new SmartArtDataHierarchy.Texts.IdentifiedText();
    }

    /**
     * Create an instance of {@link SmartArtDataHierarchy.Images }
     * 
     */
    public SmartArtDataHierarchy.Images createSmartArtDataHierarchyImages() {
        return new SmartArtDataHierarchy.Images();
    }

    /**
     * Create an instance of {@link Node }
     * 
     */
    public Node createNode() {
        return new Node();
    }

    /**
     * Create an instance of {@link SmartArtDataHierarchy.Images.Image }
     * 
     */
    public SmartArtDataHierarchy.Images.Image createSmartArtDataHierarchyImagesImage() {
        return new SmartArtDataHierarchy.Images.Image();
    }

    /**
     * Create an instance of {@link SmartArtDataHierarchy.Texts }
     * 
     */
    public SmartArtDataHierarchy.Texts createSmartArtDataHierarchyTexts() {
        return new SmartArtDataHierarchy.Texts();
    }

}
