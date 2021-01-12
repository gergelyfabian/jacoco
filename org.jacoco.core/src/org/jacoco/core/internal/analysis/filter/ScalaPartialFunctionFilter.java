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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.Set;

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

		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			matcher.match(methodNode, i, output);
		}

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

		void match(final MethodNode m, final AbstractInsnNode start,
				final IFilterOutput output) {
			if (start.getType() != InsnNode.LABEL) {
				return;
			}
			cursor = start;
			AbstractInsnNode lastLabel = start;
			AbstractInsnNode lastJump = null;

			// Default for applyOrElse
			next();
			// Ignore a single goto.
			if (cursor != null && cursor.getOpcode() != Opcodes.GOTO) {
				cursor = cursor.getPrevious();
			}
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKEINTERFACE, "scala/Function1", "apply",
					"(Ljava/lang/Object;)Ljava/lang/Object;");
			nextIs(Opcodes.ASTORE);
			next();
			// Ignore a single goto.
			if (cursor != null && cursor.getOpcode() != Opcodes.GOTO) {
				cursor = cursor.getPrevious();
			}
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ARETURN);

			if (cursor == null) {
				// Default for isDefinedAt
				cursor = start;
				next();
				// Ignore a single goto.
				if (cursor != null && cursor.getOpcode() != Opcodes.GOTO) {
					cursor = cursor.getPrevious();
				}
				nextIs(Opcodes.ICONST_0);
				nextIs(Opcodes.ISTORE);
				next();
				// Ignore a single goto.
				if (cursor != null && cursor.getOpcode() != Opcodes.GOTO) {
					cursor = cursor.getPrevious();
				}
				nextIs(Opcodes.ILOAD);
				nextIs(Opcodes.IRETURN);
			}

			if (cursor == null) {
				return;
			}
			output.ignore(start, cursor);

			final Set<AbstractInsnNode> newTargetsForFirstIf = new HashSet<AbstractInsnNode>();

			// Start from the current cursor (end of partial function) and
			// search 'if's backwards.
			for (AbstractInsnNode i = start; i != null
					&& lastLabel != null; i = i.getPrevious()) {
				if (i.getOpcode() == Opcodes.IFEQ) {
					LabelNode directJump = ((JumpInsnNode) i).label;
					AbstractInsnNode jumpNode = AbstractMatcher
							.skipNonOpcodes(((JumpInsnNode) i).label);
					LabelNode indirectJump = null;
					if (jumpNode.getOpcode() == Opcodes.GOTO) {
						indirectJump = ((JumpInsnNode) jumpNode).label;
					}
					if (directJump == start || indirectJump == start) {
						// Ignore last case.
						output.ignore(i, i);

						// The first 'if' should have a new branch for the else
						// case of this one.
						newTargetsForFirstIf.add(
								AbstractMatcher.skipNonOpcodes(i.getNext()));

						// Save references to the "last" if and label we have
						// seen (going backwards).
						lastJump = i;
						lastLabel = findLastLabel(i);
					} else if (directJump == lastLabel
							|| indirectJump == lastLabel) {
						// Ignore the branch for all ifs but the last.
						ignoreBranch(i, output);

						// The first 'if' should have a new branch for the else
						// case of this one.
						newTargetsForFirstIf.add(
								AbstractMatcher.skipNonOpcodes(i.getNext()));

						// Save references to the "last" if and label we have
						// seen (going backwards).
						lastJump = i;
						lastLabel = findLastLabel(i);
					}
				}
			}

			// The last jump we found (the first if) should receive all branches
			// that we found.
			if (lastJump != null && !"isDefinedAt".equals(m.name)) {
				output.replaceBranches(lastJump, newTargetsForFirstIf);
			}
		}

		private static AbstractInsnNode findLastLabel(
				final AbstractInsnNode jumpNode) {
			for (AbstractInsnNode i = jumpNode; i != null; i = i
					.getPrevious()) {
				if (i.getType() == InsnNode.LABEL) {
					return i;
				}
			}
			return null;
		}

		private static void ignoreBranch(final AbstractInsnNode jumpNode,
				final IFilterOutput output) {
			final LabelNode label;
			label = ((JumpInsnNode) jumpNode).label;
			final Set<AbstractInsnNode> newTargets = new HashSet<AbstractInsnNode>();
			newTargets.add(AbstractMatcher.skipNonOpcodes(label));
			output.replaceBranches(jumpNode, newTargets);
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
