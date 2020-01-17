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

final class NullConfiguration implements Configuration {

    static final NullConfiguration INSTANCE = new NullConfiguration();

    private NullConfiguration() {

    }

    @Override
    public Configuration getChild(String key) {
        return INSTANCE;
    }

    @Override
    public Configuration getChild(String key, boolean required) {
        return INSTANCE;
    }

    @Override
    public Configuration[] getChildren(String key) {
        return new Configuration[0];
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }

    @Override
    public String getAttribute(String key) throws ConfigurationException {
        return "";
    }

    @Override
    public String getAttribute(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean getAttributeAsBoolean(String key, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public float getAttributeAsFloat(String key) throws ConfigurationException {
        return 0;
    }

    @Override
    public float getAttributeAsFloat(String key, float defaultValue) {
        return defaultValue;
    }

    @Override
    public int getAttributeAsInteger(String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public String getValue() throws ConfigurationException {
        // return null;
        throw new ConfigurationException("missing value");
    }

    @Override
    public String getValue(String defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean getValueAsBoolean() throws ConfigurationException {
        return false;
    }

    @Override
    public boolean getValueAsBoolean(boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public int getValueAsInteger() throws ConfigurationException {
        return 0;
    }

    @Override
    public int getValueAsInteger(int defaultValue) {
        return defaultValue;
    }

    @Override
    public float getValueAsFloat() throws ConfigurationException {
        return 0;
    }

    @Override
    public float getValueAsFloat(float defaultValue) {
        return defaultValue;
    }

    @Override
    public String getLocation() {
        return "<no-location>";
    }
}
