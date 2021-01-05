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
public final class ScalaLoggingMacroFilter implements IFilter {
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

		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			matcher.cutLoggingMacro(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		void cutLoggingMacro(final AbstractInsnNode start,
				final IFilterOutput output) {
			cursor = start;

			nextIsInvoke(Opcodes.INVOKEVIRTUAL,
					"com/typesafe/scalalogging/Logger", "underlying",
					"()Lorg/slf4j/Logger;");
			// Invoke interface is followed by "ifeq" when scalalogging marco is
			// compiled.
			nextIs(Opcodes.INVOKEINTERFACE);
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutStart = cursor;
			final MethodInsnNode call = (MethodInsnNode) cursor;
			if (!call.owner.equals("org/slf4j/Logger")
					|| !call.name.endsWith("Enabled")
					|| !call.name.startsWith("is")) {
				return;
			}
			nextIs(Opcodes.IFEQ);
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutEnd = cursor;

			output.ignore(cutStart, cutEnd);
		}
	}

}
