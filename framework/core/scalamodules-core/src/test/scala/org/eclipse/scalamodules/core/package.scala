/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Heiko Seeberger   - initial API and implementation
 *   Roman Roelofsen   - initial API and implementation
 *   Kjetil Valstadsve - initial API and implementation
 */
package org.eclipse.scalamodules
package core

import java.util.Dictionary
import org.mockito.Mockito._
import scala.collection.Map
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import scala.collection.immutable.{ Map => IMap }
import org.osgi.framework.{ BundleContext, ServiceReference }

@org.junit.runner.RunWith(classOf[JUnitRunner])
class coreSpec extends WordSpec with ShouldMatchers with MockitoSugar {

  "A BundleContext" should {
    "be converted to a RichBundleContext implicitly" in {
      val context = mock[BundleContext]
      val richBundleContext: RichBundleContext = context
    }
  }

  "A ServiceReference" should {
    "be converted to a RichServiceReference implicitly" in {
      val serviceReference = mock[ServiceReference]
      val richServiceReference: RichServiceReference = serviceReference
    }
  }

  "A Pair" when {

    "null" should {
      "be converted to a null Map implicitly" in {
        val tuple2: (String, String) = null
        val map: Map[String, String] = tuple2
        map should be (null)
      }
    }

    "not-null" should {
      "be converted to Some implicitly" in {
        val tuple2 = "Scala" -> "Modules"
        val map: Map[String, String] = tuple2
        map should have size (1)
        map should contain key ("Scala")
        map should contain value ("Modules")
      }
    }
  }

  "Calling interface" when {
    "the type is given explicitly" should {
      "return the correct type" in {
        interface[String] should be (Some(classOf[String]))
      }
    }
  }

  "Calling withInterface" when {
    "the type is given explicitly" should {
      "return the correct type" in {
        withInterface[String] should be (classOf[String])
      }
    }
  }

  "Calling scalaMapToJavaDictionary" when {
    val emptyScalaMap = IMap[Any, Any]()
    val notEmptyScalaMap = IMap("a" -> "1")

    "the given Scala Map is null" should {
      "return null" in {
        val javaDictionary: Dictionary[Any, Any] = scalaMapToJavaDictionary(null)
        javaDictionary should be (null)
      }
    }

    "the given Scala map is not-null and empty" should {
      "return a not-null and empty Java Dictionary" in {
        val javaDictionary: Dictionary[Any, Any] = scalaMapToJavaDictionary(emptyScalaMap)
        javaDictionary should not be (null)
        javaDictionary.size should be (0)
        javaDictionary.isEmpty should be (true)
        javaDictionary.keys.hasMoreElements should be (false)
        javaDictionary.elements.hasMoreElements should be (false)
        javaDictionary get "" should equal (null)
        evaluating { javaDictionary.put("", "") } should produce [UnsupportedOperationException]
        evaluating { javaDictionary remove "" } should produce [UnsupportedOperationException]
      }
    }

    "the given Scala map is not-null and not-empty" should {
      "return a not-null and not-empty Java Dictionary" in {
        val javaDictionary = scalaMapToJavaDictionary(notEmptyScalaMap)
        javaDictionary should not be (null)
        javaDictionary get "a" should not equal (null)
      }
    }
  }

  "Calling invokeService" when {

    "the given BundleContext is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          invokeService(mock[ServiceReference], { s: String => "" })(null)
        } should produce [IllegalArgumentException]
      }
    }

    "the given ServiceReference is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          invokeService(null, { s: String => "" })(mock[BundleContext])
        } should produce [IllegalArgumentException]
      }
    }

    "the given function is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          invokeService(mock[ServiceReference], null)(mock[BundleContext])
        } should produce [IllegalArgumentException]
      }
    }

    "there is no service available" should {
      "result in appropriate calls to BundleContext and return None" in {
        val context = mock[BundleContext]
        val serviceReference = mock[ServiceReference]
        when(context.getService(serviceReference)).thenReturn(null, null)  // TODO Can we get rid of this double arg?
        invokeService(serviceReference, { s: String => "" })(context) should be (None)
      }
    }

    "there is a service available" should {
      "result in appropriate calls to BundleContext and return None" in {
        val context = mock[BundleContext]
        val serviceReference = mock[ServiceReference]
        when(context.getService(serviceReference)).thenReturn("Scala", "Scala")  // TODO Can we get rid of this double arg?
        invokeService(serviceReference, { s: String => s + "Modules" })(context) should be (Some("ScalaModules"))
        verify(context).ungetService(serviceReference)
      }
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
