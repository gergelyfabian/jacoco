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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Scala compiler generates for case classes.
 */
public final class ScalaRefEqualsFilter implements IFilter {
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
			matcher.cutNullHandling(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		void cutNullHandling(final AbstractInsnNode start,
				final IFilterOutput output) {
			cursor = start;

			nextIs(Opcodes.IFNONNULL);
			AbstractInsnNode cutStart = cursor;
			nextIs(Opcodes.POP);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.IFNULL);
			nextIs(Opcodes.GOTO);
			AbstractInsnNode cutEnd = cursor;

			if (cursor != null) {
				output.ignore(cutStart, cutEnd);
			}
		}
	}

}
