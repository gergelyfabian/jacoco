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
public class ScalaRefEqualsFilterTest extends FilterTestBase {

	private final ScalaRefEqualsFilter filter = new ScalaRefEqualsFilter();

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
	public void testScalaIfNonNullIfNullRemoved() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"whatever", "()Z", null, null);
		context.classAttributes.add("Scala");

		final Range range = new Range();

		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.IFNONNULL);
		range.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.POP);
		m.visitInsn(Opcodes.ALOAD);
		m.visitInsn(Opcodes.IFNULL);
		m.visitInsn(Opcodes.GOTO);
		range.toInclusive = m.instructions.getLast();

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertNoReplacedBranches();
	}

}
