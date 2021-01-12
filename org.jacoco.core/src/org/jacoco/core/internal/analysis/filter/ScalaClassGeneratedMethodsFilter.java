/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gergely Fábián - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaClassGeneratedMethodsFilter implements IFilter {
	private static boolean isScalaClass(final IFilterContext context) {
		return context.getClassAttributes().contains("ScalaSig")
				|| context.getClassAttributes().contains("Scala");
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!isScalaClass(context) || context.getClassName().endsWith("$")) {
			return;
		}

		final Matcher matcher = new Matcher();
		if (matcher.isStaticMethodAccessor(context.getClassName(),
				methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
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

		boolean isStaticMethodAccessor(final String className,
				final MethodNode m) {
			if ((m.access & Opcodes.ACC_PUBLIC) == 0
					|| (m.access & Opcodes.ACC_STATIC) == 0) {
				return false;
			}
			firstIs(m, Opcodes.GETSTATIC);
			next();
			// There may be no other statements between, then it may be a static
			// field accessor also.
			while (cursor != null
					&& cursor.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				next();
			}
			if (cursor == null) {
				return false;
			}
			cursor = cursor.getPrevious();
			nextIsInvoke(Opcodes.INVOKEVIRTUAL, className + "$", m.name,
					m.desc);
			next();
			if (cursor == null) {
				return false;
			}
			return (cursor.getOpcode() == Opcodes.IRETURN
					|| cursor.getOpcode() == Opcodes.LRETURN
					|| cursor.getOpcode() == Opcodes.FRETURN
					|| cursor.getOpcode() == Opcodes.DRETURN
					|| cursor.getOpcode() == Opcodes.ARETURN
					|| cursor.getOpcode() == Opcodes.RETURN);
		}
	}

}
