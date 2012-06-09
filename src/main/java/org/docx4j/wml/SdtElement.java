/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.wml;

import org.jvnet.jaxb2_commons.ppp.Child;

//import javax.xml.bind.Unmarshaller;

/**
 * This interface is implemented by the 
 * classes which represent a content
 * control (ie SdtBlock, SdtRun, CTSdtRow, CTSdtCell). 
 * 
 * @since 2.7
 *
 */
public interface SdtElement  {

    /**
     * Gets the value of the sdtPr property.
     * 
     * @return
     *     possible object is
     *     {@link SdtPr }
     *     
     */
    public SdtPr getSdtPr();

    /**
     * Sets the value of the sdtPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link SdtPr }
     *     
     */
    public void setSdtPr(SdtPr value);

    /**
     * Gets the value of the sdtEndPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtEndPr }
     *     
     */
    public CTSdtEndPr getSdtEndPr();

    /**
     * Sets the value of the sdtEndPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtEndPr }
     *     
     */
    public void setSdtEndPr(CTSdtEndPr value);

    
    public ContentAccessor getSdtContent();
    
//    /**
//     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
//     * 
//     * @param parent
//     *     The parent object in the object tree.
//     * @param unmarshaller
//     *     The unmarshaller that generated the instance.
//     */
//    void afterUnmarshal(Unmarshaller unmarshaller, Object parent);

}