/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.weiglewilczek.scalamodules

import org.mockito.{ ArgumentCaptor, Matchers }
import org.osgi.framework.BundleContext
import org.specs.Specification
import org.specs.mock.Mockito

class RichBundleContextSpec extends Specification with Mockito {

  "Creating a RichBundleContext" should {
    "throw an IllegalArgumentException given a null BundleContext" in {
      new RichBundleContext(null) must throwA[IllegalArgumentException]
    }
  }

  "Calling RichBundleContext.createService" should {
    val context = mock[BundleContext]
    val service = new TestClass1
    "throw an IllegalArgumentException given a null service object" in {
      new RichBundleContext(context).createService(null) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given null service properties" in {
      new RichBundleContext(context).createService(service, null) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null first service interface" in {
      new RichBundleContext(context).createService(service, interface1 = null) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null second service interface" in {
      new RichBundleContext(context).createService(service, interface2 = null) must throwA[IllegalArgumentException]
    }
    "throw an IllegalArgumentException given a null third service interface" in {
      new RichBundleContext(context).createService(service, interface3 = null) must throwA[IllegalArgumentException]
    }
    
    "call BundleContext.registerService with the class of the given service object itself when it does not implement a service interface" in {
      val service = new TestClass1
      new RichBundleContext(context).createService(service)
      there was one(context).registerService(Array(classOf[TestClass1].getName), service, null)
    }
    "call BundleContext.registerService with the one interface implemented by the given service object" in {
      val service = new TestClass2
      new RichBundleContext(context).createService(service)
      there was one(context).registerService(Array(classOf[TestInterface2].getName), service, null)
    }
    "call BundleContext.registerService with the two interfaces implemented by the given service object" in {
      val service = new TestClass3
      new RichBundleContext(context).createService(service)
      there was one(context).registerService(Array(classOf[TestInterface2].getName, classOf[TestInterface3].getName), service, null)
    }
    "call BundleContext.registerService with the explicitly given service interface" in {
      val service = new TestClass3
      new RichBundleContext(context).createService(service, interface1 = Some(classOf[TestInterface2]))
      there was one(context).registerService(Array(classOf[TestInterface2].getName), service, null)
    }
    "call BundleContext.registerService with all the explicitly given service interfaces" in {
      val service = new TestClass3
      new RichBundleContext(context).createService(service, interface1 = Some(classOf[TestInterface2]), interface2 = Some(classOf[TestInterface3]))
      there was one(context).registerService(Array(classOf[TestInterface2].getName, classOf[TestInterface3].getName), service, null)
    }
    "call BundleContext.registerService with the given service properties" in {
      val propertiesCaptor = ArgumentCaptor.forClass(classOf[java.util.Dictionary[String, Any]])
      new RichBundleContext(context).createService(new TestClass1, Map("a" -> "b"))
      there was one(context).registerService(Matchers.any.asInstanceOf[Array[String]], Matchers.any, propertiesCaptor.capture)
      propertiesCaptor.getValue.size mustEqual 1
      propertiesCaptor.getValue get "a" mustEqual "b"
    }
  }

  "Calling RichBundleContext.findService" should {
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null service interface" in {
      new RichBundleContext(context).findService(null.asInstanceOf[Class[TestInterface1]]) must throwA[IllegalArgumentException]
    }
    "return a not-null ServiceFinder with the correct interface" in {
      val serviceFinder = new RichBundleContext(mock[BundleContext]).findService(classOf[TestInterface1])
      serviceFinder mustNotBe null
    }
  }

  "Calling RichBundleContext.findServices" should {
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null service interface" in {
      new RichBundleContext(context).findServices(null.asInstanceOf[Class[TestInterface1]]) must throwA[IllegalArgumentException]
    }
    "return a not-null ServiceFinder with the correct interface" in {
      val servicesFinder = new RichBundleContext(mock[BundleContext]).findServices(classOf[TestInterface1])
      servicesFinder mustNotBe null
    }
  }

  "Calling RichBundleContext.watchServices" should {
    val context = mock[BundleContext]
    "throw an IllegalArgumentException given a null service interface" in {
      new RichBundleContext(context).watchServices(null.asInstanceOf[Class[TestInterface1]]) must throwA[IllegalArgumentException]
    }
    "return a not-null ServiceWatcher with the correct interface" in {
      val servicesWatcher = new RichBundleContext(mock[BundleContext]).watchServices(classOf[TestInterface1])
      servicesWatcher mustNotBe null
    }
  }
}
