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

import org.easymock.EasyMock
import org.osgi.framework.{BundleContext, ServiceReference}
import scala.collection.Map
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import Filter.set

object GetSpec extends Spec with ShouldMatchers {

  val mockCtx = EasyMock createNiceMock classOf[BundleContext]

  describe("The base class Get") {

    it("should throw an IAE when constructed with a null BundleContext") {
      intercept[IllegalArgumentException] {
        new MockGet(null, classOf[String])
      }
    }

    it("should throw an IAE when constructed with a null service interface") {
      intercept[IllegalArgumentException] {
        new MockGet(mockCtx, null)
      }
    }
  }

  describe("Get.andApply()") {

    val get = new MockGet(mockCtx, classOf[String])

    it("should throw an IAE when called with a null function") {
      intercept[IllegalArgumentException] {
        get & null.asInstanceOf[(String) => String]
      }
    }

    it("should throw an IAE when called with another null function") {
      intercept[IllegalArgumentException] {
        get & null.asInstanceOf[(String, Map[String, Any]) => String]
      }
    }
  }
}

object GetOneSpec extends Spec with ShouldMatchers {

  import GetSpec.mockCtx

  describe("GetOne.andApply(s: String => s)") {

    val getOne = new GetOne(mockCtx, classOf[String])

    it("should return None when no service is registered") {
      EasyMock reset mockCtx
      val result = getOne & { s: String => s }
      result should equal (None)
    }

    it("""should return Some(ScalaModules") when the String "x" is registered as service""") {
      val mockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock reset mockCtx
      EasyMock expect (mockCtx getServiceReference classOf[String].getName) andReturn mockRef
      EasyMock expect (mockCtx getService mockRef) andReturn "ScalaModules"
      EasyMock replay mockCtx

      val result = getOne & { s: String => s }
      result should equal (Some("ScalaModules"))
    }

    it("""should return Some("ScalaModules") when the String "Scala" is registered as service with property ("p" => "Modules")""") {
      val mockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock expect (mockRef.getPropertyKeys) andReturn ArrayHelper.create("p")
      EasyMock expect (mockRef getProperty "p") andReturn "Modules"
      EasyMock replay mockRef
      EasyMock reset mockCtx
      EasyMock expect (mockCtx getServiceReference classOf[String].getName) andReturn mockRef
      EasyMock expect (mockCtx getService mockRef) andReturn "Scala"
      EasyMock replay mockCtx

      val result = getOne & {
        (s: String, props: Map[String, Any]) => s + (props get "p" getOrElse "")
      }
      result should equal (Some("ScalaModules"))
    }
  }
}

object GetManySpec extends Spec with ShouldMatchers {

  import GetSpec.mockCtx

  describe("The class GetMany") {

    it("should throw an IAE when constructed with a null filter") {
      intercept[IllegalArgumentException] {
        new GetMany(mockCtx, classOf[String], null)
      }
    }
  }

  describe("GetMany.withFilter(String)") {

    val getMany = new GetMany(mockCtx, classOf[String], None)

    it("should return a new GetMany when called with a not-null filter") {
      val newGetMany =  getMany % set("p")
      newGetMany should not be null
    }

    it("should return a new GetMany when called with a null filter") {
      val newGetMany =  getMany % null
      newGetMany should not be null
    }
  }

  describe("GetMany.andApply(s: String => s)") {

    val getMany = new GetMany(mockCtx, classOf[String], None)

    it("should return Nil when no service is registered") {
      EasyMock reset mockCtx
      val result = getMany & { s: String => s }
      result should equal (Nil)
    }

    it("""should return List("ScalaModules") when the String "Scala" is registered as service with property ("p" => "Modules")""") {
      val mockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock expect (mockRef.getPropertyKeys) andReturn ArrayHelper.create("p")
      EasyMock expect (mockRef getProperty "p") andReturn "Modules"
      EasyMock replay mockRef
      EasyMock reset mockCtx
      EasyMock expect (mockCtx.getServiceReferences(classOf[String].getName, null)) andReturn ArrayHelper.create(mockRef)
      EasyMock expect (mockCtx getService mockRef) andReturn "Scala"
      EasyMock replay mockCtx

      val result = getMany & {
        (s: String, props: Map[String, Any]) => s + (props get "p" getOrElse "")
      }
      result should equal ("ScalaModules" :: Nil)
    }

    it("""should return List containing "ScalaModules" and "BindForge" when the Strings "ScalaModules" and "BindForge" are registered as services""") {
      val aMockRef = EasyMock createNiceMock classOf[ServiceReference]
      val bMockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock reset mockCtx
      EasyMock expect (mockCtx.getServiceReferences(classOf[String].getName, null)) andReturn ArrayHelper.create(aMockRef, bMockRef)
      EasyMock expect (mockCtx getService aMockRef) andReturn "ScalaModules"
      EasyMock expect (mockCtx getService bMockRef) andReturn "BindForge"
      EasyMock replay mockCtx

      val result = getMany & { s: String => s }
      result should have size 2
      result should contain ("ScalaModules")
      result should contain ("BindForge")
    }


    it("""should return List containing "ScalaModules" when the Strings "ScalaModules" and "BindForge" are registered as services, x with property ("p" => "modules") and a filter is applied looking for "p" """) {
      val aMockRef = EasyMock createNiceMock classOf[ServiceReference]
      val bMockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock reset mockCtx
      EasyMock expect (mockCtx.getServiceReferences(classOf[String].getName, "(p=*)")) andReturn ArrayHelper.create(aMockRef)
      EasyMock expect (mockCtx getService aMockRef) andReturn "ScalaModules"
      EasyMock expect (mockCtx getService bMockRef) andReturn "BindForge"
      EasyMock replay mockCtx

      val result = getMany % "(p=*)" & { s: String => s }
      result should equal ("ScalaModules" :: Nil)
    }
  }
}

private class MockGet[I](ctx: BundleContext, srvIntf: Class[I])
  extends Get[I](ctx, srvIntf) {

  override private[core] type Result[T] = Option[T]

  override private[core] def work[T](f: ServiceReference => Option[T]): Result[T] = None
}
