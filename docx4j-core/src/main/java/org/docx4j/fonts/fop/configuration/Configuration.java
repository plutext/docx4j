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

public interface Configuration {

    Configuration getChild(String key);

    Configuration getChild(String key, boolean required);

    Configuration[] getChildren(String key);

    String[] getAttributeNames();

    String getAttribute(String key) throws ConfigurationException;

    String getAttribute(String key, String defaultValue);

    boolean getAttributeAsBoolean(String key, boolean defaultValue);

    float getAttributeAsFloat(String key) throws ConfigurationException;

    float getAttributeAsFloat(String key, float defaultValue);

    int getAttributeAsInteger(String key, int defaultValue);

    String getValue() throws ConfigurationException;

    String getValue(String defaultValue);

    boolean getValueAsBoolean() throws ConfigurationException;

    boolean getValueAsBoolean(boolean defaultValue);

    int getValueAsInteger() throws ConfigurationException;

    int getValueAsInteger(int defaultValue);

    float getValueAsFloat() throws ConfigurationException;

    float getValueAsFloat(float defaultValue);

    String getLocation();

}
