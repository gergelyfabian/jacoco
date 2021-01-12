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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class ScalaCaseClassGeneratedMethodsFilterTest extends FilterTestBase {

	private final ScalaCaseClassGeneratedMethodsFilter filter = new ScalaCaseClassGeneratedMethodsFilter();

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
	public void testScalaIsEquals() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"equals", "(Ljava/lang/Object;)Z", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.ALOAD, 0);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsHashCode1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
				"_hashCode", "(Lscala/Product;)I", false);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsHashCode2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"hashCode", "()I", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.LDC);
		m.visitVarInsn(Opcodes.ISTORE, 1);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 0);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsToString() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"toString", "()Ljava/lang/String;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
				"_toString", "(Lscala/Product;)Ljava/lang/String;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductIterator() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productIterator", "()Lscala/collection/Iterator;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/runtime/ScalaRunTime$",
				"typedProductIterator",
				"(Lscala/Product;)Lscala/collection/Iterator;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductElement() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productElement", "(I)Ljava/lang/Object;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInsn(Opcodes.TABLESWITCH);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductArity1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productArity", "()I", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.ICONST_3);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductArity2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productArity", "()I", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.BIPUSH, 6);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductPrefix() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productPrefix", "()Ljava/lang/String;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.LDC);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsCopyDefault1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"copy$default$1", "()I", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.INVOKEVIRTUAL);
		m.visitInsn(Opcodes.IRETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsCopyDefault2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"copy$default$2", "()Lscala/collection/immutable/Set;", null,
				null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitInsn(Opcodes.INVOKEVIRTUAL);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsCopy1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"copy",
				"(ILjava/lang/String;Lscala/collection/immutable/Set;)Lmypackage/Foo;",
				null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.NEW);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.INVOKESPECIAL);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsCopy2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"copy",
				"(Lcats/data/NonEmptyList;Ljava/lang/String;Lscala/Option;Lscala/Option;Lmypackage/Foo1;Lmypackage/Foo2;)Lmypackage/Foo;",
				null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.NEW);
		m.visitInsn(Opcodes.DUP);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitInsn(Opcodes.INVOKESPECIAL);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsCurried() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"curried", "()Lscala/Function1;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");
		context.className = "Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Foo$", "curried",
				"()Lscala/Function1;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsTupled() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"tupled", "()Lscala/Function1;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");
		context.className = "Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Foo$", "tupled",
				"()Lscala/Function1;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsApply1() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"apply",
				"(Lcats/data/NonEmptyList;Lmypackage/Bar;Lmypackage/Bar;Lmypackage/Bar;Lmypackage/Bar;Lscala/Option;)Lmypackage/Foo;",
				null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");
		context.className = "mypackage/Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "apply",
				"(Lcats/data/NonEmptyList;Lmypackage/Bar;Lmypackage/Bar;Lmypackage/Bar;Lmypackage/Bar;Lscala/Option;)Lmypackage/Foo;",
				false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsApply2() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"apply",
				"(Lcats/data/NonEmptyList;Lmypackage/Bar;Lscala/Option;)Lmypackage/Foo;",
				null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");
		context.className = "mypackage/Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ILOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "apply",
				"(Lcats/data/NonEmptyList;Lmypackage/Bar;Lscala/Option;)Lmypackage/Foo;",
				false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaCutIfNonNullInInit() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "<init>", "", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");
		context.className = "mypackage/Foo";

		final Range range = new Range();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitInsn(Opcodes.PUTFIELD);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.PUTFIELD);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.IFNONNULL);
		range.fromInclusive = m.instructions.getLast();
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitInsn(Opcodes.ATHROW);
		range.toInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		// More instructions...
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(range);
		assertNoReplacedBranches();
	}

	@Test
	public void testScalaIsUnapply() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"unapply", "(Lmypackage/Foo;)Lscala/Option;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");
		context.className = "mypackage/Foo";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "unapply",
				"(Lmypackage/Foo;)Lscala/Option;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsLessInit() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"$lessinit$greater$default$4", "()Lscala/Option;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Foo", "test", "()Z", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsApplyDefault() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"apply$default$3", "()Lscala/Option;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Foo", "test", "()Z", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductElementNames() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productElementNames", "()Lscala/collection/Iterator;", null,
				null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "scala/Product",
				"productElementNames$",
				"(Lscala/Product;)Lscala/collection/Iterator;", false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsProductElementName() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"productElementName", "(I)Ljava/lang/String;", null, null);
		context.classAttributes.add("Scala");
		context.interfaceNames.add("scala/Product");

		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInsn(Opcodes.TABLESWITCH);
		// Not testing more instructions.

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticUnapply() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "unapply", "", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticUnapplySeq() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "unapplySeq", "", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticProductElementExtension() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "productElement$extension",
				"(Ljava/lang/String;I)Ljava/lang/Object;", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.ISTORE, 2);
		m.visitVarInsn(Opcodes.ILOAD, 2);
		m.visitInsn(Opcodes.TABLESWITCH);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticEqualsExtension() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "equals$extension", "", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.ALOAD);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsReadResolve() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PRIVATE, "readResolve", "()Ljava/lang/Object;",
				null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticToString() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "toString",
				"()Ljava/lang/String;", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.LDC);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaStaticIsApplyDefault() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "apply$default$3", "()Lscala/Option;", null,
				null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaStaticIsLessInit() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "$lessinit$greater$default$4",
				"()Lscala/Option;", null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitInsn(Opcodes.GETSTATIC);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void testScalaIsStaticObjectApply() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "apply",
				"(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
				null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$";

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitInsn(Opcodes.CHECKCAST);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "mypackage/Foo$", "apply",
				"(Lcats/data/NonEmptyList;Lmypackage/Bar;Lscala/Option;)Lmypackage/Foo;",
				false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
