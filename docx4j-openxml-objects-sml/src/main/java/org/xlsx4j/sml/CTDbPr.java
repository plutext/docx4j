/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DbPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DbPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="connection" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="command" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="serverCommand" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="commandType" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="2" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DbPr")
public class CTDbPr implements Child
{

    @XmlAttribute(name = "connection", required = true)
    protected String connection;
    @XmlAttribute(name = "command")
    protected String command;
    @XmlAttribute(name = "serverCommand")
    protected String serverCommand;
    @XmlAttribute(name = "commandType")
    @XmlSchemaType(name = "unsignedInt")
    protected Long commandType;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the connection property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnection() {
        return connection;
    }

    /**
     * Sets the value of the connection property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnection(String value) {
        this.connection = value;
    }

    /**
     * Gets the value of the command property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the value of the command property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommand(String value) {
        this.command = value;
    }

    /**
     * Gets the value of the serverCommand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServerCommand() {
        return serverCommand;
    }

    /**
     * Sets the value of the serverCommand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServerCommand(String value) {
        this.serverCommand = value;
    }

    /**
     * Gets the value of the commandType property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCommandType() {
        if (commandType == null) {
            return  2L;
        } else {
            return commandType;
        }
    }

    /**
     * Sets the value of the commandType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCommandType(Long value) {
        this.commandType = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
