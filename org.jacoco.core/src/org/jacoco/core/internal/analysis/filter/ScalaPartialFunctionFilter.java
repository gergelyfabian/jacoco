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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaPartialFunctionFilter implements IFilter {
	private static boolean isScalaClass(final IFilterContext context) {
		return context.getClassAttributes().contains("ScalaSig")
				|| context.getClassAttributes().contains("Scala");
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isScalaClass(context) || !context.getSuperClassName()
				.startsWith("scala/runtime/AbstractPartialFunction")) {
			return;
		}

		final Matcher matcher = new Matcher();

		matcher.applyOrElse(methodNode, output);
		matcher.isDefinedAt(methodNode, output);
		matcher.cutInit(methodNode, output);
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

		void applyOrElse(final MethodNode m, final IFilterOutput output) {
			if (!"applyOrElse".equals(m.name)) {
				return;
			}
			firstIs(m, Opcodes.ALOAD);
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.IFNULL);

			if (cursor == null) {
				return;
			}

			AbstractInsnNode ifpos = cursor;

			cursor = ((JumpInsnNode) ifpos).label;
			AbstractInsnNode cutStart = cursor;
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKEINTERFACE, "scala/Function1", "apply",
					"(Ljava/lang/Object;)Ljava/lang/Object;");
			nextIs(Opcodes.ASTORE);
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutEnd = cursor;

			// Cut out both the if (that goes to the MatchError) and the code
			// that throws the MatchError.
			output.ignore(ifpos, ifpos);
			output.ignore(cutStart, cutEnd);
		}

		void isDefinedAt(final MethodNode m, final IFilterOutput output) {
			if (!"isDefinedAt".equals(m.name)) {
				return;
			}
			firstIs(m, Opcodes.ALOAD);
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.IFNULL);
			if (cursor == null) {
				return;
			}

			AbstractInsnNode ifpos = cursor;

			cursor = ((JumpInsnNode) ifpos).label;
			AbstractInsnNode cutStart = cursor;
			nextIs(Opcodes.ICONST_0);
			nextIs(Opcodes.ISTORE);
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutEnd = cursor;

			// Cut out both the if (that goes to the MatchError) and the code
			// that throws the MatchError.
			output.ignore(ifpos, ifpos);
			output.ignore(cutStart, cutEnd);
		}

		void cutInit(final MethodNode m, final IFilterOutput output) {
			if (!"<init>".equals(m.name)
					|| (m.access & Opcodes.ACC_PUBLIC) == 0) {
				return;
			}

			firstIs(m, Opcodes.ALOAD);
			nextIs(Opcodes.IFNONNULL);
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
	}

}
