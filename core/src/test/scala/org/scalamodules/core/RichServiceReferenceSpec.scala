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
import Util.mapToJavaDictionary

import java.util.Dictionary
import org.easymock.{EasyMock, IArgumentMatcher}
import EasyMock.{aryEq, eq, isNull, same}
import org.osgi.framework.{BundleContext, ServiceReference}
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.Map
import scala.collection.immutable.{Map => IMap}

object RichServiceReferenceSpec extends Spec with ShouldMatchers {

  val mockRef = EasyMock createNiceMock classOf[ServiceReference]

  describe("The function Preamble.toRichServiceReference") {

    it("should implicitly convert a ServiceReference to RichServiceReference") {
      EasyMock reset mockRef
      EasyMock expect (mockRef.getPropertyKeys) andReturn null
      val richRef: RichServiceReference = mockRef
      richRef should not be null
    }
  }

  describe("The class RichServiceReference") {

    it("should throw an IAE when constructed with a null ServiceReference") {
      intercept[IllegalArgumentException] { 
        new RichServiceReference(null)
      }
    }
  }

  describe("RichServiceReference.properties") {

    it("should return empty Map for no service properties") {
      EasyMock reset mockRef
      EasyMock expect (mockRef.getPropertyKeys) andReturn null
      EasyMock replay mockRef
      val richRef = new RichServiceReference(mockRef)
      val props = richRef.properties
      props should not be null
      props should have size 0
    }

    it("should return Map with proper service properties") {
      EasyMock reset mockRef
      EasyMock expect (mockRef.getPropertyKeys) andReturn ArrayHelper.create("scala")
      EasyMock expect (mockRef getProperty "scala") andReturn "modules"
      EasyMock replay mockRef
      val richRef = new RichServiceReference(mockRef)
      val props = richRef.properties
      props should not be null
      props should have size 1
      props.keySet should contain ("scala")
      props get "scala" should equal (Some("modules"))
    }
  }
}
