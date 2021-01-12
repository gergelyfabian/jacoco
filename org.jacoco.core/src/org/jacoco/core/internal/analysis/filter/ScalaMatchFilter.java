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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaMatchFilter implements IFilter {
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
			if (i.getOpcode() == Opcodes.IFEQ
					|| i.getOpcode() == Opcodes.IFNULL) {
				matcher.cutMatchError(i, output);
			}
		}
	}

	private static class Matcher extends AbstractMatcher {
		void cutMatchError(final AbstractInsnNode start,
				final IFilterOutput output) {
			// Check whether the 'if' points to a MatchError.
			cursor = ((JumpInsnNode) start).label;
			// Allow for an additional goto.
			nextIs(Opcodes.GOTO);
			if (cursor == null) {
				// If there was no goto, then reset to where the 'if' pointed
				// to.
				cursor = ((JumpInsnNode) start).label;
			}
			AbstractInsnNode cutStart = cursor;
			nextIsType(Opcodes.NEW, "scala/MatchError");
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKESPECIAL, "scala/MatchError", "<init>",
					"(Ljava/lang/Object;)V");
			nextIs(Opcodes.ATHROW);
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutEnd = cursor;

			// Cut out both the if (that goes to the MatchError) and the code
			// that throws the MatchError.
			output.ignore(start, start);
			output.ignore(cutStart, cutEnd);
		}
	}

}
