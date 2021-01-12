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
package org.jacoco.core.test.validation.scala2_12;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.scala2_12.targets.ScalaMatchErrorTarget;
import org.junit.Test;

/**
 * Test of synchronized block.
 */
public class ScalaMatchErrorTest extends ValidationTestBase {

	public ScalaMatchErrorTest() {
		super(ScalaMatchErrorTarget.class);
	}

}
