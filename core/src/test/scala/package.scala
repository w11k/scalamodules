/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules

import java.util.Dictionary
import org.osgi.framework.{ BundleContext, ServiceReference }
import org.specs.Specification
import org.specs.mock.Mockito
import scala.collection.Map
import scala.collection.immutable.{ Map => IMap }

class scalamodulesSpec extends Specification with Mockito {

  "A BundleContext" should {
    "be converted to a RichBundleContext implicitly" in {
      val context = mock[BundleContext]
      val richBundleContext: RichBundleContext = context
      richBundleContext mustNotBe null // A Matcher must be executed, else the example is regarded pending!
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
    "throw an IllegalArgumentException given a null BundleContext" in {
      invokeService(serviceReference, { s: String => "" })(null) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null ServiceReference" in {
      invokeService(null, { s: String => "" })(context) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null ServiceReference" in {
      invokeService(serviceReference, null)(context) must throwA[IllegalArgumentException]
    }
    "result in appropriate calls to BundleContext and return None" in {
      context.getService(serviceReference) returns null
      invokeService(serviceReference, { s: String => "" })(context) mustBe None
      there was one(context).ungetService(serviceReference)
    }
    "result in appropriate calls to BundleContext and return Some" in {
      context.getService(serviceReference) returns "Scala"
      invokeService(serviceReference, { s: String => s + "Modules" })(context) mustEqual Some("ScalaModules")
      there was one(context).ungetService(serviceReference)
    }
  }
}

class TestClass1
class TestClass2 extends TestInterface2
class TestClass3 extends TestInterface2 with TestInterface3

trait TestInterface1 {
  def name = getClass.getName
}
trait TestInterface2
trait TestInterface3
