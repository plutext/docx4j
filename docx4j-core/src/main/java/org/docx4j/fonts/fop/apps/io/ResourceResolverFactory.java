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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.io.TempResourceResolver;
import org.apache.xmlgraphics.io.TempResourceURIGenerator;

/**
 * A factory class for {@link ResourceResolver}s.
 */
public final class ResourceResolverFactory {

    private ResourceResolverFactory() {
    }

    /**
     * Returns the default resource resolver, this is most basic resolver which can be used when
     * no there are no I/O or file access restrictions.
     *
     * @return the default resource resolver
     */
    public static ResourceResolver createDefaultResourceResolver() {
        return DefaultResourceResolver.INSTANCE;
    }

    /**
     * A helper merthod that creates an internal resource resolver using the default resover:
     * {@link ResourceResolverFactory#createDefaultResourceResolver()}.
     *
     * @param baseURI the base URI from which to resolve URIs
     * @return the default internal resource resolver
     */
    public static InternalResourceResolver createDefaultInternalResourceResolver(URI baseURI) {
        return new InternalResourceResolver(baseURI, createDefaultResourceResolver());
    }

    /**
     * Creates an interal resource resolver given a base URI and a resource resolver.
     *
     * @param baseURI the base URI from which to resolve URIs
     * @param resolver the resource resolver
     * @return the internal resource resolver
     */
    public static InternalResourceResolver createInternalResourceResolver(URI baseURI,
            ResourceResolver resolver) {
        return new InternalResourceResolver(baseURI, resolver);
    }

    /**
     * Creates a temporary-resource-scheme aware resource resolver. Temporary resource URIs are
     * created by {@link TempResourceURIGenerator}.
     *
     * @param tempResourceResolver the temporary-resource-scheme resolver to use
     * @param defaultResourceResolver the default resource resolver to use
     * @return the ressource resolver
     */
    public static ResourceResolver createTempAwareResourceResolver(
            TempResourceResolver tempResourceResolver,
            ResourceResolver defaultResourceResolver) {
        return new TempAwareResourceResolver(tempResourceResolver, defaultResourceResolver);
    }

    /**
     * This creates the builder class for binding URI schemes to implementations of
     * {@link ResourceResolver}. This allows users to define their own URI schemes such that they
     * have finer control over the acquisition of resources.
     *
     * @param defaultResolver the default resource resolver that should be used in the event that
     * none of the other registered resolvers match the scheme
     * @return the scheme aware {@link ResourceResolver} builder
     */
    public static SchemeAwareResourceResolverBuilder createSchemeAwareResourceResolverBuilder(
            ResourceResolver defaultResolver) {
        return new SchemeAwareResourceResolverBuilderImpl(defaultResolver);
    }

    private static final class DefaultResourceResolver implements ResourceResolver {

        private static final ResourceResolver INSTANCE = new DefaultResourceResolver();

        private final TempAwareResourceResolver delegate;

        private DefaultResourceResolver() {
            delegate = new TempAwareResourceResolver(new DefaultTempResourceResolver(),
                    new NormalResourceResolver());
        }

        /** {@inheritDoc} */
        public Resource getResource(URI uri) throws IOException {
            return delegate.getResource(uri);
        }

        /** {@inheritDoc} */
        public OutputStream getOutputStream(URI uri) throws IOException {
            return delegate.getOutputStream(uri);
        }

    }

    private static final class TempAwareResourceResolver implements ResourceResolver {

        private final TempResourceResolver tempResourceResolver;

        private final ResourceResolver defaultResourceResolver;

        public TempAwareResourceResolver(TempResourceResolver tempResourceHandler,
                ResourceResolver defaultResourceResolver) {
            this.tempResourceResolver = tempResourceHandler;
            this.defaultResourceResolver = defaultResourceResolver;
        }

        private static boolean isTempURI(URI uri) {
            return TempResourceURIGenerator.isTempURI(uri);
        }

        /** {@inheritDoc} */
        public Resource getResource(URI uri) throws IOException {
            if (isTempURI(uri)) {
                return tempResourceResolver.getResource(uri.getPath());
            } else {
                return defaultResourceResolver.getResource(uri);
            }
        }

        /** {@inheritDoc} */
        public OutputStream getOutputStream(URI uri) throws IOException {
            if (isTempURI(uri)) {
                return tempResourceResolver.getOutputStream(uri.getPath());
            } else {
                return defaultResourceResolver.getOutputStream(uri);
            }
        }
    }

    private static class DefaultTempResourceResolver implements TempResourceResolver {

        private final ConcurrentHashMap<String, File> tempFiles = new ConcurrentHashMap<String, File>();

        private File getTempFile(String uri) throws IllegalStateException {
            File tempFile = tempFiles.remove(uri);
            if (tempFile == null) {
                throw new IllegalStateException(uri + " was never created or has been deleted");
            }
            return tempFile;
        }

        private File createTempFile(String path) throws IOException {
            File tempFile = File.createTempFile(path, ".fop.tmp");
            File oldFile = tempFiles.put(path, tempFile);
            if (oldFile != null) {
                String errorMsg = oldFile.getAbsolutePath() + " has been already created for " + path;
                boolean newTempDeleted = tempFile.delete();
                if (!newTempDeleted) {
                    errorMsg += ". " + tempFile.getAbsolutePath() + " was not deleted.";
                }
                throw new IOException(errorMsg);
            }
            return tempFile;
        }

        /** {@inheritDoc} */
        public Resource getResource(String id) throws IOException {
            return new Resource(new FileDeletingInputStream(getTempFile(id)));
        }

        /** {@inheritDoc} */
        public OutputStream getOutputStream(String id) throws IOException {
            return new FileOutputStream(createTempFile(id));
        }
    }

    private static class FileDeletingInputStream extends FilterInputStream {

        private final File file;

        protected FileDeletingInputStream(File file) throws MalformedURLException, IOException {
            super(file.toURI().toURL().openStream());
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                file.delete();
            }
        }
    }

    private static class NormalResourceResolver implements ResourceResolver {
        public Resource getResource(URI uri) throws IOException {
            return new Resource(uri.toURL().openStream());
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return new FileOutputStream(new File(uri));
        }
    }

    private static final class SchemeAwareResourceResolver implements ResourceResolver {

        private final Map<String, ResourceResolver> schemeHandlingResourceResolvers;

        private final ResourceResolver defaultResolver;

        private SchemeAwareResourceResolver(
                Map<String, ResourceResolver> schemEHandlingResourceResolvers,
                ResourceResolver defaultResolver) {
            this.schemeHandlingResourceResolvers = schemEHandlingResourceResolvers;
            this.defaultResolver = defaultResolver;
        }

        private ResourceResolver getResourceResolverForScheme(URI uri) {
            String scheme = uri.getScheme();
            if (schemeHandlingResourceResolvers.containsKey(scheme)) {
                return schemeHandlingResourceResolvers.get(scheme);
            } else {
                return defaultResolver;
            }
        }

        /** {@inheritDoc} */
        public Resource getResource(URI uri) throws IOException {
            return getResourceResolverForScheme(uri).getResource(uri);
        }

        /** {@inheritDoc} */
        public OutputStream getOutputStream(URI uri) throws IOException {
            return getResourceResolverForScheme(uri).getOutputStream(uri);
        }
    }

    /**
     * Implementations of this interface will be builders for {@link ResourceResolver}, they bind
     * URI schemes to their respective resolver. This gives users more control over the mechanisms
     * by which URIs are resolved.
     * <p>
     * Here is an example of how this could be used:
     * </p>
     * <p><code>
     * SchemeAwareResourceResolverBuilder builder
     *      = ResourceResolverFactory.createSchemeAwareResourceResolverBuilder(defaultResolver);
     * builder.registerResourceResolverForScheme("test", testResolver);
     * builder.registerResourceResolverForScheme("anotherTest", test2Resolver);
     * ResourceResolver resolver = builder.build();
     * </code></p>
     * This will result in all URIs for the form "test:///..." will be resolved using the
     * <code>testResolver</code> object; URIs of the form "anotherTest:///..." will be resolved
     * using <code>test2Resolver</code>; all other URIs will be resolved from the defaultResolver.
     */
    public interface SchemeAwareResourceResolverBuilder {

        /**
         * Register a scheme with its respective {@link ResourceResolver}. This resolver will be
         * used as the only resolver for the specified scheme.
         *
         * @param scheme the scheme to be used with the given resolver
         * @param resourceResolver the resource resolver
         */
        void registerResourceResolverForScheme(String scheme, ResourceResolver resourceResolver);

        /**
         * Builds a {@link ResourceResolver} that will delegate to the respective resource resolver
         * when a registered URI scheme is given
         *
         * @return a resolver that delegates to the appropriate scheme resolver
         */
        ResourceResolver build();
    }

    private static final class CompletedSchemeAwareResourceResolverBuilder
            implements SchemeAwareResourceResolverBuilder {

        private static final SchemeAwareResourceResolverBuilder INSTANCE
                = new CompletedSchemeAwareResourceResolverBuilder();

        /** {@inheritDoc} */
        public ResourceResolver build() {
            throw new IllegalStateException("Resource resolver already built");
        }

        /** {@inheritDoc} */
        public void registerResourceResolverForScheme(String scheme,
                ResourceResolver resourceResolver) {
            throw new IllegalStateException("Resource resolver already built");
        }
    }

    private static final class ActiveSchemeAwareResourceResolverBuilder
            implements SchemeAwareResourceResolverBuilder {

        private final Map<String, ResourceResolver> schemeHandlingResourceResolvers
                = new HashMap<String, ResourceResolver>();

        private final ResourceResolver defaultResolver;

        private ActiveSchemeAwareResourceResolverBuilder(ResourceResolver defaultResolver) {
            this.defaultResolver = defaultResolver;
        }

        /** {@inheritDoc} */
        public void registerResourceResolverForScheme(String scheme,
                ResourceResolver resourceResolver) {
            schemeHandlingResourceResolvers.put(scheme, resourceResolver);
        }

        /** {@inheritDoc} */
        public ResourceResolver build() {
            return new SchemeAwareResourceResolver(
                    Collections.unmodifiableMap(schemeHandlingResourceResolvers), defaultResolver);
        }

    }

    private static final class SchemeAwareResourceResolverBuilderImpl
            implements SchemeAwareResourceResolverBuilder {

        private SchemeAwareResourceResolverBuilder delegate;

        private SchemeAwareResourceResolverBuilderImpl(ResourceResolver defaultResolver) {
            this.delegate = new ActiveSchemeAwareResourceResolverBuilder(defaultResolver);
        }

        /** {@inheritDoc} */
        public void registerResourceResolverForScheme(String scheme,
                ResourceResolver resourceResolver) {
            delegate.registerResourceResolverForScheme(scheme, resourceResolver);
        }

        /** {@inheritDoc} */
        public ResourceResolver build() {
            ResourceResolver resourceResolver = delegate.build();
            delegate = CompletedSchemeAwareResourceResolverBuilder.INSTANCE;
            return resourceResolver;
        }
    }
}
