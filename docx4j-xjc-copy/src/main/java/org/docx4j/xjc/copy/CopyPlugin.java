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
package org.docx4j.xjc.copy;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAssignmentTarget;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import jakarta.xml.bind.JAXBElement;

import org.docx4j.copy.CopyUtils;
import org.docx4j.copy.Copyable;
import org.jvnet.jaxb.lang.Child;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom alternative to the jaxb-tools Copyable plugin with simplifications and improvements:
 * <ul>
 * 	<li>Interface name: {@link Copyable}</li>
 * 	<li>Method: {@code T copy()}</li>
 * 	<li>No CopyStrategy/Locator boilerplate</li>
 * 	<li>Most importantly, recursively sets the parent on all copied children!</li>
 * </ul>
 */
public class CopyPlugin extends Plugin {

	private JClass copyableInterface;
	private JClass childInterface;
	private JClass objectClass;
	private JClass jaxbElementClass;
	private JClass copyUtilsClass;
	private JClass listClass;
	private JClass arraysClass;
	private JClass xmlGregorianCalendarClass;
	private JClass domNodeClass;
	private JClass serializableClass;

	@Override
	public String getOptionName() {
		return "Xdocx4j-copy";
	}

	@Override
	public String getUsage() {
		return "  -Xdocx4j-copy  : generate simple copy() + parent pointers";
	}

	@Override
	public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) {
		final JCodeModel codeModel = outline.getCodeModel();
		copyableInterface = codeModel.ref(Copyable.class);
		childInterface = codeModel.ref(Child.class);
		objectClass = codeModel.ref(Object.class);
		jaxbElementClass = codeModel.ref(JAXBElement.class);
		copyUtilsClass = codeModel.ref(CopyUtils.class);
		listClass = codeModel.ref(List.class);
		arraysClass = codeModel.ref(Arrays.class);
		xmlGregorianCalendarClass = codeModel.ref(XMLGregorianCalendar.class);
		domNodeClass = codeModel.ref(Node.class);
		serializableClass = codeModel.ref(Serializable.class);

		for (ClassOutline classOutline : outline.getClasses()) {
			final JDefinedClass implClass = classOutline.implClass;
			implClass._implements(copyableInterface);

			final JMethod copyToMethod = generateCopyToMethod(classOutline, implClass);

			generateCopyMethod(implClass, copyToMethod);
		}
		return true;
	}

	private JMethod generateCopyToMethod(ClassOutline classOutline, JDefinedClass implClass) {
		final JMethod copyToMethod = implClass.method(JMod.PUBLIC, objectClass, "copyTo");
		JVar targetParam = copyToMethod.param(objectClass, "target");
		copyToMethod.annotate(Override.class);
		final JBlock body = copyToMethod.body();

		final JBlock then = body._if(targetParam.eq(JExpr._null()))._then();
		then.assign(targetParam, JExpr._new(implClass));

		final JVar typedTarget = body.decl(implClass, "typedTarget", JExpr.cast(implClass, targetParam));

		if (implClass._extends() != null && !implClass._extends().equals(objectClass)) {
			// Assume extended class implements Copyable, and therefore we need to call super.copyTo
			body.invoke(JExpr._super(), "copyTo").arg(typedTarget);
		}

		for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
			copyField(fieldOutline, body, typedTarget);
		}

		body._return(typedTarget);
		return copyToMethod;
	}

	private void copyField(FieldOutline fieldOutline, JBlock body, JVar target) {
		final String fieldName = fieldOutline.getPropertyInfo().getName(false);
		final JFieldRef srcField = JExpr._this().ref(fieldName);
		final JFieldRef dstField = target.ref(fieldName);

		final JType rawType = fieldOutline.getRawType();

		// Primitive field: assign source directly to destination
		if (rawType.isPrimitive()) {
			body.assign(dstField, srcField);
			return;
		}

		// If not primitive, must be a class
		final JClass fieldClass = (JClass) rawType;
		final JBlock thenBlock = body._if(srcField.ne(JExpr._null()))._then();

		// List field: copy in foreach loop
		if (listClass.isAssignableFrom(fieldClass.erasure())) {
			final JClass elementType = fieldClass.getTypeParameters() != null && fieldClass.getTypeParameters().size() == 1
					? fieldClass.getTypeParameters().get(0).erasure() : objectClass;
			final JForEach forEach = thenBlock.forEach(elementType, "item", srcField);
			final JVar item = forEach.var();
			final JBlock forBody = forEach.body();
			final JVar copiedItem = forBody.decl(elementType, "copiedItem");

			copyAndAssign(forBody, item, copiedItem, elementType, target);

			forBody.invoke(dstField, "add").arg(copiedItem);
			return;
		}

		// Default: no loop needed
		final JVar objToCopy = thenBlock.decl(fieldClass, "objToCopy", srcField);
		final JVar copiedObj = thenBlock.decl(fieldClass, "copiedObj");
		copyAndAssign(thenBlock, objToCopy, copiedObj, fieldClass, target);
		thenBlock.assign(dstField, copiedObj);
	}

	private void copyAndAssign(JBlock codeBlock, JVar srcVar, JAssignmentTarget dstVar, JClass type, JVar target) {
		if (isValueType(type)) {
			// Primitive or immutable value type: assign directly to destination variable without copying
			codeBlock.assign(dstVar, srcVar);
		} else if (type.erasure().isArray()) {
			// Array: invoke Arrays.copyOf
			// TODO: handle array of reference types? Not sure that is ever generated
			codeBlock.assign(dstVar, arraysClass.staticInvoke("copyOf").arg(srcVar).arg(srcVar.ref("length")));
		} else if (type.erasure().equals(jaxbElementClass)) {
			// JAXBElement: invoke CopyUtils.copyObjectAndSetParent and cast to JAXBElement
			codeBlock.assign(dstVar, JExpr.cast(jaxbElementClass, copyUtilsClass.staticInvoke("copyObjectAndSetParent").arg(srcVar).arg(target)));
		} else if (type.erasure().equals(objectClass)) {
			// Object (should actually be Copyable or JAXBElement): invoke CopyUtils.copyObjectAndSetParent
			codeBlock.assign(dstVar, copyUtilsClass.staticInvoke("copyObjectAndSetParent").arg(srcVar).arg(target));
		} else if (type.equals(xmlGregorianCalendarClass)) {
			// XMLGregorianCalendar: invoke clone
			codeBlock.assign(dstVar, JExpr.cast(xmlGregorianCalendarClass, srcVar.invoke("clone")));
		} else if (domNodeClass.isAssignableFrom(type.erasure())) {
			// W3C DOM Node: invoke cloneNode(true)
			codeBlock.assign(dstVar, JExpr.cast(type, srcVar.invoke("cloneNode").arg(JExpr.lit(true))));
		} else if (type.equals(serializableClass)) {
			// Serializable (org.docx4j.org.w3.x2003.inkML.MappingType, org.docx4j.org.w3.x1998.math.mathML.CnType)
			// TODO: Not copying for now. Could try Apache commons lang3 SerializationUtils.clone() if this is a problem?
			codeBlock.assign(dstVar, srcVar);
		} else {
			// Default: assume the type implements (or will by the end of code gen) Copyable and Child. Invoke copy and set parent.
			// Making this the default has the nice side effect of producing compilation errors if some generated type is not covered in
			// the cases above, since the copy method usually does not exist.
			codeBlock.assign(dstVar, JExpr.cast(type, srcVar.invoke("copy")));
			codeBlock.invoke(dstVar, "setParent").arg(target);
		}
	}

	private static boolean isValueType(JType type) {
		if (type.isPrimitive() // E.g. int
				|| type.isArray() // E.g. int[]
				|| type.unboxify().isPrimitive() // E.g. Boolean
				|| type.owner().ref(Number.class).isAssignableFrom((JClass) type) // E.g. Integer, BigInteger
				|| type.fullName().equals("java.lang.String") // String
				|| type.fullName().equals("javax.xml.datatype.Duration")
		) {
			return true;
		}

		// Return true for enums
		if (type.erasure() instanceof JDefinedClass) {
			JDefinedClass definedClass = (JDefinedClass) type.erasure();
			return definedClass.getClassType() == ClassType.ENUM;
		}

		return false;
	}

	private void generateCopyMethod(JDefinedClass implClass, JMethod copyToMethod) {
		final JMethod copyMethod = implClass.method(JMod.PUBLIC, objectClass, "copy");
		copyMethod.annotate(Override.class);
		final JBlock body = copyMethod.body();
		final JVar copyVar = body.decl(implClass, "copy", JExpr._new(implClass));
		body._return(JExpr.invoke(copyToMethod).arg(copyVar));
	}
}
