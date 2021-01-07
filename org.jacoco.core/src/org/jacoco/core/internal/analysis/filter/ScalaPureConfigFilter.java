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

import java.util.regex.Pattern;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaPureConfigFilter implements IFilter {
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
		matcher.cutMacroMethods(context.getClassName(), methodNode, output);

		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (i.getOpcode() == Opcodes.IFNE) {
				matcher.methodUsingLazyCompute(context.getClassName(),
						methodNode, i, output);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		Pattern macroPattern = Pattern.compile(".*\\$macro\\$\\d+\\$\\d+.*");

		void methodUsingLazyCompute(final String className, final MethodNode m,
				final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start;
			AbstractInsnNode cutStart = cursor;
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.INVOKESPECIAL);
			if (cursor != null) {
				final MethodInsnNode call = (MethodInsnNode) cursor;

				if (!call.owner.equals(className)
						|| !call.name.startsWith(m.name + "$lzycompute")
						|| !call.desc.startsWith("()Lpureconfig/")) {
					cursor = null;
				}
			}
			nextIs(Opcodes.GOTO);
			AbstractInsnNode cutEnd = cursor;
			if (cursor != null) {
				output.ignore(cutStart, cutEnd);
			}

		}

		void cutMacroMethods(final String className, final MethodNode m,
				final IFilterOutput output) {
			if (!macroPattern.matcher(className).matches()) {
				return;
			}
			output.ignore(m.instructions.getFirst(), m.instructions.getLast());
		}

	}

}
