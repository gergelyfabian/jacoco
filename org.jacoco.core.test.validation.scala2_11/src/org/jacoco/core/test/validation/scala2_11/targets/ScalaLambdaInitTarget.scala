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
package org.jacoco.core.test.validation.scala2_11.targets

import org.jacoco.core.test.validation.targets.Stubs.{exec, noexec, nop}

/**
 * Test target for anonymous functions.
 */
object ScalaLambdaInitTarget {

  def lambdaTest(test: Int) = {
    for {
      a <- List(1) // assertFullyCovered()
      b <- List(2) // assertFullyCovered()
      c <- List(3) // assertFullyCovered()
      d <- if (test < 10) { // assertFullyCovered(0, 2)
        List(4) // assertFullyCovered()
      } else {
        List(14) // assertFullyCovered()
      }
    } yield a + b + c + d // assertFullyCovered()
  }

  def main(args: Array[String]): Unit = { // assertEmpty()

    lambdaTest(1) // assertFullyCovered()
    lambdaTest(11) // assertFullyCovered()

  }

}
