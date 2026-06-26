/* Copyright © 2026, Oracle and/or its affiliates,
 * and Contributed under the terms and conditions of Apache License 
 * Version 2.0 (the "License"), without any additional terms or conditions.
 * 
 * The contributor licenses this file to You under the License;
 * you may not use this file except in compliance with
 * the License. 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.docx4j.copy;

/**
 * Interface implemented by XJC-generated classes to provide a fast deep-copy
 * without marshalling/unmarshalling.
 */
public interface Copyable {

	/**
	 * Deep-copy this object and its descendants.
	 *
	 * <p>The returned copy and its descendants will have all parent pointers set.
	 */
	Object copy();

	/**
	 * Deep-copy this object and its descendants to the provided target object.
	 *
	 * <p>The returned copy and its descendants will have all parent pointers set.
	 */
	Object copyTo(Object target);
}
