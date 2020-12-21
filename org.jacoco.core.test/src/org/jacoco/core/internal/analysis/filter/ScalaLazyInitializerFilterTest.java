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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class ScalaLazyInitializerFilterTest extends FilterTestBase {

	private final ScalaLazyInitializerFilter filter = new ScalaLazyInitializerFilter();

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
	public void testScalaLazyInitializer1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "message$lzycompute", "()I", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.IFNE);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		// Normally there are more instructions...

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaLazyInitializer2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "NestedFoo$lzycompute$1", "()V", null,
				null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		m.visitInsn(Opcodes.MONITORENTER);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.IFNONNULL);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		// Normally there are more instructions...

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaLazyAccessorIfNe() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "message", "()I", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.IFNE);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "mypackage/Foo",
				"message$lzycompute", "()I", false);
		m.visitInsn(Opcodes.GOTO);
		range1.toInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range1);
	}

	@Test
	public void testScalaLazyAccessorIfEq() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "message", "()I", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.IFEQ);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.GOTO);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "mypackage/Foo",
				"message$lzycompute", "()I", false);
		range1.toInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range1);
	}

	@Test
	public void testScalaLazyAccessorIfIcmpne() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "message", "()I", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		final Range range1 = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.IF_ICMPNE);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "mypackage/Foo",
				"message$lzycompute", "()I", false);
		m.visitInsn(Opcodes.GOTO);
		range1.toInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range1);
	}

	@Test
	public void testScalaLazyAccessorNestedCaseClass1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "NestedFoo",
				"()Lorg/jacoco/core/test/validation/scala2_11/targets/ScalaLazyValTarget1$NestedFoo$;",
				null, null);
		context.classAttributes.add("Scala");
		context.className = "org/jacoco/core/test/validation/scala2_11/targets/ScalaLazyValTarget1";

		final Range range1 = new Range();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.IFNONNULL);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, context.className,
				"NestedFoo$lzycompute",
				"()Lorg/jacoco/core/test/validation/scala2_11/targets/ScalaLazyValTarget1$NestedFoo$;",
				false);
		m.visitInsn(Opcodes.GOTO);
		range1.toInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1);
	}

	@Test
	public void testScalaLazyAccessorNestedCaseClass2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "NestedFoo",
				"()Lorg/jacoco/core/test/validation/scala/targets/ScalaLazyValTarget1$NestedFoo$;",
				null, null);
		context.classAttributes.add("Scala");
		context.className = "org/jacoco/core/test/validation/scala/targets/ScalaLazyValTarget1";

		final Range range1 = new Range();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.IFNONNULL);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, context.className,
				"NestedFoo$lzycompute$1", "()V", false);
		range1.toInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.GETFIELD);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range1);
	}

}
