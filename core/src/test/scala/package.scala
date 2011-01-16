/*
 * Copyright 2009-2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiglewilczek.scalamodules

import java.util.Dictionary
import org.osgi.framework.{ BundleContext, ServiceReference }
import org.specs.Specification
import org.specs.mock.Mockito
import scala.collection.Map
import scala.collection.immutable.{ Map => IMap }

class scalamodulesSpec extends Specification with Mockito {

  "Calling a toRichBundleContext" should {
    "throw an IllegalArgumentException given a null BundleContext" in {
      toRichBundleContext(null) must throwA[IllegalArgumentException]
    }
  }

  "A BundleContext" should {
    "be converted to a RichBundleContext implicitly" in {
      val context = mock[BundleContext]
      val richBundleContext: RichBundleContext = context
      richBundleContext mustNotBe null // A Matcher must be executed, else the example is regarded pending!
    }
  }

  "Calling a toRichServiceReference" should {
    "throw an IllegalArgumentException given a null ServiceReference" in {
      toRichServiceReference(null) must throwA[IllegalArgumentException]
    }
  }

  "A ServiceReference" should {
    "be converted to a RichServiceReference implicitly" in {
      val serviceReference = mock[ServiceReference]
      val richServiceReference: RichServiceReference = serviceReference
      richServiceReference mustNotBe null // A Matcher must be executed, else the example is regarded pending!
    }
  }

  "A Pair" should {
    "be converted to a null Map implicitly when null" in {
      val tuple2: (String, String) = null
      val map: Map[String, String] = tuple2
      map mustBe null
    }
    "be converted to Some implicitly when not-null" in {
      val tuple2 = "Scala" -> "Modules"
      val map: Map[String, String] = tuple2
      map must haveSize(1)
      map must havePair("Scala" -> "Modules")
    }
  }

  "Calling a stringToSimpleOpBuilder" should {
    "throw an IllegalArgumentException given a null String" in {
      toSimpleOpBuilder(null) must throwA[IllegalArgumentException]
    }
  }

  "A String" should {
    "be converted to a SimpleOpBuilder implicitly" in {
      val simpleOpBuilder: SimpleOpBuilder = ""
      simpleOpBuilder mustNotBe null // A Matcher must be executed, else the example is regarded pending!
    }
  }

  "Calling a stringToPresentBuilder" should {
    "throw an IllegalArgumentException given a null String" in {
      toPresentBuilder(null) must throwA[IllegalArgumentException]
    }
  }

  "A String" should {
    "be converted to a SimpleOpBuilder implicitly" in {
      val presentBuilder: PresentBuilder = ""
      presentBuilder mustNotBe null // A Matcher must be executed, else the example is regarded pending!
    }
  }

  "Calling interface with a given type" should {
    "return Some containing a class of that type" in {
      interface[String] mustEqual Some(classOf[String])
    }
  }

  "Calling withInterface with a given type" should {
    "return a class of that type" in {
      withInterface[String] mustEqual classOf[String]
    }
  }

  "Calling scalaMapToJavaDictionary" should {
    val emptyScalaMap = IMap[Any, Any]()
    "return null given null" in {
      val javaDictionary: Dictionary[Any, Any] = scalaMapToJavaDictionary(null)
      javaDictionary mustBe null
    }
    "return an empty and immutable Java Dictionary given an empty Scala Map" in {
      val javaDictionary: Dictionary[Any, Any] = scalaMapToJavaDictionary(emptyScalaMap)
      javaDictionary mustNotBe null
      javaDictionary.size mustEqual 0
      javaDictionary.isEmpty mustBe true
      javaDictionary.keys.hasMoreElements mustBe false
      javaDictionary.elements.hasMoreElements mustBe false
      javaDictionary get "" mustBe null
      javaDictionary.put("", "") must throwA[UnsupportedOperationException]
      javaDictionary remove "" must throwA[UnsupportedOperationException]
    }
    "return an appropriate and immutable Java Dictionary given a non-empty Scala Map" in {
      val notEmptyScalaMap = IMap("a" -> "1")
      val javaDictionary = scalaMapToJavaDictionary(notEmptyScalaMap)
      javaDictionary mustNotBe null
      javaDictionary.size mustEqual 1
      javaDictionary.isEmpty mustBe false
      javaDictionary.keys.hasMoreElements mustBe true
      javaDictionary.elements.hasMoreElements mustBe true
      javaDictionary get "a" mustBe "1"
      javaDictionary.put("", "") must throwA[UnsupportedOperationException]
      javaDictionary remove "" must throwA[UnsupportedOperationException]
    }
  }

  "Calling invokeService" should {
    val context = mock[BundleContext]
    val serviceReference = mock[ServiceReference]
    "result in appropriate calls to BundleContext and return None" in {
      context.getService(serviceReference) returns null
      invokeService(serviceReference, { s: String => "" }, context) mustBe None
      there was one(context).ungetService(serviceReference)
    }
    "result in appropriate calls to BundleContext and return Some" in {
      context.getService(serviceReference) returns "Scala"
      invokeService(serviceReference, { s: String => s + "Modules" }, context) mustEqual Some("ScalaModules")
      there was one(context).ungetService(serviceReference)
    }
  }
}

class TestClass1
class TestClass2 extends TestInterface2
class TestClass3 extends TestInterface2 with TestInterface3
class TestClass4 extends TestInterface4

trait TestInterface1 {
  def name = getClass.getName
}
trait TestInterface2
trait TestInterface3
trait TestInterface4 extends TestInterface4a
trait TestInterface4a extends TestInterface4b
trait TestInterface4b extends TestInterface4c
trait TestInterface4c