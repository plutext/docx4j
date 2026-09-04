/*
 *  Copyright 2026, Plutext Pty Ltd.
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
package org.docx4j.convert.out.fo.renderers;

import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.convert.out.FOSettings;

/**
 * A hook into the FopFactoryBuilder docx4j prepares for each PDF export,
 * discovered through {@link java.util.ServiceLoader}.  docx4j-fop-word-layout
 * uses it to install Word-style line breaking; an application can register
 * its own to set FOP options docx4j does not expose.
 *
 * @since 17.0.5
 */
public interface FopFactoryCustomizer {

	void customize(FopFactoryBuilder builder, FOSettings settings);
}
