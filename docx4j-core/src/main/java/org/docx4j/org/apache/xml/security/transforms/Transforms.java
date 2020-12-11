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
package org.docx4j.org.apache.xml.security.transforms;

import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.org.apache.xml.security.c14n.CanonicalizationException;
import org.docx4j.org.apache.xml.security.c14n.Canonicalizer;
import org.docx4j.org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.docx4j.org.apache.xml.security.exceptions.XMLSecurityException;
import org.docx4j.org.apache.xml.security.signature.XMLSignatureException;
import org.docx4j.org.apache.xml.security.signature.XMLSignatureInput;
import org.docx4j.org.apache.xml.security.utils.Constants;
import org.docx4j.org.apache.xml.security.utils.SignatureElementProxy;
import org.docx4j.org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Holder of the {@link org.docx4j.org.apache.xml.security.transforms.Transform} steps to 
 * be performed on the data.
 * The input to the first Transform is the result of dereferencing the 
 * <code>URI</code> attribute of the <code>Reference</code> element.
 * The output from the last Transform is the input for the 
 * <code>DigestMethod algorithm</code>
 *
 * @author Christian Geuer-Pollmann
 * @see Transform
 * @see org.docx4j.org.apache.xml.security.signature.Reference
 */
public class Transforms extends SignatureElementProxy {

    /** Canonicalization - Required Canonical XML (omits comments) */
    public static final String TRANSFORM_C14N_OMIT_COMMENTS 
        = Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    
    /** Canonicalization - Recommended Canonical XML with Comments */
    public static final String TRANSFORM_C14N_WITH_COMMENTS 
        = Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    
    /** Canonicalization - Required Canonical XML 1.1 (omits comments) */
    public static final String TRANSFORM_C14N11_OMIT_COMMENTS 
        = Canonicalizer.ALGO_ID_C14N11_OMIT_COMMENTS;
    
    /** Canonicalization - Recommended Canonical XML 1.1 with Comments */
    public static final String TRANSFORM_C14N11_WITH_COMMENTS 
        = Canonicalizer.ALGO_ID_C14N11_WITH_COMMENTS;
    
    /** Canonicalization - Required Exclusive Canonicalization (omits comments) */
    public static final String TRANSFORM_C14N_EXCL_OMIT_COMMENTS 
        = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    
    /** Canonicalization - Recommended Exclusive Canonicalization with Comments */
    public static final String TRANSFORM_C14N_EXCL_WITH_COMMENTS 
        = Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
    
    /** Transform - Optional XSLT */
    public static final String TRANSFORM_XSLT 
        = "http://www.w3.org/TR/1999/REC-xslt-19991116";
    
    /** Transform - Required base64 decoding */
    public static final String TRANSFORM_BASE64_DECODE 
        = Constants.SignatureSpecNS + "base64";
    
    /** Transform - Recommended XPath */
    public static final String TRANSFORM_XPATH 
        = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    
    /** Transform - Required Enveloped Signature */
    public static final String TRANSFORM_ENVELOPED_SIGNATURE 
        = Constants.SignatureSpecNS + "enveloped-signature";
    
    /** Transform - XPointer */
    public static final String TRANSFORM_XPOINTER 
        = "http://www.w3.org/TR/2001/WD-xptr-20010108";
    
    /** Transform - XPath Filter */
    public static final String TRANSFORM_XPATH2FILTER 
        = "http://www.w3.org/2002/06/xmldsig-filter2";
    
    private static org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(Transforms.class);

    private Element[] transforms;

    protected Transforms() { }
    
    private boolean secureValidation;

    /**
     * Constructs {@link Transforms}.
     *
     * @param doc the {@link Document} in which <code>XMLSignature</code> will 
     * be placed
     */
    public Transforms(Document doc) {
        super(doc);
        addReturnToSelf();
    }

    /**
     * Constructs {@link Transforms} from {@link Element} which is 
     * <code>Transforms</code> Element
     *
     * @param element  is <code>Transforms</code> element
     * @param baseURI the URI where the XML instance was stored
     * @throws DOMException
     * @throws InvalidTransformException
     * @throws TransformationException
     * @throws XMLSecurityException
     * @throws XMLSignatureException
     */
    public Transforms(Element element, String baseURI)
        throws DOMException, XMLSignatureException, InvalidTransformException, 
            TransformationException, XMLSecurityException {
        super(element, baseURI);

        int numberOfTransformElems = this.getLength();

        if (numberOfTransformElems == 0) {
            // At least one Transform element must be present. Bad.
            Object exArgs[] = { Constants._TAG_TRANSFORM, Constants._TAG_TRANSFORMS };

            throw new TransformationException("xml.WrongContent", exArgs);
        }
    }
    
    /**
     * Set whether secure validation is enabled or not. The default is false.
     */
    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    /**
     * Adds the <code>Transform</code> with the specified <code>Transform 
     * algorithm URI</code>
     *
     * @param transformURI the URI form of transform that indicates which 
     * transformation is applied to data
     * @throws TransformationException
     */
    public void addTransform(String transformURI) throws TransformationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Transforms.addTransform(" + transformURI + ")");
            }

            Transform transform = new Transform(getDocument(), transformURI);

            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException(ex);
        }
    }

    /**
     * Adds the <code>Transform</code> with the specified <code>Transform 
     * algorithm URI</code>
     *
     * @param transformURI the URI form of transform that indicates which 
     * transformation is applied to data
     * @param contextElement
     * @throws TransformationException
     */
    public void addTransform(String transformURI, Element contextElement)
       throws TransformationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Transforms.addTransform(" + transformURI + ")");
            }

            Transform transform = new Transform(getDocument(), transformURI, contextElement);

            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException(ex);
        }
    }

    /**
     * Adds the <code>Transform</code> with the specified <code>Transform 
     * algorithm URI</code>.
     *
     * @param transformURI the URI form of transform that indicates which 
     * transformation is applied to data
     * @param contextNodes
     * @throws TransformationException
     */
    public void addTransform(String transformURI, NodeList contextNodes)
       throws TransformationException {

        try {
            Transform transform = new Transform(getDocument(), transformURI, contextNodes);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException(ex);
        }
    }

    /**
     * Adds a user-provided Transform step.
     *
     * @param transform {@link Transform} object
     */
    private void addTransform(Transform transform) {
        if (log.isDebugEnabled()) {
            log.debug("Transforms.addTransform(" + transform.getURI() + ")");
        }

        Element transformElement = transform.getElement();

        appendSelf(transformElement);
        addReturnToSelf();
    }

    /**
     * Applies all included <code>Transform</code>s to xmlSignatureInput and 
     * returns the result of these transformations.
     *
     * @param xmlSignatureInput the input for the <code>Transform</code>s
     * @return the result of the <code>Transforms</code>
     * @throws TransformationException
     */
    public XMLSignatureInput performTransforms(
        XMLSignatureInput xmlSignatureInput
    ) throws TransformationException {
        return performTransforms(xmlSignatureInput, null);
    }
   
    /**
     * Applies all included <code>Transform</code>s to xmlSignatureInput and 
     * returns the result of these transformations.
     *
     * @param xmlSignatureInput the input for the <code>Transform</code>s
     * @param os where to output the last transformation.
     * @return the result of the <code>Transforms</code>
     * @throws TransformationException
     */
    public XMLSignatureInput performTransforms(
        XMLSignatureInput xmlSignatureInput, OutputStream os
    ) throws TransformationException {
        try {
            int last = this.getLength() - 1;
            for (int i = 0; i < last; i++) {
                Transform t = this.item(i);
                if (log.isDebugEnabled()) {
                    String uri = t.getURI();
                    log.debug("Perform the (" + i + ")th " + uri + " transform");
                }
                checkSecureValidation(t);
                xmlSignatureInput = t.performTransform(xmlSignatureInput);
            }
            if (last >= 0) {
                Transform t = this.item(last);
                if (log.isDebugEnabled()) {
                    String uri = t.getURI();
                    log.debug("Perform the (" + last + ")th " + uri + " transform");
                }
                checkSecureValidation(t);
                xmlSignatureInput = t.performTransform(xmlSignatureInput, os);
            }

            return xmlSignatureInput;
        } catch (IOException ex) {
            throw new TransformationException(ex);
        } catch (CanonicalizationException ex) {
            throw new TransformationException(ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new TransformationException(ex);
        }
    }
    
    private void checkSecureValidation(Transform transform) throws TransformationException {
        String uri = transform.getURI();
        if (secureValidation && Transforms.TRANSFORM_XSLT.equals(uri)) {
            Object exArgs[] = { uri };

            throw new TransformationException(
                "signature.Transform.ForbiddenTransform", exArgs
            );
        }
        transform.setSecureValidation(secureValidation);
    }

    /**
     * Return the nonnegative number of transformations.
     *
     * @return the number of transformations
     */
    public int getLength() {
        initTransforms();
        return transforms.length;
    }

    /**
     * Return the <it>i</it><sup>th</sup> <code>{@link Transform}</code>.
     * Valid <code>i</code> values are 0 to <code>{@link #getLength}-1</code>.
     *
     * @param i index of {@link Transform} to return
     * @return the <it>i</it><sup>th</sup> Transform
     * @throws TransformationException
     */
    public Transform item(int i) throws TransformationException {
        try {
            initTransforms();
            return new Transform(transforms[i], this.baseURI);
        } catch (XMLSecurityException ex) {
            throw new TransformationException(ex);
        }
    }

    private void initTransforms() {
        if (transforms == null) {
            transforms = XMLUtils.selectDsNodes(getFirstChild(), "Transform");
        }
    }

    /** @inheritDoc */
    public String getBaseLocalName() {
        return Constants._TAG_TRANSFORMS;
    }
}
