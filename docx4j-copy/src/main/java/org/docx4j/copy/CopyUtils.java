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

import jakarta.xml.bind.JAXBElement;
import org.jvnet.jaxb.lang.Child;

public class CopyUtils {
    public static Object copyObjectAndSetParent(Object o, Object parent) {
        // If o is Copyable, copy it
        if (o instanceof Copyable) {
            Object copy = ((Copyable) o).copy();
            setParentIfObjectIsChild(copy, parent);
            return copy;
        }
        // If o is a JAXBElement wrapping a Copyable value, copy the value and return it wrapped in a new JAXBElement
        if (o instanceof JAXBElement) {
            JAXBElement jaxbElement = (JAXBElement) o;
            if (jaxbElement.getValue() instanceof Copyable) {
                Object copy = ((Copyable) jaxbElement.getValue()).copy();
                setParentIfObjectIsChild(copy, parent);
                return new JAXBElement(jaxbElement.getName(), jaxbElement.getDeclaredType(), jaxbElement.getScope(), copy);
            }
        }
        // Otherwise, we just won't copy o
        return o;
    }

    private static Object setParentIfObjectIsChild(Object o, Object parent) {
        if (o instanceof Child) {
            ((Child) o).setParent(parent);
        }
        return o;
    }
}
