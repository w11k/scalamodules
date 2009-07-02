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
import org.scalatest.matchers.ShouldMatchers._

object GetSpec extends Spec {

  val mockCtx = EasyMock createNiceMock classOf[BundleContext]

  describe("The base class Get") {

    it("should throw an IAE when constructed with a null BundleContext") {
      intercept[IllegalArgumentException] { new MockGet(null, classOf[String]) }
    }

    it("should throw an IAE when constructed with a null service interface") {
      intercept[IllegalArgumentException] { new MockGet(mockCtx, null) }
    }
  }
}

object GetOneSpec extends Spec {

  import GetSpec.mockCtx

  describe("getOne.andApply(s: String => s)") {

    val getOne = new GetOne(mockCtx, classOf[String])

    it("should return None when no service is registered") {
      EasyMock reset mockCtx
      val result = getOne andApply { s: String => s }
      result should equal (None)
    }

    it("""should return Some("x") when the String "x" is registered as a service""") {
      val mockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock reset mockCtx
      EasyMock expect (mockCtx getServiceReference classOf[String].getName) andReturn mockRef
      EasyMock expect (mockCtx getService mockRef) andReturn "x"
      EasyMock replay mockCtx
      
      val result = getOne andApply { s: String => s }
      result should equal (Some("x"))
    }

    it("""should return Some("scalamodules") when the String "scala" is registered as a service with property ("p" => "modules")""") {
      val mockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock expect (mockRef getProperty("p")) andReturn "modules"
      EasyMock replay mockRef
      EasyMock reset mockCtx
      EasyMock expect (mockCtx getServiceReference classOf[String].getName) andReturn mockRef
      EasyMock expect (mockCtx getService mockRef) andReturn "scala"
      EasyMock replay mockCtx
      
      val result = getOne andApply { (s: String, props: Map[String, Any]) => s + props("p") }
      result should equal (Some("scalamodules"))
    }
  }
}

object GetManySpec extends Spec {

  import GetSpec.mockCtx

  describe("getMany.andApply(s: String => s)") {

    val getMany = new GetMany(mockCtx, classOf[String], null)

    it("should return Nil when no service is registered") {
      EasyMock reset mockCtx
      val result = getMany andApply { s: String => s }
      result should equal (Nil)
    }

    it("""should return List("scalamodules") when the String "scala" is registered as a service with property ("p" => "modules")""") {
      val mockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock expect (mockRef getProperty("p")) andReturn "modules"
      EasyMock replay mockRef
      EasyMock reset mockCtx
      EasyMock expect (mockCtx.getServiceReferences(classOf[String].getName, null)) andReturn ArrayHelper.create(mockRef)
      EasyMock expect (mockCtx getService mockRef) andReturn "scala"
      EasyMock replay mockCtx
 
      val result = getMany andApply {
        (s: String, props: Map[String, Any]) => s + (props get "p" getOrElse "")
      }
      result should equal (List("scalamodules"))
    }

    it("""should return List containing "x" and "y" when the Strings "x" and "y" are registered as services""") {
      val aMockRef = EasyMock createNiceMock classOf[ServiceReference]
      val bMockRef = EasyMock createNiceMock classOf[ServiceReference]
      EasyMock reset mockCtx
      EasyMock expect (mockCtx.getServiceReferences(classOf[String].getName, null)) andReturn ArrayHelper.create(aMockRef, bMockRef)
      EasyMock expect (mockCtx getService aMockRef) andReturn "x"
      EasyMock expect (mockCtx getService bMockRef) andReturn "y"
      EasyMock replay mockCtx
      
      val result = getMany andApply { s: String => s }
      result should have size 2
      result should contain ("x")
      result should contain ("y")
    }
    
    // TODO Filter
  }
}

private class MockGet[I](ctx: BundleContext, srvIntf: Class[I]) 
  extends Get[I](ctx, srvIntf) {

  override private[core] type Result[T] = Option[T]

  override private[core] def work[T](f: ServiceReference => Option[T]): Result[T] = None
}
