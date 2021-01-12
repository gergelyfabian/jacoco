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
public class ScalaLoggingMarcoFilterTest extends FilterTestBase {

	private final ScalaLoggingMacroFilter filter = new ScalaLoggingMacroFilter();

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
	public void testScalaLoggingMacroRemoved() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"whatever", "()Z", null, null);
		context.classAttributes.add("Scala");

		final Label label = new Label();

		final Range range = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"com/typesafe/scalalogging/Logger", "underlying",
				"()Lorg/slf4j/Logger;", false);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger",
				"isDebugEnabled", "()Z", true);
		range.fromInclusive = m.instructions.getLast();

		m.visitJumpInsn(Opcodes.IFEQ, label);
		range.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertNoReplacedBranches();
	}

}
