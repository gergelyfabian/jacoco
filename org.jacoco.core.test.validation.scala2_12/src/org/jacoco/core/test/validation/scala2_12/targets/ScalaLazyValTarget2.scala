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
package org.jacoco.core.test.validation.scala2_12.targets

import org.jacoco.core.test.validation.targets.Stubs.{exec, noexec, nop}

/**
 * Test target for lazy initializers and accessors.
 */
class ScalaLazyValTarget2 { // assertFullyCovered()
  lazy val message = ScalaLazyValTarget2.generateValue(3, 4) // assertPartlyCovered()
  lazy val message2 = ScalaLazyValTarget2.generateValue("Hello") // assertPartlyCovered()
  lazy val message3 = ScalaLazyValTarget2.generateValue("Hello") // assertPartlyCovered()
  val a = 10 // assertPartlyCovered()
}

object ScalaLazyValTarget2 { // assertFullyCovered()

  def generateValue(a: Int, b: Int): Int = a * b  // assertFullyCovered()
  def generateValue(str: String): String = str + " " + str  // assertFullyCovered()

  def main(args: Array[String]): Unit = {

    val foo = new ScalaLazyValTarget2() // assertFullyCovered()
    foo.message // assertFullyCovered()
    foo.message2 // assertFullyCovered()
    foo.message3 // assertFullyCovered()
  }

}
