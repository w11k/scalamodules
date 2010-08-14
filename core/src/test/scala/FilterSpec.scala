/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.specs.Specification
import org.specs.mock.Mockito

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
      val filter: Filter =
        !(("x" === "1") && ("y" <== "2") || (("z" >== "3") and ("a" ~== "9")) or "z".present)
      filter.toString mustEqual "(!(|(&(x=1)(y<=2))(&(z>=3)(a~=9))(z=*)))"
    }
  }
}

class AndBuilderSpec extends Specification with Mockito {

  "Calling AndBuilder.&&" should {
    val component = mock[FilterComponent]
    "throw an IllegalArgumentException given null" in {
      new AndBuilder(component) && null must throwA[IllegalArgumentException]
    }
  }

  "Calling AndBuilder.and" should {
    val component = mock[FilterComponent]
    "throw an IllegalArgumentException given null" in {
      new AndBuilder(component) and null must throwA[IllegalArgumentException]
    }
  }

  "Calling OrBuilder.||" should {
    val component = mock[FilterComponent]
    "throw an IllegalArgumentException given null" in {
      new OrBuilder(component) || null must throwA[IllegalArgumentException]
    }
  }

  "Calling OrBuilder.or" should {
    val component = mock[FilterComponent]
    "throw an IllegalArgumentException given null" in {
      new OrBuilder(component) or null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.===" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") === null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.equal" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") equal null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.~==" should {
    "throw an IllegalArgumentException given null" in {
      (new SimpleOpBuilder("") ~== null) must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.approx" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") approx null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.>==" should {
    "throw an IllegalArgumentException given null" in {
      (new SimpleOpBuilder("") >== null) must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.ge" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") ge null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.greaterEqual" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") greaterEqual null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.<==" should {
    "throw an IllegalArgumentException given null" in {
      (new SimpleOpBuilder("") <== null) must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.le" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") le null must throwA[IllegalArgumentException]
    }
  }

  "Calling SimpleOpBuilder.lessEqual" should {
    "throw an IllegalArgumentException given null" in {
      new SimpleOpBuilder("") lessEqual null must throwA[IllegalArgumentException]
    }
  }
}
