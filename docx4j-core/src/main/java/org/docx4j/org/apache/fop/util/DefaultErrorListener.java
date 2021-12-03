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

package org.docx4j.org.apache.fop.util;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;

/**
 * Standard ErrorListener implementation for in-FOP use. Some Xalan-J versions don't properly
 * re-throw exceptions.
 */
public class DefaultErrorListener implements ErrorListener {

    private Logger log;

    /**
     * Main constructor
     * @param log the log instance to send log events to
     */
    public DefaultErrorListener(Logger log) {
        this.log = log;
    }

    /**
     * {@inheritDoc}
     */
    public void warning(TransformerException exc) {
        log.warn(exc.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void error(TransformerException exc) throws TransformerException {
        throw exc;
    }

    /**
     * {@inheritDoc}
     */
    public void fatalError(TransformerException exc)
            throws TransformerException {
        throw exc;
    }

}
