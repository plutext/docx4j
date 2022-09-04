/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.docx4j.fonts.fop.apps.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.util.uri.DataURIResolver;

/**
 * This object holds the base URI from which to resolve URIs against as well as the resolver for
 * resource acquisition. It also does some URI sanitization of common URI syntactical errors. This
 * class takes in a {@link org.apache.xmlgraphics.io.ResourceResolver} and delegates all relevant
 * URIs to it.
 */
public class InternalResourceResolver {
    private final URI baseUri;
    private final ResourceResolver resourceResolver;
    private final DataURIResolver dataSchemeResolver = new DataURIResolver();

    /**
     * @param baseUri the base URI from which to resolve relative URIs
     * @param resourceResolver the resolver to delegate to
     */
    public InternalResourceResolver(URI baseUri, ResourceResolver resourceResolver) {
        this.baseUri = baseUri;
        this.resourceResolver = resourceResolver;
    }

    /**
     * Returns the base URI from which to resolve all URIs against.
     *
     * @return the base URI
     */
    public URI getBaseURI() {
        return baseUri;
    }

    /**
     * Retrieve a resource given a URI in String form. This also does some syntactical sanitaion on
     * the URI.
     *
     * @param stringUri the URI in String form
     * @return the resource
     * @throws IOException if an I/O error occurred
     * @throws URISyntaxException if the URI syntax was invalid
     */
    public Resource getResource(String stringUri) throws IOException, URISyntaxException {
        if (stringUri.startsWith("data:")) {
            return new Resource(resolveDataURI(stringUri));
        }
        return getResource(cleanURI(stringUri));
    }

    /**
     * Retrieve a resource given a URI in String form.
     *
     * @param uri the resource URI
     * @return the resource
     * @throws IOException if an I/O error occurred
     */
    public Resource getResource(URI uri) throws IOException {
        if (uri.getScheme() != null && uri.getScheme().startsWith("data")) {
            return new Resource(resolveDataURI(uri.toASCIIString()));
        }
        return resourceResolver.getResource(resolveFromBase(uri));
    }

    /**
     * Returns the OutputStream for a given URI.
     *
     * @param uri the URI for the inteded stream
     * @return the output stream
     * @throws IOException if an I/O error occurrred
     */
    public OutputStream getOutputStream(URI uri) throws IOException {
        return resourceResolver.getOutputStream(resolveFromBase(uri));
    }

    /**
     * Resolves a URI against the base URI.
     *
     * @param uri the URI that requires resolution
     * @return the resolved URI
     */
    public URI resolveFromBase(URI uri) {
        return baseUri.resolve(uri);
    }

    /**
     * Performs some sanitation for some of the most common URI syntax mistakes.
     *
     * @param uriStr the URI in String form
     * @return a valid URI
     * @throws URISyntaxException if the given String was too erroneous to validate
     */
    public static URI cleanURI(String uriStr) throws URISyntaxException {
        // replace back slash with forward slash to ensure windows file:/// URLS are supported
        if (uriStr == null) {
            return null;
        }
        String fixedUri = uriStr.replace('\\', '/');
        fixedUri = fixedUri.replace(" ", "%20");
        return new URI(fixedUri);
    }

    /**
     * Performs some sanitation for some of the most common URI syntax mistakes but returns a
     * directory URI rather than a file URI.
     *
     * @param base the directory URI in String form
     * @return the directory URI
     * @throws URISyntaxException if the given String was too erroneous to validate
     */
    public static URI getBaseURI(String base) throws URISyntaxException {
        String path = base + (!base.isEmpty() && base.charAt(base.length() - 1) == '/' ? "" : "/");
        return cleanURI(path);
    }

    private InputStream resolveDataURI(String dataURI) {
        try {
            Source src = dataSchemeResolver.resolve(dataURI, "");
            return src == null ? null : ((StreamSource) src).getInputStream();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
