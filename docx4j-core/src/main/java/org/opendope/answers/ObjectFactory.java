
package org.opendope.answers;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opendope.answers package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opendope.answers
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Repeat }
     * 
     */
    public Repeat createRepeat() {
        return new Repeat();
    }

    /**
     * Create an instance of {@link Answers }
     * 
     */
    public Answers createAnswers() {
        return new Answers();
    }

    /**
     * Create an instance of {@link Repeat.Row }
     * 
     */
    public Repeat.Row createRepeatRow() {
        return new Repeat.Row();
    }

    /**
     * Create an instance of {@link Answer }
     * 
     */
    public Answer createAnswer() {
        return new Answer();
    }

}
