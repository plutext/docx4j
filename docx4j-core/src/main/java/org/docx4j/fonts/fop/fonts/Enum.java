/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 /*
 * Copyright 1997-2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

//package org.apache.avalon.framework;
package org.docx4j.fonts.fop.fonts;

import java.util.Map;

/**
 * Basic enum class for type-safe enums. Should be used as an abstract base. For example:
 *
 * <pre>
 * import org.apache.avalon.framework.Enum;
 *
 * public final class Color extends Enum {
 *   public static final Color RED = new Color( "Red" );
 *   public static final Color GREEN = new Color( "Green" );
 *   public static final Color BLUE = new Color( "Blue" );
 *
 *   private Color( final String color )
 *   {
 *     super( color );
 *   }
 * }
 * </pre>
 *
 * If further operations, such as iterating over all items, are required, the
 * {@link #Enum(String, Map)} constructor can be used to populate a <code>Map</code>, from which
 * further functionality can be derived:
 * <pre>
 * public final class Color extends Enum {
 *   static final Map map = new HashMap();
 *
 *   public static final Color RED = new Color( "Red", map );
 *   public static final Color GREEN = new Color( "Green", map );
 *   public static final Color BLUE = new Color( "Blue", map );
 *
 *   private Color( final String color, final Map map )
 *   {
 *     super( color, map );
 *   }
 *
 *   public static Iterator iterator()
 *   {
 *     return map.values().iterator();
 *   }
 * }
 * </pre>
 *
 * <p>
 * <em>NOTE:</em> between 4.0 and 4.1, the constructors' access has been changed
 * from <code>public</code> to <code>protected</code>. This is to prevent users
 * of the Enum breaking type-safety by defining new Enum items. All Enum items
 * should be defined in the Enum class, as shown above.
 * </p>
 *
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public abstract class Enum
{
    /**
     * The string representation of the Enum.
     */
    private final String m_name;

    /**
     * Constructor to add a new named item.
     * <p>
     * <em>Note:</em> access changed from <code>public</code> to
     * <code>protected</code> after 4.0. See class description.
     * </p>
     *
     * @param name Name of the item.
     */
    protected Enum( final String name )
    {
        this( name, null );
    }

    /**
     * Constructor to add a new named item.
     * <p>
     * <em>Note:</em> access changed from <code>public</code> to
     * <code>protected</code> after 4.0. See class description.
     * </p>
     *
     * @param name Name of the item.
     * @param map A <code>Map</code>, to which will be added a pointer to the newly constructed
     * object.
     */
    protected Enum( final String name, final Map map )
    {
        m_name = name;
        if( null != map )
        {
            map.put( name, this );
        }
    }

    /**
     * Tests for equality. Two Enum:s are considered equal
     * if they are of the same class and have the same names.
     * The method is also declared final - I (LSutic) did this to
     * allow the JIT to inline it easily.
     *
     * @param o the other object
     * @return the equality status
     */
    public boolean equals( Object o )
    {
        if( this == o )
            return true;
        if( !(o instanceof Enum) )
            return false;

        final Enum enumerated = (Enum)o;

        if( !getClass().equals( enumerated.getClass() ) )
            return false;
        if( m_name != null ? !m_name.equals( enumerated.m_name ) : enumerated.m_name != null )
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (m_name != null ? m_name.hashCode() : 0);
        result = 29 * result + getClass().hashCode();
        return result;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    /*public int hashCode()
    {
        return m_name.hashCode() ^ this.getClass().getName().hashCode();
    }*/

    /**
     * Retrieve the name of this Enum item, set in the constructor.
     * @return the name <code>String</code> of this Enum item
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Human readable description of this Enum item. For use when debugging.
     * @return String in the form <code>type[name]</code>, eg.:
     * <code>Color[Red]</code>.
     */
    public String toString()
    {
        return getClass().getName() + "[" + m_name + "]";
    }
}
