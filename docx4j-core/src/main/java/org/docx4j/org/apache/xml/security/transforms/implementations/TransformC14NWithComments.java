/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.docx4j.org.apache.xml.security.transforms.implementations;

import java.io.OutputStream;

import org.docx4j.org.apache.xml.security.c14n.CanonicalizationException;
import org.docx4j.org.apache.xml.security.c14n.implementations.Canonicalizer20010315WithComments;
import org.docx4j.org.apache.xml.security.signature.XMLSignatureInput;
import org.docx4j.org.apache.xml.security.transforms.Transform;
import org.docx4j.org.apache.xml.security.transforms.TransformSpi;
import org.docx4j.org.apache.xml.security.transforms.Transforms;

/**
 * Implements the <CODE>http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments</CODE>
 * transform.
 *
 * @author Christian Geuer-Pollmann
 */
public class TransformC14NWithComments extends TransformSpi {

    /** Field implementedTransformURI */
    public static final String implementedTransformURI =
        Transforms.TRANSFORM_C14N_WITH_COMMENTS;

    /** @inheritDoc */
    protected String engineGetURI() {
        return implementedTransformURI;
    }

    /** @inheritDoc */
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, OutputStream os, Transform transformObject
    ) throws CanonicalizationException {

        Canonicalizer20010315WithComments c14n = new Canonicalizer20010315WithComments();
        c14n.setSecureValidation(secureValidation);
        if (os != null) {
            c14n.setWriter(os);
        }

        byte[] result = null;
        result = c14n.engineCanonicalize(input);         		         	         
        XMLSignatureInput output = new XMLSignatureInput(result);  
        output.setSecureValidation(secureValidation);
        if (os != null) {
            output.setOutputStream(os);
        }
        return output;      
    }
}
