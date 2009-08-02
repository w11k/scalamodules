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
import Filter.{not => notf}

class FilterSpec extends Spec with ShouldMatchers {

  object emptyObject { override def toString = "   " }

  object namedObject { override def toString = "foo" }

  private def asFilter(filter: Filter) = filter

  describe("Null checks") {
    it("Should not allow null filter attributes") {
      intercept[NullPointerException] {
        set(null, 10)
      }
    }
  }

  describe("Textual representations") {
    it("Should become a legal & filter") {
      and(set("foo"), notf(set("bar"))) should equal("(&(foo=*)(!(bar=*)))")
    }
    it("Should become a legal | filter") {
      or(set("foo", "bar"), set("zot", 4)).toString should equal("(|(foo=bar)(zot=4))")
    }
    it("Should become a legal ! filter") {
      notf(set("foo", "bar")).toString should equal("(!(foo=bar))")
    }
    it("Should become a legal ! atom filter") {
      notSet("foo", "bar").toString should equal("(!(foo=bar))")
    }
    it("Should become truth filter") {
      isTrue("foo").toString should equal("(foo=true)")
    }
    it("Should become set filter") {
      set("foo").toString should equal("(foo=*)")
    }
    it("Should become lt filter") {
      lt("foo", 5).toString should equal("(foo<=5")
    }
    it("Should become bt filter") {
      lt("foo", 5).toString should equal("(foo>=5")
    }
    it("Should become nil filter") {
      NilFilter.toString should equal("")
    }
  }

  describe("Object attributes") {
    it("Should work with a non-string attribute") {
      set(namedObject, 5) should equal(set("foo", 5))
    }
    it("Should work with a non-string attribute") {
      bt(namedObject, 3) should equal(bt("foo", 3))
    }
    it("Should work with a non-string attribute") {
      lt(namedObject, 4) should equal(lt("foo", 4))
    }
    it ("Should not become a Filter, attribute is empty") {
      intercept[IllegalArgumentException] {
        set(emptyObject, 5)
      }
    }
    it ("Should not become a Filter") {
      intercept[IllegalArgumentException] {
        set(emptyObject)
      }
    }
  }

  describe("Implicit from tuple") {
    it("Should become set filter") {
      ("foo" -> 10) should equal(set("foo", 5))
    }
    it("Should become set filter") {
      ("foo" -> null) should equal(set("foo", 5))
    }
    it("Should work!") {
      ("zit" === "bar") && "foo" && ("zot" -> "zip") should equal(and(set("zit", "bar"),set("foo"),set("zot", "zip")))
    }
  }

  describe("Collapsing of composition") {
    it("Should collapse to a single level") {
      and(and(("zot" -> "zip"), ("foo" -> "bar")),
        ("a" -> "b"),
        and(("yin" -> "yang"), ("peace" -> "war"))) should equal(and
                (set("zot", "zip"), set("foo", "bar"), set("a", "b"), set("yin", "yang"), set("peace", "war")))
    }
    it("Should not collapse") {
      or(and(("zot" -> "zip"), ("foo" -> "bar")), ("a" -> "b"),
        and(("yin" -> "yang"), ("peace" -> "war"))).toString should equal("(|(&(zot=zip)(foo=bar))(a=b)(&(yin=yang)(peace=war)))")
    }
  }

  describe("Compose from methods") {
    it("Should become an and") {
      isTrue("foo") && set("zot", "zip") should equal(and(isTrue("foo"), set("zot", "zip")))
    }
    it("Should become an and") {
      isTrue("foo") and set("zot", "zip") should equal(and(isTrue("foo"), set("zot", "zip")))
    }
  }

  describe("NilFilter treatment") {
    it("Should remove NilFilters") {
      and(NilFilter,
        set("foo", "bar"), NilFilter, NilFilter,
        set("zip", 5), NilFilter) should equal (and(set("foo", "bar"), set("zip", 5)))
    }
    it("Should become a simple filter") {
      set("foo", 5) && NilFilter should equal(set("foo", 5))
    }
  }

  describe("Implicit builders") {
    it("Should become a PropertyFilterBuilder and invoke ===") {
      ("foo" === 5) should equal(set("foo", 5))
    }
    it("Should become a PropertyFilterBuilder and invoke <==") {
      ("foo" <== 4) should equal(lt("foo", 4))
    }
    it("Should become a PropertyFilterBuilder and invoke >==") {
      ("foo" >== 3) should equal(bt("foo", 3))
    }
    it("Should become a PropertyFilterBuilder and invoke ~==") {
      ("foo" ~== 5) should equal(approx("foo", 5))
    }
    it("Should become a PropertyFilterBuilder and invoke ===") {
      ("foo" === true) should equal(isTrue("foo"))
    }
    it("Should become a PropertyFilterBuilder and invoke isTrue") {
      ("foo" isTrue) should equal(isTrue("foo"))
    }
    it("Should become a PropertyFilterBuilder and invoke set") {
      ("foo" set) should equal(set("foo"))
    }
    it ("Should become an empty Filter") {
      asFilter("foo" -> emptyObject) should equal(set("foo"))
    }
    it ("Should not become a Filter") {
      intercept[IllegalArgumentException] {
        asFilter("  ")
      }
    }
    it ("Should not become a Filter") {
      intercept[NullPointerException] {
        val s: String = null
        asFilter(s)
      }
    }
  }
}