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
package org.jacoco.core.test.validation.scala.targets

import org.jacoco.core.test.validation.targets.Stubs.{exec, noexec, nop}

/**
 * Test target for lazy initializers and accessors.
 */
class ScalaLazyValTarget1 { // assertFullyCovered()
  lazy val message = ScalaLazyValTarget1.generateValue(3, 4) // assertPartlyCovered()
  val a = 10 // assertPartlyCovered()

  case class NestedFoo(a: Int, b: Int) // assertPartlyCovered()

  val foo = NestedFoo(1, 2) // assertPartlyCovered()
}

object ScalaLazyValTarget1 { // assertFullyCovered()

  def generateValue(a: Int, b: Int): Int = a * b  // assertFullyCovered()

  def main(args: Array[String]): Unit = {

    val foo = new ScalaLazyValTarget1() // assertFullyCovered()
    foo.message // assertFullyCovered()
  }

}
