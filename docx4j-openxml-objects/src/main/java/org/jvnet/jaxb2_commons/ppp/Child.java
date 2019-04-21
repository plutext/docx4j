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

/**
 * In docx4j, there are a couple of edge cases to 
 * be aware of:
 * 
 * <ol>
 * 	<li>What is the parent of something wrapped in a JAXBElement?
 * 
 *      ArrayListWml seeks to set this to the object containing the content list.
 *      
 *      (If the JAXBElement is not added to a content list, then this won't be done.
 *       TraversalUtil seeks to handle some of those cases.) </li>
 *       
 *  <li>When a field is set explicitly (eg setPPr) to some object, is the object's parent set? 
 *  
 *       Generally not.</li>
 * </ol>
 *      
 * 
 * 
 * @author jason@plutext.org, gk5885
 * 
 */
public interface Child
{
	public Object getParent();
	
	public void setParent(Object parent);
}
