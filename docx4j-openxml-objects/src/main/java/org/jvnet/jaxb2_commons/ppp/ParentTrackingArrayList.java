/**
 * Copyright 2007 Gregory A. Kick
 *
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
package org.jvnet.jaxb2_commons.ppp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author gk5885
 * 
 */
public class ParentTrackingArrayList<T extends Child> extends ArrayList<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object parent;

	/**
	 * 
	 */
	public ParentTrackingArrayList(Object parent, Class<? extends List<?>> listClass)
	{
		super();
		this.parent = parent;
	}

	/**
	 * @param c
	 */
	public ParentTrackingArrayList(Object parent, Collection<? extends T> c)
	{
		super(c);
		for (T element : this)
		{
			element.setParent(parent);
		}
	}

	/**
	 * @param initialCapacity
	 */
	public ParentTrackingArrayList(Object parent, int initialCapacity)
	{
		super(initialCapacity);
		this.parent = parent;
	}

	/**
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, T element)
	{
		element.setParent(parent);
		super.add(index, element);
	}

	/**
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(T o)
	{
		o.setParent(parent);
		return super.add(o);
	}

	/**
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		for (T element : c)
		{
			element.setParent(parent);
		}
		return super.addAll(c);
	}

	/**
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		for (T element : c)
		{
			element.setParent(parent);
		}
		return super.addAll(index, c);
	}

	/**
	 * @see java.util.ArrayList#clear()
	 */
	@Override
	public void clear()
	{
		for (T element : this)
		{
			element.setParent(null);
		}
		super.clear();
	}

	/**
	 * @see java.util.ArrayList#remove(int)
	 */
	@Override
	public T remove(int index)
	{
		T element = super.remove(index);
		element.setParent(null);
		return element;
	}

	/**
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o)
	{
		T element = super.get(super.indexOf(o));
		element.setParent(null);
		return super.remove(o);
	}

	/**
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c)
	{
		for (T element : this)
		{
			if (c.contains(element))
			{
				element.setParent(null);
			}
		}
		return super.removeAll(c);
	}

	/**
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	@Override
	public T set(int index, T element)
	{
		element.setParent(parent);
		return super.set(index, element);
	}

	/**
	 * @see java.util.ArrayList#removeRange(int, int)
	 */
	@Override
	protected void removeRange(int fromIndex, int toIndex)
	{
		for (int index = fromIndex; index < toIndex; index++)
		{
			this.get(index).setParent(null);
		}
		super.removeRange(fromIndex, toIndex);
	}
}
