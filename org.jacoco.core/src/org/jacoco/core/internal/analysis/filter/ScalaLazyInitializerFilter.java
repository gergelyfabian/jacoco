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
public final class ScalaLazyInitializerFilter implements IFilter {
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
		matcher.lazyInitializer(methodNode, output);

		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			matcher.cutLazyInitializerUsage(context.getClassName(), methodNode,
					i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		Pattern lazyComputeSuffixPattern = Pattern
				.compile(".*\\$lzycompute(\\$\\d+)?$");

		void lazyInitializer(final MethodNode m, final IFilterOutput output) {
			if ((m.access & Opcodes.ACC_PRIVATE) == 0) {
				return;
			}
			if (!lazyComputeSuffixPattern.matcher(m.name).matches()) {
				return;
			}
			firstIsALoad0(m);
			nextIs(Opcodes.DUP);
			nextIs(Opcodes.ASTORE);
			nextIs(Opcodes.MONITORENTER);
			nextIs(Opcodes.ALOAD);
			// Not checking more of the instructions.
			if (cursor != null) {
				output.ignore(m.instructions.getFirst(),
						m.instructions.getLast());
			}
		}

		void cutLazyInitializerUsage(final String className, final MethodNode m,
				final AbstractInsnNode start, final IFilterOutput output) {
			cursor = start;

			nextIs(Opcodes.IFNE);
			AbstractInsnNode cutStart = cursor;
			nextIs(Opcodes.ALOAD);
			nextIsInvoke(Opcodes.INVOKESPECIAL, className,
					m.name + "$lzycompute");
			nextIs(Opcodes.GOTO);
			if (cursor == null) {
				// Alternative implementation for Scala 2.11.
				cursor = start;
				nextIs(Opcodes.IFEQ);
				cutStart = cursor;
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.GETFIELD);
				nextIs(Opcodes.GOTO);
				nextIs(Opcodes.ALOAD);
				nextIsInvoke(Opcodes.INVOKESPECIAL, className,
						m.name + "$lzycompute");
			}
			if (cursor == null) {
				// Lazy initializer for nested case classes, scala 2.11.
				cursor = start;
				nextIs(Opcodes.IFNONNULL);
				cutStart = cursor;
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.INVOKESPECIAL);
				if (cursor != null) {
					final MethodInsnNode call = (MethodInsnNode) cursor;

					if (!call.owner.equals(className)
							|| !call.name.startsWith(m.name + "$lzycompute")
							|| !call.desc.equals(
									"()L" + className + "$" + m.name + "$;")) {
						cursor = null;
					}
				}
				nextIs(Opcodes.GOTO);
			}
			if (cursor == null) {
				// Lazy initializer for nested case classes.
				cursor = start;
				nextIs(Opcodes.IFNONNULL);
				cutStart = cursor;
				nextIs(Opcodes.ALOAD);
				nextIs(Opcodes.INVOKESPECIAL);
				if (cursor != null) {
					final MethodInsnNode call = (MethodInsnNode) cursor;

					if (!call.owner.equals(className)
							|| !call.name.startsWith(m.name + "$lzycompute")
							|| !call.desc.equals("()V")) {
						cursor = null;
					}
				}
			}
			if (cursor == null) {
				// Implementation in case of many lazy initializers.
				cursor = start;
				nextIs(Opcodes.IF_ICMPNE);
				cutStart = cursor;
				nextIs(Opcodes.ALOAD);
				nextIsInvoke(Opcodes.INVOKESPECIAL, className,
						m.name + "$lzycompute");
				nextIs(Opcodes.GOTO);
			}
			if (cursor == null) {
				return;
			}
			AbstractInsnNode cutEnd = cursor;
			output.ignore(cutStart, cutEnd);
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
