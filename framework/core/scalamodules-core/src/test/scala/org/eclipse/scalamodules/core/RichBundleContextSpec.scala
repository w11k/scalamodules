/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scalamodules
package core

import org.mockito.{ ArgumentCaptor, Matchers }
import org.mockito.Mockito._
import org.osgi.framework.BundleContext
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

@org.junit.runner.RunWith(classOf[JUnitRunner])
class RichBundleContextSpec extends WordSpec with ShouldMatchers with MockitoSugar {

  "Creating a RichBundleContext" when {

    "the given BundleContext is null" should {
      "throw an IllegalArgumentException" in {
        evaluating { new RichBundleContext(null) } should produce [IllegalArgumentException]
      }
    }
  }

  "Calling RichBundleContext.createService" when {

    "the given service object is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).createService(null)
        } should produce [IllegalArgumentException]
      }
    }

    "the given service properties are null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).createService(new TestClass1, null)
        } should produce [IllegalArgumentException]
      }
    }

    "the given first service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).createService(new TestClass1, interface1 = null)
        } should produce [IllegalArgumentException]
      }
    }

    "the given second service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).createService(new TestClass1, interface2 = null)
        } should produce [IllegalArgumentException]
      }
    }

    "the given third service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).createService(new TestClass1, interface3 = null)
        } should produce [IllegalArgumentException]
      }
    }

    "a service object not implementing a service interface is given" should {
      "call BundleContext.registerService with the class of the service object itself as service interfaces and null as properties" in {
        val context: BundleContext = mock[BundleContext]
        val service = new TestClass1
        new RichBundleContext(context).createService(service)
        verify(context).registerService(Array(classOf[TestClass1].getName), service, null)
      }
    }

    "a service object implementing one service interface is given" should {
      "call BundleContext.registerService with the interface implemented by the service object as service interfaces and null as properties" in {
        val context = mock[BundleContext]
        val service = new TestClass2
        new RichBundleContext(context).createService(service)
        verify(context).registerService(Array(classOf[TestInterface2].getName), service, null)
      }
    }

    "a service object implementing two service interfaces is given" should {
      "call BundleContext.registerService with both interfaces implemented by the service object as service interfaces and null as properties" in {
        val context = mock[BundleContext]
        val service = new TestClass3
        new RichBundleContext(context).createService(service)
        verify(context).registerService(Array(classOf[TestInterface2].getName, classOf[TestInterface3].getName), service, null)
      }
    }

    "a service object implementing two service interfaces is given and one service interface is explicitly given" should {
      "call BundleContext.registerService with the explicitly give interface as service interfaces and null as properties" in {
        val context = mock[BundleContext]
        val service = new TestClass3
        new RichBundleContext(context).createService(service, interface1 = Some(classOf[TestInterface2]))
        verify(context).registerService(Array(classOf[TestInterface2].getName), service, null)
      }
    }

    "a service object implementing two service interfaces is given and both service interfaces are explicitly given" should {
      "call BundleContext.registerService with both explicitly give interfaces as service interfaces and null as properties" in {
        val context = mock[BundleContext]
        val service = new TestClass3
        new RichBundleContext(context).createService(service, interface1 = Some(classOf[TestInterface2]), interface2 = Some(classOf[TestInterface3]))
        verify(context).registerService(Array(classOf[TestInterface2].getName, classOf[TestInterface3].getName), service, null)
      }
    }

    "service properties are given" should {
      "call BundleContext.registerService with the given service properties" in {
        val context: BundleContext = mock[BundleContext]
        val propertiesCaptor = ArgumentCaptor.forClass(classOf[java.util.Dictionary[String, Any]])
        new RichBundleContext(context).createService(new TestClass1, Map("a" -> "b"))
        verify(context).registerService(Matchers.any.asInstanceOf[Array[String]], Matchers.any, propertiesCaptor.capture)
        propertiesCaptor.getValue.size should be (1)
        propertiesCaptor.getValue.get("a") should be ("b")
      }
    }
  }

  "Calling RichBundleContext.findService" when {

    "the given service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).findService(null.asInstanceOf[Class[TestInterface1]])
        } should produce [IllegalArgumentException]
      }
    }

    "the given service interface is not-null" should {
      "return a not-null ServiceFinder with the correct interface" in {
        val serviceFinder = new RichBundleContext(mock[BundleContext]).findService(classOf[TestInterface1])
        serviceFinder should not be (null)
      }
    }
  }

  "Calling RichBundleContext.findServices" when {

    "the given service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).findServices(null.asInstanceOf[Class[TestInterface1]])
        } should produce [IllegalArgumentException]
      }
    }

    "the given service interface is not-null" should {
      "return a not-null ServicesFinder with the correct interface" in {
        val servicesFinder = new RichBundleContext(mock[BundleContext]).findServices(classOf[TestInterface1])
        servicesFinder should not be (null)
      }
    }
  }

  "Calling RichBundleContext.watchService" when {

    "the given service interface is null" should {
      "throw an IllegalArgumentException" in {
        evaluating {
          new RichBundleContext(mock[BundleContext]).watchServices(null.asInstanceOf[Class[TestInterface1]])
        } should produce [IllegalArgumentException]
      }
    }

    "the given service interface is not-null" should {
      "return a not-null ServiceFinder with the correct interface" in {
        val servicesWatcher = new RichBundleContext(mock[BundleContext]).watchServices(classOf[TestInterface1])
        servicesWatcher should not be (null)
      }
    }
  }
}
