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

import org.mockito.{ ArgumentCaptor, Matchers }
import org.osgi.framework.BundleContext
import org.specs.Specification
import org.specs.mock.Mockito

class RichBundleContextSpec extends Specification with Mockito {

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
    "call BundleContext.registerService with the interfaces implemented by the given service object" in {
      val service = new TestClass3
      new RichBundleContext(context).createService(service)
      there was one(context).registerService(Array(classOf[TestInterface2].getName, classOf[TestInterface3].getName), service, null)
    }
    "call BundleContext.registerService with the interfaces implemented by the given service object" in {
      val service = new TestClass4
      new RichBundleContext(context).createService(service)
      there was one(context).registerService(
        Array(
          classOf[TestInterface4c].getName,
          classOf[TestInterface4b].getName,
          classOf[TestInterface4a].getName,
          classOf[TestInterface4].getName),
        service,
        null)
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
