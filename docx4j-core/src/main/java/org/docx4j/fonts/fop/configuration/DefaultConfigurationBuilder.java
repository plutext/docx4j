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

/* $Id: Accessibility.java 1343632 2012-05-29 09:48:03Z vhennebert $ */
package org.docx4j.fonts.fop.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurationBuilder {

	private static Logger LOG = LoggerFactory.getLogger(DefaultConfigurationBuilder.class);	
	
//    private static final Log LOG = LogFactory.getLog(DefaultConfigurationBuilder.class.getName());

    public DefaultConfiguration build(InputStream confStream) throws ConfigurationException {
        try {
            DocumentBuilder builder = DefaultConfiguration.DBF.newDocumentBuilder();
            Document document = builder.parse(confStream);
            return new DefaultConfiguration(document.getDocumentElement());
        } catch (DOMException e) {
            throw new ConfigurationException("xml parse error", e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("xml parse error", e);
        } catch (IOException e) {
            throw new ConfigurationException("xml parse error", e);
        } catch (SAXException e) {
            throw new ConfigurationException("xml parse error", e);
        } finally {
            try {
                confStream.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public DefaultConfiguration buildFromFile(File file) throws ConfigurationException {
        try {
            DocumentBuilder builder = DefaultConfiguration.DBF.newDocumentBuilder();
            Document document = builder.parse(file);
            return new DefaultConfiguration(document.getDocumentElement());
        } catch (DOMException e) {
            throw new ConfigurationException("xml parse error", e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("xml parse error", e);
        } catch (IOException e) {
            throw new ConfigurationException("xml parse error", e);
        } catch (SAXException e) {
            throw new ConfigurationException("xml parse error", e);
        }
    }
}
