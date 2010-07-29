/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.specs.Specification

class FilterSpec extends Specification {

  """"x" === "1" (equal)""" should {
    """be converted into the filter string "(x=1)" """ in {
      val filter: Filter = "x" === "1"
      filter.toString mustEqual "(x=1)"
    }
  }

  """~"x" (present)""" should {
    """be converted into the filter string "(x=*)" """ in {
      val filter: Filter = ~"x"
      filter.toString mustEqual "(x=*)"
    }
  }

  """!(("x" === "1") && ("y" <== "2") || (("z" >== "3") and ("a" ~== "9")) or "z".present)""" should {
    """be converted into the filter string "(!(|(&(x=1)(y<=2))(&(z>=3)(a~=9))(z=*)))" """ in {
      val filter: Filter = !(("x" === "1") && ("y" <== "2") || (("z" >== "3") and ("a" ~== "9")) or "z".present)
      filter.toString mustEqual "(!(|(&(x=1)(y<=2))(&(z>=3)(a~=9))(z=*)))"
    }
  }
}
