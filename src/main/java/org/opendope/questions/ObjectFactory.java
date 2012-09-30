
package org.opendope.questions;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opendope.questions package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opendope.questions
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Question }
     * 
     */
    public Question createQuestion() {
        return new Question();
    }

    /**
     * Create an instance of {@link Questionnaire }
     * 
     */
    public Questionnaire createQuestionnaire() {
        return new Questionnaire();
    }

    /**
     * Create an instance of {@link Response.Fixed }
     * 
     */
    public Response.Fixed createResponseFixed() {
        return new Response.Fixed();
    }

    /**
     * Create an instance of {@link Questionnaire.Questions }
     * 
     */
    public Questionnaire.Questions createQuestionnaireQuestions() {
        return new Questionnaire.Questions();
    }

    /**
     * Create an instance of {@link Response.Free }
     * 
     */
    public Response.Free createResponseFree() {
        return new Response.Free();
    }

    /**
     * Create an instance of {@link Response }
     * 
     */
    public Response createResponse() {
        return new Response();
    }

    /**
     * Create an instance of {@link Response.Fixed.Item }
     * 
     */
    public Response.Fixed.Item createResponseFixedItem() {
        return new Response.Fixed.Item();
    }

}
