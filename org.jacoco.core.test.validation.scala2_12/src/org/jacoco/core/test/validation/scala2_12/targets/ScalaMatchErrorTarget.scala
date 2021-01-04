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
package org.jacoco.core.test.validation.scala2_12.targets

import org.jacoco.core.test.validation.targets.Stubs.{exec, noexec, nop}
import scala.util.{Try, Success, Failure}

/**
 * Test target for anonymous functions.
 */
object ScalaMatchErrorTarget { // assertFullyCovered()

  def lambdaTestCase(x: Any) = { // assertEmpty()
    val res = x match { // assertFullyCovered()
      case l: java.util.List[_] => // assertFullyCovered(0, 2)
        Success(l.size()) // assertFullyCovered()
      case l: Any => // assertFullyCovered()
        Failure(new IllegalArgumentException(s"$l is not allowed")) // assertFullyCovered()
    }
    res.map(_ * 2)
  }

  def main(args: Array[String]): Unit = { // assertEmpty()

    val foo = new java.util.ArrayList[String]
    foo.add("Test")
    lambdaTestCase(foo) // assertFullyCovered()
    lambdaTestCase(List(1, 2, 3)) // assertFullyCovered()

  }

}
