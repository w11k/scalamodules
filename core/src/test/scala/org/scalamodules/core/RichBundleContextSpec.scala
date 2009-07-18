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

import Preamble._
import internal.Util
import Util.mapToJavaDictionary

import java.util.Dictionary
import org.easymock.{EasyMock, IArgumentMatcher}
import EasyMock.{aryEq, eq, isNull, same}
import org.osgi.framework.{BundleContext, ServiceRegistration}
import org.osgi.util.tracker.ServiceTracker
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

object RichBundleContextSpec extends Spec with ShouldMatchers {

  val mockCtx = EasyMock createNiceMock classOf[BundleContext]

  describe("The function Preamble.toRichBundleContext") {

    it("should implicitly convert a BundleContext to RichBundleContext") {
      val rbc: RichBundleContext = mockCtx
      rbc should not be null
    }
  }

  describe("The class RichBundleContext") {

    it("should throw an IAE when constructed with a null BundleContext") {
      intercept[IllegalArgumentException] { 
        new RichBundleContext(null)
      }
    }
  }

  describe("RichBundleContext.register(RegIndepInfo)") {

    val rbc = new RichBundleContext(mockCtx)

    it("should throw an IAE when called with a null RegIndepInfo") {
      intercept[IllegalArgumentException] { 
        rbc << null.asInstanceOf[RegIndepInfo[Nothing, Nothing]]
      }
    }

    it("should return a not-null ServiceRegistration when called with a RegIndepInfo based on an implicit class") {
      object SrvImpl
      val srv = SrvImpl
      val mockReg = EasyMock createNiceMock classOf[ServiceRegistration]
      EasyMock reset mockCtx
      EasyMock expect 
        (mockCtx registerService 
          (aryEq(Array(srv.getClass.getName)), same(srv), isNull)) andReturn mockReg
      EasyMock replay mockCtx
      val result = mockCtx << new RegIndepInfo(srv)
      EasyMock verify mockCtx
      result should not be null
      result should equal (mockReg)
    }

    it("should return a not-null ServiceRegistration when called with a RegIndepInfo based on implicit interfaces") {
      trait Srv1
      trait Srv2
      object SrvImpl extends Srv1 with Srv2
      val srv = SrvImpl
      val mockReg = EasyMock createNiceMock classOf[ServiceRegistration]
      EasyMock reset mockCtx
      EasyMock expect 
        (mockCtx registerService 
           (aryEq(Array(classOf[Srv1].getName, classOf[Srv2].getName)), same(srv), isNull)) andReturn mockReg
      EasyMock replay mockCtx
      val result = mockCtx << new RegIndepInfo(srv)
      EasyMock verify mockCtx
      result should not be null
      result should equal (mockReg)
    }

    it("should return a not-null ServiceRegistration when called with a RegIndepInfo based on an explicit interface") {
      trait Srv1
      trait Srv2
      object SrvImpl extends Srv1 with Srv2
      val srv = SrvImpl
      val mockReg = EasyMock createNiceMock classOf[ServiceRegistration]
      EasyMock reset mockCtx
      EasyMock expect 
        (mockCtx registerService 
           (aryEq(Array(classOf[Srv1].getName)), same(srv), isNull)) andReturn mockReg
      EasyMock replay mockCtx
      val result = mockCtx << new RegIndepInfo(srv, Some(classOf[Srv1]))
      EasyMock verify mockCtx
      result should not be null
      result should equal (mockReg)
    }

    it("should return a not-null ServiceRegistration when called with a RegIndepInfo with service properties") {
      trait Srv1
      object SrvImpl extends Srv1
      val srv = SrvImpl
      val props = IMap("scala" -> "modules")
      val mockReg = EasyMock createNiceMock classOf[ServiceRegistration]
      EasyMock reset mockCtx
      EasyMock expect 
        (mockCtx registerService 
           (aryEq(Array(classOf[Srv1].getName)), same(srv), DictionaryMatcher.eqDict(Util mapToJavaDictionary props))) andReturn mockReg
      EasyMock replay mockCtx
      val result = mockCtx << new RegIndepInfo(srv, None, Some(props))
      EasyMock verify mockCtx
      result should not be null
      result should equal (mockReg)
    }
  }

  describe("RichBundleContext.register(RegDepInfo)") {

    val rbc = new RichBundleContext(mockCtx)

    it("should throw an IAE when called with a null RegDepInfo") {
      intercept[IllegalArgumentException] { 
        rbc << (null.asInstanceOf[RegDepInfo[Nothing, Nothing, AnyRef]])
      }
    }

    it("should return a not-null ServiceTracker when called with a RegDepInfo") {
      val info = new RegDepInfo((s: String) => "")
      val result = rbc << info
      result should not be null
    }
  }

  describe("RichBundleContext.getOne(Class)") {

    val rbc = new RichBundleContext(mockCtx)

    it("should throw an IAE when called with a null service interface") {
      intercept[IllegalArgumentException] { 
        rbc ?>> null
      }
    }

    it("should return a not-null GetOne when called with a service interface") {
      val result = rbc ?>> classOf[String]
      result should not be null
    }
  }

  describe("RichBundleContext.getMany(Class)") {

    val rbc = new RichBundleContext(mockCtx)

    it("should throw an IAE when called with a null service interface") {
      intercept[IllegalArgumentException] { 
        rbc *>> null
      }
    }

    it("should return a not-null GetMany when called with a service interface") {
      val result = rbc *>> classOf[String]
      result should not be null
    }
  }

  describe("RichBundleContext.track(Class)") {

    val rbc = new RichBundleContext(mockCtx)

    it("should throw an IAE when called with a null service interface") {
      intercept[IllegalArgumentException] {
        rbc >> null
      }
    }

    it("should return a not-null Track when called with a service interface") {
      val result = rbc >> classOf[String]
      result should not be null
    }
  }
}

// =============================================================================
// Helpers
// =============================================================================

object DictionaryMatcher {

  def eqDict[T <: Dictionary[String, String]](in: T): T = { 
    EasyMock reportMatcher new DictionaryMatcher(in)
    null.asInstanceOf[T]
  }
}

class DictionaryMatcher(expected: Dictionary[String, String]) extends IArgumentMatcher {

  val expectedDict = Util dictionaryToMap expected

  override def matches(actual: AnyRef) = {
    actual match {
      case actualDict: Dictionary[_, _] => (Util dictionaryToMap actualDict) equals expectedDict
      case _                            => false
    }
  }

  override def appendTo(buffer: StringBuffer) {
    buffer append "eqDict("
    buffer append expectedDict
    buffer append ")"
  }
}
