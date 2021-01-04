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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class ScalaMatchFilterTest extends FilterTestBase {

	private final ScalaMatchFilter filter = new ScalaMatchFilter();

	@Test
	public void testNonScala() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testScalaNop() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		context.classAttributes.add("Scala");

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testScalaMatchErrorRemoved() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"whatever", "()Z", null, null);
		context.classAttributes.add("Scala");

		final Label label1 = new Label();
		final Label label2 = new Label();

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.IFEQ, label1);
		m.visitInsn(Opcodes.NOP);
		// And a lot of other instructions...
		m.visitLabel(label1);

		m.visitJumpInsn(Opcodes.IFEQ, label2);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLabel(label2);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "scala/MatchError");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "scala/MatchError", "<init>",
				"(Ljava/lang/Object;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	@Test
	public void testScalaMatchErrorWithGotoRemoved() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"whatever", "()Z", null, null);
		context.classAttributes.add("Scala");

		final Label label1 = new Label();
		final Label label2 = new Label();

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.IFEQ, label1);
		m.visitInsn(Opcodes.NOP);
		// And a lot of other instructions...
		m.visitLabel(label1);

		m.visitJumpInsn(Opcodes.IFEQ, label2);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLabel(label2);
		m.visitInsn(Opcodes.GOTO);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "scala/MatchError");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "scala/MatchError", "<init>",
				"(Ljava/lang/Object;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	@Test
	public void testScalaMatchErrorWithIfNullRemoved() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"whatever", "()Z", null, null);
		context.classAttributes.add("Scala");

		final Label label = new Label();

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);

		m.visitJumpInsn(Opcodes.IFNULL, label);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		final Range range2 = new Range();
		m.visitLabel(label);
		range2.fromInclusive = m.instructions.getLast();
		m.visitTypeInsn(Opcodes.NEW, "scala/MatchError");
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "scala/MatchError", "<init>",
				"(Ljava/lang/Object;)V", false);
		m.visitInsn(Opcodes.ATHROW);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

}
