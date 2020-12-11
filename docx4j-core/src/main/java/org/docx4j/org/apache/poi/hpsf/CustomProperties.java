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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;

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
public class CustomProperties extends HashMap<Object,CustomProperty>
{

    /**
     * <p>Maps property IDs to property names.</p>
     */
    private Map<Long,String> dictionaryIDToName = new HashMap<Long,String>();

    /**
     * <p>Maps property names to property IDs.</p>
     */
    private Map<String,Long> dictionaryNameToID = new HashMap<String,Long>();

    /**
     * <p>Tells whether this object is pure or not.</p>
     */
    private boolean isPure = true;


    /**
     * <p>Puts a {@link CustomProperty} into this map. It is assumed that the
     * {@link CustomProperty} already has a valid ID. Otherwise use
     * {@link #put(CustomProperty)}.</p>
     */
    public CustomProperty put(final String name, final CustomProperty cp)
    {
        if (name == null)
        {
            /* Ignoring a property without a name. */
            isPure = false;
            return null;
        }
        if (!(name.equals(cp.getName())))
            throw new IllegalArgumentException("Parameter \"name\" (" + name +
                    ") and custom property's name (" + cp.getName() +
                    ") do not match.");

        /* Register name and ID in the dictionary. Mapping in both directions is possible. If there is already a  */
        final Long idKey = Long.valueOf(cp.getID());
        final Long oldID = dictionaryNameToID.get(name);
        dictionaryIDToName.remove(oldID);
        dictionaryNameToID.put(name, idKey);
        dictionaryIDToName.put(idKey, name);

        /* Put the custom property into this map. */
        final CustomProperty oldCp = super.remove(oldID);
        super.put(idKey, cp);
        return oldCp;
    }



    /**
     * <p>Puts a {@link CustomProperty} that has not yet a valid ID into this
     * map. The method will allocate a suitable ID for the custom property:</p>
     *
     * <ul>
     *
     * <li><p>If there is already a property with the same name, take the ID
     * of that property.</p></li>
     *
     * <li><p>Otherwise find the highest ID and use its value plus one.</p></li>
     *
     * </ul>
     *
     * @param customProperty
     * @return If the was already a property with the same name, the
     * @throws ClassCastException
     */
    private Object put(final CustomProperty customProperty) throws ClassCastException
    {
        final String name = customProperty.getName();

        /* Check whether a property with this name is in the map already. */
        final Long oldId = dictionaryNameToID.get(name);
        if (oldId != null)
            customProperty.setID(oldId.longValue());
        else
        {
            long max = 1;
            for (final Iterator<Long> i = dictionaryIDToName.keySet().iterator(); i.hasNext();)
            {
                final long id = i.next().longValue();
                if (id > max)
                    max = id;
            }
            customProperty.setID(max + 1);
        }
        return this.put(name, customProperty);
    }



    /**
     * <p>Removes a custom property.</p>
     * @param name The name of the custom property to remove
     * @return The removed property or <code>null</code> if the specified property was not found.
     *
     * @see java.util.HashSet#remove(java.lang.Object)
     */
    public Object remove(final String name)
    {
        final Long id = dictionaryNameToID.get(name);
        if (id == null)
            return null;
        dictionaryIDToName.remove(id);
        dictionaryNameToID.remove(name);
        return super.remove(id);
    }

    /**
     * <p>Adds a named string property.</p>
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         <code>null</code> if there was no such property before.
     */
    public Object put(final String name, final String value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(-1);
        p.setType(Variant.VT_LPWSTR);
        p.setValue(value);
        final CustomProperty cp = new CustomProperty(p, name);
        return put(cp);
    }

    /**
     * <p>Adds a named long property.</p>
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         <code>null</code> if there was no such property before.
     */
    public Object put(final String name, final Long value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(-1);
        p.setType(Variant.VT_I8);
        p.setValue(value);
        final CustomProperty cp = new CustomProperty(p, name);
        return put(cp);
    }

    /**
     * <p>Adds a named double property.</p>
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         <code>null</code> if there was no such property before.
     */
    public Object put(final String name, final Double value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(-1);
        p.setType(Variant.VT_R8);
        p.setValue(value);
        final CustomProperty cp = new CustomProperty(p, name);
        return put(cp);
    }

    /**
     * <p>Adds a named integer property.</p>
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         <code>null</code> if there was no such property before.
     */
    public Object put(final String name, final Integer value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(-1);
        p.setType(Variant.VT_I4);
        p.setValue(value);
        final CustomProperty cp = new CustomProperty(p, name);
        return put(cp);
    }

    /**
     * <p>Adds a named boolean property.</p>
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         <code>null</code> if there was no such property before.
     */
    public Object put(final String name, final Boolean value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(-1);
        p.setType(Variant.VT_BOOL);
        p.setValue(value);
        final CustomProperty cp = new CustomProperty(p, name);
        return put(cp);
    }


    /**
     * <p>Gets a named value from the custom properties.</p>
     *
     * @param name the name of the value to get
     * @return the value or <code>null</code> if a value with the specified
     *         name is not found in the custom properties.
     */
    public Object get(final String name)
    {
        final Long id = dictionaryNameToID.get(name);
        final CustomProperty cp = super.get(id);
        return cp != null ? cp.getValue() : null;
    }



    /**
     * <p>Adds a named date property.</p>
     *
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     *         <code>null</code> if there was no such property before.
     */
    public Object put(final String name, final Date value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(-1);
        p.setType(Variant.VT_FILETIME);
        p.setValue(value);
        final CustomProperty cp = new CustomProperty(p, name);
        return put(cp);
    }

    /**
     * Returns a set of all the names of our
     *  custom properties. Equivalent to 
     *  {@link #nameSet()}
     */
    public Set keySet() {
        return dictionaryNameToID.keySet();
    }

    /**
     * Returns a set of all the names of our
     *  custom properties
     */
    public Set<String> nameSet() {
        return dictionaryNameToID.keySet();
    }

    /**
     * Returns a set of all the IDs of our
     *  custom properties
     */
    public Set<String> idSet() {
        return dictionaryNameToID.keySet();
    }


    /**
     * <p>Sets the codepage.</p>
     *
     * @param codepage the codepage
     */
    public void setCodepage(final int codepage)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(PropertyIDMap.PID_CODEPAGE);
        p.setType(Variant.VT_I2);
        p.setValue(Integer.valueOf(codepage));
        put(new CustomProperty(p));
    }



    /**
     * <p>Gets the dictionary which contains IDs and names of the named custom
     * properties.
     *
     * @return the dictionary.
     */
    Map<Long,String> getDictionary()
    {
        return dictionaryIDToName;
    }


    /**
     * Checks against both String Name and Long ID
     */
   public boolean containsKey(Object key) {
      if(key instanceof Long) {
         return super.containsKey(key);
      }
      if(key instanceof String) {
         return super.containsKey(dictionaryNameToID.get(key));
      }
      return false;
   }

   /**
    * Checks against both the property, and its values. 
    */
   public boolean containsValue(Object value) {
      if(value instanceof CustomProperty) {
         return super.containsValue(value);
      } else {
         for(CustomProperty cp : super.values()) {
            if(cp.getValue() == value) {
               return true;
            }
         }
      }
      return false;
   }



   /**
     * <p>Gets the codepage.</p>
     *
     * @return the codepage or -1 if the codepage is undefined.
     */
    public int getCodepage()
    {
        int codepage = -1;
        for (final Iterator<CustomProperty> i = this.values().iterator(); codepage == -1 && i.hasNext();)
        {
            final CustomProperty cp = i.next();
            if (cp.getID() == PropertyIDMap.PID_CODEPAGE)
                codepage = ((Integer) cp.getValue()).intValue();
        }
        return codepage;
    }



    /**
     * <p>Tells whether this {@link CustomProperties} instance is pure or one or
     * more properties of the underlying low-level property set has been
     * dropped.</p>
     *
     * @return <code>true</code> if the {@link CustomProperties} is pure, else
     *         <code>false</code>.
     */
    public boolean isPure()
    {
        return isPure;
    }

    /**
     * <p>Sets the purity of the custom property set.</p>
     *
     * @param isPure the purity
     */
    public void setPure(final boolean isPure)
    {
        this.isPure = isPure;
    }
}
