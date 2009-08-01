/**
 * Copyright 2009 Heiko Seeberger and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalamodules.core.filters

import scalatest.matchers.ShouldMatchers
import scalatest.Spec

import Filter._

class FilterSpec extends Spec with ShouldMatchers {

  describe("Null checks") {

    it("Should not allow null filter attributes") {
      intercept[NullPointerException] {
        Set(null, 10)
      }
    }
  }

  describe("Textual representations") {
    it("Should become a legal & filter") {
      And(Set("foo"), Not(Set("bar"))) should equal("(&(foo=*)(!(bar=*)))")
    }
    it("Should become a legal | filter") {
      Or(Set("foo", "bar"), Set("zot", 4)).toString should equal("(|(foo=bar)(zot=4))")
    }
    it("Should become a legal ! filter") {
      Not(Set("foo", "bar")).toString should equal("(!(foo=bar))")
    }
    it("Should become a legal ! atom filter") {
      NotSet("foo", "bar").toString should equal("(!(foo=bar))")
    }
    it("Should become truth filter") {
      IsTrue("foo").toString should equal("(foo=true)")
    }
    it("Should become set filter") {
      Set("foo").toString should equal("(foo=*)")
    }
    it("Should become lt filter") {
      Lt("foo", 5).toString should equal("(foo<=5")
    }
    it("Should become bt filter") {
      Bt("foo", 5).toString should equal("(foo>=5")
    }
    it("Should become nil filter") {
      NilFilter.toString should equal("")
    }
  }

  describe("Implicit from tuple") {
    it("Should become set filter") {
      ("foo" -> 10) should equal(Set("foo", 5))
    }
    it("Should become set filter") {
      ("foo" -> null) should equal(Set("foo", 5))
    }
    it("Should work!") {
      ("zit" === "bar") && "foo" && ("zot" -> "zip") should equal(And(Set("zit", "bar"),Set("foo"),Set("zot", "zip")))
    }
  }

  describe("Collapsing of composition") {
    it("Should collapse to a single level") {
      And(And(("zot" -> "zip"), ("foo" -> "bar")),
        ("a" -> "b"),
        And(("yin" -> "yang"), ("peace" -> "war"))) should equal(And
                (Set("zot", "zip"), Set("foo", "bar"), Set("a", "b"), Set("yin", "yang"), Set("peace", "war")))
    }
    it("Should not collapse") {
      Or(And(("zot" -> "zip"), ("foo" -> "bar")), ("a" -> "b"),
        And(("yin" -> "yang"), ("peace" -> "war"))).toString should equal("(|(&(zot=zip)(foo=bar))(a=b)(&(yin=yang)(peace=war)))")
    }
  }

  describe("Compose from methods") {
    it("Should become an and") {
      IsTrue("foo") && Set("zot", "zip") should equal(And(IsTrue("foo"), Set("zot", "zip")))
    }
    it("Should become an and") {
      IsTrue("foo") and Set("zot", "zip") should equal(And(IsTrue("foo"), Set("zot", "zip")))
    }
  }

  describe("NilFilter treatment") {
    it("Should remove NilFilters") {
      And(NilFilter,
        Set("foo", "bar"), NilFilter, NilFilter,
        Set("zip", 5), NilFilter) should equal (And(Set("foo", "bar"), Set("zip", 5)))
    }
    it("Should become a simple filter") {
      Set("foo", 5) && NilFilter should equal(Set("foo", 5))
    }
  }

  describe("Implicit builders") {
    it("Should become a PropertyFilterBuilder and invoke ===") {
      ("foo" === 5) should equal(Set("foo", 5))
    }
    it("Should become a PropertyFilterBuilder and invoke <==") {
      ("foo" <== 4) should equal(Lt("foo", 4))
    }
    it("Should become a PropertyFilterBuilder and invoke >==") {
      ("foo" >== 3) should equal(Bt("foo", 3))
    }
    it("Should become a PropertyFilterBuilder and invoke ~==") {
      ("foo" ~== 5) should equal(Approx("foo", 5))
    }
    it("Should become a PropertyFilterBuilder and invoke ===") {
      ("foo" === true) should equal(IsTrue("foo"))
    }
    it("Should become a PropertyFilterBuilder and invoke isTrue") {
      ("foo" isTrue) should equal(IsTrue("foo"))
    }
    it("Should become a PropertyFilterBuilder and invoke set") {
      ("foo" set) should equal(Set("foo"))
    }
  }
}