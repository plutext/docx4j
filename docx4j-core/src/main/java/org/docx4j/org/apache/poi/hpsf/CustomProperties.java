/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.hpsf;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Maintains the instances of {@link CustomProperty} that belong to a
 * {@link DocumentSummaryInformation}. The class maintains the names of the
 * custom properties in a dictionary. It implements the {@link Map} interface
 * and by this provides a simplified view on custom properties: A property's
 * name is the key that maps to a typed value. This implementation hides
 * property IDs from the developer and regards the property names as keys to
 * typed values.</p>
 *
 * <p>While this class provides a simple API to custom properties, it ignores
 * the fact that not names, but IDs are the real keys to properties. Under the
 * hood this class maintains a 1:1 relationship between IDs and names. Therefore
 * you should not use this class to process property sets with several IDs
 * mapping to the same name or with properties without a name: the result will
 * contain only a subset of the original properties. If you really need to deal
 * such property sets, use HPSF's low-level access methods.</p>
 *
 * <p>An application can call the {@link #isPure} method to check whether a
 * property set parsed by {@link CustomProperties} is still pure (i.e.
 * unmodified) or whether one or more properties have been dropped.</p>
 *
 * <p>This class is not thread-safe; concurrent access to instances of this
 * class must be synchronized.</p>
 *
 * <p>While this class is roughly HashMap<Long,CustomProperty>, that's the
 *  internal representation. To external calls, it should appear as
 *  HashMap<String,Object> mapping between Names and Custom Property Values.</p>
 *
 * @author Rainer Klute <a
 *         href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
@SuppressWarnings("serial")
public class CustomProperties implements Map<String,Object> {

	protected static Logger log = LoggerFactory.getLogger(CustomProperties.class);
	
	  /**
     * The custom properties
     */
    private final HashMap<Long,CustomProperty> props = new HashMap<>();

    /**
     * Maps property IDs to property names and vice versa.
     */
    private final TreeBidiMap<Long,String> dictionary = new TreeBidiMap<>();

    /**
     * Tells whether this object is pure or not.
     */
    private boolean isPure = true;

    private int codepage = -1;

    /**
     * Puts a {@link CustomProperty} into this map. It is assumed that the
     * {@link CustomProperty} already has a valid ID. Otherwise use
     * {@link #put(CustomProperty)}.
     *
     * @param name the property name
     * @param cp the property
     *
     * @return the previous property stored under this name
     */
    public CustomProperty put(final String name, final CustomProperty cp) {
        if (name == null) {
            /* Ignoring a property without a name. */
            isPure = false;
            return null;
        }

        if (!name.equals(cp.getName())) {
            throw new IllegalArgumentException("Parameter \"name\" (" + name +
                    ") and custom property's name (" + cp.getName() +
                    ") do not match.");
        }

        checkCodePage(name);

        /* Register name and ID in the dictionary. Mapping in both directions is possible. If there is already a  */
        props.remove(dictionary.getKey(name));
        dictionary.put(cp.getID(), name);

        /* Put the custom property into this map. */
        return props.put(cp.getID(), cp);
    }

    /**
     * Adds a named property.
     *
     * @param key The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         {@code null} if there was no such property before.
     */
    @Override
    public Object put(String key, Object value) {
        int variantType;
        if (value instanceof String) {
            variantType = Variant.VT_LPSTR;
        } else if (value instanceof Short) {
            variantType = Variant.VT_I2;
        } else if (value instanceof Integer) {
            variantType = Variant.VT_I4;
        } else if (value instanceof Long) {
            variantType = Variant.VT_I8;
        } else if (value instanceof Float) {
            variantType = Variant.VT_R4;
        } else if (value instanceof Double) {
            variantType = Variant.VT_R8;
        } else if (value instanceof Boolean) {
            variantType = Variant.VT_BOOL;
        } else if (value instanceof BigInteger
            && ((BigInteger)value).bitLength() <= 64
            && ((BigInteger)value).compareTo(BigInteger.ZERO) >= 0) {
            variantType = Variant.VT_UI8;
        } else if (value instanceof Date) {
            variantType = Variant.VT_FILETIME;
        } else {
            throw new IllegalStateException("unsupported datatype - currently String,Short,Integer,Long,Float,Double,Boolean,BigInteger(unsigned long),Date can be processed.");
        }
        final Property p = new Property(-1, variantType, value);
        return put(new CustomProperty(p, key));
    }

    /**
     * Gets a named value from the custom properties - only works for keys of type String
     *
     * @param key the name of the value to get
     * @return the value or {@code null} if a value with the specified
     *         name is not found in the custom properties.
     */
    @Override
    public Object get(final Object key) {
        final Long id = dictionary.getKey(key);
        final CustomProperty cp = props.get(id);
        return cp != null ? cp.getValue() : null;
    }

    /**
     * Removes a custom property - only works for keys of type String
     * @param key The name of the custom property to remove
     * @return The removed property or {@code null} if the specified property was not found.
     */
    @Override
    public CustomProperty remove(Object key) {
        final Long id = dictionary.removeValue(key);
        return props.remove(id);
    }

    @Override
    public int size() {
        return props.size();
    }

    @Override
    public boolean isEmpty() {
        return props.isEmpty();
    }

    @Override
    public void clear() {
        props.clear();
    }

    @Override
    public int hashCode() {
        return props.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CustomProperties && props.equals(((CustomProperties) obj).props);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Entry<? extends String, ?> me : m.entrySet()) {
            put(me.getKey(), me.getValue());
        }
    }

    /**
     * @return the list of properties
     */
    public List<CustomProperty> properties() {
        List<CustomProperty> list = new ArrayList<>(props.size());
        list.addAll(props.values());
        return Collections.unmodifiableList(list);
    }

    /**
     * @return the list of property values - use {@link #properties()} for the wrapped values
     */
    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>(props.size());
        for (CustomProperty property : props.values()) {
            list.add(property.getValue());
        }
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Map<String,Object> set = new LinkedHashMap<>(props.size());
        for (CustomProperty property : props.values()) {
            set.put(property.getName(), property.getValue());
        }
        return Collections.unmodifiableSet(set.entrySet());
    }

    /**
     * Returns a set of all the names of our custom properties.
     * Equivalent to {@link #nameSet()}
     *
     * @return a set of all the names of our custom properties
     */
    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(dictionary.values());
    }

    /**
     * Returns a set of all the names of our custom properties
     *
     * @return a set of all the names of our custom properties
     */
    public Set<String> nameSet() {
        return Collections.unmodifiableSet(dictionary.values());
    }

    /**
     * Returns a set of all the IDs of our custom properties
     *
     * @return a set of all the IDs of our custom properties
     */
    public Set<Long> idSet() {
        return Collections.unmodifiableSet(dictionary.keySet());
    }


    /**
     * Sets the codepage.
     *
     * @param codepage the codepage
     */
    public void setCodepage(final int codepage) {
        this.codepage = codepage;
    }

    /**
     * Gets the codepage.
     *
     * @return the codepage or -1 if the codepage is undefined.
     */
    public int getCodepage() {
        return codepage;
    }

    /**
     * <p>Gets the dictionary which contains IDs and names of the named custom
     * properties.
     *
     * @return the dictionary.
     */
    Map<Long,String> getDictionary() {
        return dictionary;
    }


    /**
     * Checks against both String Name and Long ID
     */
    @Override
    public boolean containsKey(Object key) {
        return ((key instanceof Long && dictionary.containsKey(key)) || dictionary.containsValue(key));
    }

    /**
     * Checks against both the property, and its values.
     */
    @Override
    public boolean containsValue(Object value) {
        if(value instanceof CustomProperty) {
            return props.containsValue(value);
        }

        for(CustomProperty cp : props.values()) {
            if(cp.getValue() == value) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tells whether this {@link CustomProperties} instance is pure or one or
     * more properties of the underlying low-level property set has been
     * dropped.
     *
     * @return {@code true} if the {@link CustomProperties} is pure, else
     *         {@code false}.
     */
    public boolean isPure() {
        return isPure;
    }

    /**
     * Sets the purity of the custom property set.
     *
     * @param isPure the purity
     */
    public void setPure(final boolean isPure) {
        this.isPure = isPure;
    }

    /**
     * Puts a {@link CustomProperty} that has not yet a valid ID into this
     * map. The method will allocate a suitable ID for the custom property:
     *
     * <ul>
     * <li>If there is already a property with the same name, take the ID
     * of that property.
     *
     * <li>Otherwise find the highest ID and use its value plus one.
     * </ul>
     *
     * @param customProperty The {@link CustomProperty} to add.
     * @return If there was already a property with the same name, the old property
     * @throws ClassCastException
     */
    private Object put(final CustomProperty customProperty) throws ClassCastException {
        final String name = customProperty.getName();

        /* Check whether a property with this name is in the map already. */
        final Long oldId = (name == null) ? null :  dictionary.getKey(name);
        if (oldId != null) {
            customProperty.setID(oldId);
        } else {
            long lastKey = (dictionary.isEmpty()) ? 0 : dictionary.lastKey();
            long nextKey = Math.max(lastKey,PropertyIDMap.PID_MAX)+1;
            customProperty.setID(nextKey);
        }
        return this.put(name, customProperty);
    }

    private void checkCodePage(String value) {
        int cp = getCodepage();
        if (cp == -1) {
            cp = Property.DEFAULT_CODEPAGE;
        }
        if (cp == CodePageUtil.CP_UNICODE) {
            return;
        }
        String cps = "";
        try {
            cps = CodePageUtil.codepageToEncoding(cp, false);
        } catch (UnsupportedEncodingException e) {
            log.error("Codepage '{}' can't be found.", cp);
        }
        if (!cps.isEmpty() && Charset.forName(cps).newEncoder().canEncode(value)) {
            return;
        }
        log.debug("Charset '{}' can't encode '{}' - switching to unicode.", cps, value);
        setCodepage(CodePageUtil.CP_UNICODE);
    }
}