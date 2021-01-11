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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class ScalaPartialFunctionFilterTest extends FilterTestBase {

	private final ScalaPartialFunctionFilter filter = new ScalaPartialFunctionFilter();

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
	public void testScalaApplyOrElse() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"applyOrElse",
				"(Ljava/lang/Throwable;Lscala/Function1;)Ljava/lang/Object;",
				null, null);
		context.classAttributes.add("Scala");
		context.superClassName = "scala/runtime/AbstractPartialFunction";

		final Label label1 = new Label();
		final Label label2 = new Label();
		final Label label3 = new Label();

		final Range range1 = new Range();
		final Range range2 = new Range();

		Map<AbstractInsnNode, Set<AbstractInsnNode>> expectedReplacedBranches = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();

		// Simulate three jumps.
		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.IFEQ, label1);
		AbstractInsnNode firstIf = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode afterFirstIf = m.instructions.getLast();

		m.visitLabel(label1);

		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.IFEQ, label2);
		AbstractInsnNode secondIf = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode afterSecondIf = m.instructions.getLast();

		m.visitLabel(label2);

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode firstInstrAfterSecondLabel = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFEQ, label3);
		AbstractInsnNode thirdIf = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode afterThirdIf = m.instructions.getLast();

		m.visitLabel(label3);
		AbstractInsnNode thirdLabel = m.instructions.getLast();

		range2.fromInclusive = thirdLabel;
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "scala/Function1", "apply",
				"(Ljava/lang/Object;)Ljava/lang/Object;", false);
		m.visitInsn(Opcodes.ASTORE);
		m.visitInsn(Opcodes.ALOAD);
		m.visitInsn(Opcodes.ARETURN);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		// For the first 'if' we add branches to the instructions right after
		// all 'if's.
		final Set<AbstractInsnNode> firstIfNewTargets = new HashSet<AbstractInsnNode>();
		firstIfNewTargets.add(afterFirstIf);
		firstIfNewTargets.add(afterSecondIf);
		firstIfNewTargets.add(afterThirdIf);
		expectedReplacedBranches.put(firstIf, firstIfNewTargets);

		// For any "middle" 'if's (the second one in our case) we switch
		// branches to the corresponding label.
		final Set<AbstractInsnNode> secondIfNewTargets = new HashSet<AbstractInsnNode>();
		secondIfNewTargets.add(firstInstrAfterSecondLabel);
		expectedReplacedBranches.put(secondIf, secondIfNewTargets);

		// The last if is ignored, there is no branch replacement there.
		range1.fromInclusive = thirdIf;
		range1.toInclusive = thirdIf;

		assertIgnored(range2, range1);
		assertReplacedBranches(expectedReplacedBranches);
	}

	@Test
	public void testScalaIsDefinedAt() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"isDefinedAt", "(Ljava/lang/Throwable;)Z", null, null);
		context.classAttributes.add("Scala");
		context.superClassName = "scala/runtime/AbstractPartialFunction";

		final Label label1 = new Label();
		final Label label2 = new Label();
		final Label label3 = new Label();

		final Range range1 = new Range();
		final Range range2 = new Range();

		Map<AbstractInsnNode, Set<AbstractInsnNode>> expectedReplacedBranches = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();

		// Simulate three jumps.
		m.visitInsn(Opcodes.NOP);
		m.visitJumpInsn(Opcodes.IFEQ, label1);
		AbstractInsnNode firstIf = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode afterFirstIf = m.instructions.getLast();

		m.visitLabel(label1);

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode firstInstrAfterFirstLabel = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFEQ, label2);
		AbstractInsnNode secondIf = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode afterSecondIf = m.instructions.getLast();

		m.visitLabel(label2);

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode firstInstrAfterSecondLabel = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFEQ, label3);
		AbstractInsnNode thirdIf = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		AbstractInsnNode afterThirdIf = m.instructions.getLast();

		m.visitLabel(label3);
		AbstractInsnNode thirdLabel = m.instructions.getLast();

		range2.fromInclusive = thirdLabel;
		m.visitInsn(Opcodes.ICONST_0);
		m.visitInsn(Opcodes.ISTORE);
		m.visitInsn(Opcodes.ILOAD);
		m.visitInsn(Opcodes.IRETURN);
		range2.toInclusive = m.instructions.getLast();

		filter.filter(m, context, output);

		// For all but the last 'if's we add branches to the instructions right
		// after the 'if's.
		final Set<AbstractInsnNode> firstIfNewTargets = new HashSet<AbstractInsnNode>();
		firstIfNewTargets.add(firstInstrAfterFirstLabel);
		expectedReplacedBranches.put(firstIf, firstIfNewTargets);
		final Set<AbstractInsnNode> secondIfNewTargets = new HashSet<AbstractInsnNode>();
		secondIfNewTargets.add(firstInstrAfterSecondLabel);
		expectedReplacedBranches.put(secondIf, secondIfNewTargets);

		// The last if is ignored, there is no branch replacement there.
		range1.fromInclusive = thirdIf;
		range1.toInclusive = thirdIf;

		assertIgnored(range2, range1);
		assertReplacedBranches(expectedReplacedBranches);
	}

	@Test
	public void testScalaInit() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "<init>", "", null, null);
		context.classAttributes.add("Scala");
		context.superClassName = "scala/runtime/AbstractPartialFunction";

		final Label label = new Label();

		final Range range = new Range();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.IFNONNULL);
		range.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertNoReplacedBranches();
	}

}
