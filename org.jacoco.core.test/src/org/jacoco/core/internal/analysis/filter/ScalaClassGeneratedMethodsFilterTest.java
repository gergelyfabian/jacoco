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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class ScalaClassGeneratedMethodsFilterTest extends FilterTestBase {

	private final ScalaClassGeneratedMethodsFilter filter = new ScalaClassGeneratedMethodsFilter();

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
	public void testScalaIsStaticFieldAccessor1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "message",
				"()Ljava/lang/String;", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "message",
				"()Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticFieldAccessor2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "number", "()I", null,
				null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "number",
				"()I", false);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticMethodAccessor1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "main",
				"([Ljava/lang/String;)V", false);
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
