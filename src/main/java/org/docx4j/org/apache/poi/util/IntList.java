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

package org.docx4j.org.apache.poi.util;

/**
 * A List of int's; as full an implementation of the java.util.List
 * interface as possible, with an eye toward minimal creation of
 * objects
 *
 * the mimicry of List is as follows:
 * <ul>
 * <li> if possible, operations designated 'optional' in the List
 *      interface are attempted
 * <li> wherever the List interface refers to an Object, substitute
 *      int
 * <li> wherever the List interface refers to a Collection or List,
 *      substitute IntList
 * </ul>
 *
 * the mimicry is not perfect, however:
 * <ul>
 * <li> operations involving Iterators or ListIterators are not
 *      supported
 * <li> remove(Object) becomes removeValue to distinguish it from
 *      remove(int index)
 * <li> subList is not supported
 * </ul>
 *
 * @author Marc Johnson
 */
public class IntList
{
    private int[]            _array;
    private int              _limit;
    private int              fillval = 0;
    private static final int _default_size = 128;

    /**
     * create an IntList of default size
     */

    public IntList()
    {
        this(_default_size);
    }

    public IntList(final int initialCapacity)
    {
        this(initialCapacity,0);
    }


    /**
     * create a copy of an existing IntList
     *
     * @param list the existing IntList
     */

    public IntList(final IntList list)
    {
        this(list._array.length);
        System.arraycopy(list._array, 0, _array, 0, _array.length);
        _limit = list._limit;
    }

    /**
     * create an IntList with a predefined initial size
     *
     * @param initialCapacity the size for the internal array
     */

    public IntList(final int initialCapacity, int fillvalue)
    {
        _array = new int[ initialCapacity ];
        if (fillval != 0) {
            fillval = fillvalue;
            fillArray(fillval, _array, 0);
        }
        _limit = 0;
    }

    private void fillArray(int val, int[] array, int index) {
      for (int k = index; k < array.length; k++) {
        array[k] = val;
      }
    }

    /**
     * add the specfied value at the specified index
     *
     * @param index the index where the new value is to be added
     * @param value the new value
     *
     * @exception IndexOutOfBoundsException if the index is out of
     *            range (index < 0 || index > size()).
     */

    public void add(final int index, final int value)
    {
        if (index > _limit)
        {
            throw new IndexOutOfBoundsException();
        }
        else if (index == _limit)
        {
            add(value);
        }
        else
        {

            // index < limit -- insert into the middle
            if (_limit == _array.length)
            {
                growArray(_limit * 2);
            }
            System.arraycopy(_array, index, _array, index + 1,
                             _limit - index);
            _array[ index ] = value;
            _limit++;
        }
    }

    /**
     * Appends the specified element to the end of this list
     *
     * @param value element to be appended to this list.
     *
     * @return true (as per the general contract of the Collection.add
     *         method).
     */

    public boolean add(final int value)
    {
        if (_limit == _array.length)
        {
            growArray(_limit * 2);
        }
        _array[ _limit++ ] = value;
        return true;
    }

    /**
     * Appends all of the elements in the specified collection to the
     * end of this list, in the order that they are returned by the
     * specified collection's iterator.  The behavior of this
     * operation is unspecified if the specified collection is
     * modified while the operation is in progress.  (Note that this
     * will occur if the specified collection is this list, and it's
     * nonempty.)
     *
     * @param c collection whose elements are to be added to this
     *          list.
     *
     * @return true if this list changed as a result of the call.
     */

    public boolean addAll(final IntList c)
    {
        if (c._limit != 0)
        {
            if ((_limit + c._limit) > _array.length)
            {
                growArray(_limit + c._limit);
            }
            System.arraycopy(c._array, 0, _array, _limit, c._limit);
            _limit += c._limit;
        }
        return true;
    }

    /**
     * Inserts all of the elements in the specified collection into
     * this list at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements
     * to the right (increases their indices).  The new elements will
     * appear in this list in the order that they are returned by the
     * specified collection's iterator.  The behavior of this
     * operation is unspecified if the specified collection is
     * modified while the operation is in progress.  (Note that this
     * will occur if the specified collection is this list, and it's
     * nonempty.)
     *
     * @param index index at which to insert first element from the
     *              specified collection.
     * @param c elements to be inserted into this list.
     *
     * @return true if this list changed as a result of the call.
     *
     * @exception IndexOutOfBoundsException if the index is out of
     *            range (index < 0 || index > size())
     */

    public boolean addAll(final int index, final IntList c)
    {
        if (index > _limit)
        {
            throw new IndexOutOfBoundsException();
        }
        if (c._limit != 0)
        {
            if ((_limit + c._limit) > _array.length)
            {
                growArray(_limit + c._limit);
            }

            // make a hole
            System.arraycopy(_array, index, _array, index + c._limit,
                             _limit - index);

            // fill it in
            System.arraycopy(c._array, 0, _array, index, c._limit);
            _limit += c._limit;
        }
        return true;
    }

    /**
     * Removes all of the elements from this list.  This list will be
     * empty after this call returns (unless it throws an exception).
     */

    public void clear()
    {
        _limit = 0;
    }

    /**
     * Returns true if this list contains the specified element.  More
     * formally, returns true if and only if this list contains at
     * least one element e such that o == e
     *
     * @param o element whose presence in this list is to be tested.
     *
     * @return true if this list contains the specified element.
     */

    public boolean contains(final int o)
    {
        boolean rval = false;

        for (int j = 0; !rval && (j < _limit); j++)
        {
            if (_array[ j ] == o)
            {
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Returns true if this list contains all of the elements of the
     * specified collection.
     *
     * @param c collection to be checked for containment in this list.
     *
     * @return true if this list contains all of the elements of the
     *         specified collection.
     */

    public boolean containsAll(final IntList c)
    {
        boolean rval = true;

        if (this != c)
        {
            for (int j = 0; rval && (j < c._limit); j++)
            {
                if (!contains(c._array[ j ]))
                {
                    rval = false;
                }
            }
        }
        return rval;
    }

    /**
     * Compares the specified object with this list for equality.
     * Returns true if and only if the specified object is also a
     * list, both lists have the same size, and all corresponding
     * pairs of elements in the two lists are equal.  (Two elements e1
     * and e2 are equal if e1 == e2.)  In other words, two lists are
     * defined to be equal if they contain the same elements in the
     * same order.  This definition ensures that the equals method
     * works properly across different implementations of the List
     * interface.
     *
     * @param o the object to be compared for equality with this list.
     *
     * @return true if the specified object is equal to this list.
     */

    public boolean equals(final Object o)
    {
        boolean rval = this == o;

        if (!rval && (o != null) && (o.getClass() == this.getClass()))
        {
            IntList other = ( IntList ) o;

            if (other._limit == _limit)
            {

                // assume match
                rval = true;
                for (int j = 0; rval && (j < _limit); j++)
                {
                    rval = _array[ j ] == other._array[ j ];
                }
            }
        }
        return rval;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     *
     * @return the element at the specified position in this list.
     *
     * @exception IndexOutOfBoundsException if the index is out of
     *            range (index < 0 || index >= size()).
     */

    public int get(final int index)
    {
        if (index >= _limit)
        {
            throw new IndexOutOfBoundsException(
                  index + " not accessible in a list of length " + _limit
            );
        }
        return _array[ index ];
    }

    /**
     * Returns the hash code value for this list.  The hash code of a
     * list is defined to be the result of the following calculation:
     *
     * <code>
     * hashCode = 1;
     * Iterator i = list.iterator();
     * while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     * }
     * </code>
     *
     * This ensures that list1.equals(list2) implies that
     * list1.hashCode()==list2.hashCode() for any two lists, list1 and
     * list2, as required by the general contract of Object.hashCode.
     *
     * @return the hash code value for this list.
     */

    public int hashCode()
    {
        int hash = 0;

        for (int j = 0; j < _limit; j++)
        {
            hash = (31 * hash) + _array[ j ];
        }
        return hash;
    }

    /**
     * Returns the index in this list of the first occurrence of the
     * specified element, or -1 if this list does not contain this
     * element.  More formally, returns the lowest index i such that
     * (o == get(i)), or -1 if there is no such index.
     *
     * @param o element to search for.
     *
     * @return the index in this list of the first occurrence of the
     *         specified element, or -1 if this list does not contain
     *         this element.
     */

    public int indexOf(final int o)
    {
        int rval = 0;

        for (; rval < _limit; rval++)
        {
            if (o == _array[ rval ])
            {
                break;
            }
        }
        if (rval == _limit)
        {
            rval = -1;   // didn't find it
        }
        return rval;
    }

    /**
     * Returns true if this list contains no elements.
     *
     * @return true if this list contains no elements.
     */

    public boolean isEmpty()
    {
        return _limit == 0;
    }

    /**
     * Returns the index in this list of the last occurrence of the
     * specified element, or -1 if this list does not contain this
     * element.  More formally, returns the highest index i such that
     * (o == get(i)), or -1 if there is no such index.
     *
     * @param o element to search for.
     *
     * @return the index in this list of the last occurrence of the
     *         specified element, or -1 if this list does not contain
     *         this element.
     */

    public int lastIndexOf(final int o)
    {
        int rval = _limit - 1;

        for (; rval >= 0; rval--)
        {
            if (o == _array[ rval ])
            {
                break;
            }
        }
        return rval;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from
     * their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to removed.
     *
     * @return the element previously at the specified position.
     *
     * @exception IndexOutOfBoundsException if the index is out of
     *            range (index < 0 || index >= size()).
     */

    public int remove(final int index)
    {
        if (index >= _limit)
        {
            throw new IndexOutOfBoundsException();
        }
        int rval = _array[ index ];

        System.arraycopy(_array, index + 1, _array, index, _limit - index);
        _limit--;
        return rval;
    }

    /**
     * Removes the first occurrence in this list of the specified
     * element (optional operation).  If this list does not contain
     * the element, it is unchanged.  More formally, removes the
     * element with the lowest index i such that (o.equals(get(i)))
     * (if such an element exists).
     *
     * @param o element to be removed from this list, if present.
     *
     * @return true if this list contained the specified element.
     */

    public boolean removeValue(final int o)
    {
        boolean rval = false;

        for (int j = 0; !rval && (j < _limit); j++)
        {
            if (o == _array[ j ])
            {
                if (j+1 < _limit) {
                    System.arraycopy(_array, j + 1, _array, j, _limit - j);
                }
                _limit--;
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Removes from this list all the elements that are contained in
     * the specified collection
     *
     * @param c collection that defines which elements will be removed
     *          from this list.
     *
     * @return true if this list changed as a result of the call.
     */

    public boolean removeAll(final IntList c)
    {
        boolean rval = false;

        for (int j = 0; j < c._limit; j++)
        {
            if (removeValue(c._array[ j ]))
            {
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Retains only the elements in this list that are contained in
     * the specified collection.  In other words, removes from this
     * list all the elements that are not contained in the specified
     * collection.
     *
     * @param c collection that defines which elements this set will
     *          retain.
     *
     * @return true if this list changed as a result of the call.
     */

    public boolean retainAll(final IntList c)
    {
        boolean rval = false;

        for (int j = 0; j < _limit; )
        {
            if (!c.contains(_array[ j ]))
            {
                remove(j);
                rval = true;
            }
            else
            {
                j++;
            }
        }
        return rval;
    }

    /**
     * Replaces the element at the specified position in this list
     * with the specified element
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     *
     * @return the element previously at the specified position.
     *
     * @exception IndexOutOfBoundsException if the index is out of
     *            range (index < 0 || index >= size()).
     */

    public int set(final int index, final int element)
    {
        if (index >= _limit)
        {
            throw new IndexOutOfBoundsException();
        }
        int rval = _array[ index ];

        _array[ index ] = element;
        return rval;
    }

    /**
     * Returns the number of elements in this list. If this list
     * contains more than Integer.MAX_VALUE elements, returns
     * Integer.MAX_VALUE.
     *
     * @return the number of elements in this IntList
     */

    public int size()
    {
        return _limit;
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence.  Obeys the general contract of the
     * Collection.toArray method.
     *
     * @return an array containing all of the elements in this list in
     *         proper sequence.
     */

    public int [] toArray()
    {
        int[] rval = new int[ _limit ];

        System.arraycopy(_array, 0, rval, 0, _limit);
        return rval;
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence.  Obeys the general contract of the
     * Collection.toArray(Object[]) method.
     *
     * @param a the array into which the elements of this list are to
     *          be stored, if it is big enough; otherwise, a new array
     *          is allocated for this purpose.
     *
     * @return an array containing the elements of this list.
     */

    public int [] toArray(final int [] a)
    {
        int[] rval;

        if (a.length == _limit)
        {
            System.arraycopy(_array, 0, a, 0, _limit);
            rval = a;
        }
        else
        {
            rval = toArray();
        }
        return rval;
    }

    private void growArray(final int new_size)
    {
        int   size      = (new_size == _array.length) ? new_size + 1
                                                      : new_size;
        int[] new_array = new int[ size ];

        if (fillval != 0) {
          fillArray(fillval, new_array, _array.length);
        }

        System.arraycopy(_array, 0, new_array, 0, _limit);
        _array = new_array;
    }
}   // end public class IntList

