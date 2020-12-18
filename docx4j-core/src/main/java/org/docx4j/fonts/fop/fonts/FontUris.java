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

package org.docx4j.fonts.fop.fonts;

import java.io.Serializable;
import java.net.URI;

public class FontUris implements Serializable {

    private static final long serialVersionUID = 8571060588775532701L;

    private final URI embed;
    private final URI metrics;
    private final URI afm;
    private final URI pfm;

    public FontUris(URI embed, URI metrics, URI afm, URI pfm) {
        this.embed = embed;
        this.metrics = metrics;
        this.afm = afm;
        this.pfm = pfm;
    }

    public FontUris(URI embed, URI metrics) {
        this.embed = embed;
        this.metrics = metrics;
        this.afm = null;
        this.pfm = null;
    }

    public URI getEmbed() {
        return embed;
    }

    public URI getMetrics() {
        return metrics;
    }

    public URI getAfm() {
        return afm;
    }

    public URI getPfm() {
        return pfm;
    }

}

