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

import FilterHelpers._
import java.lang.String
import junit.{Assert, Test}

class FilterTest {

  @Test def andCompound = check("(&(foo=*)(!(bar=*)))", And(Set("foo"), Not(Set("bar"))))

  @Test def orCompound = check("(|(foo=bar)(zot=4))", Or(Set("foo", "bar"), Set("zot", 4)))

  @Test def notCompound = check("(!(foo=bar))", Not(Set("foo", "bar")))

  @Test def notSetAtom = check("(!(foo=bar))", NotSet("foo", "bar"))

  @Test def tupleCompound = check("(foo=5)", ("foo" -> 5))

  @Test def presentFilter = check("(foo=*)", Set("foo"))

  @Test def isFilter = check("(foo=true)", IsTrue("foo"))

  @Test def isTupleFilter = check("(foo=5)", "foo" -> 5)

  @Test def isSetFilter = check("(foo=*)", "foo")

  @Test def ltFilter = check("(foo<=5)", Lt("foo", 5))

  @Test def btFilter = check("(foo>=5)", Bt("foo", 5))

  @Test def nilFilter = check("", NilFilter)

  @Test def nilBuildFilter = check("(foo=5)", Set("foo", 5) && NilFilter)

  @Test def andMe = check("(&(foo=true)(zot=zip))", IsTrue("foo") && Set("zot", "zip"))

  @Test def filterOutNils = check("(&(foo=bar)(zip=5))", And(NilFilter, Set("foo", "bar"), NilFilter, NilFilter, Set("zip", 5), NilFilter))

  @Test def builderSet = check("(foo=*)", "foo" set)

  @Test def builderSetTrue = check("(foo=true)", "foo" isTrue)

  @Test def builderSetToTrue = check("(foo=true)", "foo" === true)

  @Test def builderBt = check("(foo>=5)", "foo" >== 5)

  @Test def builderLt = check("(foo<=6)", "foo" <== 6)

  @Test def builderAppr = check("(foo~=6)", "foo" ~== 6)

  @Test def composeBuilders = check("(&(zit=bar)(foo=*))", ("zit" === "bar") && "foo")

  @Test def composeBuildersWithTuple = check("(&(zit=bar)(foo=*)(zot=zip))", ("zit" === "bar") && "foo" && ("zot" -> "zip"))

  @Test def collapseComposite = check("(&(zot=zip)(foo=bar)(a=b)(yin=yang)(peace=war))",
    And(And(("zot" -> "zip"), ("foo" -> "bar")), ("a" -> "b"), And(("yin" -> "yang"), ("peace" -> "war"))))

  @Test def dontCollapseComposite = check("(|(&(zot=zip)(foo=bar))(a=b)(&(yin=yang)(peace=war)))",
    Or(And(("zot" -> "zip"), ("foo" -> "bar")), ("a" -> "b"), And(("yin" -> "yang"), ("peace" -> "war"))))

  @Test def nullAttr = illegal(() => Set(null, 5))

  private def illegal(fun: () => ServiceFilter): Unit = {
    try {
      Assert.fail("Not legal: " + fun())
    } catch {
      case e: NullPointerException => { return }
    }
  }

  private def check(string: String, filter: ServiceFilter) {
    Assert.assertEquals("Filter generated unexpected string", string, filter.toString)
  }
}