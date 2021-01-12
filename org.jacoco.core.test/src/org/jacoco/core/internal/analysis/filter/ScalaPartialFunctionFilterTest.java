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

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

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

		final Label label = new Label();

		final Range range1 = new Range();
		final Range range2 = new Range();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		// And a lot of other instructions...
		m.visitLabel(label);

		range2.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "scala/Function1", "apply",
				"(Ljava/lang/Object;)Ljava/lang/Object;", false);
		m.visitInsn(Opcodes.ASTORE);
		range2.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.ALOAD);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
	}

	@Test
	public void testScalaIsDefinedAt() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"isDefinedAt", "(Ljava/lang/Throwable;)Z", null, null);
		context.classAttributes.add("Scala");
		context.superClassName = "scala/runtime/AbstractPartialFunction";

		final Label label = new Label();

		final Range range1 = new Range();
		final Range range2 = new Range();

		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitJumpInsn(Opcodes.IFNULL, label);
		range1.fromInclusive = m.instructions.getLast();
		range1.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);
		// And a lot of other instructions...
		m.visitLabel(label);

		range2.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ICONST_0);
		m.visitVarInsn(Opcodes.ISTORE, 3);
		range2.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range1, range2);
		assertNoReplacedBranches();
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
