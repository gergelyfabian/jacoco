/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gergely Fábián - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaCaseClassGeneratedMethodsFilter implements IFilter {
	private static boolean isScalaClass(final IFilterContext context) {
		return context.getClassAttributes().contains("ScalaSig")
				|| context.getClassAttributes().contains("Scala");
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isScalaClass(context)) {
			return;
		}

		final Matcher matcher = new Matcher();
		if (context.getInterfaceNames().contains("scala/Product")) {
			// Scala case class "Abc" -> Java class "Abc".
			if (matcher.isEquals(methodNode) || matcher.isHashCode(methodNode)
					|| matcher.isToString(methodNode)
					|| matcher.isProductIterator(methodNode)
					|| matcher.isProductElement(methodNode)
					|| matcher.isProductArity(methodNode)
					|| matcher.isProductPrefix(methodNode)
					|| matcher.isCopyDefault(methodNode)
					|| matcher.isCopy(methodNode)
					|| matcher.isCurried(context.getClassName(), methodNode)
					|| matcher.isTupled(context.getClassName(), methodNode)
					|| matcher.isUnapply(context.getClassName(), methodNode)
					|| matcher.isApply(context.getClassName(), methodNode)
					|| matcher.isLessInitGreaterDefault(methodNode)
					|| matcher.isApplyDefault(methodNode)
					|| matcher.isProductElementNames(methodNode)
					|| matcher.isProductElementName(methodNode)) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			}

			matcher.cutIfNonNullInInit(context.getClassName(), methodNode,
					output);
		} else if (context.getClassName().endsWith("$")) {
			// Scala case class "Abc" -> Java class "Abc$".
			if (matcher.isStaticUnapply(methodNode)
					|| matcher.isStaticUnapplySeq(methodNode)
					|| matcher.isReadResolve(methodNode)
					|| matcher.isStaticToString(methodNode)
					|| matcher.isLessInitGreaterDefault(methodNode)
					|| matcher.isApplyDefault(methodNode)
					|| matcher.isStaticObjectApply(context.getClassName(),
							methodNode)
					|| matcher.isProductElement(methodNode)
					|| matcher.isEquals(methodNode)) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		void firstIs(final MethodNode m, int opcode) {
			cursor = m.instructions.getFirst();
			skipNonOpcodes();
			if (cursor != null && cursor.getOpcode() == opcode) {
				return;
			}
			cursor = null;
		}

		boolean isToString(final MethodNode m) {
			if (!"toString".equals(m.name)
					|| !"()Ljava/lang/String;".equals(m.desc)) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
					"_toString", "(Lscala/Product;)Ljava/lang/String;");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isHashCode(final MethodNode m) {
			if (!"hashCode".equals(m.name) || !"()I".equals(m.desc)) {
				return false;
			}
			// Check for two alternatives.
			firstIs(m, Opcodes.GETSTATIC);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
					"_hashCode", "(Lscala/Product;)I");
			nextIs(Opcodes.IRETURN);
			if (cursor == null) {
				firstIs(m, Opcodes.LDC);
				nextIs(Opcodes.ISTORE);
				nextIs(Opcodes.ILOAD);
				nextIs(Opcodes.ALOAD);
				// Not checking more from the instructions.
			}
			return cursor != null;
		}

		boolean isEquals(final MethodNode m) {
			if (!("equals".equals(m.name)
					|| "equals$extension".equals(m.name))) {
				return false;
			}
			firstIs(m, Opcodes.ALOAD);
			// Not checking more from the instructions.
			return cursor != null;
		}

		boolean isProductIterator(final MethodNode m) {
			if (!"productIterator".equals(m.name)
					|| !"()Lscala/collection/Iterator;".equals(m.desc)) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
					"typedProductIterator",
					"(Lscala/Product;)Lscala/collection/Iterator;");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isProductElement(final MethodNode m) {
			if (!(m.name.equals("productElement")
					|| m.name.equals("productElement$extension"))
					|| !m.desc.endsWith("Ljava/lang/Object;")) {
				return false;
			}
			firstIs(m, Opcodes.ILOAD);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.ILOAD);
			nextIsSwitch();
			// Not checking more from the instructions.
			return cursor != null;
		}

		boolean isProductArity(final MethodNode m) {
			if (!"productArity".equals(m.name) || !"()I".equals(m.desc)) {
				return false;
			} else {
				return true;
			}
		}

		boolean isProductPrefix(final MethodNode m) {
			if (!"productPrefix".equals(m.name)
					|| !"()Ljava/lang/String;".equals(m.desc)) {
				return false;
			}
			firstIs(m, Opcodes.LDC);
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isCopyDefault(final MethodNode m) {
			return m.name.startsWith("copy$default$");
		}

		boolean isCopy(final MethodNode m) {
			if (!"copy".equals(m.name)) {
				return false;
			}
			firstIs(m, Opcodes.NEW);
			nextIs(Opcodes.DUP);
			// Not checking more from the instructions.
			return cursor != null;
		}

		boolean isCurried(final String className, final MethodNode m) {
			if (!"curried".equals(m.name)
					|| !"()Lscala/Function1;".equals(m.desc)) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, className + "$", "curried",
					"()Lscala/Function1;");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isTupled(final String className, final MethodNode m) {
			if (!"tupled".equals(m.name)
					|| !"()Lscala/Function1;".equals(m.desc)) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, className + "$", "tupled",
					"()Lscala/Function1;");
			nextIs(Opcodes.ARETURN);

			return cursor != null;
		}

		boolean isApply(final String className, final MethodNode m) {
			if (!"apply".equals(m.name)
					|| !m.desc.endsWith("L" + className + ";")) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			next();
			while (cursor != null
					&& cursor.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				next();
			}
			if (cursor == null) {
				return false;
			}
			cursor = cursor.getPrevious();
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, className + "$", "apply",
					m.desc);
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		void cutIfNonNullInInit(final String className, final MethodNode m,
				final IFilterOutput output) {
			if (!"<init>".equals(m.name)
					|| (m.access & Opcodes.ACC_PUBLIC) == 0) {
				return;
			}
			firstIsALoad0(m);
			next();
			while (cursor != null && cursor.getOpcode() != Opcodes.IFNONNULL) {
				next();
			}
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutStart = cursor;

			nextIs(Opcodes.ACONST_NULL);
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutEnd = cursor;

			output.ignore(cutStart, cutEnd);
		}

		boolean isUnapply(final String className, final MethodNode m) {
			if (!"unapply".equals(m.name)
					|| !m.desc.equals("(L" + className + ";)Lscala/Option;")) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, className + "$", "unapply",
					m.desc);
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		// Only generated for Scala 2.11
		boolean isLessInitGreaterDefault(final MethodNode m) {
			return m.name.startsWith("$lessinit$greater$default");
		}

		// Only generated for Scala 2.11
		boolean isApplyDefault(final MethodNode m) {
			return m.name.startsWith("apply$default$");
		}

		boolean isProductElementNames(final MethodNode m) {
			if (!"productElementNames".equals(m.name)
					|| !m.desc.equals("()Lscala/collection/Iterator;")) {
				return false;
			}
			firstIsALoad0(m);
			nextIsInvoke(Opcodes.INVOKESTATIC, "scala/Product",
					"productElementNames$",
					"(Lscala/Product;)Lscala/collection/Iterator;");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isProductElementName(final MethodNode m) {
			if (!"productElementName".equals(m.name)
					|| !m.desc.equals("(I)Ljava/lang/String;")) {
				return false;
			}
			firstIs(m, Opcodes.ILOAD);
			nextIs(Opcodes.ISTORE);
			nextIs(Opcodes.ILOAD);
			nextIsSwitch();
			// Not checking more of the instructions.
			return cursor != null;
		}

		boolean isStaticUnapply(final MethodNode m) {
			if (!"unapply".equals(m.name)) {
				return false;
			} else {
				return true;
			}
		}

		boolean isStaticUnapplySeq(final MethodNode m) {
			if (!"unapplySeq".equals(m.name)) {
				return false;
			} else {
				return true;
			}
		}

		boolean isReadResolve(final MethodNode m) {
			if (!"readResolve".equals(m.name)
					|| !"()Ljava/lang/Object;".equals(m.desc)
					|| (m.access & Opcodes.ACC_PRIVATE) == 0) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isStaticToString(final MethodNode m) {
			if (!"toString".equals(m.name)
					|| !"()Ljava/lang/String;".equals(m.desc)) {
				return false;
			}
			firstIs(m, Opcodes.LDC);
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isStaticObjectApply(final String className,
				final MethodNode m) {
			if (!"apply".equals(m.name)
					|| !m.desc.endsWith("Ljava/lang/Object;")) {
				return false;
			}
			firstIsALoad0(m);
			next();
			while (cursor != null
					&& cursor.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				next();
			}
			if (cursor == null) {
				return false;
			}
			cursor = cursor.getPrevious();
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, className, "apply");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		final void nextIsInvoke(final int opcode, final String owner,
				final String name) {
			nextIs(opcode);
			if (cursor == null) {
				return;
			}
			final MethodInsnNode m = (MethodInsnNode) cursor;
			if (owner.equals(m.owner) && name.equals(m.name)) {
				return;
			}
			cursor = null;
		}
	}

}
