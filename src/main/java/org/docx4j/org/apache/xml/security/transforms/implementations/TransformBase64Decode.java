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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.docx4j.org.apache.xml.security.c14n.CanonicalizationException;
import org.docx4j.org.apache.xml.security.exceptions.Base64DecodingException;
import org.docx4j.org.apache.xml.security.signature.XMLSignatureInput;
import org.docx4j.org.apache.xml.security.transforms.Transform;
import org.docx4j.org.apache.xml.security.transforms.TransformSpi;
import org.docx4j.org.apache.xml.security.transforms.TransformationException;
import org.docx4j.org.apache.xml.security.transforms.Transforms;
import org.docx4j.org.apache.xml.security.utils.Base64;
import org.docx4j.org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Implements the <CODE>http://www.w3.org/2000/09/xmldsig#base64</CODE> decoding
 * transform.
 *
 * <p>The normative specification for base64 decoding transforms is
 * <A HREF="http://www.w3.org/TR/2001/CR-xmldsig-core-20010419/#ref-MIME">[MIME]</A>.
 * The base64 Transform element has no content. The input
 * is decoded by the algorithms. This transform is useful if an
 * application needs to sign the raw data associated with the encoded
 * content of an element. </p>
 *
 * <p>This transform requires an octet stream for input.
 * If an XPath node-set (or sufficiently functional alternative) is
 * given as input, then it is converted to an octet stream by
 * performing operations logically equivalent to 1) applying an XPath
 * transform with expression self::text(), then 2) taking the string-value
 * of the node-set. Thus, if an XML element is identified by a barename
 * XPointer in the Reference URI, and its content consists solely of base64
 * encoded character data, then this transform automatically strips away the
 * start and end tags of the identified element and any of its descendant
 * elements as well as any descendant comments and processing instructions.
 * The output of this transform is an octet stream.</p>
 *
 * @author Christian Geuer-Pollmann
 * @see org.docx4j.org.apache.xml.security.utils.Base64
 */
public class TransformBase64Decode extends TransformSpi {

    /** Field implementedTransformURI */
    public static final String implementedTransformURI =
        Transforms.TRANSFORM_BASE64_DECODE;

    /**
     * Method engineGetURI
     *
     * @inheritDoc
     */
    protected String engineGetURI() {
        return TransformBase64Decode.implementedTransformURI;
    }

    /**
     * Method enginePerformTransform
     *
     * @param input
     * @return {@link XMLSignatureInput} as the result of transformation
     * @inheritDoc
     * @throws CanonicalizationException
     * @throws IOException
     * @throws TransformationException
     */
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, Transform transformObject
    ) throws IOException, CanonicalizationException, TransformationException {
        return enginePerformTransform(input, null, transformObject);
    }

    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, OutputStream os, Transform transformObject
    ) throws IOException, CanonicalizationException, TransformationException {
        try {
            if (input.isElement()) {
                Node el = input.getSubNode();
                if (input.getSubNode().getNodeType() == Node.TEXT_NODE) {         	
                    el = el.getParentNode();
                }
                StringBuilder sb = new StringBuilder();
                traverseElement((Element)el, sb);
                if (os == null) {
                    byte[] decodedBytes = Base64.decode(sb.toString());            
                    XMLSignatureInput output = new XMLSignatureInput(decodedBytes);
                    output.setSecureValidation(secureValidation);
                    return output;
                } 
                Base64.decode(sb.toString(), os);
                XMLSignatureInput output = new XMLSignatureInput((byte[])null);
                output.setSecureValidation(secureValidation);
                output.setOutputStream(os);
                return output;
            }
            
            if (input.isOctetStream() || input.isNodeSet()) {
                if (os == null) {
                    byte[] base64Bytes = input.getBytes();
                    byte[] decodedBytes = Base64.decode(base64Bytes);            
                    XMLSignatureInput output = new XMLSignatureInput(decodedBytes);
                    output.setSecureValidation(secureValidation);
                    return output;
                } 
                if (input.isByteArray() || input.isNodeSet()) {
                    Base64.decode(input.getBytes(), os);
                } else {
                    Base64.decode(new BufferedInputStream(input.getOctetStreamReal()), os);
                }
                XMLSignatureInput output = new XMLSignatureInput((byte[])null);
                output.setSecureValidation(secureValidation);
                output.setOutputStream(os);
                return output;
            } 

            try {
                //Exceptional case there is current not text case testing this(Before it was a
                //a common case).
                Document doc = 
                    XMLUtils.createDocumentBuilder(false, secureValidation).parse(input.getOctetStream());

                Element rootNode = doc.getDocumentElement();
                StringBuilder sb = new StringBuilder();
                traverseElement(rootNode, sb);
                byte[] decodedBytes = Base64.decode(sb.toString());
                XMLSignatureInput output = new XMLSignatureInput(decodedBytes);
                output.setSecureValidation(secureValidation);
                return output;
            } catch (ParserConfigurationException e) {
                throw new TransformationException(e, "c14n.Canonicalizer.Exception");
            } catch (SAXException e) {
                throw new TransformationException(e, "SAX exception");
            }      
        } catch (Base64DecodingException e) {
            throw new TransformationException(e, "Base64Decoding");
        }
    }

    void traverseElement(Element node, StringBuilder sb) {
        Node sibling = node.getFirstChild();
        while (sibling != null) {
            switch (sibling.getNodeType()) {
            case Node.ELEMENT_NODE:
                traverseElement((Element)sibling, sb);
                break;
            case Node.TEXT_NODE:
                sb.append(((Text)sibling).getData());
            }
            sibling = sibling.getNextSibling();
        }
    }  
}
