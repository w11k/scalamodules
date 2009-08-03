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
package org.scalamodules.core

import scalatest.matchers.ShouldMatchers
import scalatest.Spec

import Filter._
import Filter.{not => notf}

object FilterSpec extends Spec with ShouldMatchers {

  object emptyObject { override def toString = "   " }

  object namedObject { override def toString = "foo" }

  private def asFilter(filter: Filter) = filter

  describe("Null checks") {
    it("should not allow null filter attributes") {
      intercept[NullPointerException] {
        set(null, 10)
      }
    }
  }

  describe("Textual representations") {
    it("should become a legal & filter") {
      and(set("foo"), notf(set("bar"))).asString should equal("(&(foo=*)(!(bar=*)))")
    }
    it("should become a legal | filter") {
      or(set("foo", "bar"), set("zot", 4)).asString should equal("(|(foo=bar)(zot=4))")
    }
    it("should become a legal ! filter") {
      notf(set("foo", "bar")).asString should equal("(!(foo=bar))")
    }
    it("should become a legal ! atom filter") {
      notSet("foo", "bar").asString should equal("(!(foo=bar))")
    }
    it("should become truth filter") {
      isTrue("foo").asString should equal("(foo=true)")
    }
    it("should become set filter") {
      set("foo").asString should equal("(foo=*)")
    }
    it("should become lt filter") {
      lt("foo", 5).asString should equal("(foo<=5)")
    }
    it("should become bt filter") {
      bt("foo", 5).asString should equal("(foo>=5)")
    }
    it("should become nil filter") {
      NilFilter.asString should equal("")
    }
  }

  describe("Invalid attributes") {
    it("should catch =") { intercept[IllegalArgumentException] { set("foo=bar", "foo") } }
  }

  describe("Object attributes") {
    it("should work with a non-string attribute (set)") {
      set(namedObject, 5) should equal(set("foo", 5))
    }
    it("should work with a non-string attribute (bt)") {
      bt(namedObject, 3) should equal(bt("foo", 3))
    }
    it("should work with a non-string attribute (lt)") {
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
    it("should become set filter") {
      asFilter("foo" -> 10) should equal(set("foo", 10))
    }
    it("should become set filter, part II") {
      asFilter("foo" -> null) should equal(set("foo"))
    }
    it("should work!") {
      ("zit" === "bar") && "foo" && ("zot" -> "zip") should equal(and(set("zit", "bar"),set("foo"),set("zot", "zip")))
    }
  }

  describe("Implicit from type") {
    it("should become an objectclass filter") {
      asFilter(classOf[String]) should equal (set("objectClass", "java.lang.String"))
    }
  }

  describe("Collapsing of composition") {
    it("should collapse to a single level") {
      and(and(("zot" -> "zip"), ("foo" -> "bar")),
        ("a" -> "b"),
        and(("yin" -> "yang"), ("peace" -> "war"))) should equal(and
                (set("zot", "zip"), set("foo", "bar"), set("a", "b"), set("yin", "yang"), set("peace", "war")))
    }
    it("should not collapse") {
      or(and(("zot" -> "zip"), ("foo" -> "bar")), ("a" -> "b"),
        and(("yin" -> "yang"), ("peace" -> "war"))).asString should equal("(|(&(zot=zip)(foo=bar))(a=b)(&(yin=yang)(peace=war)))")
    }
  }

  describe("Compose from methods") {
    it("should become an and") {
      isTrue("foo") && set("zot", "zip") should equal(and(isTrue("foo"), set("zot", "zip")))
    }
    it("should become an and, part II") {
      isTrue("foo") and set("zot", "zip") should equal(and(isTrue("foo"), set("zot", "zip")))
    }
  }

  describe("NilFilter treatment") {
    it("should remove NilFilters") {
      and(NilFilter,
        set("foo", "bar"), NilFilter, NilFilter,
        set("zip", 5), NilFilter) should equal (and(set("foo", "bar"), set("zip", 5)))
    }
    it("should become a simple filter") {
      set("foo", 5) && NilFilter should equal(set("foo", 5))
    }
  }

  describe("Implicit builders") {
    it("should become a PropertyFilterBuilder and invoke ===") {
      ("foo" === 5) should equal(set("foo", 5))
    }
    it("should become a PropertyFilterBuilder and invoke <==") {
      ("foo" <== 4) should equal(lt("foo", 4))
    }
    it("should become a PropertyFilterBuilder and invoke >==") {
      ("foo" >== 3) should equal(bt("foo", 3))
    }
    it("should become a PropertyFilterBuilder and invoke ~==") {
      ("foo" ~== 5) should equal(approx("foo", 5))
    }
    it("should become a PropertyFilterBuilder and invoke isTrue") {
      ("foo" === true) should equal(isTrue("foo"))
    }
    it("should become a PropertyFilterBuilder and invoke isTrue, part II") {
      ("foo" isTrue) should equal(isTrue("foo"))
    }
    it("should become a PropertyFilterBuilder and invoke set") {
      ("foo" set) should equal(set("foo"))
    }
    it("should become an empty Filter") {
      asFilter("foo" -> emptyObject) should equal(set("foo"))
    }
    it("should not become a Filter") {
      intercept[IllegalArgumentException] {
        asFilter("  ")
      }
    }
  }
}