/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.docProps.extended;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.docx4j.docProps.extended package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.docx4j.docProps.extended
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Properties.TitlesOfParts }
     * 
     */
    public Properties.TitlesOfParts createPropertiesTitlesOfParts() {
        return new Properties.TitlesOfParts();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link Properties.HLinks }
     * 
     */
    public Properties.HLinks createPropertiesHLinks() {
        return new Properties.HLinks();
    }

    /**
     * Create an instance of {@link Properties.HeadingPairs }
     * 
     */
    public Properties.HeadingPairs createPropertiesHeadingPairs() {
        return new Properties.HeadingPairs();
    }

    /**
     * Create an instance of {@link Properties.DigSig }
     * 
     */
    public Properties.DigSig createPropertiesDigSig() {
        return new Properties.DigSig();
    }

}
