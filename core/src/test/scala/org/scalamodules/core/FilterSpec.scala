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
import Filter.{not => notf} // To avoid collision with Spec stuff

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

  describe("Literal filters") {
    it("should return its input") {
      literal("(foo=5)").asString should equal("(foo=5)")
    }
    it("should return its input verbatim") {
      literal("zip-a-dee").asString should equal("zip-a-dee")
    }
    it("should combine with other filters") {
      (literal("zip-a-dee") && ("foo" -> 5)).asString should equal("(&zip-a-dee(foo=5))")
    }
    it("should combine with other VALID filters") {
      (literal("(bar=vips)") && ("foo" -> 5)).asString should equal("(&(bar=vips)(foo=5))")
    }
    it("should survive intact, even if collapsible - so as to be recognizable - principle of least surprise") {
      (literal("(&(foo=1)(bar=2))") && ("foo" -> 5)).asString should equal("(&(&(foo=1)(bar=2))(foo=5))")
    }
  }

  describe("Textual representation") {
    it("should a simple equals filter") {
      set("foo", "wobble").asString should equal("(foo=wobble)")
    }
    it("should be an exists filter") {
      exists("foo").asString should equal("(foo=*)")
    }
    it("should be an exists filter, from empty set") {
      set("foo").asString should equal("(foo=*)")
    }
    it("should be an & filter") {
      and(set("foo"), notf(set("bar"))).asString should equal("(&(foo=*)(!(bar=*)))")
    }
    it("should be an | filter") {
      or(set("foo", "bar"), set("zot", 4)).asString should equal("(|(foo=bar)(zot=4))")
    }
    it("should be a ! filter") {
      notf(set("foo", "bar")).asString should equal("(!(foo=bar))")
    }
    it("should be a negated property filter") {
      notSet("foo", "bar").asString should equal("(!(foo=bar))")
    }
    it("should be a truth filter") {
      isTrue("foo").asString should equal("(foo=true)")
    }
    it("should be an object classes filter") {
      objectClass(classOf[String], classOf[Integer]).asString should equal("(objectClass=[java.lang.String,java.lang.Integer])")
    }
    it("should be an object classes present filter") {
      objectClass().asString should equal("(objectClass=*)")
    }
    it("should be a property present filter") {
      set("foo").asString should equal("(foo=*)")
    }
    it("should be a property present filter, from single-null array") {
      set("foo", List[String](null)).asString should equal("(foo=*)")
    }
    it("should be a property filter, from single-value array") {
      set("foo", List[String]("6")).asString should equal("(foo=6)")
    }
    it("should be an lt filter") {
      lt("foo", 5).asString should equal("(foo<=5)")
    }
    it("should be a bt filter") {
      bt("foo", 5).asString should equal("(foo>=5)")
    }
    it("should be a nil filter") {
      NilFilter.asString should equal("")
    }
    it("should be an empty filter, from empty string array") {
      set("foo", Array[String]()).asString should equal("(foo=*)")
    }
    it("should be an empty filter, from empty integer array") {
      set("foo", Array[String]()).asString should equal("(foo=*)")
    }
    it("should be an int array filter") {
      set("foo", Array(1, 2, 6)).asString should equal("(foo=[1,2,6])")
    }
    it("should be an int array filter, from varargs") {
      set("foo", 1, 2, 6).asString should equal("(foo=[1,2,6])")
    }
    it("should be a string array filter") {
      set("foo", Array("a", "b", "c")).asString should equal("(foo=[a,b,c])")
    }
    it("should be a string array filter, from varargs") {
      set("foo", "a", "b", "c").asString should equal("(foo=[a,b,c])")
    }
    it("should be a string array filter, from varargs with pruning") {
      set("foo", "a", None, "c").asString should equal("(foo=[a,c])")
    }
    it("should be a string array filter, from varargs with more pruning") {
      set("foo", "a", None, "").asString should equal("(foo=a)")
    }
    it("should be a string array filter, from varargs with full pruning") {
      set("foo", emptyObject, None, "").asString should equal("(foo=*)")
    }
  }

  describe("Invalid attributes") {
    it("should be caught, equals sign for example") {
      intercept[IllegalArgumentException] {
        set("foo=bar", "foo")
      }
    }
    it("should be caught, like parantheses") {
      intercept[IllegalArgumentException] {
        set("foo(bar", "foo")
      }
    }
    it("should be caught, like tildes") {
      intercept[IllegalArgumentException] {
        set("foo~bar", "foo")
      }
    }
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
    it ("should not become a Filter when attribute is empty") {
      intercept[IllegalArgumentException] {
        set(emptyObject, 5)
      }
    }
    it ("should not become a Filter from an empty-string object") {
      intercept[IllegalArgumentException] {
        set(emptyObject)
      }
    }
  }

  describe("Option attributes") {
    it("should work with None") {
      set("foo", None) should equal(set("foo"))
    }
    it("should work with Some object") {
      set("foo", Some(5)) should equal(set("foo", 5))
    }
  }

  describe("Implicits from tuple") {
    it("should become a property filter") {
      asFilter("foo" -> 10) should equal(set("foo", 10))
    }
    it("should become a present filter") {
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

  describe("Composing with null") {
    it("should work with and-null") {
      (set("foo", 5) && null) should equal (set("foo", 5))
    }
    it("should work with or-null") {
      (set("foo", 5) || null) should equal (set("foo", 5))
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
    it("should become an empty Filter, from a tuple with empty right-hand side") {
      asFilter("foo" -> emptyObject) should equal(set("foo"))
    }
    it("should become an empty Filter, from a simple string") {
      asFilter("foo") should equal(set("foo"))
    }
    it("should not become a Filter") {
      intercept[IllegalArgumentException] {
        asFilter("  ")
      }
    }
  }

  describe("Fool-proof argument checking") {
    it("should unpack nested arg") {
      set("foo", List(List("bar"))) should equal(set("foo", "bar"))
    }
    it("should unpack double -list") {
      set("hookcrook", List("in"), null, List("for", "", List(None, "mation"))) should equal(set("hookcrook", "in", "for", "mation"))
    }
    it("should clear away all the fluff") {
      set("foo", List(None), List(List("", "", List()), List(None, None)), None, None, "") should equal(set("foo"))
    }
    it("should clear away all the fluff and become a present filter") {
      set("foo", List(None), List(List("", "", List("*")), List(None, None)), "*", None, None, "") should equal(set("foo"))
    }
    it("should clear away all the fluff and become a present filter, overriding any other data") {
      set("foo", List(None), List(List("", "", List("*")), List(None, None)), "*", None, None, "", "nevermindme") should equal(set("foo"))
    }
    it("should clear away all the fluff and find something") {
      set("foo", List(None), List(List("", "", List()), "6", List(None, None)), None, None, "") should equal(set("foo", 6))
    }
    it("should clear away all the fluff, ignore the 6 and reduce to a present filter") {
      set("foo", List(None), List(List("", "", List()), "6", List(None, None)), None, List("*", None), None, "") should equal(set("foo"))
    }
  }
}
