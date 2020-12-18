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

package org.docx4j.fonts.fop.render.java2d;

import org.docx4j.fonts.fop.fonts.FontMetrics;

/**
 * Adds method to retrieve the actual {@link java.awt.Font}
 * for use by {@link Java2DRenderer}s.
 */
public interface FontMetricsMapper extends FontMetrics {

    /**
     * Gets a {@link java.awt.Font} instance of the font that this
     * {@link FontMetrics} describes in the desired size.
     * @param size font size
     * @return font with the desired characteristics.
     */
    java.awt.Font getFont(int size);

}
