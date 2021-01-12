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
package org.jacoco.core.test.validation.scala2_11;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.scala2_11.targets.ScalaPartialFunctionTarget;
import org.junit.Test;

/**
 * Test of synchronized block.
 */
public class ScalaPartialFunctionTest extends ValidationTestBase {

	public ScalaPartialFunctionTest() {
		super(ScalaPartialFunctionTarget.class);
	}

}
