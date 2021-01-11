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
package org.jacoco.core.test.validation.scala2_11.targets

import org.jacoco.core.test.validation.targets.Stubs.{exec, noexec, nop}

/**
 * Test target for anonymous functions.
 */
object ScalaPartialFunctionTarget {

  def main(args: Array[String]): Unit = {

    val elements = List(1,2,3,4,5,6,12)

    val found = elements.collect { // assertPartlyCovered()
      case i if i % 2 == 0 => // assertPartlyCovered(2, 2)
        if (i < 10) { // assertPartlyCovered(0,2)
          i // assertFullyCovered()
        } else {
          i + 10 // assertFullyCovered()
        }
    }
    System.out.println(found)

    List("int", "char", "text", "something").collect(mapping) // assertFullyCovered()
  }

  def mapping: PartialFunction[String, Int] = { // assertPartlyCovered()
    case "int" => // assertPartlyCovered(2,3)
      1 // assertPartlyCovered()
    case "char" => // assertPartlyCovered()
      2 // assertPartlyCovered()
    case "text" => // assertPartlyCovered()
      3 // assertPartlyCovered()
    case "foo" => // assertPartlyCovered()
      4 // assertNotCovered()
    case "foo" => // assertEmpty()
      5 // assertNotCovered()
  }

}
